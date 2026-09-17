package pl.kacperikapi.mathadventure.data

import android.content.Context

class ProgressStore(context: Context) {
    private val appContext=context.applicationContext
    private val profileStore=PlayerProfileStore(appContext)
    private val globalPrefs=appContext.getSharedPreferences(PlayerProfileStore.LEGACY_PROGRESS_PREFS,Context.MODE_PRIVATE)
    fun profiles()=profileStore.profiles(); fun activeProfile()=profileStore.activeProfile(); fun activeProfileId()=profileStore.activeProfileId()
    fun addProfile(name:String)=profileStore.add(name); fun selectProfile(profileId:String){profileStore.select(profileId)}
    fun renameProfile(profileId:String,name:String)=profileStore.rename(profileId,name); fun deleteProfile(profileId:String)=profileStore.delete(profileId)
    private fun prefs()=activeProfileId()?.let{appContext.getSharedPreferences(PlayerProfileStore.progressPrefsName(it),Context.MODE_PRIVATE)}
    fun load():GameProgress{val p=prefs()?:return GameProgress();return runCatching{loadInternal(p)}.getOrElse{p.edit().clear().putInt("save_schema_version",CURRENT_SCHEMA_VERSION).apply();GameProgress()}}
    private fun loadInternal(p:android.content.SharedPreferences):GameProgress{
        val stats=LearningCategory.entries.associateWith{c->val k=c.name.lowercase();CategoryStats(safeInt(p,"solved_$k",0),safeInt(p,"correct_$k",0))}
        val maxStage=decodeIntMap(safeString(p,"max_stage_by_world",null)).ifEmpty{mapOf(1 to safeInt(p,"max_level",1).coerceAtLeast(1))}.mapNotNull{(id,v)->GameContent.worlds.firstOrNull{it.id==id}?.let{w->id to v.coerceIn(0,w.stages.size)}}.toMap()
        val maxWorld=GameContent.worlds.maxOfOrNull{it.id}?:1
        return GameProgress(schemaVersion=safeInt(p,"save_schema_version",1).coerceAtLeast(1),coins=safeInt(p,"coins",0).coerceAtLeast(0),stars=safeInt(p,"stars",0).coerceAtLeast(0),unlockedWorldId=safeInt(p,"unlocked_world",1).coerceIn(1,maxWorld),maxStageByWorld=maxStage,completedStageIds=safeString(p,"completed_stages","").orEmpty().split(',').filter{it.isNotBlank()}.toSet(),starsByStage=decodeStringIntMap(safeString(p,"stage_stars",null)).mapValues{(_,v)->v.coerceIn(0,3)},solvedTasks=safeInt(p,"solved",0).coerceAtLeast(0),correctTasks=safeInt(p,"correct",0).coerceAtLeast(0),categoryStats=stats,dailyStreak=safeInt(p,"daily_streak",0).coerceAtLeast(0),dailyLastCompletedDate=safeString(p,"daily_last_date","").orEmpty(),totalDailyMissions=safeInt(p,"daily_total",0).coerceAtLeast(0),collectedPostcards=decodeIntSet(safeString(p,"postcards",null)))
    }
    fun save(g:GameProgress){val p=prefs()?:return;val e=p.edit().putInt("save_schema_version",CURRENT_SCHEMA_VERSION).putInt("coins",g.coins).putInt("stars",g.stars).putInt("unlocked_world",g.unlockedWorldId).putString("max_stage_by_world",encodeIntMap(g.maxStageByWorld)).putString("completed_stages",g.completedStageIds.joinToString(",")).putString("stage_stars",encodeStringIntMap(g.starsByStage)).putInt("solved",g.solvedTasks).putInt("correct",g.correctTasks).putInt("daily_streak",g.dailyStreak).putString("daily_last_date",g.dailyLastCompletedDate).putInt("daily_total",g.totalDailyMissions).putString("postcards",g.collectedPostcards.sorted().joinToString(","));g.categoryStats.forEach{(c,s)->val k=c.name.lowercase();e.putInt("solved_$k",s.solved).putInt("correct_$k",s.correct)};e.apply()}
    fun loadLanguage():String{val s=globalSafeString("language",null);return if(s.isNullOrBlank())AppLanguages.detectDeviceLanguage() else AppLanguages.normalize(s)}
    fun saveLanguage(tag:String)=globalPrefs.edit().putString("language",AppLanguages.normalize(tag)).putBoolean(INITIAL_LANGUAGE_CHOICE_KEY,true).apply()
    fun hasSavedLanguage()=globalSafeBoolean(INITIAL_LANGUAGE_CHOICE_KEY,false)
    fun loadNarratorEnabled()=globalSafeBoolean("narrator",true);fun saveNarratorEnabled(v:Boolean)=globalPrefs.edit().putBoolean("narrator",v).apply()
    fun loadSoundEnabled()=globalSafeBoolean("sound",true);fun saveSoundEnabled(v:Boolean)=globalPrefs.edit().putBoolean("sound",v).apply()
    fun resetAllProgress():GameProgress{prefs()?.edit()?.clear()?.putInt("save_schema_version",CURRENT_SCHEMA_VERSION)?.commit();QuestionHistoryStore(appContext).clear();return GameProgress(schemaVersion=CURRENT_SCHEMA_VERSION)}
    private fun safeInt(p:android.content.SharedPreferences,k:String,d:Int)=try{p.getInt(k,d)}catch(_:ClassCastException){p.edit().remove(k).apply();d}
    private fun safeString(p:android.content.SharedPreferences,k:String,d:String?)=try{p.getString(k,d)}catch(_:ClassCastException){p.edit().remove(k).apply();d}
    private fun globalSafeBoolean(k:String,d:Boolean)=try{globalPrefs.getBoolean(k,d)}catch(_:ClassCastException){globalPrefs.edit().remove(k).apply();d}
    private fun globalSafeString(k:String,d:String?)=try{globalPrefs.getString(k,d)}catch(_:ClassCastException){globalPrefs.edit().remove(k).apply();d}
    private fun encodeIntMap(m:Map<Int,Int>)=m.entries.joinToString(";"){"${it.key}:${it.value}"};private fun decodeIntMap(r:String?)=r.orEmpty().split(';').mapNotNull{val a=it.split(':');if(a.size!=2)null else{val k=a[0].toIntOrNull();val v=a[1].toIntOrNull();if(k==null||v==null)null else k to v}}.toMap()
    private fun encodeStringIntMap(m:Map<String,Int>)=m.entries.joinToString(";"){"${it.key}:${it.value}"};private fun decodeStringIntMap(r:String?)=r.orEmpty().split(';').mapNotNull{val i=it.lastIndexOf(':');if(i<=0)null else{val k=it.substring(0,i);val v=it.substring(i+1).toIntOrNull();if(v==null)null else k to v}}.toMap();private fun decodeIntSet(r:String?)=r.orEmpty().split(',').mapNotNull{it.toIntOrNull()}.toSet()
    companion object{const val CURRENT_SCHEMA_VERSION=2;private const val INITIAL_LANGUAGE_CHOICE_KEY="initial_language_choice_v2"}
}
