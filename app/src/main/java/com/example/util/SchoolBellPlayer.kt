package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.exp
import kotlin.math.sin

class SchoolBellPlayer(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val langResult = textToSpeech?.setLanguage(Locale("id", "ID"))
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech?.setLanguage(Locale.US)
                    }
                    isTtsReady = true
                }
            }
        } catch (e: Exception) {
            Log.w("SchoolBellPlayer", "TTS initialization failed: ${e.message}")
        }
    }

    /**
     * Plays a rich school bell chime sequence with optional voice announcement.
     * @param bellType "MASUK", "PERGANTIAN", "ISTIRAHAT", "PULANG", or "MANUAL"
     * @param announcementMessage Speech / display message (e.g. "Jam Ke-2 Dimulai")
     */
    fun ringBell(
        bellType: String,
        announcementMessage: String = "",
        enableVoice: Boolean = true,
        scope: CoroutineScope
    ) {
        // Trigger haptic vibration
        vibrate()

        scope.launch(Dispatchers.Default) {
            try {
                // Melodic frequency chords for school chime
                when (bellType.uppercase()) {
                    "MASUK" -> {
                        playTone(523.25, 0.4) // C5
                        delay(250)
                        playTone(659.25, 0.4) // E5
                        delay(250)
                        playTone(783.99, 0.6) // G5
                        delay(350)
                        playTone(1046.50, 0.9) // C6
                    }
                    "ISTIRAHAT" -> {
                        playTone(783.99, 0.5) // G5
                        delay(300)
                        playTone(659.25, 0.5) // E5
                        delay(300)
                        playTone(523.25, 0.8) // C5
                    }
                    "PULANG" -> {
                        // Westminster full quarter
                        playTone(659.25, 0.4) // E5
                        delay(250)
                        playTone(523.25, 0.4) // C5
                        delay(250)
                        playTone(587.33, 0.4) // D5
                        delay(250)
                        playTone(392.00, 0.8) // G4
                    }
                    else -> {
                        // Standard Period Change / Manual Bell
                        playTone(659.25, 0.4) // E5
                        delay(250)
                        playTone(783.99, 0.4) // G5
                        delay(250)
                        playTone(880.00, 0.6) // A5
                    }
                }

                // If voice announcement is active and TTS is ready
                if (enableVoice && announcementMessage.isNotBlank()) {
                    delay(500)
                    speakAnnouncement(announcementMessage)
                }
            } catch (e: Exception) {
                Log.e("SchoolBellPlayer", "Error playing chime: ${e.message}", e)
            }
        }
    }

    private fun speakAnnouncement(text: String) {
        try {
            if (isTtsReady && textToSpeech != null) {
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "school_bell_tts")
            }
        } catch (e: Exception) {
            Log.w("SchoolBellPlayer", "TTS speak failed: ${e.message}")
        }
    }

    private fun playTone(frequencyHz: Double, durationSeconds: Double) {
        val sampleRate = 44100
        val numSamples = (sampleRate * durationSeconds).toInt()
        val generatedSound = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Fundamental sine wave with natural exponential decay for realistic bell timbre
            val wave = sin(2.0 * Math.PI * frequencyHz * t) * 0.7 +
                    sin(2.0 * Math.PI * (frequencyHz * 2.0) * t) * 0.2 +
                    sin(2.0 * Math.PI * (frequencyHz * 3.0) * t) * 0.1
            val envelope = exp(-3.5 * (t / durationSeconds))
            generatedSound[i] = (wave * envelope * Short.MAX_VALUE).toInt().toShort()
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(generatedSound.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(generatedSound, 0, generatedSound.size)
        audioTrack.play()

        // Release resources safely after track playback
        Thread.sleep((durationSeconds * 1000).toLong() + 50)
        try {
            audioTrack.stop()
            audioTrack.release()
        } catch (ignored: Exception) {}
    }

    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 300, 150, 300), -1)
                }
            }
        } catch (e: Exception) {
            Log.w("SchoolBellPlayer", "Vibration failed: ${e.message}")
        }
    }

    fun release() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (ignored: Exception) {}
    }
}
