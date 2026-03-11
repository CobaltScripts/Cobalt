package org.cobalt.api.hud.modules.bestiary

import com.google.gson.JsonParser
import java.util.concurrent.ConcurrentHashMap
import org.cobalt.api.util.setupConnection

object BestiaryTracker {

  @Volatile var apiKey: String = ""
  @Volatile var playerUuid: String = ""

  @Volatile var lastFetchTime: Long = 0L
  @Volatile var sessionStartTime: Long = 0L
  @Volatile var isFetching: Boolean = false
  @Volatile var lastError: String? = null
  @Volatile var refreshIntervalSeconds: Double = 60.0

  // Current bestiary data from the API
  @Volatile var currentMobs: List<BestiaryMob> = emptyList()

  // Session start snapshot for calculating session kills
  private val sessionStartKills = ConcurrentHashMap<String, Int>()

  // Total kills across all mobs
  @Volatile var totalKills: Int = 0
  @Volatile var sessionStartTotalKills: Int = 0

  fun startSession() {
    sessionStartTime = System.currentTimeMillis()
    sessionStartKills.clear()
    currentMobs.forEach { mob ->
      sessionStartKills[mob.apiKey] = mob.kills
    }
    sessionStartTotalKills = totalKills
  }

  fun getSessionKills(): Int {
    return totalKills - sessionStartTotalKills
  }

  fun getSessionKillsForMob(apiKey: String): Int {
    val startKills = sessionStartKills[apiKey] ?: return 0
    val currentMob = currentMobs.find { it.apiKey == apiKey } ?: return 0
    return currentMob.kills - startKills
  }

  fun getKillsPerHour(): Double {
    val elapsed = System.currentTimeMillis() - sessionStartTime
    if (elapsed < 1000) return 0.0
    val hours = elapsed / 3600000.0
    return getSessionKills() / hours
  }

  fun getKillsPerHourForMob(apiKey: String): Double {
    val elapsed = System.currentTimeMillis() - sessionStartTime
    if (elapsed < 1000) return 0.0
    val hours = elapsed / 3600000.0
    return getSessionKillsForMob(apiKey) / hours
  }

  fun fetchBestiary() {
    if (apiKey.isBlank() || playerUuid.isBlank()) {
      lastError = "API key or UUID not set"
      return
    }
    if (isFetching) return

    isFetching = true
    lastError = null

    Thread({
      try {
        val url = "https://api.hypixel.net/v2/skyblock/profiles?key=$apiKey&uuid=$playerUuid"
        val response = setupConnection(url, timeout = 10000, useCaches = false)
          .bufferedReader().readText()

        val json = JsonParser.parseString(response).asJsonObject

        if (!json.get("success").asBoolean) {
          lastError = json.get("cause")?.asString ?: "API request failed"
          return@Thread
        }

        val profiles = json.getAsJsonArray("profiles") ?: run {
          lastError = "No profiles found"
          return@Thread
        }

        // Find selected profile
        var selectedProfile: com.google.gson.JsonObject? = null
        for (profile in profiles) {
          val obj = profile.asJsonObject
          if (obj.has("selected") && obj.get("selected").asBoolean) {
            selectedProfile = obj
            break
          }
        }
        if (selectedProfile == null) {
          selectedProfile = profiles.firstOrNull()?.asJsonObject
        }

        if (selectedProfile == null) {
          lastError = "No profile data"
          return@Thread
        }

        val members = selectedProfile.getAsJsonObject("members") ?: run {
          lastError = "No member data"
          return@Thread
        }

        // Find the player's UUID entry (try both with and without dashes)
        val cleanUuid = playerUuid.replace("-", "")
        val memberData = members.get(cleanUuid)?.asJsonObject
          ?: members.get(playerUuid)?.asJsonObject
          ?: members.entrySet().firstOrNull()?.value?.asJsonObject

        if (memberData == null) {
          lastError = "Player not found in profile"
          return@Thread
        }

        val mobs = BestiaryData.parseFromMemberData(memberData)
        val isFirstFetch = currentMobs.isEmpty()

        currentMobs = mobs
        totalKills = mobs.sumOf { it.kills }
        lastFetchTime = System.currentTimeMillis()

        if (isFirstFetch || sessionStartTime == 0L) {
          startSession()
        }
      } catch (e: Exception) {
        lastError = e.message ?: "Unknown error"
      } finally {
        isFetching = false
      }
    }, "Bestiary-Fetch").start()
  }

  fun getSessionDurationFormatted(): String {
    if (sessionStartTime == 0L) return "0:00:00"
    val elapsed = System.currentTimeMillis() - sessionStartTime
    val seconds = (elapsed / 1000) % 60
    val minutes = (elapsed / 60000) % 60
    val hours = elapsed / 3600000
    return "%d:%02d:%02d".format(hours, minutes, seconds)
  }

  fun formatNumber(number: Int): String {
    return "%,d".format(number)
  }

  fun formatNumber(number: Double): String {
    return "%,.1f".format(number)
  }
}
