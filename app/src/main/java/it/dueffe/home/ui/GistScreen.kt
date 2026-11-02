package it.dueffe.home.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.dueffe.home.AppViewModel
import it.dueffe.home.data.Gist
import it.dueffe.home.data.Http
import it.dueffe.home.ui.theme.DF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GistScreen(gists: List<Gist>, vm: AppViewModel, listState: LazyListState, bottomPadding: Dp) {
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = bottomPadding)) {
        item(key = "head") {
            Column(Modifier.statusBarsPadding().padding(horizontal = 20.dp).padding(top = 18.dp, bottom = 10.dp)) {
                Text("Gist", style = MaterialTheme.typography.displaySmall, color = DF.Text)
                Text("Script e piccoli strumenti condivisi su GitHub Gist", style = MaterialTheme.typography.bodyMedium, color = DF.Muted)
            }
        }
        itemsIndexed(gists, key = { _, g -> g.id }) { i, g -> GistCard(g, vm, Modifier.reveal(i)) }
    }
}

@Composable
private fun GistCard(g: Gist, vm: AppViewModel, modifier: Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by rememberSaveable(g.id) { mutableStateOf(false) }
    val arrow by animateFloatAsState(if (expanded) 180f else 0f, spring(dampingRatio = 0.6f), label = "arrow")
    val file = g.files.first()
    CardSurface(modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(), padding = PaddingValues(0.dp)) {
        Column(Modifier.animateContentSize(spring(dampingRatio = 0.85f, stiffness = 320f))) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(DF.SurfaceHigher)
                        .border(1.dp, DF.LineStrong, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Description, null, tint = DF.Primary, modifier = Modifier.size(22.dp)) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(file.name, style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 15.sp), color = DF.Text)
                    VerticalGap(4.dp)
                    Text(g.description, style = MaterialTheme.typography.bodyMedium, color = DF.Muted)
                    VerticalGap(8.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        file.language?.let { Pill(it) }
                        Pill(formatSize(file.size), color = DF.Muted)
                        Pill(relativeDate(g.updatedAt), color = DF.Muted)
                    }
                }
            }
            // Anteprima del codice: si espande con una molla.
            val lines = file.preview.lines()
            val shown = if (expanded) lines else lines.take(6)
            Box(
                Modifier.padding(horizontal = 12.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(DF.Bg).border(1.dp, DF.Line, RoundedCornerShape(16.dp)),
            ) {
                Column(Modifier.horizontalScroll(rememberScrollState()).padding(14.dp)) {
                    shown.forEachIndexed { i, line ->
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = DF.Muted.copy(alpha = 0.45f))) { append((i + 1).toString().padStart(3) + "  ") }
                                append(highlight(line))
                            },
                            fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, lineHeight = 17.sp, color = DF.Text.copy(alpha = 0.88f),
                            softWrap = false,
                        )
                    }
                }
            }
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                GistAction(Icons.Rounded.ContentCopy, "Copia") {
                    scope.launch {
                        val text = runCatching { withContext(Dispatchers.IO) { Http.get(file.rawUrl) } }.getOrNull()
                        if (text != null) {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText(file.name, text))
                            vm.toast("${file.name} copiato negli appunti")
                        } else vm.toast("Copia non riuscita: sei offline?")
                    }
                }
                GistAction(Icons.AutoMirrored.Rounded.OpenInNew, "GitHub") { openUrl(context, g.url) }
                Spacer(Modifier.weight(1f))
                if (lines.size > 6) {
                    Row(
                        Modifier.clip(CircleShape).pressScale(0.9f) { expanded = !expanded }.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(if (expanded) "Riduci" else "Mostra tutto", style = MaterialTheme.typography.labelLarge, color = DF.Primary)
                        Icon(Icons.Rounded.ExpandMore, null, tint = DF.Primary, modifier = Modifier.graphicsLayer { rotationZ = arrow })
                    }
                }
            }
        }
    }
}

@Composable
private fun GistAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.pressScale(0.92f, onClick).clip(CircleShape).background(DF.SurfaceHigher)
            .border(1.dp, DF.Line, CircleShape).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = DF.Text, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = DF.Text)
    }
}

private val keywords = Regex("""(?i)\b(echo|set|if|else|goto|call|exit|for|do|in|not|exist|pause|rem|setlocal|endlocal|start|choice|errorlevel|cls|title)\b""")

/** Evidenziazione minima per script batch: commenti, etichette, variabili e parole chiave. */
private fun highlight(line: String) = buildAnnotatedString {
    val trimmed = line.trimStart()
    when {
        trimmed.startsWith("::") || trimmed.lowercase().startsWith("rem ") ->
            withStyle(SpanStyle(color = DF.Muted.copy(alpha = 0.7f))) { append(line) }
        trimmed.startsWith(":") -> withStyle(SpanStyle(color = DF.PrimaryStrong)) { append(line) }
        else -> {
            var last = 0
            val tokens = Regex("""%[^%\s]+%|!\w+!|"[^"]*"""").findAll(line).toList() + keywords.findAll(line).toList()
            tokens.sortedBy { it.range.first }.forEach { m ->
                if (m.range.first < last) return@forEach
                append(line.substring(last, m.range.first))
                val color = when {
                    m.value.startsWith("%") || m.value.startsWith("!") -> DF.Success
                    m.value.startsWith("\"") -> DF.Secondary
                    else -> DF.Primary
                }
                withStyle(SpanStyle(color = color)) { append(m.value) }
                last = m.range.last + 1
            }
            append(line.substring(last))
        }
    }
}
