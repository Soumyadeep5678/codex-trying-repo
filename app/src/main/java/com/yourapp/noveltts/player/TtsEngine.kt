package com.yourapp.noveltts.player

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TtsEngine(
    context: Context,
    private val onDone: (String) -> Unit
) {
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }.apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onError(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) {
                    utteranceId?.let(onDone)
                }
            })
        }
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun voices() = tts?.voices?.toList().orEmpty()

    fun setVoiceByName(name: String) {
        tts?.voice = tts?.voices?.firstOrNull { it.name == name }
    }

    fun speak(text: String, utteranceId: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
