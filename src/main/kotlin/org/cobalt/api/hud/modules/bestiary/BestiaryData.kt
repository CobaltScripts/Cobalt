package org.cobalt.api.hud.modules.bestiary

import com.google.gson.JsonObject
import com.google.gson.JsonParser

data class BestiaryMob(
  val name: String,
  val apiKey: String,
  val kills: Int,
  val tier: Int,
  val maxTier: Int,
  val killsForCurrentTier: Int,
  val killsForNextTier: Int,
)

object BestiaryData {

  // Bestiary tier thresholds - cumulative kills needed to reach each tier
  // These match the Hypixel SkyBlock bestiary milestone requirements
  private val TIER_THRESHOLDS = intArrayOf(
    0, 10, 25, 75, 150, 250, 500, 1500, 2500, 5000,
    15000, 25000, 50000, 100000
  )

  // Boss tier thresholds (fewer kills needed)
  private val BOSS_TIER_THRESHOLDS = intArrayOf(
    0, 2, 5, 10, 25, 50, 100, 150, 250, 500,
    750, 1000
  )

  // Mobs that use boss thresholds
  private val BOSS_MOBS = setOf(
    "dragon", "magma_boss", "brood_mother", "svara",
    "ashfang", "bladesoul", "headless_horseman",
    "thunder", "vanquisher"
  )

  fun parseBestiary(profileJson: String): List<BestiaryMob> {
    val mobs = mutableListOf<BestiaryMob>()

    try {
      val json = JsonParser.parseString(profileJson).asJsonObject
      val profile = findSelectedProfile(json) ?: return emptyList()
      val members = profile.getAsJsonObject("members") ?: return emptyList()

      // Find the player's member entry (first available)
      val memberEntry = members.entrySet().firstOrNull() ?: return emptyList()
      val member = memberEntry.value.asJsonObject

      val bestiary = member.getAsJsonObject("bestiary") ?: return emptyList()
      val kills = bestiary.getAsJsonObject("kills") ?: return emptyList()

      for (entry in kills.entrySet()) {
        val mobKey = entry.key
        val killCount = entry.value.asInt
        val isBoss = BOSS_MOBS.any { mobKey.contains(it, ignoreCase = true) }
        val thresholds = if (isBoss) BOSS_TIER_THRESHOLDS else TIER_THRESHOLDS
        val maxTier = thresholds.size - 1

        val tier = calculateTier(killCount, thresholds)
        val killsForCurrent = if (tier > 0) thresholds[tier] else 0
        val killsForNext = if (tier < maxTier) thresholds[tier + 1] else thresholds[maxTier]

        mobs.add(
          BestiaryMob(
            name = formatMobName(mobKey),
            apiKey = mobKey,
            kills = killCount,
            tier = tier,
            maxTier = maxTier,
            killsForCurrentTier = killsForCurrent,
            killsForNextTier = killsForNext,
          )
        )
      }
    } catch (_: Exception) {
      // Return whatever we've parsed so far
    }

    return mobs.sortedByDescending { it.kills }
  }

  fun parseFromMemberData(memberJson: JsonObject): List<BestiaryMob> {
    val mobs = mutableListOf<BestiaryMob>()

    try {
      val bestiary = memberJson.getAsJsonObject("bestiary") ?: return emptyList()
      val kills = bestiary.getAsJsonObject("kills") ?: return emptyList()

      for (entry in kills.entrySet()) {
        val mobKey = entry.key
        val killCount = entry.value.asInt
        val isBoss = BOSS_MOBS.any { mobKey.contains(it, ignoreCase = true) }
        val thresholds = if (isBoss) BOSS_TIER_THRESHOLDS else TIER_THRESHOLDS
        val maxTier = thresholds.size - 1

        val tier = calculateTier(killCount, thresholds)
        val killsForCurrent = if (tier > 0) thresholds[tier] else 0
        val killsForNext = if (tier < maxTier) thresholds[tier + 1] else thresholds[maxTier]

        mobs.add(
          BestiaryMob(
            name = formatMobName(mobKey),
            apiKey = mobKey,
            kills = killCount,
            tier = tier,
            maxTier = maxTier,
            killsForCurrentTier = killsForCurrent,
            killsForNextTier = killsForNext,
          )
        )
      }
    } catch (_: Exception) {
    }

    return mobs.sortedByDescending { it.kills }
  }

  private fun findSelectedProfile(json: JsonObject): JsonObject? {
    if (json.has("profiles")) {
      val profiles = json.getAsJsonArray("profiles")
      for (profile in profiles) {
        val obj = profile.asJsonObject
        if (obj.has("selected") && obj.get("selected").asBoolean) {
          return obj
        }
      }
      // Fallback to first profile
      return profiles.firstOrNull()?.asJsonObject
    }
    return json
  }

  private fun calculateTier(kills: Int, thresholds: IntArray): Int {
    for (i in thresholds.indices.reversed()) {
      if (kills >= thresholds[i]) return i
    }
    return 0
  }

  private fun formatMobName(apiKey: String): String {
    return apiKey
      .replace("_", " ")
      .split(" ")
      .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
  }
}
