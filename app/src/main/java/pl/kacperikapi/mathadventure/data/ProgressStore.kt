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
    fun selectProfile(profileId: String) { profileStore.select(profileId) }
    fun renameProfile(profileId: String, name: String) = profileStore.rename(profileId, name)
    fun deleteProfile(profileId: String) = profileStore.delete(profileId)
    private fun prefs() = activeProfileId()?.let { appContext.getSharedPreferences(PlayerProfileStore.progressPrefsName(it), Context.MODE_PRIVATE) }

    fun load(): GameProgress {
        val prefs = prefs() ?: return GameProgress()
        return runCatching { loadInternal(prefs) }.getOrElse { prefs.edit().clear().putInt("save_schema_version", CURRENT_SCHEMA_VERSION).apply(); GameProgress() }
    }

    private fun loadInternal(prefs: android.content.SharedPreferences): GameProgress {
        val stats = LearningCategory.entries.associateWith { category ->
            val key = category.name.lowercase(); CategoryStats(safeInt(prefs, "solved_$key", 0), safeInt(prefs, "correct_$key", 0))
        }
        val maxStage = decodeIntMap(safeString(prefs, "max_stage_by_world", null)).ifEmpty { mapOf(1 to safeInt(prefs, "max_level", 1).coerceAtLeast(1)) }
            .mapNotNull { (worldId, value) -> GameContent.worlds.firstOrNull { it.id == worldId }?.let { world -> worldId to value.coerceIn(0, world.stages.size) } }.toMap()
        val maxWorldId = GameContent.worlds.maxOfOrNull { it.id } ?: 1
        return GameProgress(
            schemaVersion=safeInt(prefs,"save_schema_version",1).coerceAtLeast(1), coins=safeInt(prefs,"coins",0).coerceAtLeast(0), stars=safeInt(prefs,"stars",0).coerceAtLeast(0),
            unlockedWorldId=safeInt(prefs,"unlocked_world",1).coerceIn(1,maxWorldId), maxStageByWorld=maxStage,
            completedStageIds=safeString(prefs,"completed_stages","").orEmpty().split(',').filter{it.isNotBlank()}.toSet(),
            starsByStage=decodeStringIntMap(safeString(prefs,"stage_stars",null)).mapValues{(_,v)->v.coerceIn(0,3)}, solvedTasks=safeInt(prefs,"solved",0).coerceAtLeast(0),
            correctTasks=safeInt(prefs,"correct",0).coerceAtLeast(0), categoryStats=stats, dailyStreak=safeInt(prefs,"daily_streak",0).coerceAtLeast(0),
            dailyLastCompletedDate=safeString(prefs,"daily_last_date","").orEmpty(), totalDailyMissions=safeInt(prefs,"daily_total",0).coerceAtLeast(0), collectedPostcards=decodeIntSet(safeString(prefs,"postcards",null))
        )
    }

    fun save(progress: GameProgress) {
        val prefs=prefs()?:return
        val editor=prefs.edit().putInt("save_schema_version",CURRENT_SCHEMA_VERSION).putInt("coins",progress.coins).putInt("stars",progress.stars).putInt("unlocked_world",progress.unlockedWorldId)
            .putString("max_stage_by_world",encodeIntMap(progress.maxStageByWorld)).putString("completed_stages",progress.completedStageIds.joinToString(",")).putString("stage_stars",encodeStringIntMap(progress.starsByStage))
            .putInt("solved",progress.solvedTasks).putInt("correct",progress.correctTasks).putInt("daily_streak",progress.dailyStreak).putString("daily_last_date",progress.dailyLastCompletedDate)
            .putInt("daily_total",progress.totalDailyMissions).putString("postcards",progress.collectedPostcards.sorted().joinToString(","))
        progress.categoryStats.forEach{(category,stats)->val key=category.name.lowercase();editor.putInt("solved_$key",stats.solved).putInt("correct_$key",stats.correct)};editor.apply()
    }

    fun loadLanguage(): String { val saved=globalSafeString("language",null); return if(saved.isNullOrBlank()) AppLanguages.detectDeviceLanguage() else AppLanguages.normalize(saved) }
    fun saveLanguage(tag:String)=globalPrefs.edit().putString("language",AppLanguages.normalize(tag)).apply()
    // v2 deliberately requires one explicit language choice even for users upgrading from an older build.
    fun hasSavedLanguage():Boolean=globalSafeBoolean(INITIAL_LANGUAGE_CHOICE_KEY,false)
    fun completeInitialLanguageChoice(tag:String)=globalPrefs.edit().putString("language",AppLanguages.normalize(tag)).putBoolean(INITIAL_LANGUAGE_CHOICE_KEY,true).apply()
    fun loadNarratorEnabled():Boolean=globalSafeBoolean("narrator",true)
    fun saveNarratorEnabled(enabled:Boolean)=globalPrefs.edit().putBoolean("narrator",enabled).apply()
    fun loadSoundEnabled():Boolean=globalSafeBoolean("sound",true)
    fun saveSoundEnabled(enabled:Boolean)=globalPrefs.edit().putBoolean("sound",enabled).apply()
    fun resetAllProgress():GameProgress{prefs()?.edit()?.clear()?.putInt("save_schema_version",CURRENT_SCHEMA_VERSION)?.commit();QuestionHistoryStore(appContext).clear();return GameProgress(schemaVersion=CURRENT_SCHEMA_VERSION)}

    private fun safeInt(p:android.content.SharedPreferences,k:String,d:Int):Int=try{p.getInt(k,d)}catch(_:ClassCastException){p.edit().remove(k).apply();d}
    private fun safeString(p:android.content.SharedPreferences,k:String,d:String?):String?=try{p.getString(k,d)}catch(_:ClassCastException){p.edit().remove(k).apply();d}
    private fun globalSafeBoolean(k:String,d:Boolean):Boolean=try{globalPrefs.getBoolean(k,d)}catch(_:ClassCastException){globalPrefs.edit().remove(k).apply();d}
    private fun globalSafeString(k:String,d:String?):String?=try{globalPrefs.getString(k,d)}catch(_:ClassCastException){globalPrefs.edit().remove(k).apply();d}
    private fun encodeIntMap(m:Map<Int,Int>)=m.entries.joinToString(";"){"${it.key}:${it.value}"}
    private fun decodeIntMap(r:String?):Map<Int,Int>=r.orEmpty().split(';').mapNotNull{val p=it.split(':');if(p.size!=2)null else{val k=p[0].toIntOrNull();val v=p[1].toIntOrNull();if(k==null||v==null)null else k to v}}.toMap()
    private fun encodeStringIntMap(m:Map<String,Int>)=m.entries.joinToString(";"){"${it.key}:${it.value}"}
    private fun decodeStringIntMap(r:String?):Map<String,Int>=r.orEmpty().split(';').mapNotNull{val i=it.lastIndexOf(':');if(i<=0)null else{val k=it.substring(0,i);val v=it.substring(i+1).toIntOrNull();if(v==null)null else k to v}}.toMap()
    private fun decodeIntSet(r:String?):Set<Int>=r.orEmpty().split(',').mapNotNull{it.toIntOrNull()}.toSet()
    companion object{const val CURRENT_SCHEMA_VERSION=2;private const val INITIAL_LANGUAGE_CHOICE_KEY="initial_language_choice_v2"}
}
