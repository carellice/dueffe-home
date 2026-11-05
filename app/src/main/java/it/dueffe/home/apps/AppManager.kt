package it.dueffe.home.apps

import android.content.Context
import android.content.pm.PackageManager
import it.dueffe.home.data.Http
import it.dueffe.home.data.Project
import it.dueffe.home.update.Updater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Un'app del catalogo presente sul telefono. */
data class InstalledApp(val project: Project, val packageName: String, val version: String) {
    val latest: String? get() = project.release?.tag?.removePrefix("v")
    val hasUpdate: Boolean get() = latest?.let { Updater.isNewer(it, version) } == true
}

/** Stato del download di un APK scaricato dentro Dueffe Home. */
sealed interface AppDownload {
    data class Downloading(val progress: Float) : AppDownload
    data class Ready(val file: File) : AppDownload
    data class Failed(val message: String) : AppDownload
}

/**
 * Riconosce le app del catalogo installate, scarica i loro APK nella cache e li passa all'installer di Android.
 * Il package viene dal catalogo; se manca, lo legge dall'APK scaricato e lo ricorda.
 */
class AppManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("apps", Context.MODE_PRIVATE)
    private val dir get() = File(context.cacheDir, "apps").apply { mkdirs() }

    fun packageOf(project: Project): String? = project.androidPackage ?: prefs.getString(project.id, null)

    suspend fun installed(projects: List<Project>): List<InstalledApp> = withContext(Dispatchers.IO) {
        projects.filter { it.apk != null }.mapNotNull { p ->
            val pkg = packageOf(p) ?: return@mapNotNull null
            val info = try { context.packageManager.getPackageInfo(pkg, 0) } catch (_: PackageManager.NameNotFoundException) { null }
            info?.let { InstalledApp(p, pkg, it.versionName.orEmpty()) }
        }
    }

    suspend fun download(project: Project, onProgress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        val apk = project.apk ?: error("Nessun APK nella release")
        dir.listFiles { f -> f.name.startsWith("${project.id}-") }?.forEach { it.delete() }
        val target = File(dir, "${project.id}-${project.release?.tag ?: "latest"}.apk")
        Http.download(apk.url, target, onProgress)
        context.packageManager.getPackageArchiveInfo(target.path, 0)?.packageName?.let {
            prefs.edit().putString(project.id, it).apply()
        }
        target
    }

    fun install(file: File): Boolean = Updater.install(context, file)

    fun open(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        context.startActivity(intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }
}
