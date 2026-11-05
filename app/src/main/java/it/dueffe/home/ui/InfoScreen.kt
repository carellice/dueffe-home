package it.dueffe.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.dueffe.home.AppViewModel
import it.dueffe.home.BuildConfig
import it.dueffe.home.UpdateState
import it.dueffe.home.data.Catalog
import it.dueffe.home.ui.theme.DF
import it.dueffe.home.ui.theme.Motion

@Composable
fun InfoScreen(catalog: Catalog, vm: AppViewModel, scroll: ScrollState, onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(scroll).statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.align(Alignment.Start).size(44.dp).pressScale(0.86f, onBack).clip(CircleShape).background(DF.SurfaceContainer).border(1.dp, DF.Line, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Indietro", tint = DF.Text) }
        OrbitLogo(Modifier.reveal(0))
        VerticalGap(18.dp)
        Text("Dueffe Home", style = MaterialTheme.typography.displaySmall, color = DF.Text, modifier = Modifier.reveal(1))
        Text("Versione ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium, color = DF.Muted, modifier = Modifier.reveal(2))
        VerticalGap(26.dp)
        UpdateCard(vm, Modifier.reveal(3))
        VerticalGap(16.dp)
        CardSurface(Modifier.fillMaxWidth().reveal(4)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape).background(DF.Bg).border(2.dp, DF.Primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { FfMark(Modifier.size(32.dp)) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(catalog.owner.name, style = MaterialTheme.typography.titleLarge, color = DF.Text)
                    Text("@${catalog.owner.login} su GitHub", style = MaterialTheme.typography.bodySmall, color = DF.Muted)
                }
            }
        }
        VerticalGap(16.dp)
        Column(Modifier.fillMaxWidth().reveal(5), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LinkRow(Icons.Rounded.Person, "Profilo GitHub", "Tutti i repository") { openUrl(context, catalog.owner.url) }
            LinkRow(Icons.Rounded.Code, "Codice di Dueffe Home", "Repository pubblica") { openUrl(context, "https://github.com/${BuildConfig.GITHUB_REPO}") }
            LinkRow(Icons.Rounded.Language, "Versione web", "Apri Dueffe Home nel browser") { openUrl(context, "https://carellice.github.io/dueffe-home/") }
            LinkRow(Icons.Rounded.Sync, "Aggiorna il catalogo", "Ultimo aggiornamento ${relativeDate(catalog.generatedAt)}") { vm.refresh() }
        }
        VerticalGap(16.dp)
        HiddenContentCard(vm, Modifier.reveal(6))
        VerticalGap(24.dp)
        Text(
            "Il catalogo si rigenera ogni giorno da GitHub: nuovi progetti, release e gist compaiono da soli.",
            style = MaterialTheme.typography.bodySmall, color = DF.Muted.copy(alpha = 0.75f),
        )
    }
}

/** Sblocco con PIN dei progetti nascosti dal catalogo. */
@Composable
private fun HiddenContentCard(vm: AppViewModel, modifier: Modifier) {
    val focus = LocalFocusManager.current
    var asking by rememberSaveable { mutableStateOf(false) }
    var pin by rememberSaveable { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }
    val submit = {
        if (vm.unlock(pin)) { asking = false; pin = ""; focus.clearFocus(); vm.toast("Contenuti nascosti sbloccati") } else wrong = true
    }
    CardSurface(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            if (vm.unlocked) {
                StatusLine(Icons.Rounded.LockOpen, DF.Success, "Contenuti nascosti visibili", "Li trovi nel catalogo insieme agli altri")
                VerticalGap(14.dp)
                GoldButton("Nascondi di nuovo", Icons.Rounded.Lock, Modifier.fillMaxWidth(), filled = false) { vm.lock(); vm.toast("Contenuti nascosti") }
            } else {
                StatusLine(Icons.Rounded.Lock, DF.Primary, "Contenuti nascosti", "Inserisci il PIN per mostrarli")
                VerticalGap(14.dp)
                if (asking) {
                    OutlinedTextField(
                        pin, { pin = it.filter(Char::isDigit).take(12); wrong = false },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = wrong,
                        label = { Text(if (wrong) "PIN non corretto" else "PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DF.Primary, unfocusedBorderColor = DF.LineStrong, cursorColor = DF.Primary,
                            focusedLabelColor = DF.Primary, unfocusedLabelColor = DF.Muted, focusedTextColor = DF.Text, unfocusedTextColor = DF.Text,
                        ),
                    )
                    VerticalGap(12.dp)
                    GoldButton("Sblocca", Icons.Rounded.LockOpen, Modifier.fillMaxWidth()) { submit() }
                } else {
                    GoldButton("Sblocca con PIN", Icons.Rounded.LockOpen, Modifier.fillMaxWidth(), filled = false) { asking = true }
                }
            }
        }
    }
}

/** Il monogramma al centro, con due orbite dorate che girano in senso opposto. */
@Composable
private fun OrbitLogo(modifier: Modifier) {
    val t = rememberInfiniteTransition(label = "orbit")
    val a by t.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "a")
    val b = breathing(2600)
    Box(modifier.size(150.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(DF.PrimaryStrong.copy(alpha = 0.25f + 0.15f * b), Color.Transparent)), size.minDimension / 2)
            val stroke = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round)
            val inset = 10.dp.toPx()
            drawArc(DF.Primary, a, 110f, false, Offset(inset, inset), Size(size.width - inset * 2, size.height - inset * 2), style = stroke)
            drawArc(DF.LineStrong, a + 180f, 70f, false, Offset(inset, inset), Size(size.width - inset * 2, size.height - inset * 2), style = stroke)
            val in2 = 24.dp.toPx()
            drawArc(DF.Secondary.copy(alpha = 0.6f), -a * 1.4f, 80f, false, Offset(in2, in2), Size(size.width - in2 * 2, size.height - in2 * 2), style = stroke)
        }
        Box(
            Modifier.size(86.dp).clip(RoundedCornerShape(26.dp)).background(DF.Bg).border(1.dp, DF.LineStrong, RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center,
        ) { FfMark(Modifier.size(50.dp).graphicsLayer { rotationZ = -3f + 6f * b }) }
    }
}

@Composable
private fun UpdateCard(vm: AppViewModel, modifier: Modifier) {
    CardSurface(modifier.fillMaxWidth()) {
        AnimatedContent(
            vm.update,
            contentKey = { it::class },
            transitionSpec = { (fadeIn(tween(260)) + scaleIn(initialScale = 0.94f, animationSpec = spring(dampingRatio = 0.7f))) togetherWith fadeOut(tween(160)) },
            label = "update",
        ) { s ->
            Column(Modifier.fillMaxWidth()) {
                when (s) {
                    UpdateState.Idle, UpdateState.UpToDate -> {
                        StatusLine(
                            if (s == UpdateState.UpToDate) Icons.Rounded.CheckCircle else Icons.Rounded.Refresh,
                            if (s == UpdateState.UpToDate) DF.Success else DF.Primary,
                            if (s == UpdateState.UpToDate) "Sei aggiornato" else "Aggiornamenti",
                            if (s == UpdateState.UpToDate) "Hai l'ultima versione disponibile" else "Controlla le nuove versioni su GitHub",
                        )
                        VerticalGap(14.dp)
                        GoldButton("Controlla aggiornamenti", Icons.Rounded.Refresh, Modifier.fillMaxWidth(), filled = false) { vm.checkUpdates() }
                    }
                    UpdateState.Checking -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spinner(Modifier.size(28.dp))
                            Spacer(Modifier.width(14.dp))
                            Text("Cerco nuove versioni…", style = MaterialTheme.typography.titleMedium, color = DF.Text)
                        }
                    }
                    is UpdateState.Available -> {
                        StatusLine(Icons.Rounded.Download, DF.Primary, "Disponibile la ${s.info.version}", "Stai usando la ${BuildConfig.VERSION_NAME} · ${formatSize(s.info.size)}")
                        if (s.info.notes.isNotBlank()) {
                            VerticalGap(10.dp)
                            Text(cleanNotes(s.info.notes).take(500), style = MaterialTheme.typography.bodySmall, color = DF.Muted)
                        }
                        VerticalGap(14.dp)
                        GoldButton("Scarica e installa", Icons.Rounded.Download, Modifier.fillMaxWidth()) { vm.downloadUpdate() }
                    }
                    is UpdateState.Downloading -> {
                        val p by animateFloatAsState(s.progress, tween(300, easing = Motion.Standard), label = "p")
                        StatusLine(Icons.Rounded.Download, DF.Primary, "Download della ${s.info.version}", "${(p * 100).toInt()}% di ${formatSize(s.info.size)}")
                        VerticalGap(14.dp)
                        ProgressBar(p)
                    }
                    is UpdateState.Ready -> {
                        StatusLine(Icons.Rounded.InstallMobile, DF.Success, "Pronto da installare", "Android ti chiederà di confermare")
                        VerticalGap(14.dp)
                        GoldButton("Installa ${s.info.version}", Icons.Rounded.InstallMobile, Modifier.fillMaxWidth()) { vm.install() }
                    }
                    is UpdateState.Failed -> {
                        StatusLine(Icons.Rounded.ErrorOutline, DF.Error, "Qualcosa non va", s.message)
                        VerticalGap(14.dp)
                        GoldButton("Riprova", Icons.Rounded.Refresh, Modifier.fillMaxWidth(), filled = false) { vm.checkUpdates() }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusLine(icon: ImageVector, tint: Color, title: String, subtitle: String) {
    val pop = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 300f)) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(44.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value }.clip(CircleShape).background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = tint) }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = DF.Text)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DF.Muted)
        }
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    val t = rememberInfiniteTransition(label = "bar")
    val x by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1100, easing = LinearEasing)), label = "x")
    Box(Modifier.fillMaxWidth().height(12.dp).clip(CircleShape).background(DF.SurfaceHigher)) {
        Box(
            Modifier.fillMaxWidth(progress.coerceIn(0.02f, 1f)).height(12.dp).clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(DF.PrimaryStrong, DF.Secondary, DF.PrimaryStrong),
                        start = Offset(x * 300f - 300f, 0f), end = Offset(x * 300f, 0f),
                        tileMode = androidx.compose.ui.graphics.TileMode.Repeated,
                    ),
                ),
        )
    }
}

@Composable
fun Spinner(modifier: Modifier) {
    val t = rememberInfiniteTransition(label = "spinner")
    val a by t.animateFloat(0f, 360f, infiniteRepeatable(tween(850, easing = LinearEasing)), label = "a")
    Canvas(modifier) {
        drawArc(DF.Line, 0f, 360f, false, style = Stroke(3.dp.toPx()))
        drawArc(DF.Primary, a, 100f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun LinkRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().pressScale(0.97f, onClick).clip(RoundedCornerShape(20.dp)).background(DF.Surface)
            .border(1.dp, DF.Line, RoundedCornerShape(20.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = DF.Primary)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = DF.Text)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DF.Muted)
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = DF.Muted, modifier = Modifier.size(18.dp))
    }
}
