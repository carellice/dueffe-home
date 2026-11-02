package it.dueffe.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.dueffe.home.data.Catalog
import it.dueffe.home.data.Project
import it.dueffe.home.ui.theme.DF
import it.dueffe.home.ui.theme.Motion

private enum class Sort(val label: String) { Recent("Aggiornati"), Name("A-Z"), Stars("Stelle") }

@Composable
fun ExploreScreen(
    catalog: Catalog,
    listState: LazyListState,
    category: String?,
    onCategory: (String?) -> Unit,
    scopes: SharedScopes?,
    bottomPadding: Dp,
    onOpen: (Opened) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var platform by rememberSaveable { mutableStateOf<String?>(null) }
    var sort by rememberSaveable { mutableStateOf(Sort.Recent) }

    val results = remember(catalog, query, platform, category, sort) {
        val q = query.trim().lowercase()
        catalog.projects
            .filter { category == null || it.category == category }
            .filter { platform == null || platform in it.platforms }
            .filter {
                q.isEmpty() || it.name.lowercase().contains(q) || it.description.lowercase().contains(q) ||
                    it.id.lowercase().contains(q) || it.topics.any { t -> t.contains(q) } || (it.language?.lowercase()?.contains(q) == true)
            }
            .let { list ->
                when (sort) {
                    Sort.Recent -> list.sortedByDescending { it.updatedAt }
                    Sort.Name -> list.sortedBy { it.name.lowercase() }
                    Sort.Stars -> list.sortedByDescending { it.stars }
                }
            }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = bottomPadding)) {
        item(key = "head") {
            Column(Modifier.statusBarsPadding().padding(top = 18.dp)) {
                Text("Esplora", style = MaterialTheme.typography.displaySmall, color = DF.Text, modifier = Modifier.padding(horizontal = 20.dp))
                Text("Cerca tra ${catalog.projects.size} progetti", style = MaterialTheme.typography.bodyMedium, color = DF.Muted, modifier = Modifier.padding(horizontal = 20.dp))
                VerticalGap(18.dp)
                SearchField(query, { query = it })
                VerticalGap(14.dp)
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { DfChip("Tutti", category == null) { onCategory(null) } }
                    items(catalog.categories, key = { it.id }) { c ->
                        DfChip(c.title, category == c.id, icon = categoryIcon(c.id)) { onCategory(if (category == c.id) null else c.id) }
                    }
                }
                VerticalGap(8.dp)
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("android", "windows", "macos", "web")) { p ->
                        DfChip(platformLabel(p), platform == p, icon = platformIcon(p)) { platform = if (platform == p) null else p }
                    }
                }
                VerticalGap(14.dp)
                Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (results.size == 1) "1 risultato" else "${results.size} risultati",
                        style = MaterialTheme.typography.labelLarge, color = DF.Muted, modifier = Modifier.weight(1f),
                    )
                    Row(
                        Modifier.clip(CircleShape).pressScale(0.9f) { sort = Sort.entries[(sort.ordinal + 1) % Sort.entries.size] }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Sort, null, tint = DF.Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(sort.label, style = MaterialTheme.typography.labelLarge, color = DF.Primary)
                    }
                }
                VerticalGap(6.dp)
            }
        }
        items(results, key = { it.id }) { p ->
            ExploreRow(
                p, scopes,
                Modifier.animateItem(
                    fadeInSpec = tween(320),
                    placementSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
                    fadeOutSpec = tween(180),
                ),
            ) { onOpen(Opened(p, "explore")) }
        }
        if (results.isEmpty()) item(key = "empty") { EmptyState() }
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(shape)
            .background(DF.Surface).border(1.dp, if (value.isEmpty()) DF.Line else DF.LineStrong, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, null, tint = DF.Primary)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text("App, giochi, linguaggi…", style = MaterialTheme.typography.bodyLarge, color = DF.Muted)
            BasicTextField(
                value, onChange, singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = DF.Text),
                cursorBrush = SolidColor(DF.Primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AnimatedVisibility(value.isNotEmpty(), enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
            Icon(Icons.Rounded.Close, "Cancella", tint = DF.Muted, modifier = Modifier.size(22.dp).pressScale(0.8f) { onChange("") })
        }
    }
}

@Composable
private fun ExploreRow(p: Project, scopes: SharedScopes?, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.fillMaxWidth().pressScale(0.97f, onClick).padding(horizontal = 20.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProjectIcon(p, 64.dp, Modifier.sharedIcon(scopes, "explore", p.id))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.name, style = MaterialTheme.typography.titleMedium, color = DF.Text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (p.stars > 0) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Rounded.Star, null, tint = DF.Primary, modifier = Modifier.size(13.dp))
                    Text(p.stars.toString(), style = MaterialTheme.typography.labelSmall, color = DF.Muted)
                }
            }
            Text(p.description, style = MaterialTheme.typography.bodySmall, color = DF.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            VerticalGap(5.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlatformRow(p.platforms, size = 13.dp)
                Spacer(Modifier.width(8.dp))
                p.language?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = DF.Muted) }
                p.release?.let { Spacer(Modifier.width(8.dp)); Pill(it.tag) }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    val t = rememberInfiniteTransition(label = "empty")
    val wobble by t.animateFloat(-12f, 12f, infiniteRepeatable(tween(1300, easing = Motion.Standard), androidx.compose.animation.core.RepeatMode.Reverse), label = "w")
    Column(Modifier.fillMaxWidth().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(84.dp).graphicsLayer { rotationZ = wobble }.clip(CircleShape)
                .background(Brush.radialGradient(listOf(DF.SurfaceHigher, DF.Surface))),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.SearchOff, null, tint = DF.Primary, modifier = Modifier.size(38.dp)) }
        VerticalGap(16.dp)
        Text("Nessun progetto trovato", style = MaterialTheme.typography.titleLarge, color = DF.Text)
        Text("Prova con un'altra parola o togli un filtro", style = MaterialTheme.typography.bodyMedium, color = DF.Muted)
    }
}
