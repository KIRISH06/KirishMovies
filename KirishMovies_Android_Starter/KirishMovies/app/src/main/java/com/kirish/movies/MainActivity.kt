package com.kirish.movies

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

private val Bg = Color(0xFF0D0F12)
private val Panel = Color(0xFF1A1D22)
private val Accent = Color(0xFF25D6A2)
private val Muted = Color(0xFF9AA2AD)

data class Film(
    val id: String,
    val title: String,
    val year: String,
    val license: String = "",
    val description: String = "",
    val mp4: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KirishMoviesApp(this) }
    }
}

@Composable
private fun KirishMoviesApp(context: Context) {
    var query by remember { mutableStateOf("") }
    var films by remember { mutableStateOf<List<Film>>(emptyList()) }
    var selected by remember { mutableStateOf<Film?>(null) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Loading free-to-watch catalog…") }
    var tab by remember { mutableStateOf("Home") }
    val scope = rememberCoroutineScope()

    fun search(text: String) {
        loading = true
        message = "Searching authorized catalog…"
        scope.launch {
            try {
                films = ArchiveCatalog.search(text)
                message = if (films.isEmpty()) "No licensed movie results found. Try another search." else ""
            } catch (e: Exception) {
                message = "Could not load catalog. Check internet and try again."
            } finally { loading = false }
        }
    }

    LaunchedEffect(Unit) { search("") }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg, surface = Panel, primary = Accent,
            onBackground = Color.White, onSurface = Color.White
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("KIRISH", color = Accent, fontSize = 23.sp, fontWeight = FontWeight.Black)
                    Text(" MOVIES", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("v1.0", color = Muted, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("Search public-domain movies…", color = Muted) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                    trailingIcon = {
                        TextButton(onClick = { search(query) }) { Text("Search", color = Accent) }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFF353A42),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )

                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    listOf("Trending", "Movies", "Classics", "Short Films").forEach {
                        Surface(color = if (it == "Movies") Accent.copy(alpha = .18f) else Panel,
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier.clickable { search(if (it == "Classics") "classic" else "") }) {
                            Text(it, Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                color = if (it == "Movies") Accent else Color.White, fontSize = 13.sp)
                        }
                    }
                }

                Text("Explore movies", Modifier.padding(start = 18.dp, bottom = 10.dp),
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                if (loading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth(), color = Accent)
                }
                if (message.isNotBlank()) {
                    Text(message, Modifier.fillMaxWidth().padding(22.dp),
                        color = Muted, fontSize = 14.sp)
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(films, key = { it.id }) { film ->
                        FilmRow(film) { selected = film }
                    }
                }

                NavigationBar(containerColor = Color(0xFF171A1F)) {
                    listOf("Home", "Downloads", "Me").forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Text(when(item) { "Home" -> "⌂"; "Downloads" -> "↓"; else -> "●" }, fontSize = 20.sp) },
                            label = { Text(item) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Accent, selectedTextColor = Accent,
                                indicatorColor = Accent.copy(alpha = .12f),
                                unselectedTextColor = Muted, unselectedIconColor = Muted
                            )
                        )
                    }
                }
            }
        }

        selected?.let { film ->
            AlertDialog(
                onDismissRequest = { selected = null },
                containerColor = Panel,
                title = { Text(film.title, color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (film.year.isBlank()) "Year not listed" else film.year, color = Muted)
                        Text(film.description.ifBlank { "Description not provided by the archive." },
                            color = Color.White, maxLines = 5)
                        Text("License: ${film.license}", color = Accent, fontSize = 12.sp)
                        Text("Check the item page and license terms before sharing or reusing this film.",
                            color = Muted, fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            if (film.mp4.isNotBlank()) openPlayer(context, film.mp4)
                            else Toast.makeText(context, "No MP4 file available for this item", Toast.LENGTH_SHORT).show()
                        }) { Icon(Icons.Default.PlayArrow, null); Text("Play") }
                        TextButton(onClick = {
                            if (film.mp4.isNotBlank()) downloadFilm(context, film)
                            else Toast.makeText(context, "No downloadable MP4 found", Toast.LENGTH_SHORT).show()
                        }) { Icon(Icons.Default.Download, null); Text("Download") }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selected = null }) { Text("Close", color = Muted) }
                }
            )
        }
    }
}

@Composable
private fun FilmRow(film: Film, onClick: () -> Unit) {
    Surface(
        color = Panel, shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(width = 74.dp, height = 98.dp)
                    .background(Color(0xFF26313A), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) { Text("▶", color = Accent, fontSize = 24.sp) }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(film.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(film.year.ifBlank { "Classic film" }, color = Muted, fontSize = 12.sp)
                Text("LICENSE INFO AVAILABLE", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Internet Archive", color = Muted, fontSize = 11.sp)
            }
            Text("›", color = Muted, fontSize = 28.sp)
        }
    }
}

private object ArchiveCatalog {
    suspend fun search(term: String): List<Film> = withContext(Dispatchers.IO) {
        val safe = term.trim().take(80)
        val q = if (safe.isBlank()) "mediatype:movies AND licenseurl:*"
                else "mediatype:movies AND licenseurl:* AND (title:(${safe}) OR subject:(${safe}))"
        val params = "q=${enc(q)}&fl[]=identifier&fl[]=title&fl[]=year&fl[]=licenseurl&rows=24&page=1&output=json"
        val json = getJson("https://archive.org/advancedsearch.php?$params")
        val docs = json.optJSONObject("response")?.optJSONArray("docs") ?: return@withContext emptyList()
        val result = mutableListOf<Film>()
        for (i in 0 until docs.length()) {
            val doc = docs.optJSONObject(i) ?: continue
            val id = doc.optString("identifier")
            if (id.isBlank()) continue
            try {
                val meta = getJson("https://archive.org/metadata/${enc(id)}")
                val md = meta.optJSONObject("metadata") ?: continue
                val license = md.optString("licenseurl").ifBlank { md.optString("rights") }
                if (license.isBlank() || license == "null") continue
                val files = meta.optJSONArray("files") ?: continue
                var mp4 = ""
                for (j in 0 until files.length()) {
                    val f = files.optJSONObject(j) ?: continue
                    val name = f.optString("name")
                    val format = f.optString("format").lowercase()
                    if (name.lowercase().endsWith(".mp4") &&
                        !name.contains("__ia_thumb") &&
                        (format.contains("h.264") || format.contains("mpeg4") || format.contains("mpeg-4") || format.isBlank())) {
                        mp4 = "https://archive.org/download/${enc(id)}/${encPath(name)}"
                        break
                    }
                }
                result.add(Film(
                    id = id,
                    title = doc.optString("title").ifBlank { md.optString("title", id) },
                    year = doc.optString("year"),
                    license = license,
                    description = md.optString("description").take(500),
                    mp4 = mp4
                ))
            } catch (_: Exception) { /* Skip incomplete records */ }
        }
        result
    }

    private fun enc(value: String) = URLEncoder.encode(value, "UTF-8")
    private fun encPath(value: String) = value.split("/").joinToString("/") { enc(it).replace("+", "%20") }

    private fun getJson(address: String): JSONObject {
        val connection = (URL(address).openConnection() as HttpURLConnection)
        connection.connectTimeout = 12000
        connection.readTimeout = 15000
        connection.setRequestProperty("User-Agent", "KirishMovies/1.0 (Android)")
        return try {
            connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
        } finally { connection.disconnect() }
    }
}

private fun openPlayer(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { setDataAndType(Uri.parse(url), "video/*") })
    } catch (_: Exception) {
        Toast.makeText(context, "No compatible video player found", Toast.LENGTH_SHORT).show()
    }
}

private fun downloadFilm(context: Context, film: Film) {
    try {
        val request = DownloadManager.Request(Uri.parse(film.mp4))
            .setTitle(film.title)
            .setDescription("Downloading from Internet Archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, "${film.id}.mp4")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
        Toast.makeText(context, "Could not start download", Toast.LENGTH_SHORT).show()
    }
}
