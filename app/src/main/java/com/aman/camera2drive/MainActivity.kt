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

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { startCamera() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.statusText)
        record = findViewById(R.id.recordButton)

        findViewById<Button>(R.id.signInButton).setOnClickListener {
            DriveAuth.signIn(this) { onDriveReady() }
        }

        GoogleSignIn.getLastSignedInAccount(this)?.let { onDriveReady() }

        record.setOnClickListener {
            val i = Intent(this, RecordingService::class.java)
            if (record.text.toString().startsWith("Start")) {
                i.action = RecordingService.ACTION_START
                ContextCompat.startForegroundService(this, i)
                status.text = "● RECORDING • 5 MIN FILES"
                record.text = "Stop recording"
            } else {
                i.action = RecordingService.ACTION_STOP
                startService(i)
                status.text = "READY"
                record.text = "Start recording"
            }
        }

        val p = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        if (p.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            permissionLauncher.launch(p)
        } else startCamera()
    }

    private fun onDriveReady() {
        status.text = "DRIVE CONNECTED"
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
            }
        }
    }

    private fun startCamera() {
        val view = findViewById<PreviewView>(R.id.previewView)
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = future.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = view.surfaceProvider
            }
            provider.unbindAll()
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)
        }, ContextCompat.getMainExecutor(this))
    }
}