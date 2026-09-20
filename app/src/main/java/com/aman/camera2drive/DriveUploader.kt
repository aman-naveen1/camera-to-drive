package com.aman.camera2drive

import android.content.Context
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.auth.http.HttpCredentialsAdapter
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as DriveFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class DriveUploader(private val context: Context) {
    private val queue = ConcurrentLinkedQueue<java.io.File>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var running = false

    init {
        java.io.File(context.cacheDir, "segments")
            .listFiles()
            ?.filter { it.isFile && it.extension.equals("mp4", ignoreCase = true) }
            ?.sortedBy { it.lastModified() }
            ?.forEach { queue.add(it) }
    }

    fun enqueue(file: java.io.File) {
        if (!file.exists()) return
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
                    if (!file.exists()) continue
                    if (!upload(file)) {
                        queue.add(file)
                        delay(10_000)
                    }
                }
            } finally {
                running = false
                if (queue.isNotEmpty()) drain()
            }
        }
    }

    private suspend fun upload(file: java.io.File): Boolean {
        repeat(5) { attempt ->
            try {
                // AuthorizationClient returns a fresh cached/renewed access token
                // after the user has granted drive.file in the foreground.
                val accessToken = DriveAuth.getAccessToken(context) ?: return false

                val credential = GoogleCredentials.create(
                    AccessToken(accessToken, null)
                )

                val drive = Drive.Builder(
                    NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    HttpCredentialsAdapter(credential)
                ).setApplicationName("DriveCam").build()

                val folderId = getOrCreateFolder(drive)

                val metadata = DriveFile()
                    .setName(file.name)
                    .setMimeType("video/mp4")
                    .setParents(listOf(folderId))

                val request = drive.files()
                    .create(metadata, FileContent("video/mp4", file))
                    .setFields("id,name")

                val response: HttpResponse = request.getMediaHttpUploader()
                    .setDirectUploadEnabled(false)
                    .setChunkSize(5 * 1024 * 1024)
                    .upload(request.buildHttpRequestUrl())

                // Google recommends checking the HTTP response before treating
                // a resumable upload as successful. Never delete local footage
                // on a non-2xx response.
                val success = response.isSuccessStatusCode
                response.disconnect()
                if (!success) return false

                // Never delete a segment until Drive confirms the upload.
                // If deletion fails, keep the uploaded copy and avoid re-uploading it.
                file.delete()
                return true
            } catch (_: Exception) {
                delay(2000L * (attempt + 1))
            }
        }
        return false
    }

    private fun getOrCreateFolder(drive: Drive): String {
        val prefs = context.getSharedPreferences("drivecam", Context.MODE_PRIVATE)
        val cachedId = prefs.getString("folder_id", null)

        if (cachedId != null) {
            try {
                drive.files().get(cachedId).setFields("id").execute()
                return cachedId
            } catch (_: Exception) {
                prefs.edit().remove("folder_id").apply()
            }
        }

        val existing = drive.files().list()
            .setQ("name = 'DriveCam' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
            .setSpaces("drive")
            .setFields("files(id,name)")
            .setPageSize(1)
            .execute()
            .files

        val folderId = if (existing.isNotEmpty()) {
            existing[0].id
        } else {
            val folder = DriveFile()
                .setName("DriveCam")
                .setMimeType("application/vnd.google-apps.folder")

            drive.files().create(folder, null).setFields("id").execute().id
        }

        prefs.edit().putString("folder_id", folderId).apply()
        return folderId
    }
}