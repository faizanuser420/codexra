package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object AlarmRingtonePlayer {

    private const val TAG = "AlarmRingtonePlayer"
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private var volumeFadeRunnable: Runnable? = null

    fun play(
        context: Context,
        ringtoneUriString: String?,
        isVibrate: Boolean,
        isGradualVolume: Boolean
    ) {
        stop()

        // 1. Setup Vibrator
        if (isVibrate) {
            startVibration(context)
        }

        // 2. Setup Ringtone MediaPlayer
        try {
            val alertUri: Uri = if (!ringtoneUriString.isNullOrBlank()) {
                try {
                    Uri.parse(ringtoneUriString)
                } catch (e: Exception) {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alertUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                if (isGradualVolume) {
                    setVolume(0.1f, 0.1f)
                } else {
                    setVolume(1.0f, 1.0f)
                }
                prepare()
                start()
            }

            if (isGradualVolume) {
                startGradualVolumeIncrease()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing alarm ringtone with custom URI, trying system fallback", e)
            tryFallbackRingtone(context)
        }
    }

    private fun tryFallbackRingtone(context: Context) {
        try {
            val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, fallbackUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                setVolume(1.0f, 1.0f)
                prepare()
                start()
            }
        } catch (ex: Exception) {
            Log.e(TAG, "System fallback ringtone also failed", ex)
        }
    }

    private fun startGradualVolumeIncrease() {
        var currentVol = 0.1f
        volumeFadeRunnable = object : Runnable {
            override fun run() {
                currentVol += 0.1f
                if (currentVol > 1.0f) currentVol = 1.0f
                try {
                    mediaPlayer?.setVolume(currentVol, currentVol)
                } catch (e: Exception) {
                    return
                }
                if (currentVol < 1.0f) {
                    handler.postDelayed(this, 2000)
                }
            }
        }
        handler.postDelayed(volumeFadeRunnable!!, 2000)
    }

    private fun startVibration(context: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        val pattern = longArrayOf(0, 800, 400, 800, 400, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    fun stop() {
        volumeFadeRunnable?.let { handler.removeCallbacks(it) }
        volumeFadeRunnable = null

        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        } finally {
            mediaPlayer = null
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibrator", e)
        } finally {
            vibrator = null
        }
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
}
