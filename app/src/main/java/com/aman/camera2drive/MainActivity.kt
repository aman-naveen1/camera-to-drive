package com.aman.camera2drive

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.gms.auth.api.identity.AuthorizationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.security.SecureRandom
import android.util.Base64
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var status: TextView
    private lateinit var drivePill: TextView
    private lateinit var qualityPill: TextView
    private lateinit var focusHint: TextView
    private lateinit var record: Button
    private lateinit var signIn: Button
    private lateinit var gestureLayer: View

    private val uiScope = MainScope()
    private val credentialManager by lazy { CredentialManager.create(this) }
    private var camera: Camera? = null
    private var driveReady = false
    private var recording = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val cameraGranted = results[Manifest.permission.CAMERA] == true ||
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val audioGranted = results[Manifest.permission.RECORD_AUDIO] == true ||
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (cameraGranted && audioGranted) startCamera()
        else status.text = "CAMERA + MICROPHONE REQUIRED"
    }

    private val driveAuthorizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            try {
                val authorizationResult =
                    DriveAuth.fromIntent(this, result.data)
                if (authorizationResult.hasResolution()) {
                    status.text = "DRIVE PERMISSION NOT GRANTED"
                    return@registerForActivityResult
                }
                onDriveReady()
            } catch (e: Exception) {
                status.text = "DRIVE AUTHORIZATION FAILED"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        status = findViewById(R.id.statusText)
        drivePill = findViewById(R.id.drivePill)
        qualityPill = findViewById(R.id.qualityPill)
        focusHint = findViewById(R.id.focusHint)
        record = findViewById(R.id.recordButton)
        signIn = findViewById(R.id.signInButton)
        gestureLayer = findViewById(R.id.gestureLayer)

        findViewById<View>(R.id.securityButton).setOnClickListener {
            startActivity(Intent(this, SecurityActivity::class.java))
        }

        previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE

        signIn.setOnClickListener {
            connectGoogle()
        }

        record.setOnClickListener {
            if (!driveReady) {
                status.text = "CONNECT GOOGLE DRIVE FIRST"
                connectGoogle()
                return@setOnClickListener
            }
            if (!hasCameraPermissions()) {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                )
                return@setOnClickListener
            }

            if (recording) stopRecording() else startRecording()
        }

        installCameraGestures()

        if (!hasCameraPermissions()) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        } else {
            startCamera()
        }

        // Automatic sign-in for returning users. Credential Manager can do this
        // without making the user pick an account again.
        signInGoogle(filterAuthorized = true, autoSelect = true, showErrors = false)
    }

    private fun connectGoogle() {
        status.text = "SIGNING IN…"
        signInGoogle(filterAuthorized = true, autoSelect = true, showErrors = true)
    }

    private fun signInGoogle(
        filterAuthorized: Boolean,
        autoSelect: Boolean,
        showErrors: Boolean
    ) {
        val webClientId = getString(R.string.google_web_client_id)
        if (webClientId.startsWith("REPLACE_WITH_")) {
            status.text = "ADD YOUR GOOGLE WEB CLIENT ID"
            return
        }

        uiScope.launch {
            try {
                val option = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(filterAuthorized)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(autoSelect)
                    .setNonce(generateNonce())
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()

                val result = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = request
                )

                val tokenCredential = extractGoogleCredential(result.credential)
                if (tokenCredential == null) {
                    status.text = "GOOGLE ACCOUNT NOT AVAILABLE"
                    return@launch
                }

                status.text = "GOOGLE ACCOUNT CONNECTED"
                authorizeDrive()
            } catch (_: androidx.credentials.exceptions.NoCredentialException) {
                if (!filterAuthorized) {
                    status.text = "NO GOOGLE ACCOUNT AVAILABLE"
                } else {
                    signInGoogle(
                        filterAuthorized = false,
                        autoSelect = false,
                        showErrors = showErrors
                    )
                }
            } catch (_: CancellationException) {
                // User dismissed the Google sheet; leave the camera usable.
                if (showErrors) status.text = "GOOGLE SIGN-IN DISMISSED"
            } catch (_: GetCredentialException) {
                if (showErrors) status.text = "GOOGLE SIGN-IN FAILED"
            } catch (_: Exception) {
                if (showErrors) status.text = "GOOGLE SIGN-IN FAILED"
            }
        }
    }

    private fun extractGoogleCredential(credential: Credential): GoogleIdTokenCredential? {
        return try {
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleIdTokenCredential.createFrom(credential.data)
            } else {
                null
            }
        } catch (_: GoogleIdTokenParsingException) {
            null
        }
    }

    private fun authorizeDrive() {
        status.text = "GRANTING DRIVE ACCESS…"
        DriveAuth.authorize(
            activity = this,
            launchResolution = { driveAuthorizationLauncher.launch(it) },
            onReady = { onDriveReady() },
            onFailure = { status.text = "DRIVE AUTHORIZATION FAILED" }
        )
    }

    private fun onDriveReady() {
        driveReady = true
        drivePill.text = "DRIVE • READY"
        status.text = if (recording) "● RECORDING • CLOUD UPLOAD" else "READY • DRIVE CONNECTED"
        signIn.isEnabled = false
        signIn.visibility = View.GONE
        record.isEnabled = true
    }

    private fun startRecording() {
        try {
            val intent = Intent(this, RecordingService::class.java)
                .setAction(RecordingService.ACTION_START)
            ContextCompat.startForegroundService(this, intent)
            recording = true
            record.contentDescription = "Stop recording"
            status.text = "● RECORDING • UPLOADING"
            qualityPill.text = "AUTO • 1080P30"
        } catch (_: Exception) {
            status.text = "COULD NOT START RECORDING"
        }
    }

    private fun stopRecording() {
        val intent = Intent(this, RecordingService::class.java)
            .setAction(RecordingService.ACTION_STOP)
        startService(intent)
        recording = false
        record.contentDescription = "Start recording"
        status.text = "READY • DRIVE CONNECTED"
    }

    private fun hasCameraPermissions(): Boolean =
        checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        if (!hasCameraPermissions()) return

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                val provider = future.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )

                val supported = androidx.camera.video.QualitySelector.getSupportedQualities(
                    camera!!.cameraInfo
                )
                qualityPill.text = when {
                    supported.contains(androidx.camera.video.Quality.FHD) -> "AUTO • 1080P"
                    supported.contains(androidx.camera.video.Quality.HD) -> "AUTO • 720P"
                    else -> "AUTO • DEVICE"
                }
                drivePill.text = if (driveReady) "DRIVE • READY" else "DRIVE • OFFLINE"
                status.text = if (driveReady) "READY • DRIVE CONNECTED" else "READY • CONNECT GOOGLE"
            } catch (_: Exception) {
                status.text = "CAMERA INITIALIZATION FAILED"
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun installCameraGestures() {
        gestureLayer.setOnTouchListener(object : View.OnTouchListener {
            private var initialDistance = 0f
            private var initialZoom = 1f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val activeCamera = camera ?: return false

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        initialDistance = 0f
                        return true
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        if (event.pointerCount >= 2) {
                            initialDistance = distance(event)
                            initialZoom = activeCamera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                        }
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (event.pointerCount >= 2 && initialDistance > 0f) {
                            val ratio = distance(event) / initialDistance
                            val maxZoom = activeCamera.cameraInfo.zoomState.value?.maxZoomRatio ?: 20f
                            val newZoom = (initialZoom * ratio).coerceIn(1f, maxZoom)
                            activeCamera.cameraControl.setZoomRatio(newZoom)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (initialDistance <= 0f) {
                            val point = previewView.meteringPointFactory.createPoint(
                                event.x,
                                event.y
                            )
                            val action = FocusMeteringAction.Builder(point)
                                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                                .build()
                            activeCamera.cameraControl.startFocusAndMetering(action)
                            showFocus()
                        }
                        return true
                    }
                }
                return true
            }
        })
    }

    private fun distance(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    private fun showFocus() {
        focusHint.animate().cancel()
        focusHint.alpha = 1f
        focusHint.animate().alpha(0f).setDuration(650).start()
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(
            bytes,
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
        )
    }

    override fun onDestroy() {
        uiScope.cancel()
        super.onDestroy()
    }
}