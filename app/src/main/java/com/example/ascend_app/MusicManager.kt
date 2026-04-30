package com.example.ascend_app

import android.content.Context
import android.media.MediaPlayer

object MusicManager {
    private var player: MediaPlayer? = null
    private var currentTrack: Int = -1

    fun play(context: Context, resId: Int) {
        if (currentTrack == resId) return  // already playing this track
        stop()
        player = MediaPlayer.create(context, resId).apply {
            isLooping = true
            setVolume(0.5f, 0.5f)  // low volume — adjust 0.0f to 1.0f
            start()
        }
        currentTrack = resId
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
        currentTrack = -1
    }

    fun pause() {
        player?.pause()
    }

    fun resume() {
        player?.start()
    }

    fun setMuted(muted: Boolean) {
        if (muted) player?.setVolume(0f, 0f)
        else player?.setVolume(0.2f, 0.2f)
    }
}