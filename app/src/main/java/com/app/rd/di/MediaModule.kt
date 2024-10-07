package com.app.rd.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import com.app.rd.player.notification.JetAudioNotificationManager
import com.app.rd.player.service.JetAudioServiceHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MediaModule {


    @Provides
    @Singleton
    fun provideAudioAttribute(): AudioAttributes =
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()


    @Provides
    @Singleton
    @OptIn(androidx.media3.common.util.UnstableApi::class)
    fun provideExoplayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setTrackSelector(DefaultTrackSelector(context))
        .build()


    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): MediaSession = MediaSession.Builder(context, player).build()


    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): JetAudioNotificationManager = JetAudioNotificationManager(context, player)


    @Provides
    @Singleton
    fun provideServiceHandler(
        exoPlayer: ExoPlayer
    ): JetAudioServiceHandler = JetAudioServiceHandler(exoPlayer)

}