package com.wijha.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient

class WijhaApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        // ويكيميديا تطلب User-Agent واضحاً من التطبيقات
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "WijhaApp/1.0 (Android travel planner)")
                        .build(),
                )
            }
            .build()
        return ImageLoader.Builder(this)
            .okHttpClient(client)
            .crossfade(true)
            .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("images"))
                    .maxSizeBytes(150L * 1024 * 1024)
                    .build()
            }
            .build()
    }
}
