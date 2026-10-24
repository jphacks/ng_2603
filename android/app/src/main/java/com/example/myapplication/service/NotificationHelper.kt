package com.example.myapplication.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.domain.model.DisappointmentMessage

class NotificationHelper(
    private val context: Context
) {
    companion object {
        const val CHANNEL_ID_MONITORING = "monitoring_service_channel"
        const val CHANNEL_ID_DISAPPOINTMENT = "disappointment_alert_channel"
        const val NOTIFICATION_ID_FOREGROUND_SERVICE = 1001
        const val NOTIFICATION_ID_DISAPPOINTMENT_ALERT = 1002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val monitoringChannel = NotificationChannel(
                CHANNEL_ID_MONITORING,
                "常駐アプリ監視ステータス",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "スクリーンタイムおよびアプリ使用の監視常駐通知"
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ID_DISAPPOINTMENT,
                "親からの呆れメッセージ警告",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "使いすぎ時に届く段階的な呆れメッセージ通知"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(monitoringChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    /**
     * 常駐フォアグラウンドサービス用の通知を生成
     */
    fun buildMonitoringNotification(statusText: String): Notification {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_MONITORING)
            .setContentTitle("親の呆れ顔ガード稼働中")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    /**
     * 段階的呆れメッセージのローカル通知を送信
     */
    fun showDisappointmentNotification(
        message: DisappointmentMessage,
        appName: String,
        enableVibration: Boolean
    ) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            message.level.levelNumber,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DISAPPOINTMENT)
            .setContentTitle("【${message.level.title}】 $appName")
            .setContentText(message.mainQuote)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${message.mainQuote}\n${message.subQuote}"))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (enableVibration) {
            triggerVibration()
        }

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_DISAPPOINTMENT_ALERT,
                builder.build()
            )
        } catch (_: SecurityException) {
            // Android 13+ で権限未許可時の例外ハンドリング
        }
    }

    /**
     * 嫌がらせ・警告用バイブレーションの発火
     */
    fun triggerVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // ため息・チクチクとした短い連続振動パターン
                val timings = longArrayOf(0, 150, 100, 250, 150, 400)
                val amplitudes = intArrayOf(0, 180, 0, 220, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                it.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 200, 100, 300), -1)
            }
        }
    }
}
