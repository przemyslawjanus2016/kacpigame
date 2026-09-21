package pl.kacperikapi.mathadventure.data

import java.util.Locale

data class LanguageProfile(
    val tag: String,
    val shortLabel: String,
    val nativeName: String,
    val countryName: String,
    val countryFlag: String,
    val capital: String,
    val currencyCode: String,
    val currencySymbol: String,
    val landmark: String,
    val ttsLocale: Locale
)

object AppLanguages {
    val supported = listOf(
        LanguageProfile("pl", "PL", "Polski", "Polska", "🇵🇱", "Warszawa", "PLN", "zł", "Wawel", Locale("pl", "PL"))
    )

    fun normalize(tag: String?): String = "pl"

    fun profile(tag: String?): LanguageProfile = supported.first()

    fun next(tag: String?): String = "pl"

    fun label(tag: String?): String = "PL"

    fun allLabels(): String = "PL"
}
