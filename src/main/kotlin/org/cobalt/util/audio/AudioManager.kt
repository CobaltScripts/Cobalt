package org.cobalt.util.audio

import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import kotlin.math.log10
import org.slf4j.LoggerFactory

object AudioManager {
  private val logger =
    LoggerFactory.getLogger(this::class.java)

  // clip.addLineListener callbacks fire on the Java Sound line-event thread, not the caller's thread
  private val clips = CopyOnWriteArrayList<Clip>()

  // YES I KNOW THIS PLAYS THRU SPEAKERS ISNTEAD OF HEADPHONES IDK WHY
  fun play(file: File, volume: Float = 1.0f) {
    if (!file.exists()) return

    try {
      val audioStream = AudioSystem.getAudioInputStream(file)
      val clip = AudioSystem.getClip()

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
