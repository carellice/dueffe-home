package it.dueffe.home.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import it.dueffe.home.R
import it.dueffe.home.data.Project
import it.dueffe.home.ui.theme.DF
import it.dueffe.home.ui.theme.DisplayFont
import it.dueffe.home.ui.theme.Motion
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

// ---------- Interazioni e movimento ----------

/** Click con "molla": l'elemento si comprime mentre è premuto e rimbalza al rilascio. */
fun Modifier.pressScale(pressedScale: Float = 0.94f, onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) pressedScale else 1f,
        spring(dampingRatio = if (pressed) 1f else 0.42f, stiffness = if (pressed) 900f else 380f),
        label = "press",
    )
    graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = source, indication = null, onClick = onClick)
}

/** Entrata a cascata: dissolvenza, salita e leggero zoom con ritardo per indice. */
fun Modifier.reveal(index: Int, step: Long = 55): Modifier = composed {
    val p = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index.coerceAtMost(10) * step)
        p.animateTo(1f, tween(620, easing = Motion.Letter))
    }
    graphicsLayer {
        alpha = p.value
        translationY = (1f - p.value) * 42.dp.toPx()
        val s = 0.94f + 0.06f * p.value
        scaleX = s; scaleY = s
    }
}

/** Riflesso dorato che scorre, usato come segnaposto di caricamento. */
fun Modifier.shimmer(): Modifier = composed {
    val t = rememberInfiniteTransition(label = "shimmer")
    val x by t.animateFloat(-1f, 2f, infiniteRepeatable(tween(1400, easing = LinearEasing)), label = "x")
    drawBehind {
        val w = size.width
        drawRect(
            Brush.linearGradient(
                listOf(DF.SurfaceHigh, DF.SurfaceHigher, DF.SurfaceHigh),
                start = Offset(w * x - w, 0f), end = Offset(w * x, size.height),
            ),
        )
    }
}

// ---------- Elementi di base ----------

@Composable
fun ProjectIcon(project: Project, size: Dp, modifier: Modifier = Modifier, corner: Dp = size * 0.27f) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier.size(size).clip(shape).background(DF.SurfaceHigh).border(1.dp, DF.Line, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (project.icon != null) {
            SubcomposeAsyncImage(
                model = project.icon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                loading = { Box(Modifier.fillMaxSize().shimmer()) },
                error = { Monogram(project, size) },
            )
        } else {
            Monogram(project, size)
        }
    }
}

@Composable
private fun Monogram(project: Project, size: Dp) {
    val hue = (abs(project.id.hashCode()) % 360).toFloat()
    val a = Color.hsv(hue, 0.55f, 0.55f)
    val b = Color.hsv((hue + 40f) % 360f, 0.7f, 0.22f)
    Box(
        Modifier.fillMaxSize().background(Brush.linearGradient(listOf(a, b))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            project.name.trim().take(1).uppercase(),
            fontFamily = DisplayFont, fontWeight = FontWeight.ExtraBold,
            fontSize = (size.value * 0.46f).sp, color = DF.Text,
        )
    }
}

@Composable
fun FfMark(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.ff_mark), contentDescription = null, modifier = modifier)
}

@Composable
fun DfChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(if (selected) DF.Primary else DF.Surface, tween(260), label = "chipBg")
    val fg by animateColorAsState(if (selected) DF.OnPrimary else DF.Text, tween(260), label = "chipFg")
    val border by animateColorAsState(if (selected) DF.Primary else DF.Line, tween(260), label = "chipBorder")
    Row(
        modifier
            .pressScale(0.92f, onClick)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

@Composable
fun GoldButton(
    text: String,
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    accent: Color = DF.Primary,
    dense: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(if (dense) 14.dp else 18.dp)
    val onAccent = if (luminance(accent) > 0.45f) DF.OnPrimary else DF.Text
    Row(
        modifier
            .pressScale(0.95f, onClick)
            .clip(shape)
            .then(
                if (filled) Modifier.background(Brush.linearGradient(listOf(accent, lerpColor(accent, DF.PrimaryStrong, 0.35f))))
                else Modifier.background(DF.Surface).border(1.dp, DF.LineStrong, shape),
            )
            .then(if (dense) Modifier.padding(horizontal = 8.dp, vertical = 9.dp) else Modifier.padding(horizontal = 18.dp, vertical = 14.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val fg = if (filled) onAccent else DF.Text
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge.copy(fontSize = if (dense) 13.sp else 15.sp), color = fg, maxLines = 1)
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = DF.Text)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = DF.Muted)
        }
        if (action != null && onAction != null) {
            Row(
                Modifier.pressScale(0.9f, onAction).clip(CircleShape).padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(action, style = MaterialTheme.typography.labelLarge, color = DF.Primary)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = DF.Primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun PlatformRow(platforms: List<String>, modifier: Modifier = Modifier, tint: Color = DF.Muted, size: Dp = 14.dp) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        platforms.forEach { Icon(platformIcon(it), platformLabel(it), tint = tint, modifier = Modifier.size(size)) }
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, color: Color = DF.Primary, shape: Shape = CircleShape) {
    Text(
        text,
        modifier
            .clip(shape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.32f), shape)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun CardSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    padding: PaddingValues = PaddingValues(18.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(DF.SurfaceHigh, DF.Surface)))
            .border(1.dp, DF.Line, shape)
            .padding(padding),
    ) { content() }
}

@Composable
fun VerticalGap(h: Dp) = Spacer(Modifier.height(h))

// ---------- Colore d'accento estratto dall'icona ----------

@Composable
fun rememberAccent(url: String?): Color {
    val context = LocalContext.current
    var color by remember(url) { mutableStateOf(DF.Primary) }
    LaunchedEffect(url) {
        if (url == null) return@LaunchedEffect
        val request = ImageRequest.Builder(context).data(url).allowHardware(false).size(48).build()
        val result = context.imageLoader.execute(request)
        if (result is SuccessResult) {
            runCatching { dominantColor(result.drawable.toBitmap(32, 32)) }.getOrNull()?.let { color = it }
        }
    }
    return animateColorAsState(color, tween(700), label = "accent").value
}

private fun dominantColor(bitmap: Bitmap): Color? {
    var r = 0f; var g = 0f; var b = 0f; var weight = 0f
    val hsv = FloatArray(3)
    for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
        val px = bitmap.getPixel(x, y)
        if ((px ushr 24) < 160) continue
        android.graphics.Color.colorToHSV(px, hsv)
        if (hsv[1] < 0.25f || hsv[2] < 0.3f) continue
        val w = hsv[1] * hsv[2]
        r += ((px shr 16) and 0xFF) * w; g += ((px shr 8) and 0xFF) * w; b += (px and 0xFF) * w; weight += w
    }
    if (weight < 6f) return null
    val avg = android.graphics.Color.rgb((r / weight).toInt(), (g / weight).toInt(), (b / weight).toInt())
    android.graphics.Color.colorToHSV(avg, hsv)
    hsv[1] = hsv[1].coerceIn(0.35f, 0.8f)
    hsv[2] = hsv[2].coerceAtLeast(0.88f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

fun luminance(c: Color): Float = 0.2126f * c.red + 0.7152f * c.green + 0.0722f * c.blue
fun lerpColor(a: Color, b: Color, t: Float) = Color(
    a.red + (b.red - a.red) * t, a.green + (b.green - a.green) * t,
    a.blue + (b.blue - a.blue) * t, a.alpha + (b.alpha - a.alpha) * t,
)

// ---------- Testi e formati ----------

fun platformIcon(p: String): ImageVector = when (p) {
    "android" -> Icons.Rounded.Android
    "windows" -> Icons.Rounded.DesktopWindows
    "macos" -> Icons.Rounded.LaptopMac
    "web" -> Icons.Rounded.Language
    else -> Icons.Rounded.Widgets
}

fun platformLabel(p: String): String = when (p) {
    "android" -> "Android"
    "windows" -> "Windows"
    "macos" -> "macOS"
    "web" -> "Web"
    else -> p
}

fun categoryIcon(id: String): ImageVector = when (id) {
    "android" -> Icons.Rounded.PhoneAndroid
    "giochi" -> Icons.Rounded.SportsEsports
    "desktop" -> Icons.Rounded.DesktopWindows
    "web" -> Icons.Rounded.Language
    "sviluppo" -> Icons.Rounded.Terminal
    else -> Icons.Rounded.Widgets
}

private val italian = Locale.ITALIAN

fun relativeDate(iso: String?): String {
    val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
    val days = Duration.between(instant, Instant.now()).toDays()
    return when {
        days < 1 -> "oggi"
        days < 2 -> "ieri"
        days < 7 -> "$days giorni fa"
        days < 30 -> "${days / 7} sett. fa"
        days < 365 -> if (days / 30 == 1L) "1 mese fa" else "${days / 30} mesi fa"
        else -> if (days / 365 == 1L) "1 anno fa" else "${days / 365} anni fa"
    }
}

fun fullDate(iso: String?): String {
    val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
    return DateTimeFormatter.ofPattern("d MMMM yyyy", italian).format(instant.atZone(ZoneId.systemDefault()))
}

fun formatSize(bytes: Long): String = when {
    bytes <= 0 -> ""
    bytes < 1024 * 1024 -> String.format(italian, "%.0f KB", bytes / 1024.0)
    else -> String.format(italian, "%.1f MB", bytes / 1024.0 / 1024.0)
}

fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) { }
}

fun shareText(context: Context, title: String, text: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(send, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Respiro lento per gli elementi decorativi (0..1..0). */
@Composable
fun breathing(periodMs: Int = 3200): Float {
    val t = rememberInfiniteTransition(label = "breath")
    return t.animateFloat(0f, 1f, infiniteRepeatable(tween(periodMs, easing = Motion.Standard), RepeatMode.Reverse), label = "b").value
}
