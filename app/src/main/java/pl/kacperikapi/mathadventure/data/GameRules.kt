package pl.kacperikapi.mathadventure.data

/** Central progression and age-difficulty rules for the adventure campaign. */
object GameRules {
    const val STAGES_PER_WORLD = 10
    const val QUESTIONS_PER_ROUND = 7
    const val PASSING_CORRECT = 5
    const val MIN_AGE = 4
    const val MAX_AGE = 8

    /** Ten smooth difficulty steps covering ages 4–8. */
    private val stageAgeTargets = listOf(4, 4, 5, 5, 6, 6, 7, 7, 8, 8)

    fun targetAge(stageNumber: Int): Int =
        stageAgeTargets[(stageNumber - 1).coerceIn(0, stageAgeTargets.lastIndex)]

    fun requirement(language: String): String = when (AppLanguages.normalize(language)) {
        "en" -> "To unlock the next stage, get at least $PASSING_CORRECT correct answers out of $QUESTIONS_PER_ROUND."
        "de" -> "Um die nächste Etappe freizuschalten, brauchst du mindestens $PASSING_CORRECT richtige Antworten von $QUESTIONS_PER_ROUND."
        "es" -> "Para desbloquear la siguiente etapa, consigue al menos $PASSING_CORRECT respuestas correctas de $QUESTIONS_PER_ROUND."
        "it" -> "Per sbloccare la tappa successiva, ottieni almeno $PASSING_CORRECT risposte corrette su $QUESTIONS_PER_ROUND."
        "sk" -> "Na odomknutie ďalšej etapy potrebuješ aspoň $PASSING_CORRECT správnych odpovedí zo $QUESTIONS_PER_ROUND."
        else -> "Aby odblokować kolejny etap, zdobądź co najmniej $PASSING_CORRECT poprawnych odpowiedzi z $QUESTIONS_PER_ROUND."
    }

    fun failedNotice(language: String, correct: Int, total: Int): String = when (AppLanguages.normalize(language)) {
        "en" -> "You got $correct/$total. ${requirement(language)} The next stage is still locked."
        "de" -> "Du hast $correct/$total richtig. ${requirement(language)} Die nächste Etappe bleibt gesperrt."
        "es" -> "Has acertado $correct/$total. ${requirement(language)} La siguiente etapa sigue bloqueada."
        "it" -> "Hai risposto correttamente a $correct/$total. ${requirement(language)} La tappa successiva resta bloccata."
        "sk" -> "Máš $correct/$total správne. ${requirement(language)} Ďalšia etapa zostáva zamknutá."
        else -> "Masz $correct/$total poprawnych odpowiedzi. ${requirement(language)} Kolejny etap pozostaje zablokowany."
    }

    fun requirementPl(): String = requirement("pl")
    fun requirementEn(): String = requirement("en")
}
