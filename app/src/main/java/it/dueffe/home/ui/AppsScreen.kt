package it.dueffe.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.dueffe.home.AppViewModel
import it.dueffe.home.apps.AppDownload
import it.dueffe.home.apps.InstalledApp
import it.dueffe.home.data.Catalog
import it.dueffe.home.data.Project
import it.dueffe.home.ui.theme.DF

/** Le app del catalogo installate sul telefono: aggiornamenti e apertura diretta. */
@Composable
fun AppsScreen(
    catalog: Catalog,
    vm: AppViewModel,
    listState: LazyListState,
    scopes: SharedScopes?,
    bottomPadding: Dp,
    onOpen: (Opened) -> Unit,
    onExplore: () -> Unit,
) {
    val apps = vm.installed.filter { a -> catalog.projects.any { it.id == a.project.id } }
    val updates = apps.filter { it.hasUpdate }
    val upToDate = apps.filterNot { it.hasUpdate }
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = bottomPadding)) {
        item(key = "head") {
            Column(Modifier.statusBarsPadding().padding(horizontal = 20.dp).padding(top = 18.dp, bottom = 10.dp)) {
                Text("Le tue app", style = MaterialTheme.typography.displaySmall, color = DF.Text)
                Text("Le app del catalogo installate su questo telefono", style = MaterialTheme.typography.bodyMedium, color = DF.Muted)
                VerticalGap(16.dp)
                GoldButton(
                    if (vm.refreshing) "Controllo in corso…" else "Controlla aggiornamenti", Icons.Rounded.Refresh,
                    Modifier.fillMaxWidth(), filled = false,
                ) { vm.refresh() }
            }
        }
        if (apps.isEmpty()) {
            item(key = "empty") { EmptyApps(Modifier.reveal(1), onExplore) }
        }
        if (updates.isNotEmpty()) {
            item(key = "updates-head") {
                SectionHeader("Aggiornamenti", if (updates.size == 1) "1 app da aggiornare" else "${updates.size} app da aggiornare", Modifier.padding(top = 18.dp))
            }
            itemsIndexed(updates, key = { _, a -> "u/${a.project.id}" }) { i, a -> InstalledRow(a, vm, scopes, Modifier.reveal(i)) { onOpen(Opened(a.project, "apps")) } }
        }
        if (upToDate.isNotEmpty()) {
            item(key = "installed-head") {
                SectionHeader("Installate", if (upToDate.size == 1) "1 app aggiornata" else "${upToDate.size} app aggiornate", Modifier.padding(top = 18.dp))
            }
            itemsIndexed(upToDate, key = { _, a -> "i/${a.project.id}" }) { i, a -> InstalledRow(a, vm, scopes, Modifier.reveal(i)) { onOpen(Opened(a.project, "apps")) } }
        }
    }
}

@Composable
private fun InstalledRow(app: InstalledApp, vm: AppViewModel, scopes: SharedScopes?, modifier: Modifier, onClick: () -> Unit) {
    val p = app.project
    Row(
        modifier.fillMaxWidth().pressScale(0.97f, onClick).padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProjectIcon(p, 54.dp, Modifier.sharedIcon(scopes, "apps", p.id))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(p.name, style = MaterialTheme.typography.titleMedium, color = DF.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                if (app.hasUpdate) "${app.version} → ${app.latest}" else "Versione ${app.version}",
                style = MaterialTheme.typography.bodySmall, color = if (app.hasUpdate) DF.Primary else DF.Muted, maxLines = 1,
            )
        }
        Spacer(Modifier.width(10.dp))
        AppActionButton(p, vm, Modifier.width(112.dp), compact = true)
    }
}

@Composable
private fun EmptyApps(modifier: Modifier, onExplore: () -> Unit) {
    Column(modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(DF.Primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Apps, null, tint = DF.Primary, modifier = Modifier.size(34.dp))
        }
        VerticalGap(16.dp)
        Text("Nessuna app installata", style = MaterialTheme.typography.titleLarge, color = DF.Text)
        VerticalGap(6.dp)
        Text(
            "Installa un'app dal catalogo: la ritroverai qui, pronta da aprire e da aggiornare.",
            style = MaterialTheme.typography.bodyMedium, color = DF.Muted, textAlign = TextAlign.Center,
        )
        VerticalGap(18.dp)
        GoldButton("Esplora le app", Icons.Rounded.Explore) { onExplore() }
    }
}

/**
 * Pulsante principale per un'app con APK: scarica e installa dentro Dueffe Home,
 * poi diventa Apri, o Aggiorna quando esce una versione nuova.
 */
@Composable
fun AppActionButton(project: Project, vm: AppViewModel, modifier: Modifier = Modifier, accent: Color = DF.Primary, compact: Boolean = false) {
    val installed = vm.installedOf(project)
    val download = vm.downloads[project.id]
    val (label, icon, filled) = when {
        download is AppDownload.Downloading -> Triple("${(download.progress * 100).toInt()}%", Icons.Rounded.Download, false)
        installed != null && installed.hasUpdate && download is AppDownload.Ready -> Triple("Installa", Icons.Rounded.InstallMobile, true)
        installed != null && installed.hasUpdate -> Triple("Aggiorna", Icons.Rounded.SystemUpdate, true)
        installed != null -> Triple("Apri", Icons.AutoMirrored.Rounded.OpenInNew, compact.not())
        download is AppDownload.Ready -> Triple("Installa", Icons.Rounded.InstallMobile, true)
        else -> Triple(if (compact) "Installa" else "Installa APK", Icons.Rounded.Download, true)
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        GoldButton(label, if (compact) null else icon, Modifier.fillMaxWidth(), filled = filled, accent = accent) {
            when {
                download is AppDownload.Downloading -> Unit
                installed != null && !installed.hasUpdate -> vm.openApp(installed)
                else -> vm.downloadApp(project)
            }
        }
        if (download is AppDownload.Downloading) {
            VerticalGap(6.dp)
            Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(DF.SurfaceHigher)) {
                Box(Modifier.fillMaxWidth(download.progress.coerceIn(0.02f, 1f)).height(4.dp).clip(CircleShape).background(accent))
            }
        }
    }
}
