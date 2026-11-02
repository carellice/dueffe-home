package it.dueffe.home.data

import org.json.JSONArray
import org.json.JSONObject

data class Catalog(
    val generatedAt: String,
    val owner: Owner,
    val categories: List<Category>,
    val projects: List<Project>,
    val gists: List<Gist>,
) {
    val featured: List<Project> get() = projects.filter { it.featured }
    fun inCategory(id: String) = projects.filter { it.category == id }
    fun category(id: String) = categories.firstOrNull { it.id == id }

    companion object {
        fun parse(text: String): Catalog {
            val o = JSONObject(text)
            val owner = o.getJSONObject("owner")
            return Catalog(
                generatedAt = o.getString("generatedAt"),
                owner = Owner(owner.getString("login"), owner.getString("name"), owner.getString("avatar"), owner.getString("url")),
                categories = o.getJSONArray("categories").objects().map {
                    Category(it.getString("id"), it.getString("title"), it.getString("subtitle"))
                },
                projects = o.getJSONArray("projects").objects().map(::parseProject),
                gists = o.getJSONArray("gists").objects().map(::parseGist),
            )
        }

        private fun parseProject(p: JSONObject) = Project(
            id = p.getString("id"),
            name = p.getString("name"),
            description = p.getString("description"),
            about = p.str("about"),
            features = p.optJSONArray("features").strings(),
            category = p.getString("category"),
            platforms = p.optJSONArray("platforms").strings(),
            language = p.str("language"),
            topics = p.optJSONArray("topics").strings(),
            stars = p.optInt("stars"),
            forks = p.optInt("forks"),
            license = p.str("license"),
            createdAt = p.str("createdAt"),
            updatedAt = p.str("updatedAt"),
            repoUrl = p.getString("repoUrl"),
            webUrl = p.str("webUrl"),
            icon = p.str("icon"),
            screenshots = p.optJSONArray("screenshots").strings(),
            video = p.str("video"),
            links = p.optJSONArray("links")?.objects()?.map {
                ProjectLink(it.getString("type"), it.getString("label"), it.getString("url"))
            }.orEmpty(),
            release = p.optJSONObject("release")?.let { r ->
                Release(
                    tag = r.getString("tag"),
                    name = r.getString("name"),
                    date = r.str("date"),
                    url = r.getString("url"),
                    notes = r.str("notes").orEmpty(),
                    assets = r.getJSONArray("assets").objects().map {
                        ReleaseAsset(it.getString("name"), it.getString("url"), it.optLong("size"), it.optInt("downloads"))
                    },
                )
            },
            featured = p.optBoolean("featured"),
            dueffe = p.optBoolean("dueffe"),
        )

        private fun parseGist(g: JSONObject) = Gist(
            id = g.getString("id"),
            description = g.getString("description"),
            url = g.getString("url"),
            createdAt = g.str("createdAt"),
            updatedAt = g.str("updatedAt"),
            files = g.getJSONArray("files").objects().map {
                GistFile(it.getString("name"), it.str("language"), it.optLong("size"), it.getString("rawUrl"), it.str("preview").orEmpty())
            },
        )
    }
}

data class Owner(val login: String, val name: String, val avatar: String, val url: String)
data class Category(val id: String, val title: String, val subtitle: String)
data class ProjectLink(val type: String, val label: String, val url: String)

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val about: String?,
    val features: List<String>,
    val category: String,
    val platforms: List<String>,
    val language: String?,
    val topics: List<String>,
    val stars: Int,
    val forks: Int,
    val license: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val repoUrl: String,
    val webUrl: String?,
    val icon: String?,
    val screenshots: List<String>,
    val video: String?,
    val links: List<ProjectLink>,
    val release: Release?,
    val featured: Boolean,
    val dueffe: Boolean,
) {
    val apk: ReleaseAsset? get() = release?.assets?.firstOrNull { it.name.endsWith(".apk", true) }
    val primaryDownload: ReleaseAsset? get() = apk ?: release?.assets?.firstOrNull()
}

data class Release(
    val tag: String,
    val name: String,
    val date: String?,
    val url: String,
    val notes: String,
    val assets: List<ReleaseAsset>,
)

data class ReleaseAsset(val name: String, val url: String, val size: Long, val downloads: Int)

data class Gist(
    val id: String,
    val description: String,
    val url: String,
    val createdAt: String?,
    val updatedAt: String?,
    val files: List<GistFile>,
)

data class GistFile(val name: String, val language: String?, val size: Long, val rawUrl: String, val preview: String)

internal fun JSONObject.str(key: String): String? = if (!has(key) || isNull(key)) null else getString(key)
internal fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
internal fun JSONArray?.strings(): List<String> = if (this == null) emptyList() else (0 until length()).map { getString(it) }
