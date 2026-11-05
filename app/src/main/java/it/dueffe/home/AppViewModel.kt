package it.dueffe.home

import android.app.Application
import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.dueffe.home.apps.AppDownload
import it.dueffe.home.apps.AppManager
import it.dueffe.home.apps.InstalledApp
import it.dueffe.home.data.Catalog
import it.dueffe.home.data.CatalogRepository
import it.dueffe.home.data.Project
import it.dueffe.home.update.UpdateInfo
import it.dueffe.home.update.Updater
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(val info: UpdateInfo, val progress: Float) : UpdateState
    data class Ready(val info: UpdateInfo, val file: File) : UpdateState
    data class Failed(val message: String) : UpdateState
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = CatalogRepository(app)
    private val apps = AppManager(app)
    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private var fullCatalog by mutableStateOf<Catalog?>(null)

    /** Contenuti nascosti sbloccati con il PIN dalla pagina Info. */
    var unlocked by mutableStateOf(prefs.getBoolean("unlocked", false))
        private set

    val catalog by derivedStateOf { fullCatalog?.let { if (unlocked) it else it.withoutHidden() } }

    var installed by mutableStateOf<List<InstalledApp>>(emptyList())
        private set
    val downloads = mutableStateMapOf<String, AppDownload>()
    var refreshing by mutableStateOf(false)
        private set
    var update by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set
    var updateBannerDismissed by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)
        private set

    private var messageJob: Job? = null

    init {
        viewModelScope.launch {
            fullCatalog = repository.loadLocal()
            refreshInstalled()
            refresh(silent = true)
        }
        checkUpdates(silent = true)
    }

    fun toast(text: String) {
        messageJob?.cancel()
        message = text
        messageJob = viewModelScope.launch { delay(2800); message = null }
    }

    fun refresh(silent: Boolean = false) {
        if (refreshing) return
        viewModelScope.launch {
            refreshing = !silent
            try {
                fullCatalog = repository.refresh()
                refreshInstalled()
                if (!silent) toast("Catalogo aggiornato")
            } catch (_: Exception) {
                if (!silent) toast("Sei offline: mostro l'ultimo catalogo salvato")
            } finally {
                refreshing = false
            }
        }
    }

    fun checkUpdates(silent: Boolean = false) {
        if (update is UpdateState.Checking || update is UpdateState.Downloading) return
        viewModelScope.launch {
            update = UpdateState.Checking
            update = try {
                val info = Updater.latest()
                if (Updater.isNewer(info.version)) UpdateState.Available(info) else UpdateState.UpToDate
            } catch (e: Exception) {
                if (silent) UpdateState.Idle else UpdateState.Failed("Impossibile controllare: ${e.message ?: "rete non disponibile"}")
            }
        }
    }

    fun downloadUpdate() {
        val info = when (val s = update) {
            is UpdateState.Available -> s.info
            is UpdateState.Failed -> return checkUpdates()
            else -> return
        }
        viewModelScope.launch {
            update = UpdateState.Downloading(info, 0f)
            update = try {
                val file = Updater.download(getApplication(), info) { p ->
                    update = UpdateState.Downloading(info, p)
                }
                UpdateState.Ready(info, file)
            } catch (e: Exception) {
                UpdateState.Failed("Download non riuscito: ${e.message ?: "errore di rete"}")
            }
        }
    }

    fun install() {
        val ready = update as? UpdateState.Ready ?: return
        if (!Updater.install(getApplication(), ready.file)) {
            toast("Consenti l'installazione per Dueffe Home, poi tocca di nuovo Installa")
        }
    }

    fun unlock(pin: String): Boolean {
        val hash = MessageDigest.getInstance("SHA-256").digest(pin.trim().toByteArray()).joinToString("") { "%02x".format(it) }
        if (hash != PIN_HASH) return false
        unlocked = true
        prefs.edit().putBoolean("unlocked", true).apply()
        refreshInstalled()
        return true
    }

    fun lock() {
        unlocked = false
        prefs.edit().putBoolean("unlocked", false).apply()
        refreshInstalled()
    }

    /** Rilegge le app installate: va chiamata anche al ritorno nell'app, dopo un'installazione. */
    fun refreshInstalled() {
        val projects = catalog?.projects ?: return
        viewModelScope.launch {
            installed = apps.installed(projects)
            // Un download già installato non serve più.
            installed.forEach { a -> if (downloads[a.project.id] is AppDownload.Ready && !a.hasUpdate) downloads.remove(a.project.id) }
        }
    }

    fun installedOf(project: Project): InstalledApp? = installed.firstOrNull { it.project.id == project.id }

    /** Scarica l'APK dentro l'app e apre l'installer di Android. */
    fun downloadApp(project: Project) {
        val current = downloads[project.id]
        if (current is AppDownload.Downloading) return
        if (current is AppDownload.Ready && current.file.exists()) return installApp(project)
        viewModelScope.launch {
            downloads[project.id] = AppDownload.Downloading(0f)
            try {
                val file = apps.download(project) { p -> downloads[project.id] = AppDownload.Downloading(p) }
                downloads[project.id] = AppDownload.Ready(file)
                installApp(project)
            } catch (e: Exception) {
                downloads[project.id] = AppDownload.Failed(e.message ?: "errore di rete")
                toast("Download non riuscito: ${e.message ?: "errore di rete"}")
            }
        }
    }

    private fun installApp(project: Project) {
        val ready = downloads[project.id] as? AppDownload.Ready ?: return
        if (!apps.install(ready.file)) toast("Consenti l'installazione per Dueffe Home, poi tocca di nuovo Installa")
    }

    fun openApp(app: InstalledApp) {
        if (!apps.open(app.packageName)) toast("${app.project.name} non si può aprire da qui")
    }

    private companion object {
        // SHA-256 del PIN che mostra i contenuti nascosti.
        const val PIN_HASH = "d7fdeffc0f8ba1b3f6b504bed173480e62a2915fb118d0e2a6f946131ac6fbcc"
    }
}
