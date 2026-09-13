package pl.kacperikapi.mathadventure.ui.components

import android.graphics.BitmapFactory
import android.text.Html
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pl.kacperikapi.mathadventure.data.CommonsPhotoAttribution
import pl.kacperikapi.mathadventure.ui.theme.Cream
import pl.kacperikapi.mathadventure.ui.theme.WoodBrown
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Loads a real Wikimedia Commons image and stores it in app-private cache.
 *
 * For the first 30 cards we use a curated exact file name. Remaining cards can use
 * a precise Commons search query; in that mode the API also returns author/licence
 * attribution which is shown in the mission card.
 */
@Composable
fun WikimediaPhoto(
    fileName: String,
    contentDescription: String,
    @DrawableRes fallbackRes: Int?,
    modifier: Modifier = Modifier,
    searchQuery: String? = null,
    onAttribution: (CommonsPhotoAttribution?) -> Unit = {}
) {
    val context = LocalContext.current
    var image by remember(fileName, searchQuery) { mutableStateOf<ImageBitmap?>(null) }
    var finished by remember(fileName, searchQuery) { mutableStateOf(false) }

    LaunchedEffect(fileName, searchQuery) {
        val loaded = withContext(Dispatchers.IO) {
            loadAndCacheCommonsPhoto(context.filesDir, fileName, searchQuery)
        }
        image = loaded?.bitmap
        onAttribution(loaded?.attribution)
        finished = true
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Cream),
        contentAlignment = Alignment.Center
    ) {
        if (fallbackRes != null) {
            Image(
                painter = painterResource(fallbackRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        image?.let {
            Image(
                bitmap = it,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        if (!finished && image == null) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = .18f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WoodBrown, strokeWidth = 3.dp)
            }
        }
    }
}

private data class LoadedCommonsPhoto(
    val bitmap: ImageBitmap,
    val attribution: CommonsPhotoAttribution?
)

private data class ResolvedCommonsImage(
    val imageUrl: String,
    val attribution: CommonsPhotoAttribution
)

private fun loadAndCacheCommonsPhoto(filesDir: File, fileName: String, searchQuery: String?): LoadedCommonsPhoto? {
    return runCatching {
        val folder = File(filesDir, "attraction_photos").apply { mkdirs() }
        val identity = if (!searchQuery.isNullOrBlank()) "search:$searchQuery" else "file:$fileName"
        val safeName = "commons_${identity.hashCode().toString().replace("-", "n")}.img"
        val cached = File(folder, safeName)
        val meta = File(folder, "$safeName.meta")

        var attribution: CommonsPhotoAttribution? = readAttribution(meta)
        var imageUrl: String? = null

        if (!cached.exists() || cached.length() < 1024L) {
            if (!searchQuery.isNullOrBlank()) {
                val resolved = resolveCommonsSearch(searchQuery) ?: return@runCatching null
                imageUrl = resolved.imageUrl
                attribution = resolved.attribution
                writeAttribution(meta, resolved.attribution)
            } else {
                if (fileName.isBlank()) return@runCatching null
                val encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20")
                imageUrl = "https://commons.wikimedia.org/wiki/Special:Redirect/file/$encoded?width=1280"
            }

            val downloadUrl = imageUrl ?: return@runCatching null
            val connection = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 20_000
                instanceFollowRedirects = true
                useCaches = true
                setRequestProperty(
                    "User-Agent",
                    "KacperKapiEducationalAdventure/0.4.0 (Android educational app; Wikimedia Commons image cache)"
                )
            }

            try {
                val code = connection.responseCode
                if (code !in 200..299) return@runCatching null
                val temp = File(folder, "$safeName.tmp")
                connection.inputStream.use { input ->
                    temp.outputStream().buffered().use { output -> input.copyTo(output) }
                }
                if (temp.length() >= 1024L) {
                    if (cached.exists()) cached.delete()
                    if (!temp.renameTo(cached)) {
                        temp.copyTo(cached, overwrite = true)
                        temp.delete()
                    }
                } else {
                    temp.delete()
                    return@runCatching null
                }
            } finally {
                connection.disconnect()
            }
        }

        if (!cached.exists()) return@runCatching null
        val bitmap = BitmapFactory.decodeFile(cached.absolutePath)?.asImageBitmap() ?: return@runCatching null
        LoadedCommonsPhoto(bitmap, attribution)
    }.getOrNull()
}

private fun resolveCommonsSearch(searchQuery: String): ResolvedCommonsImage? {
    val encoded = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.name())
    val api = "https://commons.wikimedia.org/w/api.php" +
        "?action=query&generator=search&gsrsearch=$encoded&gsrnamespace=6&gsrlimit=5" +
        "&prop=imageinfo&iiprop=url%7Cextmetadata&iiurlwidth=1280&format=json"
    val connection = (URL(api).openConnection() as HttpURLConnection).apply {
        connectTimeout = 10_000
        readTimeout = 15_000
        setRequestProperty("Accept", "application/json")
        setRequestProperty("User-Agent", "KacperKapiEducationalAdventure/0.4.0")
    }
    return try {
        if (connection.responseCode !in 200..299) return null
        val payload = connection.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(payload)
        val pages = root.optJSONObject("query")?.optJSONObject("pages") ?: return null
        val keys = pages.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val page = pages.optJSONObject(key) ?: continue
            val info = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
            val imageUrl = info.optString("thumburl").ifBlank { info.optString("url") }
            if (imageUrl.isBlank()) continue
            val meta = info.optJSONObject("extmetadata")
            val artist = cleanHtml(meta?.optJSONObject("Artist")?.optString("value").orEmpty())
                .ifBlank { "Wikimedia Commons contributor" }
            val license = cleanHtml(meta?.optJSONObject("LicenseShortName")?.optString("value").orEmpty())
                .ifBlank { "see source" }
            val source = info.optString("descriptionurl").ifBlank {
                val title = page.optString("title").removePrefix("File:")
                "https://commons.wikimedia.org/wiki/File:" + URLEncoder.encode(title, StandardCharsets.UTF_8.name()).replace("+", "_")
            }
            return ResolvedCommonsImage(
                imageUrl = imageUrl,
                attribution = CommonsPhotoAttribution(artist, license, source)
            )
        }
        null
    } finally {
        connection.disconnect()
    }
}

private fun cleanHtml(value: String): String =
    Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString().replace(Regex("\\s+"), " ").trim()

private fun writeAttribution(file: File, attribution: CommonsPhotoAttribution) {
    runCatching {
        val json = JSONObject()
            .put("author", attribution.author)
            .put("license", attribution.license)
            .put("source", attribution.sourcePage)
        file.writeText(json.toString())
    }
}

private fun readAttribution(file: File): CommonsPhotoAttribution? = runCatching {
    if (!file.exists()) return@runCatching null
    val json = JSONObject(file.readText())
    CommonsPhotoAttribution(
        author = json.optString("author", "Wikimedia Commons contributor"),
        license = json.optString("license", "see source"),
        sourcePage = json.optString("source", "https://commons.wikimedia.org/")
    )
}.getOrNull()
