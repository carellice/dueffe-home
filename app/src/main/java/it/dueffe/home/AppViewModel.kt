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
import it.dueffe.home.data.Release
import it.dueffe.home.update.UpdateInfo
import it.dueffe.home.update.Updater
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

/** Ordine delle app in "Le tue app". */
enum class AppSort { Name, Updated, Usage }

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = CatalogRepository(app)
    private val apps = AppManager(app)
    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private var fullCatalog by mutableStateOf<Catalog?>(null)

    // Release lette in diretta da GitHub: il catalogo su Pages si rigenera solo ogni tanto.
    private val liveReleases = mutableStateMapOf<String, Release>()
    private val releaseChecked = mutableMapOf<String, Long>()

    /** Contenuti nascosti sbloccati con il PIN dalla pagina Info. */
    var unlocked by mutableStateOf(prefs.getBoolean("unlocked", false))
        private set

    val catalog by derivedStateOf { fullCatalog?.let { if (unlocked) it else it.withoutHidden() }?.withReleases(liveReleases.toMap()) }

    var installed by mutableStateOf<List<InstalledApp>>(emptyList())
        private set
    val downloads = mutableStateMapOf<String, AppDownload>()
    var refreshing by mutableStateOf(false)
        private set

    /** Vero mentre "Aggiorna tutto" scarica gli APK; poi gli installer si aprono uno dopo l'altro. */
    var updatingAll by mutableStateOf(false)
        private set
    private val installQueue = ArrayDeque<String>()

    var appsGrid by mutableStateOf(prefs.getBoolean("apps_grid", false))
        private set
    var appsSort by mutableStateOf(runCatching { AppSort.valueOf(prefs.getString("apps_sort", null).orEmpty()) }.getOrDefault(AppSort.Name))
        private set

    /** Tempo d'uso per package; null finché Android non concede l'accesso ai dati di utilizzo. */
    var usage by mutableStateOf<Map<String, Long>?>(null)
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
            loadInstalled()
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
                loadInstalled()
                if (!silent) toast("Catalogo aggiornato")
            } catch (_: Exception) {
                if (!silent) toast("Sei offline: mostro l'ultimo catalogo salvato")
            }
            try {
                checkReleases(installed.map { it.project }, force = !silent)
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
        viewModelScope.launch { loadInstalled() }
    }

    /** Al ritorno nell'app: rilegge le installate e, durante "Aggiorna tutto", apre l'installer successivo. */
    fun onResume() {
        viewModelScope.launch {
            loadInstalled()
            installNext()
        }
    }

    private suspend fun loadInstalled() {
        val projects = catalog?.projects ?: return
        installed = apps.installed(projects)
        usage = apps.usage()
        // Un download già installato non serve più.
        installed.forEach { a -> if (downloads[a.project.id] is AppDownload.Ready && !a.hasUpdate) downloads.remove(a.project.id) }
    }

    /** Aggiorna la release di un progetto leggendola da GitHub, al più una volta ogni dieci minuti. */
    fun checkRelease(project: Project) {
        viewModelScope.launch { checkReleases(listOf(project), force = false) }
    }

    private suspend fun checkReleases(projects: List<Project>, force: Boolean) {
        val now = System.currentTimeMillis()
        val due = projects.filter { force || now - (releaseChecked[it.id] ?: 0L) > RELEASE_TTL }
        if (due.isEmpty()) return
        due.forEach { releaseChecked[it.id] = now }
        val fresh = coroutineScope {
            due.map { p -> async { p.id to runCatching { repository.latestRelease(p) }.getOrNull() } }.awaitAll()
        }
        var changed = false
        fresh.forEach { (id, release) ->
            if (release != null && liveReleases[id] != release) {
                liveReleases[id] = release
                changed = true
            }
        }
        if (changed) loadInstalled()
    }

    fun toggleAppsGrid() {
        appsGrid = !appsGrid
        prefs.edit().putBoolean("apps_grid", appsGrid).apply()
    }

    fun sortApps(sort: AppSort) {
        appsSort = sort
        prefs.edit().putString("apps_sort", sort.name).apply()
        if (sort == AppSort.Usage) refreshInstalled()
    }

    fun installedOf(project: Project): InstalledApp? = installed.firstOrNull { it.project.id == project.id }

    /** Scarica l'APK dentro l'app e apre l'installer di Android. */
    fun downloadApp(project: Project) {
        val current = downloads[project.id]
        if (current is AppDownload.Downloading) return
        if (current is AppDownload.Ready && current.file.exists()) return installApp(project)
        viewModelScope.launch {
            if (fetchApk(project)) installApp(project)
        }
    }

    /** Scarica insieme gli aggiornamenti di tutte le app, poi apre un installer alla volta. */
    fun updateAll() {
        if (updatingAll) return
        val targets = installed.filter { it.hasUpdate }.map { it.project }
        if (targets.isEmpty()) return
        updatingAll = true
        viewModelScope.launch {
            val ready = try {
                coroutineScope {
                    targets.map { p ->
                        async {
                            val current = downloads[p.id]
                            val ok = (current is AppDownload.Ready && current.file.exists()) ||
                                (current !is AppDownload.Downloading && fetchApk(p))
                            p.id.takeIf { ok }
                        }
                    }.awaitAll().filterNotNull()
                }
            } finally {
                updatingAll = false
            }
            installQueue.clear()
            installQueue.addAll(ready)
            installNext()
        }
    }

    private fun installNext() {
        while (installQueue.isNotEmpty()) {
            val id = installQueue.removeFirst()
            val ready = downloads[id] as? AppDownload.Ready ?: continue
            if (!apps.install(ready.file)) {
                installQueue.clear()
                toast("Consenti l'installazione per Dueffe Home, poi tocca di nuovo Aggiorna tutto")
            }
            return
        }
    }

    private suspend fun fetchApk(project: Project): Boolean {
        downloads[project.id] = AppDownload.Downloading(0f)
        return try {
            val file = apps.download(project) { p -> downloads[project.id] = AppDownload.Downloading(p) }
            downloads[project.id] = AppDownload.Ready(file)
            true
        } catch (e: Exception) {
            downloads[project.id] = AppDownload.Failed(e.message ?: "errore di rete")
            toast("Download di ${project.name} non riuscito: ${e.message ?: "errore di rete"}")
            false
        }
    }

    private fun installApp(project: Project) {
        val ready = downloads[project.id] as? AppDownload.Ready ?: return
        if (!apps.install(ready.file)) toast("Consenti l'installazione per Dueffe Home, poi tocca di nuovo Installa")
    }

    fun openApp(app: InstalledApp) {
        prefs.edit().putInt("opens/${app.project.id}", opens(app) + 1).apply()
        if (!apps.open(app.packageName)) toast("${app.project.name} non si può aprire da qui")
    }

    /** Quante volte l'app è stata aperta da qui: ordina per utilizzo quando Android non dà i suoi dati. */
    fun opens(app: InstalledApp): Int = prefs.getInt("opens/${app.project.id}", 0)

    fun hasUsageAccess(): Boolean = apps.hasUsageAccess()

    private companion object {
        const val RELEASE_TTL = 10 * 60 * 1000L

        // SHA-256 del PIN che mostra i contenuti nascosti.
        const val PIN_HASH = "d7fdeffc0f8ba1b3f6b504bed173480e62a2915fb118d0e2a6f946131ac6fbcc"
    }
}
