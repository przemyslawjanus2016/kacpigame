package pl.kacperikapi.mathadventure.data

import android.content.Context
import java.util.UUID

data class PlayerProfile(
    val id: String,
    val name: String
)

/**
 * Local-only player profiles. No account, cloud identity or child's personal data leaves the device.
 */
class PlayerProfileStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val legacyPrefs = appContext.getSharedPreferences(LEGACY_PROGRESS_PREFS, Context.MODE_PRIVATE)

    init {
        migrateLegacyProgressIfNeeded()
    }

    fun profiles(): List<PlayerProfile> = profileIds().mapNotNull { id ->
        prefs.getString("name_$id", null)?.takeIf { it.isNotBlank() }?.let { PlayerProfile(id, it) }
    }

    fun activeProfileId(): String? {
        val active = prefs.getString(KEY_ACTIVE_PROFILE, null)
        return active?.takeIf { id -> profileIds().contains(id) }
    }

    fun activeProfile(): PlayerProfile? = activeProfileId()?.let { id -> profiles().firstOrNull { it.id == id } }

    fun select(profileId: String) {
        if (profileIds().contains(profileId)) {
            prefs.edit().putString(KEY_ACTIVE_PROFILE, profileId).apply()
        }
    }

    fun add(name: String): PlayerProfile {
        val safeName = sanitizeName(name)
        require(safeName.isNotBlank()) { "Profile name cannot be blank" }
        val id = UUID.randomUUID().toString().replace("-", "").take(16)
        val ids = profileIds().toMutableList().apply { add(id) }
        prefs.edit()
            .putString(KEY_PROFILE_IDS, ids.joinToString(","))
            .putString("name_$id", safeName)
            .putString(KEY_ACTIVE_PROFILE, id)
            .apply()
        return PlayerProfile(id, safeName)
    }

    fun rename(profileId: String, name: String) {
        if (!profileIds().contains(profileId)) return
        val safeName = sanitizeName(name)
        if (safeName.isBlank()) return
        prefs.edit().putString("name_$profileId", safeName).apply()
    }

    fun delete(profileId: String) {
        val ids = profileIds().toMutableList()
        if (!ids.remove(profileId)) return
        appContext.getSharedPreferences(progressPrefsName(profileId), Context.MODE_PRIVATE).edit().clear().apply()
        val editor = prefs.edit()
            .putString(KEY_PROFILE_IDS, ids.joinToString(","))
            .remove("name_$profileId")
        if (prefs.getString(KEY_ACTIVE_PROFILE, null) == profileId) {
            if (ids.isEmpty()) editor.remove(KEY_ACTIVE_PROFILE) else editor.putString(KEY_ACTIVE_PROFILE, ids.first())
        }
        editor.apply()
    }

    private fun profileIds(): List<String> = prefs.getString(KEY_PROFILE_IDS, "")
        .orEmpty()
        .split(',')
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    /**
     * Existing installs had one global progress slot. Preserve it as a local profile instead of losing progress.
     */
    private fun migrateLegacyProgressIfNeeded() {
        if (prefs.getBoolean(KEY_LEGACY_MIGRATED, false)) return
        val hasProgress = LEGACY_PROGRESS_KEYS.any { legacyPrefs.contains(it) }
        if (profileIds().isEmpty() && hasProgress) {
            val id = LEGACY_PROFILE_ID
            prefs.edit()
                .putString(KEY_PROFILE_IDS, id)
                .putString("name_$id", "Gracz 1")
                .putString(KEY_ACTIVE_PROFILE, id)
                .putBoolean(KEY_LEGACY_MIGRATED, true)
                .apply()

            val target = appContext.getSharedPreferences(progressPrefsName(id), Context.MODE_PRIVATE)
            val editor = target.edit()
            legacyPrefs.all.forEach { (key, value) ->
                if (key in GLOBAL_SETTING_KEYS) return@forEach
                when (value) {
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is String -> editor.putString(key, value)
                    is Set<*> -> @Suppress("UNCHECKED_CAST") editor.putStringSet(key, value as? Set<String>)
                }
            }
            editor.putInt("save_schema_version", ProgressStore.CURRENT_SCHEMA_VERSION).apply()
        } else {
            prefs.edit().putBoolean(KEY_LEGACY_MIGRATED, true).apply()
        }
    }

    private fun sanitizeName(raw: String): String = raw.trim().replace(Regex("\\s+"), " ").take(24)

    companion object {
        const val LEGACY_PROGRESS_PREFS = "kacper_kapi_progress"
        const val LEGACY_PROFILE_ID = "legacy"
        private const val PREFS_NAME = "kacper_kapi_profiles"
        private const val KEY_PROFILE_IDS = "profile_ids"
        private const val KEY_ACTIVE_PROFILE = "active_profile"
        private const val KEY_LEGACY_MIGRATED = "legacy_profile_migrated"

        private val GLOBAL_SETTING_KEYS = setOf("language", "narrator", "sound")
        private val LEGACY_PROGRESS_KEYS = setOf(
            "coins", "stars", "unlocked_world", "max_stage_by_world", "completed_stages",
            "stage_stars", "solved", "correct", "daily_streak", "daily_last_date", "daily_total", "postcards"
        )

        fun progressPrefsName(profileId: String): String = "kacper_kapi_progress_$profileId"
    }
}
