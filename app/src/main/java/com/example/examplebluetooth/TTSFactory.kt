package com.example.examplebluetooth

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TTSFactory {
    companion object {
        fun createTTS(contxt: Context): TextToSpeech {
            lateinit var tts: TextToSpeech
            tts = TextToSpeech(contxt) {
                if (it == TextToSpeech.SUCCESS) {
                    val result = tts.setLanguage(Locale("zh", "hk"))

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported!")
                    } else {
                        Log.i("TTS", "tts ready")
                    }
                }
            }
            return tts
        }
    }
}