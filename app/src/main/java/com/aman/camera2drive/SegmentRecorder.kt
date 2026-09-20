package com.aman.camera2drive

import android.content.Context
import android.os.Handler
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import java.io.File
import java.util.concurrent.TimeUnit

class SegmentRecorder(
    private val context: Context,
    private val uploader: DriveUploader
) {
    companion object {
        private const val SEGMENT_MINUTES = 5L
    }

    private var current: Recording? = null
    private var active = false
    private var provider: ProcessCameraProvider? = null
    private val handler = Handler(context.mainLooper)

    fun start() {
        if (active) return
        active = true
        startNextSegment()
    }

    private fun startNextSegment() {
        if (!active) return

        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (!active) return@addListener

            try {
                val owner = context as? LifecycleService ?: run {
                    active = false
                    return@addListener
                }

                val cameraProvider = future.get()
                provider = cameraProvider

                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.fromOrderedList(
                            listOf(Quality.FHD, Quality.HD, Quality.SD)
                        )
                    )
                    .build()

                val capture = VideoCapture.withOutput(recorder)
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    owner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    capture
                )

                val dir = File(context.cacheDir, "segments").apply { mkdirs() }
                val file = File(dir, "segment_" + System.currentTimeMillis() + ".mp4")

                current = recorder.prepareRecording(
                    context,
                    FileOutputOptions.Builder(file).build()
                ).withAudioEnabled()
                    .start(ContextCompat.getMainExecutor(context)) { event ->
                        if (event is VideoRecordEvent.Finalize) {
                            current = null

                            if (!event.hasError() && file.exists() && file.length() > 0L) {
                                uploader.enqueue(file)
                            } else {
                                file.delete()
                            }

                            if (active) startNextSegment()
                        }
                    }

                handler.postDelayed(
                    { current?.stop() },
                    TimeUnit.MINUTES.toMillis(SEGMENT_MINUTES)
                )
            } catch (_: Exception) {
                active = false
                current = null
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stop() {
        active = false
        handler.removeCallbacksAndMessages(null)
        current?.stop()
        current = null
        provider?.unbindAll()
    }
}