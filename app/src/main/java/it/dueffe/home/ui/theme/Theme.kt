package it.dueffe.home.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import it.dueffe.home.R

/** Token del design system Dueffe (fondo quasi nero, superfici calde, avorio e oro). */
object DF {
    val Bg = Color(0xFF050505)
    val BgElevated = Color(0xFF0B0A08)
    val Surface = Color(0xFF11100D)
    val SurfaceHigh = Color(0xFF1A1711)
    val SurfaceHigher = Color(0xFF242016)
    val SurfaceContainer = Color(0xF012100C)
    val Text = Color(0xFFFFFAF0)
    val Muted = Color(0xFFB8AA8D)
    val Line = Color(0x29FFDC80)
    val LineStrong = Color(0x57FFD84D)
    val Primary = Color(0xFFFFD84D)
    val PrimaryStrong = Color(0xFFFFB800)
    val Secondary = Color(0xFFFFF0AD)
    val OnPrimary = Color(0xFF161006)
    val Success = Color(0xFFA8F0B0)
    val Error = Color(0xFFFF9B85)
    val Nav = Color(0xF5080705)
}

/** Curve del design system. */
object Motion {
    val Reveal = CubicBezierEasing(.22f, .78f, .16f, 1f)
    val Mark = CubicBezierEasing(.2f, .82f, .2f, 1f)
    val Letter = CubicBezierEasing(.16f, .86f, .24f, 1f)
    val Standard = CubicBezierEasing(.4f, 0f, .2f, 1f)
}

@OptIn(ExperimentalTextApi::class)
private fun bricolage(weight: Int) = Font(
    R.font.bricolage, FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

@OptIn(ExperimentalTextApi::class)
private fun inter(weight: Int) = Font(
    R.font.inter, FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val DisplayFont = FontFamily(bricolage(400), bricolage(500), bricolage(600), bricolage(700), bricolage(800))
val BodyFont = FontFamily(inter(400), inter(500), inter(600), inter(700))

/** Il font del logo ("font-dueffe"): solo nella splash e nel nome in alto nella Home. */
val DueffeFont = FontFamily(Font(R.font.font_dueffe))

private val typography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.ExtraBold, fontSize = 48.sp, lineHeight = 48.sp, letterSpacing = (-0.035).em),
    displayMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.ExtraBold, fontSize = 38.sp, lineHeight = 40.sp, letterSpacing = (-0.03).em),
    displaySmall = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-0.02).em),
    headlineMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 30.sp, letterSpacing = (-0.02).em),
    headlineSmall = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = (-0.01).em),
    titleLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp),
    titleSmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    bodyLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.04.em),
)

private val colors = darkColorScheme(
    primary = DF.Primary,
    onPrimary = DF.OnPrimary,
    primaryContainer = DF.SurfaceHigher,
    onPrimaryContainer = DF.Secondary,
    secondary = DF.Secondary,
    onSecondary = DF.OnPrimary,
    background = DF.Bg,
    onBackground = DF.Text,
    surface = DF.Surface,
    onSurface = DF.Text,
    surfaceVariant = DF.SurfaceHigh,
    onSurfaceVariant = DF.Muted,
    surfaceContainer = DF.Surface,
    surfaceContainerHigh = DF.SurfaceHigh,
    surfaceContainerHighest = DF.SurfaceHigher,
    outline = DF.LineStrong,
    outlineVariant = DF.Line,
    error = DF.Error,
)

@Composable
fun DueffeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
