package it.dueffe.home.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Insights
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.dueffe.home.AppSort
import it.dueffe.home.AppViewModel
import it.dueffe.home.apps.AppDownload
import it.dueffe.home.apps.InstalledApp
import it.dueffe.home.data.Catalog
import it.dueffe.home.data.Project
import it.dueffe.home.ui.theme.DF
import java.text.Collator
import java.util.Locale

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
    val context = LocalContext.current
    val apps = sortedApps(vm.installed.filter { a -> catalog.projects.any { it.id == a.project.id } }, vm)
    val updates = apps.filter { it.hasUpdate }
    val upToDate = apps.filterNot { it.hasUpdate }
    val open: (InstalledApp) -> Unit = { a -> onOpen(Opened(a.project, "apps")) }
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = bottomPadding)) {
        item(key = "head") {
            Column(Modifier.statusBarsPadding().padding(top = 18.dp, bottom = 10.dp)) {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text("Le tue app", style = MaterialTheme.typography.displaySmall, color = DF.Text)
                    Text("Le app del catalogo installate su questo telefono", style = MaterialTheme.typography.bodyMedium, color = DF.Muted)
                    VerticalGap(16.dp)
                    GoldButton(
                        if (vm.refreshing) "Controllo in corso…" else "Controlla aggiornamenti", Icons.Rounded.Refresh,
                        Modifier.fillMaxWidth(), filled = false,
                    ) { vm.refresh() }
                }
                if (apps.isNotEmpty()) {
                    VerticalGap(14.dp)
                    SortBar(vm)
                    if (vm.appsSort == AppSort.Usage && vm.usage == null) {
                        VerticalGap(12.dp)
                        UsageHint(Modifier.padding(horizontal = 20.dp)) { openUsageSettings(context) }
                    }
                }
            }
        }
        if (apps.isEmpty()) {
            item(key = "empty") { EmptyApps(Modifier.reveal(1), onExplore) }
        }
        if (updates.isNotEmpty()) {
            item(key = "updates-head") {
                Column(Modifier.padding(top = 18.dp)) {
                    SectionHeader("Aggiornamenti", if (updates.size == 1) "1 app da aggiornare" else "${updates.size} app da aggiornare")
                    VerticalGap(12.dp)
                    val busy = vm.updatingAll || updates.any { vm.downloads[it.project.id] is AppDownload.Downloading }
                    GoldButton(
                        if (busy) "Download in corso…" else "Aggiorna tutto", Icons.Rounded.SystemUpdate,
                        Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    ) { vm.updateAll() }
                    VerticalGap(4.dp)
                }
            }
            appsSection("u", updates, vm, scopes, open)
        }
        if (upToDate.isNotEmpty()) {
            item(key = "installed-head") {
                SectionHeader("Installate", if (upToDate.size == 1) "1 app aggiornata" else "${upToDate.size} app aggiornate", Modifier.padding(top = 18.dp))
            }
            appsSection("i", upToDate, vm, scopes, open)
        }
    }
}

/** Ordina per nome, per ultimo aggiornamento sul telefono o per tempo d'uso (o aperture da qui, senza accesso all'utilizzo). */
private fun sortedApps(apps: List<InstalledApp>, vm: AppViewModel): List<InstalledApp> {
    val collator = Collator.getInstance(Locale.ITALIAN).apply { strength = Collator.PRIMARY }
    val byName = Comparator<InstalledApp> { a, b -> collator.compare(a.project.name, b.project.name) }
    return when (vm.appsSort) {
        AppSort.Name -> apps.sortedWith(byName)
        AppSort.Updated -> apps.sortedWith(compareByDescending<InstalledApp> { it.lastUpdate }.then(byName))
        AppSort.Usage -> {
            val usage = vm.usage
            if (usage != null) apps.sortedWith(compareByDescending<InstalledApp> { usage[it.packageName] ?: 0L }.then(byName))
            else apps.sortedWith(compareByDescending<InstalledApp> { vm.opens(it) }.then(byName))
        }
    }
}

private fun LazyListScope.appsSection(prefix: String, apps: List<InstalledApp>, vm: AppViewModel, scopes: SharedScopes?, onOpen: (InstalledApp) -> Unit) {
    if (vm.appsGrid) {
        itemsIndexed(apps.chunked(GRID_COLUMNS), key = { _, row -> "$prefix/g/${row.joinToString { it.project.id }}" }) { i, row ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp).reveal(i),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { a -> InstalledTile(a, vm, scopes, Modifier.weight(1f)) { onOpen(a) } }
                repeat(GRID_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    } else {
        itemsIndexed(apps, key = { _, a -> "$prefix/${a.project.id}" }) { i, a -> InstalledRow(a, vm, scopes, Modifier.reveal(i)) { onOpen(a) } }
    }
}

private const val GRID_COLUMNS = 3

@Composable
private fun SortBar(vm: AppViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LazyRow(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { DfChip("Nome", vm.appsSort == AppSort.Name, icon = Icons.AutoMirrored.Rounded.Sort) { vm.sortApps(AppSort.Name) } }
            item { DfChip("Aggiornate", vm.appsSort == AppSort.Updated, icon = Icons.Rounded.History) { vm.sortApps(AppSort.Updated) } }
            item { DfChip("Più usate", vm.appsSort == AppSort.Usage, icon = Icons.Rounded.Insights) { vm.sortApps(AppSort.Usage) } }
        }
        Box(
            Modifier.padding(end = 20.dp).size(40.dp).pressScale(0.9f) { vm.toggleAppsGrid() }
                .clip(CircleShape).background(DF.Surface).border(1.dp, DF.Line, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (vm.appsGrid) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                if (vm.appsGrid) "Vista a elenco" else "Vista a griglia",
                tint = DF.Text, modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun UsageHint(modifier: Modifier, onGrant: () -> Unit) {
    CardSurface(modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
        Column {
            Text(
                "Per ordinare in base a quanto usi le app, consenti a Dueffe Home l'accesso ai dati di utilizzo. Finché non lo fai conto le aperture da qui.",
                style = MaterialTheme.typography.bodyMedium, color = DF.Muted,
            )
            VerticalGap(12.dp)
            GoldButton("Consenti accesso", Icons.Rounded.Insights, Modifier.fillMaxWidth(), filled = false, onClick = onGrant)
        }
    }
}

private fun openUsageSettings(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
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
        UninstallButton(app, vm)
        Spacer(Modifier.width(4.dp))
        AppActionButton(p, vm, Modifier.width(112.dp), compact = true)
    }
}

/** Il cestino: Android chiede conferma prima di disinstallare. */
@Composable
internal fun UninstallButton(app: InstalledApp, vm: AppViewModel, modifier: Modifier = Modifier) {
    Box(
        modifier.size(40.dp).pressScale(0.9f) { vm.uninstallApp(app) }.clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.DeleteOutline, "Disinstalla ${app.project.name}", tint = DF.Muted, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun InstalledTile(app: InstalledApp, vm: AppViewModel, scopes: SharedScopes?, modifier: Modifier, onClick: () -> Unit) {
    val p = app.project
    Box(modifier) {
        Column(
            Modifier.clip(RoundedCornerShape(20.dp)).pressScale(0.96f, onClick).padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProjectIcon(p, 64.dp, Modifier.sharedIcon(scopes, "apps", p.id))
            VerticalGap(8.dp)
            Text(
                p.name, style = MaterialTheme.typography.titleSmall, color = DF.Text,
                maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
            )
            Text(
                if (app.hasUpdate) "${app.version} → ${app.latest}" else app.version,
                style = MaterialTheme.typography.bodySmall, color = if (app.hasUpdate) DF.Primary else DF.Muted,
                maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
            )
            VerticalGap(8.dp)
            AppActionButton(p, vm, Modifier.fillMaxWidth(), compact = true, dense = true)
        }
        UninstallButton(app, vm, Modifier.align(Alignment.TopEnd))
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
fun AppActionButton(
    project: Project,
    vm: AppViewModel,
    modifier: Modifier = Modifier,
    accent: Color = DF.Primary,
    compact: Boolean = false,
    dense: Boolean = false,
) {
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
        GoldButton(label, if (compact) null else icon, Modifier.fillMaxWidth(), filled = filled, accent = accent, dense = dense) {
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
