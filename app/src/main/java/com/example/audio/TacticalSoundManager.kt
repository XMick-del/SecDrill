package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class TacticalSoundEffect(
    val code: String,
    val description: String
) {
    TERMINAL_CLICK("SFX_CLK_2400HZ", "Tactical Relay Click"),
    VECTOR_CYCLE("SFX_VEC_CHIRP", "Telemetry Vector Shift"),
    EVAL_VERIFIED("SFX_VERIFIED_LOCK", "Target Mitigated Triad"),
    EVAL_BREACH_ALERT("SFX_BREACH_KLAXON", "Control Breach Klaxon"),
    PAYWALL_INTERCEPT("SFX_INTERCEPT_WARN", "Clearance Intercept Alert"),
    CLEARANCE_GRANTED("SFX_OMEGA_UPLINK", "Omega Clearance Harmonic")
}

/**
 * Synthesizes real-time post-human military terminal acoustic feedback using 16-bit PCM AudioTrack
 * waveforms (square/sine/harmonic envelopes) with zero external asset dependencies.
 */
object TacticalSoundManager {

    private const val SAMPLE_RATE = 44100
    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    var isSoundEnabled: Boolean = true
        private set

    @Volatile
    var lastPlayedEffect: TacticalSoundEffect? = null
        private set

    fun setMuted(muted: Boolean) {
        isSoundEnabled = !muted
    }

    fun toggleSound(): Boolean {
        isSoundEnabled = !isSoundEnabled
        if (isSoundEnabled) {
            play(TacticalSoundEffect.TERMINAL_CLICK)
        }
        return isSoundEnabled
    }

    fun play(effect: TacticalSoundEffect) {
        lastPlayedEffect = effect
        if (!isSoundEnabled) return

        audioScope.launch {
            runCatching {
                val pcmSamples = synthesizeWaveform(effect)
                playPcmBuffer(pcmSamples)
            }
        }
    }

    internal fun synthesizeWaveform(effect: TacticalSoundEffect): ShortArray {
        return when (effect) {
            TacticalSoundEffect.TERMINAL_CLICK -> generateTerminalClick()
            TacticalSoundEffect.VECTOR_CYCLE -> generateVectorCycleChirp()
            TacticalSoundEffect.EVAL_VERIFIED -> generateVerifiedTriad()
            TacticalSoundEffect.EVAL_BREACH_ALERT -> generateBreachKlaxon()
            TacticalSoundEffect.PAYWALL_INTERCEPT -> generateInterceptWarning()
            TacticalSoundEffect.CLEARANCE_GRANTED -> generateClearanceUplink()
        }
    }

    /**
     * Crisp 32ms military terminal key-click: fast frequency drop from 2600Hz to 950Hz with sharp decay.
     */
    private fun generateTerminalClick(): ShortArray {
        val durationSec = 0.032
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = 2600.0 - (1650.0 * progress)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val envelope = exp(-progress * 9.5)
            // Blend sine + subtle square edge for crisp mechanical terminal transient
            val raw = (0.7 * sin(phase) + 0.3 * if (sin(phase) >= 0.0) 1.0 else -1.0)
            buffer[i] = (raw * envelope * Short.MAX_VALUE * 0.42).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            ).toShort()
        }
        return buffer
    }

    /**
     * Dual-step HUD telemetry chirp (1200Hz -> 1800Hz, 70ms total).
     */
    private fun generateVectorCycleChirp(): ShortArray {
        val durationSec = 0.070
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = if (progress < 0.48) 1200.0 else 1800.0
            val localProgress = if (progress < 0.48) progress / 0.48 else (progress - 0.48) / 0.52
            val envelope = exp(-localProgress * 4.2)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val raw = sin(phase) + 0.25 * sin(phase * 2.0)
            buffer[i] = (raw * envelope * Short.MAX_VALUE * 0.32).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            ).toShort()
        }
        return buffer
    }

    /**
     * Ascending 3-step verified lock harmonic triad (880Hz -> 1320Hz -> 1760Hz, 240ms).
     */
    private fun generateVerifiedTriad(): ShortArray {
        val notes = doubleArrayOf(880.0, 1174.66, 1760.0) // A5 -> D6 -> A6 tactical lock
        val noteDuration = 0.080
        val samplesPerNote = (SAMPLE_RATE * noteDuration).toInt()
        val totalSamples = samplesPerNote * notes.size
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val noteIndex = (i / samplesPerNote).coerceAtMost(notes.lastIndex)
            val localSample = i % samplesPerNote
            val localProgress = localSample.toDouble() / samplesPerNote
            val freq = notes[noteIndex]
            val attack = (localProgress * 14.0).coerceAtMost(1.0)
            val decay = exp(-localProgress * 3.6)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            // Fundamental + octave harmonic
            val raw = 0.75 * sin(phase) + 0.25 * sin(phase * 2.0)
            buffer[i] = (raw * attack * decay * Short.MAX_VALUE * 0.40).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            ).toShort()
        }
        return buffer
    }

    /**
     * Dual-pulse low-end tactical breach klaxon buzz (360Hz -> 220Hz square-harmonic alert, 280ms).
     */
    private fun generateBreachKlaxon(): ShortArray {
        val durationSec = 0.280
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            // Gap between pulse 1 and pulse 2
            if (progress in 0.44..0.52) {
                buffer[i] = 0
                continue
            }
            val freq = if (progress < 0.44) 340.0 else 225.0
            val localProgress = if (progress < 0.44) progress / 0.44 else (progress - 0.52) / 0.48
            val envelope = exp(-localProgress * 2.4)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            // Edgy square-overtone waveform for alert buzzer feel
            val sq = if (sin(phase) >= 0.0) 0.6 else -0.6
            val raw = sq + 0.4 * sin(phase * 3.0)
            buffer[i] = (raw * envelope * Short.MAX_VALUE * 0.38).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            ).toShort()
        }
        return buffer
    }

    /**
     * Staccato 3-pulse high-priority clearance intercept warning (1480Hz / 980Hz, 250ms).
     */
    private fun generateInterceptWarning(): ShortArray {
        val freqs = doubleArrayOf(1480.0, 980.0, 1480.0)
        val stepDuration = 0.082
        val samplesPerStep = (SAMPLE_RATE * stepDuration).toInt()
        val totalSamples = samplesPerStep * freqs.size
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val idx = (i / samplesPerStep).coerceAtMost(freqs.lastIndex)
            val localProgress = (i % samplesPerStep).toDouble() / samplesPerStep
            if (localProgress > 0.80) {
                buffer[i] = 0
                continue
            }
            val freq = freqs[idx]
            val env = exp(-localProgress * 3.0)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val raw = 0.65 * sin(phase) + 0.35 * (if (sin(phase) >= 0) 1.0 else -1.0)
            buffer[i] = (raw * env * Short.MAX_VALUE * 0.36).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            ).toShort()
        }
        return buffer
    }

    /**
     * 4-step ascending cyber-uplink arpeggio when authorizing Level-5 Omega Clearance.
     */
    private fun generateClearanceUplink(): ShortArray {
        val notes = doubleArrayOf(660.0, 990.0, 1320.0, 1980.0)
        val stepDuration = 0.068
        val samplesPerStep = (SAMPLE_RATE * stepDuration).toInt()
        val totalSamples = samplesPerStep * notes.size
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val idx = (i / samplesPerStep).coerceAtMost(notes.lastIndex)
            val localProgress = (i % samplesPerStep).toDouble() / samplesPerStep
            val freq = notes[idx]
            val env = exp(-localProgress * 3.2)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val raw = 0.8 * sin(phase) + 0.2 * sin(phase * 2.0)
            buffer[i] = (raw * env * Short.MAX_VALUE * 0.38).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            ).toShort()
        }
        return buffer
    }

    private fun playPcmBuffer(samples: ShortArray) {
        if (samples.isEmpty()) return
        val byteCount = samples.size * 2
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(byteCount.coerceAtLeast(AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            val durationMs = ((samples.size.toDouble() / SAMPLE_RATE) * 1000.0).toLong() + 45L
            Thread.sleep(durationMs)
        } finally {
            runCatching {
                audioTrack.stop()
                audioTrack.release()
            }
        }
    }
}
