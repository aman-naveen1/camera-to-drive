package com.aman.camera2drive

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService

class RecordingService : LifecycleService() {
    companion object {
        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"
    }

    private lateinit var segmentRecorder: SegmentRecorder

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(
                NotificationChannel(
                    "drivecam",
                    "DriveCam",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        segmentRecorder = SegmentRecorder(this, DriveUploader(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(
                    42,
                    NotificationCompat.Builder(this, "drivecam")
                        .setSmallIcon(android.R.drawable.presence_video_online)
                        .setContentTitle("DriveCam recording")
                        .setContentText("Recording and uploading 5-minute segments")
                        .setOngoing(true)
                        .build()
                )
                segmentRecorder.start()
            }
            ACTION_STOP -> {
                segmentRecorder.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}