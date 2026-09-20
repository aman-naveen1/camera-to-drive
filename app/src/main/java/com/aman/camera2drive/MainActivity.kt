package com.aman.camera2drive

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {
    private lateinit var status: TextView
    private lateinit var record: Button
    private lateinit var signIn: Button

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val cameraGranted = results[Manifest.permission.CAMERA] == true ||
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val audioGranted = results[Manifest.permission.RECORD_AUDIO] == true ||
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (cameraGranted && audioGranted) startCamera()
        else status.text = "CAMERA + MICROPHONE PERMISSION REQUIRED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.statusText)
        record = findViewById(R.id.recordButton)
        signIn = findViewById(R.id.signInButton)

        signIn.setOnClickListener {
            status.text = "CONNECTING TO GOOGLE DRIVE…"
            DriveAuth.signIn(this) { onDriveReady() }
        }

        if (GoogleSignIn.getLastSignedInAccount(this) != null) onDriveReady()

        record.setOnClickListener {
            if (record.text.toString().startsWith("Start")) {
                if (!hasCameraPermissions()) {
                    status.text = "ALLOW CAMERA + MICROPHONE FIRST"
                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                    return@setOnClickListener
                }
                try {
                    val intent = Intent(this, RecordingService::class.java)
                        .setAction(RecordingService.ACTION_START)
                    ContextCompat.startForegroundService(this, intent)
                    status.text = "● RECORDING • UPLOADING"
                    record.text = "Stop recording"
                } catch (_: Exception) {
                    status.text = "COULD NOT START RECORDING"
                }
            } else {
                val intent = Intent(this, RecordingService::class.java)
                    .setAction(RecordingService.ACTION_STOP)
                startService(intent)
                status.text = "DRIVE CONNECTED • READY"
                record.text = "Start recording"
            }
        }

        if (!hasCameraPermissions()) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        } else startCamera()
    }

    private fun hasCameraPermissions(): Boolean =
        checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun onDriveReady() {
        status.text = "DRIVE CONNECTED • READY"
        signIn.isEnabled = false
        record.isEnabled = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DriveAuth.RC_SIGN_IN) {
            try {
                GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
                onDriveReady()
            } catch (_: Exception) {
                status.text = "GOOGLE SIGN-IN FAILED"
                signIn.isEnabled = true
            }
        }
    }

    private fun startCamera() {
        if (!hasCameraPermissions()) return
        val view = findViewById<PreviewView>(R.id.previewView)
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                val provider = future.get()
                val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            } catch (_: Exception) {
                status.text = "CAMERA INITIALIZATION FAILED"
            }
        }, ContextCompat.getMainExecutor(this))
    }
}