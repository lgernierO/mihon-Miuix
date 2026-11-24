package eu.kanade.tachiyomi.data.coil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.extension.util.ExtensionLoader

/**
 * The icon of an installed extension, drawn at [density].
 */
data class InstalledExtensionIcon(
    val pkgName: String,
    val versionCode: Long,
    val density: Int,
)

/**
 * The icon of the installed extension that provides the source with [sourceId].
 */
data class InstalledSourceIcon(val sourceId: Long)

class ExtensionIconFetcher(
    private val context: Context,
    private val getPkgName: suspend () -> String?,
    private val density: Int?,
    private val trimPadding: Boolean = false,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val pkgName = getPkgName() ?: error("No installed extension")
        val appInfo = ExtensionLoader.getExtensionPackageInfoFromPkgName(context, pkgName)?.applicationInfo
            ?: error("Extension $pkgName isn't installed")
        val drawable: Drawable = if (density == null) {
            appInfo.loadIcon(context.packageManager)
        } else {
            context.packageManager.getResourcesForApplication(appInfo)
                .getDrawableForDensity(appInfo.icon, density, null)
                ?: error("Extension $pkgName has no icon")
        }
        // Adaptive icons aren't bitmaps, and only bitmaps are kept in the memory cache.
        val bitmap = drawable.toBitmap()
        return ImageFetchResult(
            image = (if (trimPadding) bitmap.trimTransparentPadding() else bitmap).asImage(),
            isSampled = false,
            dataSource = DataSource.DISK,
        )
    }

    class ExtensionFactory(private val context: Context) : Fetcher.Factory<InstalledExtensionIcon> {
        override fun create(data: InstalledExtensionIcon, options: Options, imageLoader: ImageLoader): Fetcher {
            return ExtensionIconFetcher(context, { data.pkgName }, data.density)
        }
    }

    class SourceFactory(
        private val context: Context,
        private val extensionManager: Lazy<ExtensionManager>,
    ) : Fetcher.Factory<InstalledSourceIcon> {
        override fun create(data: InstalledSourceIcon, options: Options, imageLoader: ImageLoader): Fetcher {
            return ExtensionIconFetcher(
                context = context,
                getPkgName = { extensionManager.value.getExtensionPackage(data.sourceId) },
                density = null,
                trimPadding = true,
            )
        }
    }
}

/** Remove only transparent outer pixels so the source row clips the artwork, not its empty border. */
private fun Bitmap.trimTransparentPadding(): Bitmap {
    if (!hasAlpha()) return this
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)
    var left = width
    var top = height
    var right = -1
    var bottom = -1
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (pixels[y * width + x] ushr 24 == 0) continue
            left = minOf(left, x)
            top = minOf(top, y)
            right = maxOf(right, x)
            bottom = maxOf(bottom, y)
        }
    }
    if (right < left || bottom < top) return this
    if (left == 0 && top == 0 && right == width - 1 && bottom == height - 1) return this
    return Bitmap.createBitmap(this, left, top, right - left + 1, bottom - top + 1)
}

class InstalledExtensionIconKeyer : Keyer<InstalledExtensionIcon> {
    override fun key(data: InstalledExtensionIcon, options: Options): String {
        return "extension;${data.pkgName};${data.versionCode};${data.density}"
    }
}

class InstalledSourceIconKeyer : Keyer<InstalledSourceIcon> {
    override fun key(data: InstalledSourceIcon, options: Options): String {
        return "source;${data.sourceId};trimmed-v1"
    }
}
