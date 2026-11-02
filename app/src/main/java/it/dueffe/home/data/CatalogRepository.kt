package it.dueffe.home.data

import android.content.Context
import it.dueffe.home.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Il catalogo vive su GitHub Pages ed è rigenerato da una GitHub Action.
 * L'APK ne include una copia per il primo avvio offline; l'ultima copia scaricata resta in cache.
 */
class CatalogRepository(private val context: Context) {
    private val cacheFile = File(context.filesDir, "catalog.json")

    suspend fun loadLocal(): Catalog = withContext(Dispatchers.IO) {
        val bundled = Catalog.parse(context.assets.open("catalog.json").bufferedReader().use { it.readText() })
        val cached = runCatching { Catalog.parse(cacheFile.readText()) }.getOrNull()
        if (cached != null && cached.generatedAt >= bundled.generatedAt) cached else bundled
    }

    suspend fun refresh(): Catalog = withContext(Dispatchers.IO) {
        var failure: Exception? = null
        // Il parametro cambia ogni minuto per scavalcare la cache CDN di Pages.
        val stamp = System.currentTimeMillis() / 60_000
        for (url in listOf(BuildConfig.CATALOG_URL, BuildConfig.CATALOG_FALLBACK_URL)) {
            try {
                val text = Http.get("$url?t=$stamp", "application/json")
                val catalog = Catalog.parse(text)
                cacheFile.writeText(text)
                return@withContext catalog
            } catch (e: Exception) {
                failure = e
            }
        }
        throw failure ?: IOException("Catalogo non disponibile")
    }
}
