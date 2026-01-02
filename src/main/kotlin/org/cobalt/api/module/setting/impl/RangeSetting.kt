package org.cobalt.api.module.setting.impl

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.math.BigDecimal
import java.math.RoundingMode
import org.cobalt.api.module.setting.Setting

class RangeSetting(
  name: String,
  description: String,
  private val defaultValue: Pair<Double, Double>,
  val min: Double,
  val max: Double,
) : Setting<Pair<Double, Double>>(name, description, defaultValue) {

  override fun read(element: JsonElement) {
    if (element.isJsonObject) {
      val obj = element.asJsonObject
      val start = obj.get("start")?.asDouble ?: defaultValue.first
      val end = obj.get("end")?.asDouble ?: defaultValue.second
      setAndClampValue(Pair(start, end))
    }
  }

  override fun write(): JsonElement {
    return JsonObject().apply {
      add("start", JsonPrimitive(value.first.roundTo()))
      add("end", JsonPrimitive(value.second.roundTo()))
    }
  }

  private fun setAndClampValue(newValue: Pair<Double, Double>) {
    val v1 = newValue.first.coerceIn(min, max)
    val v2 = newValue.second.coerceIn(min, max)
    this.value = Pair(minOf(v1, v2), maxOf(v1, v2))
  }

  private fun Double.roundTo(): Double =
    BigDecimal(this).setScale(1, RoundingMode.HALF_UP).toDouble()

}
