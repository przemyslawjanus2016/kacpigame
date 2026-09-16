package pl.kacperikapi.mathadventure.data

import android.content.Context

class ProgressStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("kacper_kapi_progress", Context.MODE_PRIVATE)

    fun load(): GameProgress = runCatching { loadInternal() }.getOrElse {
        val language = loadLanguage()
        val narrator = loadNarratorEnabled()
        val sound = loadSoundEnabled()
        prefs.edit().clear()
            .putString("language", language)
            .putBoolean("narrator", narrator)
            .putBoolean("sound", sound)
            .apply()
        GameProgress()
    }

    private fun loadInternal(): GameProgress {
        val stats = LearningCategory.entries.associateWith { category ->
            val key = category.name.lowercase()
            CategoryStats(
                solved = safeInt("solved_$key", 0),
                correct = safeInt("correct_$key", 0)
            )
        }
        val maxStage = decodeIntMap(safeString("max_stage_by_world", null))
            .ifEmpty {
                val old = safeInt("max_level", 1).coerceIn(1, 10)
                mapOf(1 to old)
            }
        return GameProgress(
            coins = safeInt("coins", 0).coerceAtLeast(0),
            stars = safeInt("stars", 0).coerceAtLeast(0),
            unlockedWorldId = safeInt("unlocked_world", 1).coerceIn(1, GameContent.worlds.size),
            maxStageByWorld = maxStage.mapValues { (_, v) -> v.coerceIn(0, 10) },
            completedStageIds = safeString("completed_stages", "")
                .orEmpty().split(',').filter { it.isNotBlank() }.toSet(),
            starsByStage = decodeStringIntMap(safeString("stage_stars", null))
                .mapValues { (_, v) -> v.coerceIn(0, 3) },
            solvedTasks = safeInt("solved", 0).coerceAtLeast(0),
            correctTasks = safeInt("correct", 0).coerceAtLeast(0),
            categoryStats = stats,
            dailyStreak = safeInt("daily_streak", 0).coerceAtLeast(0),
            dailyLastCompletedDate = safeString("daily_last_date", "").orEmpty(),
            totalDailyMissions = safeInt("daily_total", 0).coerceAtLeast(0),
            collectedPostcards = decodeIntSet(safeString("postcards", null))
        )
    }

    fun save(progress: GameProgress) {
        val editor = prefs.edit()
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

    fun loadLanguage(): String =
        AppLanguages.normalize(safeString("language", "pl"))

    fun saveLanguage(tag: String) {
        prefs.edit().putString("language", AppLanguages.normalize(tag)).apply()
    }

    fun loadNarratorEnabled(): Boolean = safeBoolean("narrator", true)
    fun saveNarratorEnabled(enabled: Boolean) = prefs.edit().putBoolean("narrator", enabled).apply()
    fun loadSoundEnabled(): Boolean = safeBoolean("sound", true)
    fun saveSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean("sound", enabled).apply()

    /**
     * Starts the adventure from zero while keeping UI preferences.
     * Also clears anti-repeat history for a genuinely fresh playthrough.
     */
    fun resetAllProgress(): GameProgress {
        val language = loadLanguage()
        val narrator = loadNarratorEnabled()
        val sound = loadSoundEnabled()
        prefs.edit().clear()
            .putString("language", language)
            .putBoolean("narrator", narrator)
            .putBoolean("sound", sound)
            .commit()
        QuestionHistoryStore(appContext).clear()
        runCatching {
            java.io.File(appContext.filesDir, "attraction_photos").deleteRecursively()
        }
        return GameProgress()
    }

    private fun safeInt(key: String, default: Int): Int = try {
        prefs.getInt(key, default)
    } catch (_: ClassCastException) {
        prefs.edit().remove(key).apply(); default
    }

    private fun safeBoolean(key: String, default: Boolean): Boolean = try {
        prefs.getBoolean(key, default)
    } catch (_: ClassCastException) {
        prefs.edit().remove(key).apply(); default
    }

    private fun safeString(key: String, default: String?): String? = try {
        prefs.getString(key, default)
    } catch (_: ClassCastException) {
        prefs.edit().remove(key).apply(); default
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
}
