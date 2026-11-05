package it.dueffe.home.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import it.dueffe.home.AppViewModel
import it.dueffe.home.data.Catalog
import it.dueffe.home.ui.theme.DF
import it.dueffe.home.ui.theme.Motion
import kotlinx.coroutines.launch
import kotlin.math.sign

enum class Tab(val label: String, val icon: ImageVector, val inNav: Boolean = true) {
    Home("Home", Icons.Rounded.Home),
    Explore("Esplora", Icons.Rounded.Explore),
    Gist("Gist", Icons.Rounded.Code),
    Apps("Le tue app", Icons.Rounded.Apps),
    // Info si apre dal pulsante in alto a destra della Home, non dalla barra.
    Info("Info", Icons.Rounded.Info, inNav = false),
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DueffeHomeApp(vm: AppViewModel) {
    var splashDone by rememberSaveable { mutableStateOf(false) }
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var infoFrom by rememberSaveable { mutableStateOf(Tab.Home) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var opened by remember { mutableStateOf<Opened?>(null) }
    val history = remember { ArrayList<Opened>() }

    // Stati di scorrimento tenuti fuori dalle schermate, così tornando indietro restano dove erano.
    val homeList = rememberLazyListState()
    val exploreList = rememberLazyListState()
    val gistList = rememberLazyListState()
    val appsList = rememberLazyListState()
    val infoScroll = rememberScrollState()
    val featuredCount = vm.catalog?.featured?.size ?: 0
    val pager = rememberPagerState { featuredCount }
    val scope = rememberCoroutineScope()

    // Le installazioni avvengono fuori dall'app: al ritorno si rilegge cosa è installato.
    LifecycleResumeEffect(Unit) {
        vm.onResume()
        onPauseOrDispose { }
    }

    BackHandler(opened != null) { opened = history.removeLastOrNull() }
    BackHandler(opened == null && tab != Tab.Home && tab != Tab.Info) { tab = Tab.Home }
    BackHandler(opened == null && tab == Tab.Info) { tab = infoFrom }

    val open: (Opened) -> Unit = { o -> opened?.let { history.add(it) }; opened = o }
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 112.dp

    Box(Modifier.fillMaxSize()) {
        AuroraBackground(scroll = {
            if (tab == Tab.Home && homeList.firstVisibleItemIndex == 0) homeList.firstVisibleItemScrollOffset.toFloat() else 900f
        })
        val catalog = vm.catalog
        if (catalog != null) {
            SharedTransitionLayout {
                AnimatedContent(
                    opened,
                    contentKey = { it?.project?.id },
                    transitionSpec = {
                        if (targetState != null) {
                            (fadeIn(tween(360, 60)) + slideInVertically(tween(520, easing = Motion.Reveal)) { it / 14 }) togetherWith
                                (fadeOut(tween(260)) + scaleOut(targetScale = 0.96f, animationSpec = tween(360)))
                        } else {
                            (fadeIn(tween(320)) + scaleIn(initialScale = 0.96f, animationSpec = tween(420, easing = Motion.Reveal))) togetherWith
                                (fadeOut(tween(220)) + slideOutVertically(tween(380)) { it / 12 })
                        }
                    },
                    label = "detail",
                ) { target ->
                    val scopes = SharedScopes(this@SharedTransitionLayout, this@AnimatedContent)
                    if (target == null) {
                        MainTabs(
                            catalog, vm, tab, category, scopes, splashDone,
                            homeList, exploreList, gistList, appsList, infoScroll, pager, bottomPadding,
                            onTab = { t ->
                                if (t == tab) scope.launch {
                                    when (t) {
                                        Tab.Home -> homeList.animateScrollToItem(0)
                                        Tab.Explore -> exploreList.animateScrollToItem(0)
                                        Tab.Gist -> gistList.animateScrollToItem(0)
                                        Tab.Apps -> appsList.animateScrollToItem(0)
                                        Tab.Info -> infoScroll.animateScrollTo(0)
                                    }
                                }
                                if (t == Tab.Info && tab != Tab.Info) infoFrom = tab
                                tab = t
                            },
                            onInfoBack = { tab = infoFrom },
                            onCategory = { category = it; tab = Tab.Explore },
                            onCategoryInline = { category = it },
                            onOpen = open,
                        )
                    } else {
                        DetailScreen(target, catalog, vm, scopes, onBack = { opened = history.removeLastOrNull() }, onOpen = open)
                    }
                }
            }
        }

        AnimatedVisibility(
            vm.message != null,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 100.dp, start = 24.dp, end = 24.dp),
            enter = slideInVertically(spring(dampingRatio = 0.7f, stiffness = 400f)) { it } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut(),
        ) {
            var last by remember { mutableStateOf("") }
            vm.message?.let { last = it }
            Text(
                last, style = MaterialTheme.typography.labelLarge, color = DF.Text,
                modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(DF.SurfaceHigher)
                    .border(1.dp, DF.LineStrong, RoundedCornerShape(18.dp)).padding(horizontal = 18.dp, vertical = 14.dp),
            )
        }

        if (!splashDone) SplashOverlay { splashDone = true }
    }
}

@Composable
private fun MainTabs(
    catalog: Catalog,
    vm: AppViewModel,
    tab: Tab,
    category: String?,
    scopes: SharedScopes,
    splashDone: Boolean,
    homeList: androidx.compose.foundation.lazy.LazyListState,
    exploreList: androidx.compose.foundation.lazy.LazyListState,
    gistList: androidx.compose.foundation.lazy.LazyListState,
    appsList: androidx.compose.foundation.lazy.LazyListState,
    infoScroll: androidx.compose.foundation.ScrollState,
    pager: androidx.compose.foundation.pager.PagerState,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onTab: (Tab) -> Unit,
    onInfoBack: () -> Unit,
    onCategory: (String?) -> Unit,
    onCategoryInline: (String?) -> Unit,
    onOpen: (Opened) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            tab,
            transitionSpec = {
                val dir = (targetState.ordinal - initialState.ordinal).sign
                (fadeIn(tween(300, 80)) + slideInHorizontally(tween(460, easing = Motion.Reveal)) { dir * it / 5 }) togetherWith
                    (fadeOut(tween(160)) + slideOutHorizontally(tween(300)) { -dir * it / 8 })
            },
            label = "tabs",
        ) { t ->
            when (t) {
                Tab.Home -> HomeScreen(
                    catalog, vm, homeList, pager, scopes,
                    headlineDelay = if (splashDone) 0L else 2300L,
                    bottomPadding = bottomPadding,
                    onOpen = onOpen, onCategory = onCategory,
                    onGists = { onTab(Tab.Gist) }, onInfo = { onTab(Tab.Info) },
                )
                Tab.Explore -> ExploreScreen(catalog, exploreList, category, onCategoryInline, scopes, bottomPadding, onOpen)
                Tab.Gist -> GistScreen(catalog.gists, vm, gistList, bottomPadding)
                Tab.Apps -> AppsScreen(catalog, vm, appsList, scopes, bottomPadding, onOpen) { onTab(Tab.Explore) }
                Tab.Info -> InfoScreen(catalog, vm, infoScroll, onInfoBack)
            }
        }
        Box(
            Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars.add(WindowInsets(top = 12.dp)))
                .background(Brush.verticalGradient(listOf(DF.Bg.copy(alpha = 0.92f), DF.Bg.copy(alpha = 0f)))),
        )
        AnimatedVisibility(
            tab.inNav,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(spring(dampingRatio = 0.8f, stiffness = 400f)) { it } + fadeIn(),
            exit = slideOutVertically(tween(260)) { it } + fadeOut(tween(200)),
        ) {
            BottomNav(if (tab.inNav) tab else Tab.Home, onTab, vm.installed.any { it.hasUpdate }, Modifier)
        }
    }
}

/** Barra fluttuante: l'indicatore dorato si allunga come una goccia mentre raggiunge la nuova voce. */
@Composable
private fun BottomNav(selected: Tab, onSelect: (Tab) -> Unit, updateDot: Boolean, modifier: Modifier) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(68.dp)
            .shadow(24.dp, shape, ambientColor = DF.PrimaryStrong, spotColor = DF.Bg)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(DF.SurfaceHigh, DF.BgElevated)))
            .border(1.dp, DF.Line, shape),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(6.dp)) {
            val tabs = Tab.entries.filter { it.inNav }
            val slot = maxWidth / tabs.size
            val memory = remember { IntArray(1) { selected.ordinal } }
            val dir = remember(selected) { val d = (selected.ordinal - memory[0]).sign; memory[0] = selected.ordinal; d }
            val fast = spring<androidx.compose.ui.unit.Dp>(dampingRatio = 0.75f, stiffness = 900f)
            val slow = spring<androidx.compose.ui.unit.Dp>(dampingRatio = 0.7f, stiffness = 260f)
            val left by animateDpAsState(slot * selected.ordinal, if (dir > 0) slow else fast, label = "l")
            val right by animateDpAsState(slot * (selected.ordinal + 1), if (dir > 0) fast else slow, label = "r")
            Box(
                Modifier.offset(x = left).width(right - left).fillMaxHeight().clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(DF.Primary, DF.PrimaryStrong))),
            )
            Row(Modifier.fillMaxSize()) {
                tabs.forEach { t ->
                    NavItem(t, t == selected, updateDot && t == Tab.Apps, Modifier.weight(1f)) { onSelect(t) }
                }
            }
        }
    }
}

@Composable
private fun NavItem(tab: Tab, selected: Boolean, dot: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bounce = remember { Animatable(1f) }
    val tilt = remember { Animatable(0f) }
    LaunchedEffect(selected) {
        if (selected) {
            launch { bounce.snapTo(0.6f); bounce.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 500f)) }
            launch { tilt.snapTo(-14f); tilt.animateTo(0f, spring(dampingRatio = 0.3f, stiffness = 300f)) }
        }
    }
    val color = if (selected) DF.OnPrimary else DF.Muted
    Column(
        modifier.fillMaxHeight().clickable(remember { MutableInteractionSource() }, null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Box {
            Icon(
                tab.icon, tab.label, tint = color,
                modifier = Modifier.size(24.dp).graphicsLayer { scaleX = bounce.value; scaleY = bounce.value; rotationZ = tilt.value },
            )
            if (dot) Box(Modifier.align(Alignment.TopEnd).size(8.dp).clip(RoundedCornerShape(4.dp)).background(DF.Error))
        }
        Text(tab.label, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
    }
}
