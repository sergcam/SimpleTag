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

//val LikeNavType = object : NavType<List<MusicData>>(isNullableAllowed = false) {
//    override fun get(bundle: Bundle, key: String): List<MusicData> {
//        return bundle.getSerializable(key) as List<MusicData>
//    }
//
//    override fun parseValue(value: String): List<MusicData> {
//        return Json.decodeFromString(value)
//    }
//
//    override fun serializeAsValue(value: List<MusicData>): String {
//        return Uri.encode(Json.encodeToString(value))
//    }
//
//    override fun put(bundle: Bundle, key: String, value: List<MusicData>) {
//        bundle.putSerializable(key, value as java.io.Serializable)
//    }
//
//}
