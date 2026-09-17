package pl.kacperikapi.mathadventure.data

import android.content.Context

/** Keeps a rolling list of recently displayed question IDs to avoid visible repetition
 * while still allowing procedural generators to keep producing fresh random variants.
 */
class QuestionHistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("kacper_kapi_question_history", Context.MODE_PRIVATE)
    private val maxRecent = 200

    fun recentIds(): Set<String> = prefs.getString("recent", "")
        .orEmpty()
        .split('|')
        .filter { it.isNotBlank() }
        .takeLast(maxRecent)
        .toSet()

    fun remember(id: String) {
        val current = prefs.getString("recent", "")
            .orEmpty()
            .split('|')
            .filter { it.isNotBlank() && it != id }
            .takeLast(maxRecent - 1)
            .toMutableList()
        current += id
        prefs.edit().putString("recent", current.joinToString("|")).apply()
    }

    fun clear() {
        prefs.edit().remove("recent").apply()
    }
}
