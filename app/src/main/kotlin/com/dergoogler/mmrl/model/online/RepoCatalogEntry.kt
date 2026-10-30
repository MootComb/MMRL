package com.dergoogler.mmrl.model.online

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RepoCatalogEntry(
    val name: String,
    val description: String? = null,
    val author: String? = null,
    val repoUrl: String,
    val license: String? = null,
) {
    fun toOnlineModule(): OnlineModule =
        OnlineModule(
            repoUrl = repoUrl,
            id = repoUrl,
            name = name,
            version = "",
            versionCode = 0,
            author = author.orEmpty(),
            description = description,
            license = license ?: "",
            track = TrackJson(
                typeName = "ONLINE_JSON",
                added = 0f,
                source = repoUrl,
                antifeatures = emptyList(),
            ),
            versions = emptyList(),
        )
}
