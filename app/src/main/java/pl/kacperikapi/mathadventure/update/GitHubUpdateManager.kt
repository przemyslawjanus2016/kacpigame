package pl.kacperikapi.mathadventure.update

import android.content.Context
import java.io.File

/**
 * Compatibility facade kept for the existing Settings screen.
 *
 * Google Play builds must be updated through Google Play, so the old
 * GitHub APK downloader/installer is intentionally disabled.
 */
object GitHubUpdateManager {
    data class UpdateInfo(
        val version: String,
        val apkUrl: String,
        val releaseUrl: String,
        val notes: String,
        val publishedAt: String
    )

    sealed interface CheckResult {
        data class Available(val info: UpdateInfo) : CheckResult
        data object UpToDate : CheckResult
        data class Error(val message: String) : CheckResult
    }

    suspend fun checkForUpdate(): CheckResult = CheckResult.UpToDate

    @Suppress("UNUSED_PARAMETER")
    suspend fun downloadApk(context: Context, info: UpdateInfo): Result<File> =
        Result.failure(IllegalStateException("Updates are delivered through Google Play"))

    @Suppress("UNUSED_PARAMETER")
    fun installApk(context: Context, apk: File): Boolean = false

    internal fun isNewer(remote: String, current: String): Boolean {
        fun parse(v: String): List<Int> = v.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val a = parse(remote)
        val b = parse(current)
        val size = maxOf(a.size, b.size)
        for (i in 0 until size) {
            val av = a.getOrElse(i) { 0 }
            val bv = b.getOrElse(i) { 0 }
            if (av != bv) return av > bv
        }
        return false
    }
}
