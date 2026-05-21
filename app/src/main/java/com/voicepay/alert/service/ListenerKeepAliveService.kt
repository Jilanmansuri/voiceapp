package com.voicepay.alert.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.voicepay.alert.R
import com.voicepay.alert.ui.main.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.voicepay.alert.VoicePayApplication

/**
 * Lightweight foreground service to keep the listener active on aggressive OEMs.
 */
class ListenerKeepAliveService : Service() {

    companion object {
        private const val CHANNEL_ID = "voicepay_listener"
        private const val NOTIFICATION_ID = 1001

        fun startIfNeeded(context: Context) {
            val app = context.applicationContext as VoicePayApplication
            val settings = runBlocking { app.settingsRepository.settingsFlow.first() }
            if (!settings.keepAliveService) return

            val intent = Intent(context, ListenerKeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ListenerKeepAliveService::class.java))
        }

        fun updateNotification(context: Context, listenerConnected: Boolean) {
            val nm = context.getSystemService(NotificationManager::class.java)
            val notification = buildNotification(context, listenerConnected)
            nm.notify(NOTIFICATION_ID, notification)
        }

        private fun buildNotification(context: Context, listenerConnected: Boolean): Notification {
            createChannel(context)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val status = if (listenerConnected) {
                context.getString(R.string.status_listener_active)
            } else {
                context.getString(R.string.status_listener_inactive)
            }
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(status)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        }

        private fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.channel_listener_name),
                    NotificationManager.IMPORTANCE_LOW
                )
                context.getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(channel)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val connected = com.voicepay.alert.util.NotificationAccessHelper
            .isNotificationListenerEnabled(this)
        val notification = buildNotification(this, connected)
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
