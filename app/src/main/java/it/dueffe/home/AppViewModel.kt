package it.dueffe.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.dueffe.home.data.Catalog
import it.dueffe.home.data.CatalogRepository
import it.dueffe.home.update.UpdateInfo
import it.dueffe.home.update.Updater
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

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

    var catalog by mutableStateOf<Catalog?>(null)
        private set
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
            catalog = repository.loadLocal()
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
                catalog = repository.refresh()
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
}
