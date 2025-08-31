package dev.secam.simpletag

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import dev.secam.simpletag.data.MusicDataFetcher
import dev.secam.simpletag.data.MusicDataKeyer

@HiltAndroidApp
class SimpleTag : Application(), SingletonImageLoader.Factory {
    //    @Inject
//    lateinit var preferencesRepo: PreferencesRepo
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(5 * 1024 * 1024)
                    .build()
            }
            .logger(DebugLogger())
            .components {
                add(MusicDataKeyer)
                add(
                    MusicDataFetcher.Factory()
                )
            }
//            .crossfade(true)
            .build()
    }
    override fun onCreate() {
        super.onCreate()
    }
}