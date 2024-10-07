package com.app.rd.player.service

import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.app.rd.player.notification.JetAudioNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class JetAudioService : MediaSessionService() {

    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var notificationManager: JetAudioNotificationManager


    @OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.startNotificationService(
                mediaSession = mediaSession,
                mediaSessionService = this@JetAudioService
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // Create your Player and MediaSession in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        super.onDestroy()
        mediaSession.run {
            release()
            if (player.playbackState != Player.STATE_IDLE) {
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
        }
    }

}