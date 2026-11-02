package it.dueffe.home.data

import it.dueffe.home.BuildConfig
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object Http {
    private fun open(url: String, accept: String?): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 20_000
            useCaches = false
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "DueffeHome/${BuildConfig.VERSION_NAME} (Android)")
            if (accept != null) setRequestProperty("Accept", accept)
        }

    fun get(url: String, accept: String? = null): String {
        val c = open(url, accept)
        try {
            if (c.responseCode !in 200..299) throw IOException("HTTP ${c.responseCode}")
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    fun download(url: String, target: File, onProgress: (Float) -> Unit) {
        val c = open(url, "application/octet-stream")
        try {
            if (c.responseCode !in 200..299) throw IOException("HTTP ${c.responseCode}")
            val total = c.contentLengthLong.takeIf { it > 0 } ?: -1L
            var read = 0L
            var lastReport = 0L
            c.inputStream.use { input ->
                target.outputStream().use { out ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        read += n
                        if (total > 0 && read - lastReport > 128 * 1024) {
                            lastReport = read
                            onProgress(read.toFloat() / total)
                        }
                    }
                }
            }
            if (total > 0 && read != total) throw IOException("Download incompleto")
            onProgress(1f)
        } finally {
            c.disconnect()
        }
    }
}
