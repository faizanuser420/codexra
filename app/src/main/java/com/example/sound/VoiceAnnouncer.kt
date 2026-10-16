package com.example.sound

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class VoiceAnnouncer(context: Context, onInitCallback: ((Boolean) -> Unit)? = null) {

    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.getDefault())
                isReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                onInitCallback?.invoke(isReady)
            } else {
                isReady = false
                onInitCallback?.invoke(false)
            }
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isReady || text.isBlank()) {
            onDone?.invoke()
            return
        }
        val utteranceId = "codexra_voice_${System.currentTimeMillis()}"
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(id: String?) {
                onDone?.invoke()
            }
            override fun onError(utteranceId: String?) {
                onDone?.invoke()
            }
        })
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("VoiceAnnouncer", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceAnnouncer", "Error shutting down TTS", e)
        } finally {
            tts = null
            isReady = false
        }
    }
}
