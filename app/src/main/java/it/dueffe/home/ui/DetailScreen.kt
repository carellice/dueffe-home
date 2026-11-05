package it.dueffe.home.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import it.dueffe.home.AppViewModel
import it.dueffe.home.apps.InstalledApp
import it.dueffe.home.update.Updater
import it.dueffe.home.data.Catalog
import it.dueffe.home.data.Project
import it.dueffe.home.ui.theme.DF
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(opened: Opened, catalog: Catalog, vm: AppViewModel, scopes: SharedScopes?, onBack: () -> Unit, onOpen: (Opened) -> Unit) {
    // Dal catalogo corrente, così la release letta in diretta da GitHub arriva anche qui.
    val p = catalog.projects.firstOrNull { it.id == opened.project.id } ?: opened.project
    LaunchedEffect(p.id) { vm.checkRelease(p) }
    val context = LocalContext.current
    val list = rememberLazyListState()
    val accent = rememberAccent(p.icon)
    var viewer by remember { mutableStateOf<Int?>(null) }
    BackHandler(viewer != null) { viewer = null }

    val collapsed by remember { derivedStateOf { list.firstVisibleItemIndex > 0 || list.firstVisibleItemScrollOffset > 420 } }
    val topAlpha by animateFloatAsState(if (collapsed) 1f else 0f, tween(260), label = "top")

    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = list, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item(key = "hero") {
                Box {
                    // Fondale: icona sfocata e alone del suo colore, con parallasse.
                    Box(
                        Modifier.fillMaxWidth().height(380.dp).graphicsLayer {
                            if (list.firstVisibleItemIndex == 0) translationY = list.firstVisibleItemScrollOffset * 0.5f
                        },
                    ) {
                        AsyncImage(
                            p.screenshots.firstOrNull() ?: p.icon, null, contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                                .then(if (Build.VERSION.SDK_INT >= 31) Modifier.blur(60.dp) else Modifier)
                                .graphicsLayer { alpha = if (Build.VERSION.SDK_INT >= 31) 0.55f else 0.12f },
                        )
                        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(accent.copy(alpha = 0.35f), Color.Transparent), radius = 900f)))
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, DF.Bg))))
                    }
                    Column(Modifier.statusBarsPadding().padding(top = 70.dp).padding(horizontal = 20.dp)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            ProjectIcon(p, 116.dp, Modifier.sharedIcon(scopes, opened.origin, p.id))
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f).reveal(1)) {
                                Pill(categoryLabel(p.category).uppercase(), color = accent)
                                VerticalGap(8.dp)
                                Text(p.name, style = MaterialTheme.typography.displaySmall, color = DF.Text)
                                if (p.language != null) Text(p.language, style = MaterialTheme.typography.bodyMedium, color = DF.Muted)
                            }
                        }
                        VerticalGap(18.dp)
                        Text(p.description, style = MaterialTheme.typography.bodyLarge, color = DF.Text.copy(alpha = 0.86f), modifier = Modifier.reveal(2))
                        VerticalGap(18.dp)
                        StatsStrip(p, vm.installedOf(p), Modifier.reveal(3))
                        VerticalGap(18.dp)
                        Actions(p, vm, accent, Modifier.reveal(4))
                    }
                }
            }
            if (p.screenshots.isNotEmpty() || p.video != null) {
                item(key = "shots") {
                    Column(Modifier.padding(top = 28.dp).reveal(5)) {
                        SectionHeader("Anteprima", if (p.screenshots.size == 1) "1 immagine" else "${p.screenshots.size} immagini")
                        VerticalGap(12.dp)
                        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            itemsIndexed(p.screenshots) { i, url -> Screenshot(url, Modifier.reveal(i)) { viewer = i } }
                            if (p.video != null) item {
                                VideoTile { openUrl(context, p.video) }
                            }
                        }
                    }
                }
            }
            if (p.about != null) item(key = "about") { AboutBlock(p.about, Modifier.padding(top = 28.dp).reveal(6)) }
            if (p.features.isNotEmpty()) item(key = "features") { Features(p.features, accent, Modifier.padding(top = 28.dp)) }
            p.release?.let { r ->
                item(key = "release") {
                    Column(Modifier.padding(top = 28.dp)) {
                        SectionHeader("Novità", "${r.name} · ${fullDate(r.date)}")
                        VerticalGap(12.dp)
                        CardSurface(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                            Column {
                                if (r.notes.isNotBlank()) {
                                    Text(cleanNotes(r.notes), style = MaterialTheme.typography.bodyMedium, color = DF.Muted, maxLines = 10, overflow = TextOverflow.Ellipsis)
                                    VerticalGap(14.dp)
                                }
                                r.assets.forEach { a ->
                                    Row(
                                        // Gli APK si scaricano e si installano dentro l'app, il resto si apre nel browser.
                                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                            .pressScale(0.97f) { if (a.name.endsWith(".apk", true) && a == p.apk) vm.downloadApp(p) else openUrl(context, a.url) }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(Icons.Rounded.Inventory2, null, tint = DF.Primary, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(a.name, style = MaterialTheme.typography.titleSmall, color = DF.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(
                                                listOf(formatSize(a.size), if (a.downloads > 0) "${a.downloads} download" else "").filter { it.isNotEmpty() }.joinToString(" · "),
                                                style = MaterialTheme.typography.bodySmall, color = DF.Muted,
                                            )
                                        }
                                        Icon(Icons.Rounded.Download, "Scarica", tint = DF.Text, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item(key = "links") {
                Column(Modifier.padding(top = 28.dp)) {
                    SectionHeader("Link utili", null)
                    VerticalGap(10.dp)
                    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        p.links.forEach { l ->
                            Row(
                                Modifier.fillMaxWidth().pressScale(0.97f) { openUrl(context, l.url) }.clip(RoundedCornerShape(18.dp))
                                    .background(DF.Surface).border(1.dp, DF.Line, RoundedCornerShape(18.dp)).padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(linkIcon(l.type), null, tint = accent, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(l.label, style = MaterialTheme.typography.titleSmall, color = DF.Text)
                                    Text(l.url.removePrefix("https://"), style = MaterialTheme.typography.bodySmall, color = DF.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, tint = DF.Muted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            if (p.topics.isNotEmpty()) item(key = "topics") {
                FlowRow(Modifier.padding(20.dp).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    p.topics.forEach { Pill("#$it", color = DF.Muted) }
                }
            }
            val related = catalog.inCategory(p.category).filter { it.id != p.id }.take(8)
            if (related.isNotEmpty()) item(key = "related") {
                Column(Modifier.padding(top = 24.dp)) {
                    SectionHeader("Potrebbe piacerti anche", categoryLabel(p.category))
                    VerticalGap(14.dp)
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        itemsIndexed(related, key = { _, r -> r.id }) { i, r -> AppTile(r, "related", null, Modifier.reveal(i)) { onOpen(Opened(r, "related")) } }
                    }
                }
            }
        }

        // Barra superiore: i pulsanti restano, il titolo compare scorrendo.
        Row(
            Modifier.fillMaxWidth()
                .background(DF.Bg.copy(alpha = 0.92f * topAlpha))
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIcon(Icons.AutoMirrored.Rounded.ArrowBack, "Indietro", onBack)
            Text(
                p.name, style = MaterialTheme.typography.titleLarge, color = DF.Text, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp).graphicsLayer { alpha = topAlpha; translationY = (1f - topAlpha) * 16f },
            )
            RoundIcon(Icons.Rounded.Share, "Condividi") {
                shareText(context, p.name, "${p.name} — ${p.description}\n${p.webUrl ?: p.repoUrl}")
            }
        }

        AnimatedVisibility(
            visible = viewer != null,
            enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.9f, animationSpec = spring(dampingRatio = 0.8f)),
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.94f),
        ) {
            ShotViewer(p.screenshots, viewer ?: 0) { viewer = null }
        }
    }
}

@Composable
private fun StatsStrip(p: Project, installed: InstalledApp?, modifier: Modifier) {
    // Se sul telefono c'è una versione più nuova della release nota (catalogo indietro, offline), mostra quella.
    val version = when {
        installed != null && installed.version.isNotBlank() &&
            (installed.latest == null || Updater.isNewer(installed.version, installed.latest!!)) -> "v${installed.version.removePrefix("v")}"
        else -> p.release?.tag ?: "—"
    }
    val cells = buildList {
        add(Triple(version, "versione", Icons.Rounded.NewReleases))
        add(Triple(p.stars.toString(), if (p.stars == 1) "stella" else "stelle", Icons.Rounded.Star))
        add(Triple(relativeDate(p.updatedAt).ifEmpty { "—" }, "aggiornato", Icons.Rounded.Code))
        p.license?.let { add(Triple(it, "licenza", Icons.Rounded.Inventory2)) }
    }
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(DF.Surface.copy(alpha = 0.85f))
            .border(1.dp, DF.Line, RoundedCornerShape(22.dp)).padding(vertical = 12.dp),
    ) {
        cells.forEachIndexed { i, (value, label, _) ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, style = MaterialTheme.typography.titleMedium, color = DF.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(label, style = MaterialTheme.typography.labelSmall, color = DF.Muted)
            }
            if (i < cells.lastIndex) Box(Modifier.width(1.dp).height(34.dp).background(DF.Line).align(Alignment.CenterVertically))
        }
    }
}

@Composable
private fun Actions(p: Project, vm: AppViewModel, accent: Color, modifier: Modifier) {
    val context = LocalContext.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PlatformRow(p.platforms, tint = DF.Secondary, size = 18.dp)
        val download = p.primaryDownload
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                p.apk != null -> AppActionButton(p, vm, Modifier.weight(1f), accent = accent)
                download != null -> GoldButton(
                    if (download.name.endsWith(".apk", true)) "Scarica APK" else "Scarica",
                    Icons.Rounded.Download, Modifier.weight(1f), accent = accent,
                ) { openUrl(context, download.url) }
                p.webUrl != null -> GoldButton("Apri", Icons.Rounded.Language, Modifier.weight(1f), accent = accent) { openUrl(context, p.webUrl) }
                else -> GoldButton("Vedi su GitHub", Icons.Rounded.Code, Modifier.weight(1f), accent = accent) { openUrl(context, p.repoUrl) }
            }
            if (download != null && p.webUrl != null) {
                GoldButton("Apri", Icons.Rounded.Language, Modifier.weight(0.7f), filled = false) { openUrl(context, p.webUrl) }
            } else if (download != null || p.webUrl != null) {
                GoldButton("GitHub", Icons.Rounded.Code, Modifier.weight(0.7f), filled = false) { openUrl(context, p.repoUrl) }
            }
        }
    }
}

@Composable
private fun Screenshot(url: String, modifier: Modifier, onClick: () -> Unit) {
    SubcomposeAsyncImage(
        url, null,
        contentScale = ContentScale.FillHeight,
        modifier = modifier.height(340.dp).widthIn(min = 120.dp, max = 560.dp)
            .pressScale(0.96f, onClick).clip(RoundedCornerShape(22.dp)).border(1.dp, DF.Line, RoundedCornerShape(22.dp)),
        loading = { Box(Modifier.fillMaxSize().shimmer()) },
        error = { Box(Modifier.fillMaxSize().background(DF.Surface)) },
    )
}

@Composable
private fun VideoTile(onClick: () -> Unit) {
    Box(
        Modifier.height(340.dp).width(190.dp).pressScale(0.96f, onClick).clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(DF.SurfaceHigher, DF.Surface))).border(1.dp, DF.LineStrong, RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center,
    ) {
        val b = breathing(1600)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(64.dp).graphicsLayer { scaleX = 1f + 0.08f * b; scaleY = 1f + 0.08f * b }.clip(CircleShape).background(DF.Primary),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.PlayArrow, null, tint = DF.OnPrimary, modifier = Modifier.size(36.dp)) }
            VerticalGap(10.dp)
            Text("Guarda il video", style = MaterialTheme.typography.labelLarge, color = DF.Text)
        }
    }
}

@Composable
private fun AboutBlock(text: String, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val arrow by animateFloatAsState(if (expanded) 180f else 0f, spring(dampingRatio = 0.6f), label = "arrow")
    Column(modifier) {
        SectionHeader("Informazioni", null)
        VerticalGap(10.dp)
        Column(Modifier.padding(horizontal = 20.dp).animateContentSize(spring(dampingRatio = 0.85f, stiffness = 300f))) {
            Text(text, style = MaterialTheme.typography.bodyLarge, color = DF.Text.copy(alpha = 0.85f), maxLines = if (expanded) Int.MAX_VALUE else 4, overflow = TextOverflow.Ellipsis)
            if (text.length > 220) {
                Row(Modifier.padding(top = 6.dp).clip(CircleShape).pressScale(0.9f) { expanded = !expanded }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (expanded) "Mostra meno" else "Leggi tutto", style = MaterialTheme.typography.labelLarge, color = DF.Primary)
                    Icon(Icons.Rounded.ExpandMore, null, tint = DF.Primary, modifier = Modifier.graphicsLayer { rotationZ = arrow })
                }
            }
        }
    }
}

@Composable
private fun Features(items: List<String>, accent: Color, modifier: Modifier) {
    Column(modifier) {
        SectionHeader("Cosa sa fare", null)
        VerticalGap(12.dp)
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEachIndexed { i, f ->
                val dot = remember { Animatable(0f) }
                LaunchedEffect(Unit) { kotlinx.coroutines.delay(200L + i * 90L); dot.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 300f)) }
                Row(Modifier.reveal(i, 70), verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.padding(top = 6.dp).size(10.dp).graphicsLayer { scaleX = dot.value; scaleY = dot.value; rotationZ = 45f }
                            .clip(RoundedCornerShape(3.dp)).background(accent),
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(f, style = MaterialTheme.typography.bodyMedium, color = DF.Text.copy(alpha = 0.88f))
                }
            }
        }
    }
}

@Composable
private fun RoundIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).pressScale(0.86f, onClick).clip(CircleShape).background(DF.SurfaceContainer).border(1.dp, DF.Line, CircleShape),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, label, tint = DF.Text) }
}

/** Visore a schermo intero: scorrimento, pizzico per zoomare, doppio tocco e trascinamento verso il basso per chiudere. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShotViewer(urls: List<String>, start: Int, onClose: () -> Unit) {
    val pager = rememberPagerState(initialPage = start) { urls.size }
    val scope = rememberCoroutineScope()
    val drag = remember { Animatable(0f) }
    Box(
        Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = (0.96f - abs(drag.value) / 1400f).coerceIn(0.3f, 0.96f)))
            .draggable(
                rememberDraggableState { d -> scope.launch { drag.snapTo(drag.value + d) } },
                Orientation.Vertical,
                onDragStopped = { v ->
                    if (abs(drag.value) > 260f || abs(v) > 2400f) onClose() else drag.animateTo(0f, spring(dampingRatio = 0.7f))
                },
            ),
    ) {
        HorizontalPager(pager, Modifier.fillMaxSize().graphicsLayer { translationY = drag.value }, beyondViewportPageCount = 1) { page ->
            ZoomableImage(urls[page])
        }
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIcon(Icons.Rounded.Close, "Chiudi", onClose)
            Spacer(Modifier.weight(1f))
            Text("${pager.currentPage + 1} / ${urls.size}", style = MaterialTheme.typography.labelLarge, color = DF.Text,
                modifier = Modifier.clip(CircleShape).background(DF.SurfaceContainer).padding(horizontal = 14.dp, vertical = 8.dp))
        }
        Row(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 20.dp)) {
            urls.indices.forEach { i ->
                val w by animateFloatAsState(if (i == pager.currentPage) 22f else 6f, spring(dampingRatio = 0.6f), label = "d")
                Box(Modifier.padding(horizontal = 3.dp).height(6.dp).width(w.dp).clip(CircleShape).background(if (i == pager.currentPage) DF.Primary else DF.LineStrong))
            }
        }
    }
}

@Composable
private fun ZoomableImage(url: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val shownScale by animateFloatAsState(scale, spring(dampingRatio = 0.8f, stiffness = 500f), label = "s")
    Box(
        Modifier.fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        if (event.changes.size > 1 || scale > 1f) {
                            scale = (scale * event.calculateZoom()).coerceIn(1f, 4f)
                            offset = if (scale == 1f) Offset.Zero else offset + event.calculatePan()
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    if (scale > 1f) { scale = 1f; offset = Offset.Zero } else scale = 2.5f
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            url, null, contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(vertical = 70.dp).graphicsLayer {
                scaleX = shownScale; scaleY = shownScale
                translationX = offset.x; translationY = offset.y
            },
            loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Spinner(Modifier.size(36.dp)) } },
        )
    }
}

private fun linkIcon(type: String): ImageVector = when (type) {
    "web" -> Icons.Rounded.Language
    "github" -> Icons.Rounded.Code
    "release", "releases" -> Icons.Rounded.NewReleases
    "issues" -> Icons.Rounded.BugReport
    else -> Icons.AutoMirrored.Rounded.OpenInNew
}

/** Le note di rilascio arrivano in Markdown: ne tengo solo il testo leggibile. */
fun cleanNotes(md: String) = md
    .replace(Regex("""!\[[^\]]*]\([^)]*\)"""), "")
    .replace(Regex("""\[([^\]]*)]\([^)]*\)"""), "$1")
    .replace(Regex("""(?m)^#+\s*"""), "")
    .replace(Regex("""\*\*|__|`"""), "")
    .replace(Regex("""\n{3,}"""), "\n\n")
    .trim()
