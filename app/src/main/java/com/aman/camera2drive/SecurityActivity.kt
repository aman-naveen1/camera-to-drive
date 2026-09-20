package com.aman.camera2drive

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import com.aman.camera2drive.BuildConfig
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class SecurityActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        findViewById<View>(R.id.securityBack).setOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.securityChecks)
        val checks = listOf(
            check(
                "GOOGLE DRIVE ACCESS",
                "drive.file scope • limited to files created/used by DriveCam",
                "LIMITED"
            ),
            check(
                "LOCAL VIDEO STORAGE",
                "Temporary segments stay in app-private cache storage",
                "SANDBOXED"
            ),
            check(
                "NETWORK",
                "Cleartext HTTP disabled; Google API traffic uses the platform TLS stack",
                "HTTPS ONLY"
            ),
            check(
                "ANDROID COMPONENTS",
                "Recording service is not exported to other apps",
                "LOCKED"
            ),
            check(
                "CAMERA + MICROPHONE",
                "Access requires runtime permission from Android",
                permissionsStatus()
            ),
            check(
                "OAUTH CLIENT",
                "Web client ID is public configuration; no client secret is bundled",
                "OK"
            ),
            check(
                "BUILD",
                if (BuildConfig.DEBUG)
                    "Debug build • debugging features may be available"
                else
                    "Release build • production configuration",
                if (BuildConfig.DEBUG) "DEBUG" else "RELEASE"
            )
        )

        checks.forEach { (title, detail, state) ->
            val row = layoutInflater.inflate(R.layout.security_check_row, container, false)
            row.findViewById<TextView>(R.id.checkTitle).text = title
            row.findViewById<TextView>(R.id.checkDetail).text = detail
            row.findViewById<TextView>(R.id.checkState).text = state
            container.addView(row)
        }
    }

    private fun permissionsStatus(): String {
        val camera = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val audio = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        return if (camera && audio) "GRANTED" else "NOT GRANTED"
    }

    private fun check(title: String, detail: String, state: String) =
        Triple(title, detail, state)
}
