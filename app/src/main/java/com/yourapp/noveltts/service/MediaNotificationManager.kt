package com.yourapp.noveltts.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.yourapp.noveltts.MainActivity
import com.yourapp.noveltts.R

class MediaNotificationManager(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "noveltts_playback"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_desc)
            }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    fun build(
        title: String,
        isPlaying: Boolean,
        sessionToken: android.support.v4.media.session.MediaSessionCompat.Token
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            100,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun action(action: String, icon: Int, title: String): NotificationCompat.Action {
            val intent = Intent(context, TtsPlaybackService::class.java).setAction(action)
            val pi = PendingIntent.getService(context, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)
            return NotificationCompat.Action(icon, title, pi)
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText("Novel playback")
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .addAction(action(TtsPlaybackService.ACTION_PREV_CHAPTER, android.R.drawable.ic_media_previous, "Prev"))
            .addAction(
                action(
                    if (isPlaying) TtsPlaybackService.ACTION_PAUSE else TtsPlaybackService.ACTION_PLAY,
                    if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    if (isPlaying) "Pause" else "Play"
                )
            )
            .addAction(action(TtsPlaybackService.ACTION_NEXT_CHAPTER, android.R.drawable.ic_media_next, "Next"))
            .setStyle(MediaStyle().setMediaSession(sessionToken).setShowActionsInCompactView(0, 1, 2))
            .build()
    }
}
