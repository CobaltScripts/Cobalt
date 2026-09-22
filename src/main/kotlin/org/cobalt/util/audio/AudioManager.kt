package org.cobalt.util.audio

import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.Mixer
import kotlin.math.log10
import org.cobalt.Cobalt
import org.slf4j.LoggerFactory

object AudioManager {
  private val logger =
    LoggerFactory.getLogger(this::class.java)

  private val clips = CopyOnWriteArrayList<Clip>()

  private const val OPENAL_SOFT_PREFIX = "OpenAL Soft on "

  private fun resolveMixerInfo(): Mixer.Info? {

    val rawDeviceName = Cobalt.minecraft.options.soundDevice().get().takeIf { it.isNotBlank() } ?: return null

    val targetName = rawDeviceName.removePrefix(OPENAL_SOFT_PREFIX)
    val mixers = AudioSystem.getMixerInfo()

    mixers.firstOrNull { it.name.equals(targetName, ignoreCase = true) }?.let { return it }

    val loose = mixers.firstOrNull {
      it.name.contains(targetName, ignoreCase = true) || targetName.contains(it.name, ignoreCase = true)
    }
    if (loose == null) {
      logger.warn("No javax.sound.sampled mixer matched Minecraft's sound device '{}'", rawDeviceName)
    }
    return loose
  }

  fun play(file: File, volume: Float = 1.0f) {
    if (!file.exists()) return

    try {
      val audioStream = AudioSystem.getAudioInputStream(file)

      val mixerInfo = resolveMixerInfo()
      val clip: Clip = if (mixerInfo != null) {
        try {
          AudioSystem.getClip(mixerInfo)
        } catch (e: Exception) {
          logger.warn("Failed to open clip on Minecraft's sound device, falling back to default mixer", e)
          AudioSystem.getClip()
        }
      } else {
        AudioSystem.getClip()
      }

      clip.open(audioStream)

      val gain = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl

      val db = if (volume <= 0f) {
        gain.minimum
      } else {
        (20.0 * log10(volume.toDouble()))
          .toFloat()
          .coerceIn(gain.minimum, gain.maximum)
      }

      gain.value = db

      clip.addLineListener {
        if (clip.isOpen && !clip.isRunning) {
          clip.close()
          audioStream.close()
          clips.remove(clip)
        }
      }

      clips += clip
      clip.start()

    } catch (e: Exception) {
      logger.error("Error while playing audio", e)
    }
  }

  fun stopAll() {
    clips.toList().forEach {
      it.stop()
      it.close()
    }

    clips.clear()
  }
}
