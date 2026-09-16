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
        LanguageProfile("pl", "PL", "Polski", "Polska", "🇵🇱", "Warszawa", "PLN", "zł", "Wawel", Locale("pl", "PL")),
        LanguageProfile("en", "EN", "English", "United Kingdom", "🇬🇧", "London", "GBP", "£", "Big Ben", Locale.UK),
        LanguageProfile("de", "DE", "Deutsch", "Deutschland", "🇩🇪", "Berlin", "EUR", "€", "Brandenburger Tor", Locale.GERMANY),
        LanguageProfile("es", "ES", "Español", "España", "🇪🇸", "Madrid", "EUR", "€", "Sagrada Família", Locale("es", "ES")),
        LanguageProfile("it", "IT", "Italiano", "Italia", "🇮🇹", "Roma", "EUR", "€", "Colosseo", Locale.ITALY),
        LanguageProfile("sk", "SK", "Slovenčina", "Slovensko", "🇸🇰", "Bratislava", "EUR", "€", "Bratislavský hrad", Locale("sk", "SK"))
    )

    fun normalize(tag: String?): String {
        val short = tag.orEmpty().substringBefore('-').lowercase()
        return supported.firstOrNull { it.tag == short }?.tag ?: "pl"
    }

    /**
     * Language used on the very first launch, before the player chooses anything.
     * We follow the device/app locale for all supported languages. For devices using
     * another language we fall back to English, which is the safest international default.
     */
    fun detectDeviceLanguage(locale: Locale = Locale.getDefault()): String {
        val short = locale.language.substringBefore('-').lowercase()
        return supported.firstOrNull { it.tag == short }?.tag ?: "en"
    }

    fun profile(tag: String?): LanguageProfile =
        supported.first { it.tag == normalize(tag) }

    fun next(tag: String?): String {
        val current = normalize(tag)
        val index = supported.indexOfFirst { it.tag == current }.coerceAtLeast(0)
        return supported[(index + 1) % supported.size].tag
    }

    fun label(tag: String?): String = profile(tag).shortLabel

    fun allLabels(): String = supported.joinToString(" • ") { it.shortLabel }
}
