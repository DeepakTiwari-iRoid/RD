package com.app.rd.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer


class MultiAudioPlayer(private val context: Context, private val uris: List<Uri>) {

    private val players: MutableList<ExoPlayer> = mutableListOf()

    // Initialize ExoPlayer instances for each audio URI
    fun initializePlayers() {
        for (uri in uris) {
            val player = ExoPlayer.Builder(context).build()
            val mediaItem = MediaItem.fromUri(uri)

            player.setMediaItem(mediaItem)
            player.prepare()
            players.add(player)
        }
    }

    // Start playback of all players simultaneously
    fun playAll() {
        for (player in players) {
            player.playWhenReady = true
        }
    }

    // Stop playback of all players
    fun stopAll() {
        for (player in players) {
            player.stop()
            player.release()
        }
        players.clear()
    }

    // Control the volume of a specific player
    fun setPlayerVolume(index: Int, volume: Float) {
        if (index in players.indices) {
            players[index].volume = volume // Volume should be between 0.0 and 1.0
        }
    }
}