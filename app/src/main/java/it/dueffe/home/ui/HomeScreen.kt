package it.dueffe.home.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import it.dueffe.home.AppViewModel
import it.dueffe.home.UpdateState
import it.dueffe.home.data.Catalog
import it.dueffe.home.data.Project
import it.dueffe.home.ui.theme.DF
import it.dueffe.home.ui.theme.DueffeFont
import it.dueffe.home.ui.theme.Motion
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    catalog: Catalog,
    vm: AppViewModel,
    listState: LazyListState,
    pagerState: PagerState,
    scopes: SharedScopes?,
    headlineDelay: Long,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onOpen: (Opened) -> Unit,
    onCategory: (String?) -> Unit,
    onGists: () -> Unit,
    onInfo: () -> Unit,
) {
    val pull = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = vm.refreshing,
        onRefresh = { vm.refresh() },
        state = pull,
        indicator = { FfRefreshIndicator(pull, vm.refreshing, Modifier.align(Alignment.TopCenter)) },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomPadding),
        ) {
            item(key = "header") {
                HomeHeader(catalog, headlineDelay, vm.update is UpdateState.Available, onInfo, Modifier.graphicsLayer {
                    // Parallasse: l'intestazione scorre più lenta e sfuma.
                    if (listState.firstVisibleItemIndex == 0) {
                        val o = listState.firstVisibleItemScrollOffset.toFloat()
                        translationY = o * 0.45f
                        alpha = (1f - o / 900f).coerceIn(0f, 1f)
                    }
                })
            }
            item(key = "update") {
                val s = vm.update
                AnimatedVisibility(
                    visible = s is UpdateState.Available && !vm.updateBannerDismissed,
                    enter = expandVertically(spring(dampingRatio = 0.8f, stiffness = 300f)) + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    val version = (s as? UpdateState.Available)?.info?.version.orEmpty()
                    UpdateBanner(version, onOpen = onInfo, onDismiss = { vm.updateBannerDismissed = true })
                }
            }
            if (catalog.featured.isNotEmpty()) {
                item(key = "featured") {
                    Column(Modifier.reveal(1)) {
                        SectionHeader("In evidenza", "I progetti a cui tengo di più")
                        VerticalGap(14.dp)
                        FeaturedPager(catalog.featured, pagerState, scopes, onOpen)
                    }
                }
            }
            item(key = "categories") {
                Column(Modifier.padding(top = 30.dp).reveal(2)) {
                    SectionHeader("Categorie", "Ordinati per tipo, per trovare subito tutto", action = "Tutti", onAction = { onCategory(null) })
                    VerticalGap(14.dp)
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(catalog.categories, key = { _, c -> c.id }) { i, c ->
                            CategoryCard(c.title, catalog.inCategory(c.id).size, categoryIcon(c.id), i, Modifier.reveal(i + 2)) { onCategory(c.id) }
                        }
                    }
                }
            }
            val recent = catalog.projects.filter { it.release?.date != null }.sortedByDescending { it.release?.date }.take(5)
            if (recent.isNotEmpty()) {
                item(key = "recent") {
                    Column(Modifier.padding(top = 30.dp)) {
                        SectionHeader("Ultime release", "Le versioni appena uscite")
                        VerticalGap(10.dp)
                        recent.forEachIndexed { i, p -> RecentRow(p, scopes, Modifier.reveal(i + 3)) { onOpen(Opened(p, "recent")) } }
                    }
                }
            }
            catalog.categories.forEach { cat ->
                val items = catalog.inCategory(cat.id)
                item(key = "cat-${cat.id}") {
                    Column(Modifier.padding(top = 30.dp)) {
                        SectionHeader(cat.title, cat.subtitle, action = "Vedi tutti", onAction = { onCategory(cat.id) })
                        VerticalGap(14.dp)
                        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            itemsIndexed(items, key = { _, p -> p.id }) { i, p ->
                                AppTile(p, "row-${cat.id}", scopes, Modifier.reveal(i)) { onOpen(Opened(p, "row-${cat.id}")) }
                            }
                        }
                    }
                }
            }
            if (catalog.gists.isNotEmpty()) {
                item(key = "gist") { GistTeaser(catalog.gists.size, Modifier.padding(top = 32.dp), onGists) }
            }
            item(key = "footer") { Footer(catalog) }
        }
    }
}

@Composable
private fun HomeHeader(catalog: Catalog, headlineDelay: Long, updateDot: Boolean, onInfo: () -> Unit, modifier: Modifier) {
    Column(modifier.statusBarsPadding().padding(horizontal = 20.dp).padding(top = 14.dp, bottom = 26.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dueffe Home", fontFamily = DueffeFont, fontSize = 22.sp, color = DF.Text, modifier = Modifier.weight(1f))
            Box(
                Modifier.size(40.dp).pressScale(0.88f, onInfo),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.fillMaxSize().clip(CircleShape).background(DF.Bg).border(1.5.dp, DF.LineStrong, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Info, "Info", tint = DF.Text, modifier = Modifier.size(22.dp)) }
                if (updateDot) Box(Modifier.align(Alignment.TopEnd).size(10.dp).clip(CircleShape).background(DF.Error).border(2.dp, DF.Bg, CircleShape))
            }
        }
        VerticalGap(30.dp)
        AnimatedHeadline(listOf("Tutti i miei", "progetti, in un", "solo posto."), goldWord = "progetti", startDelayMs = headlineDelay)
        VerticalGap(10.dp)
        Text(
            "App Android, giochi, strumenti per il PC e web app di ${catalog.owner.name}. Tutto aggiornato in tempo reale da GitHub.",
            style = MaterialTheme.typography.bodyLarge, color = DF.Muted,
            modifier = Modifier.reveal(4, 90),
        )
        VerticalGap(22.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val releases = catalog.projects.count { it.release != null }
            StatTile(catalog.projects.size, "progetti", Modifier.weight(1f).reveal(5, 90), headlineDelay)
            StatTile(releases, "con release", Modifier.weight(1f).reveal(6, 90), headlineDelay)
            StatTile(catalog.gists.size, "gist", Modifier.weight(1f).reveal(7, 90), headlineDelay)
        }
    }
}

@Composable
private fun StatTile(value: Int, label: String, modifier: Modifier, startDelay: Long) {
    val n = remember { Animatable(0f) }
    LaunchedEffect(value) {
        delay(startDelay + 300)
        n.animateTo(value.toFloat(), tween(1400, easing = Motion.Reveal))
    }
    CardSurface(modifier, RoundedCornerShape(20.dp), PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        Column {
            Text(n.value.toInt().toString(), style = MaterialTheme.typography.headlineMedium, color = DF.Primary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = DF.Muted, maxLines = 1)
        }
    }
}

@Composable
private fun UpdateBanner(version: String, onOpen: () -> Unit, onDismiss: () -> Unit) {
    val shine = rememberInfiniteTransition(label = "shine")
    val x by shine.animateFloat(-0.5f, 1.5f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "x")
    Row(
        Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 26.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(DF.Primary, DF.PrimaryStrong)))
            .drawWithContent {
                drawContent()
                drawRect(
                    Brush.linearGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent),
                        start = Offset(size.width * x - 120f, 0f), end = Offset(size.width * x + 120f, size.height),
                    ),
                )
            }
            .pressScale(0.97f, onOpen)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.SystemUpdate, null, tint = DF.OnPrimary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Nuova versione $version", style = MaterialTheme.typography.titleMedium, color = DF.OnPrimary)
            Text("Tocca per aggiornare Dueffe Home", style = MaterialTheme.typography.bodySmall, color = DF.OnPrimary.copy(alpha = 0.8f))
        }
        Icon(Icons.Rounded.Close, "Chiudi", tint = DF.OnPrimary, modifier = Modifier.size(36.dp).clip(CircleShape).pressScale(0.85f, onDismiss).padding(8.dp))
    }
}

@Composable
private fun FeaturedPager(items: List<Project>, state: PagerState, scopes: SharedScopes?, onOpen: (Opened) -> Unit) {
    // Avanzamento automatico, in pausa mentre l'utente trascina.
    LaunchedEffect(state.settledPage, state.isScrollInProgress) {
        if (state.isScrollInProgress || items.size < 2) return@LaunchedEffect
        delay(5200)
        state.animateScrollToPage((state.settledPage + 1) % items.size, animationSpec = tween(900, easing = Motion.Reveal))
    }
    val halo = rememberInfiniteTransition(label = "halo")
    val angle by halo.animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(4200, easing = LinearEasing)), label = "angle")
    HorizontalPager(
        state = state,
        contentPadding = PaddingValues(horizontal = 30.dp),
        pageSpacing = 14.dp,
        modifier = Modifier.fillMaxWidth().height(410.dp),
        key = { items[it].id },
    ) { page ->
        val p = items[page]
        val offset = (state.currentPage - page) + state.currentPageOffsetFraction
        val a = abs(offset).coerceIn(0f, 1f)
        val accent = rememberAccent(p.icon)
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    cameraDistance = 14f * density
                    rotationY = offset * 16f
                    val s = lerp(1f, 0.86f, a)
                    scaleX = s; scaleY = s
                    alpha = lerp(1f, 0.55f, a)
                }
                .pressScale(0.97f) { onOpen(Opened(p, "featured")) }
                .clip(RoundedCornerShape(32.dp))
                .background(DF.Surface)
                .drawWithContent {
                    drawContent()
                    // Alone dorato che percorre il bordo della card attiva.
                    val r = size.maxDimension
                    val c = center
                    drawRoundRect(
                        Brush.linearGradient(
                            listOf(Color.Transparent, accent.copy(alpha = 0.95f * (1f - a)), Color.Transparent),
                            start = c + Offset(cos(angle) * r, sin(angle) * r) * 0.5f,
                            end = c - Offset(cos(angle) * r, sin(angle) * r) * 0.5f,
                        ),
                        cornerRadius = CornerRadius(32.dp.toPx()),
                        style = Stroke(2.dp.toPx()),
                    )
                },
        ) {
            val backdrop = p.screenshots.firstOrNull()
            if (backdrop != null) {
                AsyncImage(
                    backdrop, null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        translationX = offset * 90.dp.toPx()
                        scaleX = 1.18f; scaleY = 1.18f
                    },
                )
            } else {
                AsyncImage(
                    p.icon, null, contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (Build.VERSION.SDK_INT >= 31) Modifier.blur(46.dp) else Modifier)
                        .graphicsLayer {
                            translationX = offset * 90.dp.toPx()
                            scaleX = 1.7f; scaleY = 1.7f
                            alpha = if (Build.VERSION.SDK_INT >= 31) 0.75f else 0.18f
                        },
                )
                Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(accent.copy(alpha = 0.45f), Color.Transparent))))
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, 0.35f to DF.Bg.copy(alpha = 0.15f), 1f to DF.Bg.copy(alpha = 0.97f))))
            Pill(
                categoryLabel(p.category).uppercase(), Modifier.padding(18.dp).align(Alignment.TopStart),
                color = DF.Secondary,
            )
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProjectIcon(p, 62.dp, Modifier.sharedIcon(scopes, "featured", p.id))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.name, style = MaterialTheme.typography.headlineMedium, color = DF.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        PlatformRow(p.platforms, tint = DF.Secondary)
                    }
                }
                VerticalGap(12.dp)
                Text(p.description, style = MaterialTheme.typography.bodyMedium, color = DF.Text.copy(alpha = 0.82f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                VerticalGap(14.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    p.release?.let { Pill(it.tag, color = accent); Spacer(Modifier.width(8.dp)) }
                    Spacer(Modifier.weight(1f))
                    Row(
                        Modifier.clip(CircleShape).background(accent).padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Scopri", style = MaterialTheme.typography.labelLarge, color = if (luminance(accent) > 0.45f) DF.OnPrimary else DF.Text)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(16.dp), tint = if (luminance(accent) > 0.45f) DF.OnPrimary else DF.Text)
                    }
                }
            }
        }
    }
    VerticalGap(14.dp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        items.indices.forEach { i ->
            val selected = i == state.currentPage
            val w by animateDpAsState(if (selected) 24.dp else 7.dp, spring(dampingRatio = 0.6f, stiffness = 400f), label = "dot")
            Box(
                Modifier.padding(horizontal = 3.dp).height(7.dp).width(w).clip(CircleShape)
                    .background(if (selected) DF.Primary else DF.LineStrong),
            )
        }
    }
}

@Composable
private fun CategoryCard(title: String, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, index: Int, modifier: Modifier, onClick: () -> Unit) {
    val float = rememberInfiniteTransition(label = "float")
    val dy by float.animateFloat(-3f, 3f, infiniteRepeatable(tween(1800 + index * 230, easing = Motion.Standard), androidx.compose.animation.core.RepeatMode.Reverse), label = "dy")
    CardSurface(modifier.width(150.dp).pressScale(0.94f, onClick), RoundedCornerShape(24.dp), PaddingValues(16.dp)) {
        Column {
            Box(
                Modifier.size(44.dp).offset(y = dy.dp).clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(DF.Primary, DF.PrimaryStrong))),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = DF.OnPrimary, modifier = Modifier.size(24.dp)) }
            VerticalGap(14.dp)
            Text(title, style = MaterialTheme.typography.titleMedium, color = DF.Text, maxLines = 1)
            Text(if (count == 1) "1 progetto" else "$count progetti", style = MaterialTheme.typography.bodySmall, color = DF.Muted)
        }
    }
}

@Composable
private fun RecentRow(p: Project, scopes: SharedScopes?, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.fillMaxWidth().pressScale(0.97f, onClick).padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProjectIcon(p, 54.dp, Modifier.sharedIcon(scopes, "recent", p.id))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(p.name, style = MaterialTheme.typography.titleMedium, color = DF.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(p.description, style = MaterialTheme.typography.bodySmall, color = DF.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.NewReleases, null, tint = DF.Primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(p.release?.tag.orEmpty(), style = MaterialTheme.typography.labelMedium, color = DF.Primary)
            }
            Text(relativeDate(p.release?.date), style = MaterialTheme.typography.labelSmall, color = DF.Muted)
        }
    }
}

@Composable
fun AppTile(p: Project, origin: String, scopes: SharedScopes?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.width(104.dp).pressScale(0.92f, onClick)) {
        Box {
            // Riflesso dell'icona sotto la tessera.
            val accent = rememberAccent(p.icon)
            Box(
                Modifier.matchParentSize().offset(y = 10.dp).graphicsLayer { scaleX = 0.8f; scaleY = 0.8f; alpha = 0.55f }
                    .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.6f), Color.Transparent))),
            )
            ProjectIcon(p, 104.dp, Modifier.sharedIcon(scopes, origin, p.id))
        }
        VerticalGap(10.dp)
        Text(p.name, style = MaterialTheme.typography.titleSmall, color = DF.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlatformRow(p.platforms, size = 12.dp, modifier = Modifier.weight(1f, fill = false))
            p.release?.let {
                Spacer(Modifier.width(6.dp))
                Text(it.tag, style = MaterialTheme.typography.labelSmall, color = DF.Muted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun GistTeaser(count: Int, modifier: Modifier, onClick: () -> Unit) {
    CardSurface(modifier.padding(horizontal = 20.dp).fillMaxWidth().pressScale(0.97f, onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(DF.SurfaceHigher).border(1.dp, DF.LineStrong, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.Code, null, tint = DF.Primary) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Gist e script", style = MaterialTheme.typography.titleMedium, color = DF.Text)
                Text("$count piccoli strumenti pronti da usare", style = MaterialTheme.typography.bodySmall, color = DF.Muted)
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = DF.Primary)
        }
    }
}

@Composable
private fun Footer(catalog: Catalog) {
    Column(Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val b = breathing()
        FfMark(Modifier.height(34.dp).graphicsLayer { alpha = 0.5f + 0.5f * b; rotationZ = -4f + 8f * b })
        VerticalGap(8.dp)
        Text("Fatto con cura da ${catalog.owner.name}", style = MaterialTheme.typography.bodySmall, color = DF.Muted)
        Text("Catalogo aggiornato ${relativeDate(catalog.generatedAt)}", style = MaterialTheme.typography.labelSmall, color = DF.Muted.copy(alpha = 0.6f))
    }
}

/** Indicatore del trascinamento per aggiornare: il monogramma ruota con il gesto. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FfRefreshIndicator(state: PullToRefreshState, refreshing: Boolean, modifier: Modifier) {
    val spin = rememberInfiniteTransition(label = "spin")
    val r by spin.animateFloat(0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "r")
    val f = if (refreshing) 1f else state.distanceFraction.coerceIn(0f, 1.4f)
    if (f <= 0f) return
    Box(
        modifier
            .statusBarsPadding()
            .graphicsLayer {
                translationY = lerp(-40.dp.toPx(), 24.dp.toPx(), f.coerceAtMost(1f))
                alpha = f.coerceAtMost(1f)
                val s = 0.6f + 0.4f * f.coerceAtMost(1f)
                scaleX = s; scaleY = s
            }
            .size(48.dp)
            .clip(CircleShape)
            .background(DF.SurfaceHigher)
            .border(1.dp, DF.LineStrong, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        FfMark(Modifier.size(24.dp).graphicsLayer { rotationZ = if (refreshing) r else f * 300f })
    }
}

fun categoryLabel(id: String): String = when (id) {
    "android" -> "App Android"
    "giochi" -> "Gioco"
    "desktop" -> "Windows e Mac"
    "web" -> "Web app"
    "sviluppo" -> "AI e sviluppo"
    else -> id
}
