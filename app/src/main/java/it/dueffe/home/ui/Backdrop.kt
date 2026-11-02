package it.dueffe.home.ui

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.dueffe.home.R
import it.dueffe.home.ui.theme.DF
import it.dueffe.home.ui.theme.DisplayFont
import it.dueffe.home.ui.theme.Motion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** Fondale vivo: nebulose dorate che derivano lentamente e pulviscolo che sale. */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier, scroll: () -> Float = { 0f }) {
    val t = rememberInfiniteTransition(label = "aurora")
    val a by t.animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(32_000, easing = LinearEasing)), label = "a")
    val dust = remember { List(34) { i -> Dust(seed(i * 3 + 1), seed(i * 7 + 2), 0.35f + seed(i * 11 + 3), 0.6f + seed(i * 5 + 4) * 1.8f) } }
    Canvas(modifier.fillMaxSize()) {
        drawRect(DF.Bg)
        val w = size.width
        val h = size.height
        val s = scroll()
        blob(Offset(w * (0.18f + 0.14f * cos(a)), h * 0.06f + 50f * sin(a * 2) - s * 0.2f), w * 0.95f, Color(0x38FFB800))
        blob(Offset(w * (0.95f + 0.08f * sin(a)), h * (0.38f + 0.05f * cos(a)) - s * 0.1f), w * 0.75f, Color(0x24FFD84D))
        blob(Offset(w * (0.35f + 0.2f * sin(a + 1.3f)), h * (0.92f + 0.04f * sin(a * 3))), w * 1.05f, Color(0x1F8A5A00))
        val phase = a / (2 * PI).toFloat()
        dust.forEach { d ->
            val y = ((d.y - phase * d.speed) % 1f + 1f) % 1f
            val twinkle = 0.5f + 0.5f * sin(a * 6 * d.speed + d.x * 20)
            drawCircle(
                DF.Secondary.copy(alpha = 0.08f + 0.28f * twinkle * (1f - y * 0.6f)),
                radius = d.r * density,
                center = Offset(w * d.x + 14f * sin(a * 2 + d.y * 9), h * y),
            )
        }
    }
}

private data class Dust(val x: Float, val y: Float, val speed: Float, val r: Float)

private fun seed(i: Int): Float {
    val v = sin(i * 12.9898f) * 43758.547f
    return v - kotlin.math.floor(v)
}

private fun DrawScope.blob(center: Offset, radius: Float, color: Color) {
    drawCircle(Brush.radialGradient(listOf(color, Color.Transparent), center, radius), radius, center)
}

fun animationsDisabled(scale: Float) = scale == 0f

/**
 * Splash «Dueffe + Home» secondo il design system: monogramma ff che cade ruotato,
 * Due/e che si aprono, lettere di Home con overshoot, tagline, e uscita con un
 * foro circolare che si allarga e rivela l'app.
 */
@Composable
fun SplashOverlay(onFinished: () -> Unit) {
    val context = LocalContext.current
    val reduce = remember {
        animationsDisabled(Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f))
    }
    val mark = remember { Animatable(0f) }
    val sides = remember { Animatable(0f) }
    val letters = remember { List(4) { Animatable(0f) } }
    val tagline = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }
    val hole = remember { Animatable(0f) }
    val glow = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (reduce) {
            listOf(mark, sides, tagline, glow).forEach { it.snapTo(1f) }
            letters.forEach { it.snapTo(1f) }
            delay(500)
            onFinished()
            return@LaunchedEffect
        }
        launch { mark.animateTo(1f, tween(1275, easing = Motion.Mark)) }
        launch { sides.animateTo(1f, tween(1250, easing = Motion.Reveal)) }
        launch { glow.animateTo(1f, tween(1600, easing = Motion.Standard)) }
        letters.forEachIndexed { i, l ->
            launch {
                delay(480L + i * 70L)
                l.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 420f))
            }
        }
        launch {
            delay(1190)
            tagline.animateTo(1f, tween(420, easing = Motion.Letter))
        }
        delay(2150)
        launch { exit.animateTo(1f, tween(260, easing = Motion.Standard)) }
        delay(120)
        hole.animateTo(1f, tween(720, easing = Motion.Reveal))
        onFinished()
    }

    val density = LocalDensity.current
    val fontSize = 50.sp
    val em = with(density) { fontSize.toPx() }
    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } }
            }
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawRect(DF.Bg)
                val c = center
                val g = glow.value
                drawCircle(
                    Brush.radialGradient(listOf(DF.PrimaryStrong.copy(alpha = 0.28f * g), Color.Transparent), c, size.minDimension * 0.7f),
                    size.minDimension * 0.7f, c,
                )
                drawContent()
                val r = hole.value * hypot(size.width, size.height) / 2f * 1.08f
                if (r > 0f) {
                    drawCircle(Color.Black, r, c, blendMode = BlendMode.Clear)
                    drawCircle(DF.Primary.copy(alpha = (1f - hole.value) * 0.9f), r, c, style = Stroke(width = 3.dp.toPx()))
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .graphicsLayer {
                    alpha = 1f - exit.value
                    translationY = -10.dp.toPx() * exit.value
                    val s = 1f - 0.015f * exit.value
                    scaleX = s; scaleY = s
                }
                .clearAndSetSemantics { contentDescription = "Dueffe Home" },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy((-3).dp)) {
                val wordStyle = MaterialTheme.typography.displayLarge.copy(fontSize = fontSize, color = DF.Text)
                Text(
                    "Due", style = wordStyle,
                    modifier = Modifier.graphicsLayer {
                        translationX = (1f - sides.value) * em * 0.5f
                        alpha = sides.value
                    }.drawWithContent {
                        clipRect(left = size.width * (1f - sides.value)) { this@drawWithContent.drawContent() }
                    },
                )
                Image(
                    painterResource(R.drawable.ff_mark), null,
                    modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .height(with(density) { (em * 0.98f).toDp() })
                        .graphicsLayer {
                            val m = mark.value
                            translationY = -0.72f * em * (1f - m)
                            val s = 0.44f + 0.56f * m
                            scaleX = s; scaleY = s
                            rotationZ = -9f * (1f - m)
                            alpha = (m * 2f).coerceAtMost(1f)
                        },
                )
                Text(
                    "e", style = wordStyle,
                    modifier = Modifier.graphicsLayer {
                        translationX = -(1f - sides.value) * em * 0.5f
                        alpha = sides.value
                    }.drawWithContent {
                        clipRect(right = size.width * sides.value) { this@drawWithContent.drawContent() }
                    },
                )
                Spacer(Modifier.width(18.dp))
                "Home".forEachIndexed { i, ch ->
                    Text(
                        ch.toString(), style = wordStyle.copy(color = DF.Primary),
                        modifier = Modifier.graphicsLayer {
                            val v = letters[i].value
                            alpha = v.coerceIn(0f, 1f)
                            translationY = 0.72f * em * (1f - v)
                            rotationZ = 5f * (1f - v)
                            val s = 0.82f + 0.18f * v
                            scaleX = s; scaleY = s
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        },
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Tutti i progetti di Flavio, in un solo posto",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Medium,
                color = DF.Muted,
                modifier = Modifier.graphicsLayer {
                    alpha = tagline.value
                    translationY = (1f - tagline.value) * 10.dp.toPx()
                },
            )
        }
    }
}

/** Intestazione grande con le parole che salgono una alla volta e una sottolineatura dorata disegnata a mano. */
@Composable
fun AnimatedHeadline(lines: List<String>, goldWord: String, startDelayMs: Long, modifier: Modifier = Modifier) {
    val words = remember(lines) { lines.map { it.split(' ') } }
    val total = words.sumOf { it.size }
    val anims = remember(total) { List(total) { Animatable(0f) } }
    val underline = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(startDelayMs) // parte quando la splash si apre
        anims.forEachIndexed { i, a -> launch { delay(i * 70L); a.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 260f)) } }
        delay(total * 70L + 250)
        underline.animateTo(1f, tween(700, easing = Motion.Reveal))
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(0.dp)) {
        var index = 0
        words.forEach { line ->
            Row {
                line.forEachIndexed { wi, word ->
                    val a = anims[index++]
                    val gold = word.trim(',', '.') == goldWord
                    Text(
                        word + if (wi < line.lastIndex) " " else "",
                        style = MaterialTheme.typography.displayMedium,
                        color = if (gold) DF.Primary else DF.Text,
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = a.value.coerceIn(0f, 1f)
                                translationY = (1f - a.value) * 28.dp.toPx()
                                rotationX = (1f - a.value) * -50f
                            }
                            .then(
                                if (gold) Modifier.drawWithContent {
                                    drawContent()
                                    val p = underline.value
                                    if (p > 0f) {
                                        val y = size.height * 0.94f
                                        val path = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(0f, y)
                                            val steps = 24
                                            for (s in 1..steps) {
                                                val f = s / steps.toFloat()
                                                if (f > p) break
                                                lineTo(size.width * 0.92f * f, y + sin(f * PI.toFloat() * 2.2f) * 3.dp.toPx() - f * 4.dp.toPx())
                                            }
                                        }
                                        drawPath(path, DF.PrimaryStrong, style = Stroke(width = 3.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                                    }
                                } else Modifier,
                            ),
                    )
                }
            }
        }
    }
}
