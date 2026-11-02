package it.dueffe.home.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.dueffe.home.data.Project

/** Ambiti necessari per l'elemento condiviso (l'icona vola dalla lista al dettaglio). */
@OptIn(ExperimentalSharedTransitionApi::class)
class SharedScopes(val transition: SharedTransitionScope, val visibility: AnimatedVisibilityScope)

/** Da dove è stato aperto il dettaglio: rende unica la chiave dell'icona condivisa. */
data class Opened(val project: Project, val origin: String)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedIcon(scopes: SharedScopes?, origin: String, id: String): Modifier {
    if (scopes == null) return this
    return with(scopes.transition) {
        this@sharedIcon.sharedElement(
            rememberSharedContentState("icon/$origin/$id"),
            scopes.visibility,
            boundsTransform = { _, _ -> spring(dampingRatio = 0.82f, stiffness = 320f) },
        )
    }
}
