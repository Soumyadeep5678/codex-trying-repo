package com.yourapp.noveltts.service

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class MediaSessionManager(
    context: Context,
    callback: MediaSessionCompat.Callback
) {
    val mediaSession = MediaSessionCompat(context, "NovelTtsSession").apply {
        setCallback(callback)
        isActive = true
    }

    fun setPlaying(isPlaying: Boolean) {
        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
            .build()
        mediaSession.setPlaybackState(playbackState)
    }

    fun release() = mediaSession.release()
}
