package pl.kacperikapi.mathadventure.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pl.kacperikapi.mathadventure.BuildConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lightweight updater for the GitHub-distributed build.
 * It reads the latest public GitHub Release and installs the attached APK.
 * No GitHub token is stored in the app.
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

    private val latestReleaseApi: String
        get() = "https://api.github.com/repos/${BuildConfig.GITHUB_OWNER}/${BuildConfig.GITHUB_REPO}/releases/latest"

    suspend fun checkForUpdate(): CheckResult = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(latestReleaseApi).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 12_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "KacperKapiEducationalAdventure/${BuildConfig.VERSION_NAME}")
            }
            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    return@runCatching CheckResult.Error("GitHub HTTP $responseCode")
                }
                val payload = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(payload)
                val remoteVersion = json.optString("tag_name").removePrefix("v").trim()
                val releaseUrl = json.optString("html_url")
                val notes = json.optString("body")
                val publishedAt = json.optString("published_at")
                val assets = json.optJSONArray("assets")
                var apkUrl = ""
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i) ?: continue
                        val name = asset.optString("name")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url")
                            if (name.contains("KacperKapi", ignoreCase = true)) break
                        }
                    }
                }
                if (remoteVersion.isBlank() || apkUrl.isBlank()) {
                    return@runCatching CheckResult.Error("Release nie zawiera poprawnego pliku APK")
                }
                if (isNewer(remoteVersion, BuildConfig.VERSION_NAME)) {
                    CheckResult.Available(UpdateInfo(remoteVersion, apkUrl, releaseUrl, notes, publishedAt))
                } else {
                    CheckResult.UpToDate
                }
            } finally {
                connection.disconnect()
            }
        }.getOrElse { CheckResult.Error(it.message ?: it.javaClass.simpleName) }
    }

    suspend fun downloadApk(context: Context, info: UpdateInfo): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val folder = File(context.cacheDir, "updates").apply { mkdirs() }
            val output = File(folder, "KacperKapi-v${info.version}.apk")
            val temp = File(folder, "${output.name}.part")
            val connection = (URL(info.apkUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 60_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "KacperKapiEducationalAdventure/${BuildConfig.VERSION_NAME}")
            }
            try {
                if (connection.responseCode !in 200..299) {
                    error("Pobieranie APK: HTTP ${connection.responseCode}")
                }
                connection.inputStream.use { input ->
                    temp.outputStream().buffered().use { outputStream -> input.copyTo(outputStream) }
                }
                if (temp.length() < 50_000L) error("Pobrany plik APK jest nieprawidłowy")
                if (output.exists()) output.delete()
                if (!temp.renameTo(output)) {
                    temp.copyTo(output, overwrite = true)
                    temp.delete()
                }
                output
            } finally {
                connection.disconnect()
            }
        }
    }

    /** Returns true when Android is ready to show the APK installer. */
    fun installApk(context: Context, apk: File): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            val permissionIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(permissionIntent)
            return false
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }

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
