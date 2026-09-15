package pl.kacperikapi.mathadventure.data

/** Central progression and age-difficulty rules for the adventure campaign. */
object GameRules {
    const val STAGES_PER_WORLD = 7
    const val QUESTIONS_PER_ROUND = 7
    const val PASSING_CORRECT = 5
    const val MIN_AGE = 4
    const val MAX_AGE = 8

    /**
     * Seven smooth difficulty steps covering ages 4–8.
     * The age is a content target, not a strict assessment of the child.
     */
    private val stageAgeTargets = listOf(4, 4, 5, 6, 7, 8, 8)

    fun targetAge(stageNumber: Int): Int =
        stageAgeTargets[(stageNumber - 1).coerceIn(0, stageAgeTargets.lastIndex)]

    fun requirementPl(): String =
        "Aby odblokować kolejny etap, zdobądź co najmniej $PASSING_CORRECT poprawnych odpowiedzi z $QUESTIONS_PER_ROUND."

    fun requirementEn(): String =
        "To unlock the next stage, get at least $PASSING_CORRECT correct answers out of $QUESTIONS_PER_ROUND."
}
