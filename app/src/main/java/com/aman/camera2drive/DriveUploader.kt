package com.aman.camera2drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

class DriveUploader(private val context: Context) {
    private val queue = ConcurrentLinkedQueue<File>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var running = false

    fun enqueue(file: File) {
        queue.add(file)
        drain()
    }

    private fun drain() {
        if (running) return
        running = true
        scope.launch {
            try {
                while (true) {
                    val file = queue.poll() ?: break
                    if (!upload(file)) {
                        queue.add(file)
                        delay(10_000)
                    }
                }
            } finally { running = false }
        }
    }

    private suspend fun upload(file: File): Boolean {
        repeat(5) { attempt ->
            try {
                val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
                val credential = GoogleAccountCredential.usingOAuth2(
                    context, listOf(DriveScopes.DRIVE_FILE)
                ).apply { selectedAccount = account.account }

                val drive = Drive.Builder(
                    com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport(),
                    com.google.api.client.json.gson.GsonFactory.getDefaultInstance(),
                    credential
                ).setApplicationName("DriveCam").build()

                val metadata = com.google.api.services.drive.model.File()
                    .setName(file.name)
                    .setMimeType("video/mp4")
                drive.files().create(metadata, FileContent("video/mp4", file))
                    .setFields("id,name").execute()

                file.delete()
                return true
            } catch (_: Exception) {
                delay(2000L * (attempt + 1))
            }
        }
        return false
    }
}