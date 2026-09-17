package pl.kacperikapi.mathadventure.data

import android.content.Context

class ProgressStore(context: Context) {
    private val appContext = context.applicationContext
    private val profileStore = PlayerProfileStore(appContext)
    private val globalPrefs = appContext.getSharedPreferences(PlayerProfileStore.LEGACY_PROGRESS_PREFS, Context.MODE_PRIVATE)

    fun profiles(): List<PlayerProfile> = profileStore.profiles()
    fun activeProfile(): PlayerProfile? = profileStore.activeProfile()
    fun activeProfileId(): String? = profileStore.activeProfileId()

    fun addProfile(name: String): PlayerProfile = profileStore.add(name)

    fun selectProfile(profileId: String) {
        profileStore.select(profileId)
    }

    fun renameProfile(profileId: String, name: String) = profileStore.rename(profileId, name)
    fun deleteProfile(profileId: String) = profileStore.delete(profileId)

    private fun prefs() = activeProfileId()?.let {
        appContext.getSharedPreferences(PlayerProfileStore.progressPrefsName(it), Context.MODE_PRIVATE)
    }

    fun load(): GameProgress {
        val prefs = prefs() ?: return GameProgress()
        return runCatching { loadInternal(prefs) }.getOrElse {
            prefs.edit().clear().putInt("save_schema_version", CURRENT_SCHEMA_VERSION).apply()
            GameProgress()
        }
    }

    private fun loadInternal(prefs: android.content.SharedPreferences): GameProgress {
        val stats = LearningCategory.entries.associateWith { category ->
            val key = category.name.lowercase()
            CategoryStats(
                solved = safeInt(prefs, "solved_$key", 0),
                correct = safeInt(prefs, "correct_$key", 0)
            )
        }
        val maxStage = decodeIntMap(safeString(prefs, "max_stage_by_world", null))
            .ifEmpty {
                val old = safeInt(prefs, "max_level", 1).coerceAtLeast(1)
                mapOf(1 to old)
            }
            .mapNotNull { (worldId, value) ->
                GameContent.worlds.firstOrNull { it.id == worldId }?.let { world ->
                    worldId to value.coerceIn(0, world.stages.size)
                }
            }.toMap()

        val maxWorldId = GameContent.worlds.maxOfOrNull { it.id } ?: 1
        return GameProgress(
            schemaVersion = safeInt(prefs, "save_schema_version", 1).coerceAtLeast(1),
            coins = safeInt(prefs, "coins", 0).coerceAtLeast(0),
            stars = safeInt(prefs, "stars", 0).coerceAtLeast(0),
            unlockedWorldId = safeInt(prefs, "unlocked_world", 1).coerceIn(1, maxWorldId),
            maxStageByWorld = maxStage,
            completedStageIds = safeString(prefs, "completed_stages", "")
                .orEmpty().split(',').filter { it.isNotBlank() }.toSet(),
            starsByStage = decodeStringIntMap(safeString(prefs, "stage_stars", null))
                .mapValues { (_, v) -> v.coerceIn(0, 3) },
            solvedTasks = safeInt(prefs, "solved", 0).coerceAtLeast(0),
            correctTasks = safeInt(prefs, "correct", 0).coerceAtLeast(0),
            categoryStats = stats,
            dailyStreak = safeInt(prefs, "daily_streak", 0).coerceAtLeast(0),
            dailyLastCompletedDate = safeString(prefs, "daily_last_date", "").orEmpty(),
            totalDailyMissions = safeInt(prefs, "daily_total", 0).coerceAtLeast(0),
            collectedPostcards = decodeIntSet(safeString(prefs, "postcards", null))
        )
    }

    fun save(progress: GameProgress) {
        val prefs = prefs() ?: return
        val editor = prefs.edit()
            .putInt("save_schema_version", CURRENT_SCHEMA_VERSION)
            .putInt("coins", progress.coins)
            .putInt("stars", progress.stars)
            .putInt("unlocked_world", progress.unlockedWorldId)
            .putString("max_stage_by_world", encodeIntMap(progress.maxStageByWorld))
            .putString("completed_stages", progress.completedStageIds.joinToString(","))
            .putString("stage_stars", encodeStringIntMap(progress.starsByStage))
            .putInt("solved", progress.solvedTasks)
            .putInt("correct", progress.correctTasks)
            .putInt("daily_streak", progress.dailyStreak)
            .putString("daily_last_date", progress.dailyLastCompletedDate)
            .putInt("daily_total", progress.totalDailyMissions)
            .putString("postcards", progress.collectedPostcards.sorted().joinToString(","))

        progress.categoryStats.forEach { (category, stats) ->
            val key = category.name.lowercase()
            editor.putInt("solved_$key", stats.solved)
            editor.putInt("correct_$key", stats.correct)
        }
        editor.apply()
    }

    /** UI preferences are device-wide, while game progress belongs to the selected local player. */
    fun loadLanguage(): String {
        val saved = globalSafeString("language", null)
        return if (saved.isNullOrBlank()) AppLanguages.detectDeviceLanguage() else AppLanguages.normalize(saved)
    }

    fun saveLanguage(tag: String) = globalPrefs.edit().putString("language", AppLanguages.normalize(tag)).apply()
    fun hasSavedLanguage(): Boolean = globalPrefs.contains("language")
    fun loadNarratorEnabled(): Boolean = globalSafeBoolean("narrator", true)
    fun saveNarratorEnabled(enabled: Boolean) = globalPrefs.edit().putBoolean("narrator", enabled).apply()
    fun loadSoundEnabled(): Boolean = globalSafeBoolean("sound", true)
    fun saveSoundEnabled(enabled: Boolean) = globalPrefs.edit().putBoolean("sound", enabled).apply()

    /** Starts only the active player's adventure from zero. Other local profiles are untouched. */
    fun resetAllProgress(): GameProgress {
        prefs()?.edit()?.clear()?.putInt("save_schema_version", CURRENT_SCHEMA_VERSION)?.commit()
        QuestionHistoryStore(appContext).clear()
        return GameProgress(schemaVersion = CURRENT_SCHEMA_VERSION)
    }

    private fun safeInt(prefs: android.content.SharedPreferences, key: String, default: Int): Int = try {
        prefs.getInt(key, default)
    } catch (_: ClassCastException) {
        prefs.edit().remove(key).apply(); default
    }

    private fun safeString(prefs: android.content.SharedPreferences, key: String, default: String?): String? = try {
        prefs.getString(key, default)
    } catch (_: ClassCastException) {
        prefs.edit().remove(key).apply(); default
    }

    private fun globalSafeBoolean(key: String, default: Boolean): Boolean = try {
        globalPrefs.getBoolean(key, default)
    } catch (_: ClassCastException) {
        globalPrefs.edit().remove(key).apply(); default
    }

    private fun globalSafeString(key: String, default: String?): String? = try {
        globalPrefs.getString(key, default)
    } catch (_: ClassCastException) {
        globalPrefs.edit().remove(key).apply(); default
    }

    private fun encodeIntMap(map: Map<Int, Int>): String = map.entries.joinToString(";") { "${it.key}:${it.value}" }

    private fun decodeIntMap(raw: String?): Map<Int, Int> = raw.orEmpty().split(';').mapNotNull { item ->
        val p = item.split(':')
        if (p.size != 2) null else {
            val k = p[0].toIntOrNull()
            val v = p[1].toIntOrNull()
            if (k == null || v == null) null else k to v
        }
    }.toMap()

    private fun encodeStringIntMap(map: Map<String, Int>): String = map.entries.joinToString(";") { "${it.key}:${it.value}" }

    private fun decodeStringIntMap(raw: String?): Map<String, Int> = raw.orEmpty().split(';').mapNotNull { item ->
        val index = item.lastIndexOf(':')
        if (index <= 0) null else {
            val k = item.substring(0, index)
            val v = item.substring(index + 1).toIntOrNull()
            if (v == null) null else k to v
        }
    }.toMap()

    private fun decodeIntSet(raw: String?): Set<Int> = raw.orEmpty()
        .split(',')
        .mapNotNull { it.toIntOrNull() }
        .toSet()

    companion object {
        const val CURRENT_SCHEMA_VERSION = 2
    }
}
