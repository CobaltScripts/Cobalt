package org.cobalt.internal.rotation

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Enum representing various easing functions for smooth animations and transitions.
 * Each enum entry provides a lambda that calculates the eased value for a given time `t`.
 *
 * @property ease The easing function lambda taking a float `t` (0.0 to 1.0) and returning the eased value.
 */
enum class EasingType(val ease: (Float) -> Float) {

    LINEAR({ it }),
    EASE_IN_SINE({ t -> (1 - cos(t * PI / 2)).toFloat() }),
    EASE_OUT_SINE({ t -> sin(t * PI / 2).toFloat() }),
    EASE_IN_OUT_SINE({ t -> (-0.5f * (cos(PI * t) - 1)).toFloat() }),
    EASE_IN_QUAD({ t -> t * t }),
    EASE_OUT_QUAD({ t -> t * (2 - t) }),
    EASE_IN_OUT_QUAD({ t -> if (t < 0.5f) 2 * t * t else -1 + (4 - 2 * t) * t }),
    EASE_IN_CUBIC({ t -> t * t * t }),
    EASE_OUT_CUBIC({ t -> (t - 1).let { it * it * it + 1 } }),
    EASE_IN_OUT_CUBIC({ t ->
        if (t < 0.5f) 4 * t * t * t
        else (t - 1) * (2 * t - 2) * (2 * t - 2) + 1
    }),
    EASE_IN_QUART({ t -> t * t * t * t }),
    EASE_OUT_QUART({ t -> 1 - (t - 1).let { it * it * it * it } }),
    EASE_IN_OUT_QUART({ t ->
        if (t < 0.5f) 8 * t * t * t * t
        else 1 - 8 * (t - 1).let { it * it * it * it }
    }),
    EASE_IN_QUINT({ t -> t * t * t * t * t }),
    EASE_OUT_QUINT({ t -> 1 + (t - 1).let { it * it * it * it * it } }),
    EASE_IN_OUT_QUINT({ t ->
        if (t < 0.5f) 16 * t * t * t * t * t
        else 1 + 16 * (t - 1).let { it * it * it * it * it }
    }),
    EASE_IN_EXPO({ t ->
        if (t == 0f) 0f else (2.0).pow((10.0 * (t - 1))).toFloat()
    }),
    EASE_OUT_EXPO({ t ->
        if (t == 1f) 1f else 1 - (2.0).pow((-10.0 * t)).toFloat()
    }),
    EASE_IN_OUT_EXPO({ t ->
        when {
            t == 0f -> 0f
            t == 1f -> 1f
            t < 0.5f -> ((2.0).pow((20.0 * t - 10.0)) / 2).toFloat()
            else -> (2 - (2.0).pow((-20.0 * t + 10.0)) / 2).toFloat()
        }
    }),
    EASE_IN_CIRC({ t -> (1 - sqrt(1 - t * t)) }),
    EASE_OUT_CIRC({ t ->
        sqrt(1 - (t - 1).let { it * it })
    }),
    EASE_IN_OUT_CIRC({ t ->
        if (t < 0.5f)
          ((1 - sqrt(1 - (2 * t).let { it * it })) / 2)
        else
          ((sqrt(1 - (-2 * t + 2).let { it * it }) + 1) / 2)
    }),
    EASE_IN_BACK({ t ->
        val c1 = 1.70158f
        val c3 = c1 + 1f
        c3 * t * t * t - c1 * t * t
    }),
    EASE_OUT_BACK({ t ->
        val c1 = 1.70158f
        val c3 = c1 + 1f
        val x = t - 1
        1 + c3 * x * x * x + c1 * x * x
    }),
    EASE_IN_OUT_BACK({ t ->
        val c1 = 1.70158f
        val c2 = c1 * 1.525f

        if (t < 0.5f) {
            val k = 2 * t
            (k * k * ((c2 + 1) * k - c2)) / 2
        } else {
            val k = 2 * t - 2
            (k * k * ((c2 + 1) * k + c2) + 2) / 2
        }
    });

    /**
     * Applies the easing function to interpolate between a start and end value.
     *
     * @param from The starting value.
     * @param to The ending value.
     * @param progress The current progress of the animation (0.0 to 1.0).
     * @return The interpolated value based on the easing function.
     */
    fun apply(from: Float, to: Float, progress: Float): Float {
        val t = progress.coerceIn(0f, 1f)
        return from + (to - from) * ease(t)
    }

}
