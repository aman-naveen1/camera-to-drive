package com.aman.camera2drive

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
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
                val notification = NotificationCompat.Builder(this, "drivecam")
                    .setSmallIcon(android.R.drawable.presence_video_online)
                    .setContentTitle("DriveCam recording")
                    .setContentText("Recording + uploading 5-minute segments")
                    .setOngoing(true)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .build()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        42,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    )
                } else {
                    startForeground(42, notification)
                }

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
}