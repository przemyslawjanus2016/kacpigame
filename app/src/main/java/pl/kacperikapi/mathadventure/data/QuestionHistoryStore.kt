package pl.kacperikapi.mathadventure.data

import android.content.Context

/** Keeps a persistent rolling list of displayed question IDs to avoid visible repetition. */
class QuestionHistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("kacper_kapi_question_history", Context.MODE_PRIVATE)
    private val maxRecent = 5000

    fun recentIds(): Set<String> = prefs.getString("recent", "")
        .orEmpty()
        .split('|')
        .filter { it.isNotBlank() }
        .toSet()

    fun remember(id: String) {
        val current = prefs.getString("recent", "")
            .orEmpty()
            .split('|')
            .filter { it.isNotBlank() && it != id }
            .toMutableList()
        current += id
        val trimmed = current.takeLast(maxRecent)
        prefs.edit().putString("recent", trimmed.joinToString("|")).apply()
    }

    fun clear() {
        prefs.edit().remove("recent").apply()
    }
}
