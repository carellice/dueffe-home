package it.dueffe.home.apps

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import it.dueffe.home.data.Http
import it.dueffe.home.data.Project
import it.dueffe.home.update.Updater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Un'app del catalogo presente sul telefono. */
data class InstalledApp(val project: Project, val packageName: String, val version: String, val lastUpdate: Long = 0) {
    // Il tag può avere un prefisso, come "streamwise-v2.0.140": conta solo il numero di versione.
    val latest: String? get() = project.release?.tag?.let { Regex("""\d+(\.\d+)*""").find(it)?.value }
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
            info?.let { InstalledApp(p, pkg, it.versionName.orEmpty(), it.lastUpdateTime) }
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

    /** Tempo in primo piano negli ultimi 30 giorni per package; null se manca l'accesso ai dati di utilizzo. */
    suspend fun usage(): Map<String, Long>? = withContext(Dispatchers.IO) {
        if (!hasUsageAccess()) return@withContext null
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        usm.queryAndAggregateUsageStats(now - 30L * 24 * 60 * 60 * 1000, now).mapValues { it.value.totalTimeInForeground }
    }

    fun hasUsageAccess(): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun install(file: File): Boolean = Updater.install(context, file)

    fun open(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        context.startActivity(intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }
}
