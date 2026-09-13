package com.hermes.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.hermes.android.MainActivity

/**
 * Foreground service (dataSync): keeps Hermes gateway + cron alive.
 * Android suspends background jobs -> WakeLock + battery-opt exemption
 * prompt (see SetupWizard) + BootReceiver resume.
 */
class HermesForegroundService : Service() {

    private var wake: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "hermes:gateway").apply {
            acquire(12 * 60 * 60 * 1000L)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotif(intent?.getStringExtra(EXTRA_STATUS) ?: "Gateway running"))
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { wake?.release() }
        super.onDestroy()
    }

    private fun ensureChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Hermes Agent", NotificationManager.IMPORTANCE_LOW)
        )
    }

    private fun buildNotif(status: String): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle("Hermes Agent")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL = "hermes_service"
        const val NOTIF_ID = 41
        const val EXTRA_STATUS = "status"
        fun start(ctx: Context, status: String = "Gateway running") {
            val i = Intent(ctx, HermesForegroundService::class.java).putExtra(EXTRA_STATUS, status)
            if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i) else ctx.startService(i)
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val done = ctx.getSharedPreferences("hermes", Context.MODE_PRIVATE)
                .getBoolean("setup_done", false)
            if (done) HermesForegroundService.start(ctx, "Resuming gateway…")
        }
    }
}
