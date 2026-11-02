package it.dueffe.home.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import it.dueffe.home.BuildConfig
import it.dueffe.home.data.Http
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException

data class UpdateInfo(
    val version: String,
    val tag: String,
    val notes: String,
    val apkUrl: String,
    val size: Long,
    val pageUrl: String,
    val date: String?,
)

/** Aggiornamenti in-app: legge l'ultima release pubblica della repo e installa l'APK allegato. */
object Updater {
    private const val API = "https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases/latest"

    suspend fun latest(): UpdateInfo = withContext(Dispatchers.IO) {
        val json = JSONObject(Http.get(API, "application/vnd.github+json"))
        val tag = json.getString("tag_name")
        val assets = json.getJSONArray("assets")
        val apk = (0 until assets.length()).map { assets.getJSONObject(it) }
            .firstOrNull { it.getString("name").endsWith(".apk", true) }
            ?: throw IOException("La release $tag non contiene un APK")
        UpdateInfo(
            version = tag.removePrefix("v"),
            tag = tag,
            notes = json.optString("body").takeIf { it != "null" }.orEmpty().trim(),
            apkUrl = apk.getString("browser_download_url"),
            size = apk.optLong("size"),
            pageUrl = json.getString("html_url"),
            date = json.optString("published_at").takeIf { it.isNotBlank() && it != "null" },
        )
    }

    fun isNewer(remote: String, local: String = BuildConfig.VERSION_NAME): Boolean {
        val r = remote.split('.', '-').map { it.toIntOrNull() ?: 0 }
        val l = local.split('.', '-').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(r.size, l.size)) {
            val a = r.getOrElse(i) { 0 }
            val b = l.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }

    suspend fun download(context: Context, info: UpdateInfo, onProgress: (Float) -> Unit): File =
        withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "updates").apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() }
            val target = File(dir, "dueffe-home-${info.version}.apk")
            Http.download(info.apkUrl, target, onProgress)
            target
        }

    /** Restituisce false se Android deve prima concedere l'installazione da questa app. */
    fun install(context: Context, apk: File): Boolean {
        if (!context.packageManager.canRequestPackageInstalls()) {
            val settings = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try { context.startActivity(settings) } catch (_: ActivityNotFoundException) { }
            return false
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", apk)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }
}
