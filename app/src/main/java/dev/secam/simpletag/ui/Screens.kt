package dev.secam.simpletag.ui

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import dev.secam.simpletag.data.MusicData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
object Selector

@Serializable
object Settings

@Serializable
object About

@Serializable
data class Editor(
    val musicList: List<MusicData>
)

inline fun <reified T> navTypeOf(
    isNullableAllowed: Boolean = false,
    json: Json = Json,
) = object : NavType<T>(isNullableAllowed = isNullableAllowed) {
    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let(json::decodeFromString)

    override fun parseValue(value: String): T = json.decodeFromString(Uri.decode(value))

    override fun serializeAsValue(value: T): String = Uri.encode(json.encodeToString(value))

    override fun put(bundle: Bundle, key: String, value: T) =
        bundle.putString(key, json.encodeToString(value))

}

