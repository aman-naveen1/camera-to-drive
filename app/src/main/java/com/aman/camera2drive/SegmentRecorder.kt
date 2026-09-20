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
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import java.io.File
import java.util.concurrent.TimeUnit

class SegmentRecorder(private val context: Context, private val uploader: DriveUploader) {
    private var current: Recording? = null
    private var active = false
    private val handler = Handler(context.mainLooper)

    fun start() {
        if (active) return
        active = true
        nextSegment()
    }

    private fun nextSegment() {
        if (!active) return
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (!active) return@addListener
            val owner = context as LifecycleService
            val provider = future.get()
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.FHD))
                .build()
            val capture = VideoCapture.withOutput(recorder)
            provider.unbindAll()
            provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, capture)

            val dir = File(context.cacheDir, "segments").apply { mkdirs() }
            val file = File(dir, "segment_"+System.currentTimeMillis()+".mp4")
            current = recorder.prepareRecording(context, FileOutputOptions.Builder(file).build())
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(context)) { event ->
                    if (event is androidx.camera.video.VideoRecordEvent.Finalize) {
                        if (!event.hasError() && file.exists() && file.length() > 0L) uploader.enqueue(file)
                        else file.delete()
                        nextSegment()
                    }
                }
            handler.postDelayed({ current?.stop() }, TimeUnit.MINUTES.toMillis(5))
        }, ContextCompat.getMainExecutor(context))
    }

    fun stop() {
        active = false
        handler.removeCallbacksAndMessages(null)
        current?.stop()
        current = null
    }
}