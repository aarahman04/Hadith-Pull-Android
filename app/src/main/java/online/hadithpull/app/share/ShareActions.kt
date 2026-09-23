package online.hadithpull.app.share

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** §3.4: lowercase, non-alphanumeric runs become '-', trim '-' from both ends. */
fun slugify(text: String): String =
    text.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "-").trim('-')

/** §3.4 file naming. The number is slugified too, so a compound number like "1645, 1646" can't put a comma into the name. */
fun cardFileName(book: String, number: String): String {
    val name = "${slugify(book)}-${slugify(number)}"
    return if (name.isEmpty() || name.startsWith("-")) "hadith.png" else "$name.png"
}

private const val CACHE_CARDS_DIR = "cards"
private const val CACHE_CLIP_DIR = "clip"

private fun fileProviderUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

private fun writePngToCacheDir(context: Context, bitmap: Bitmap, fileName: String, dirName: String): Uri {
    val dir = File(context.cacheDir, dirName)
    dir.deleteRecursively()
    dir.mkdirs()
    val file = File(dir, fileName)
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    return fileProviderUri(context, file)
}

/** §3.4: cacheDir/cards/, emptied before each write. */
suspend fun writeCardToCache(context: Context, bitmap: Bitmap, fileName: String): Uri =
    withContext(Dispatchers.IO) { writePngToCacheDir(context, bitmap, fileName, CACHE_CARDS_DIR) }

/** Every image intent: stream + clipData (so the Android 10+ chooser shows a preview) + read permission. */
private fun imageIntent(uri: Uri): Intent = Intent(Intent.ACTION_SEND).apply {
    type = "image/png"
    putExtra(Intent.EXTRA_STREAM, uri)
    clipData = ClipData.newRawUri("", uri)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

/** "Share the image": image + text, through the system chooser. */
fun shareImageChooserIntent(uri: Uri, shareText: String): Intent {
    val send = imageIntent(uri).apply {
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_TITLE, "Hadith")
    }
    return Intent.createChooser(send, "Share Hadith")
}

sealed interface SaveResult {
    data object Success : SaveResult
    data object PermissionNeeded : SaveResult
    data object Failure : SaveResult
}

private fun uniqueFileIn(dir: File, fileName: String): File {
    var candidate = File(dir, fileName)
    if (!candidate.exists()) return candidate
    val base = fileName.substringBeforeLast(".")
    val ext = fileName.substringAfterLast(".", "")
    var n = 1
    while (candidate.exists()) {
        candidate = File(dir, "$base ($n).$ext")
        n++
    }
    return candidate
}

/**
 * §3.4 "Save to device". API 29+ writes through MediaStore with no permission; API 26-28 needs
 * WRITE_EXTERNAL_STORAGE, requested by the caller (a Composable) on PermissionNeeded — this
 * function only checks whether it's already granted, since requesting one needs an Activity.
 */
suspend fun saveCardToDevice(context: Context, bitmap: Bitmap, fileName: String): SaveResult = withContext(Dispatchers.IO) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Hadith Pull")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext SaveResult.Failure
            val stream = resolver.openOutputStream(uri) ?: return@withContext SaveResult.Failure
            stream.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            SaveResult.Success
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return@withContext SaveResult.PermissionNeeded
            }
            @Suppress("DEPRECATION")
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Hadith Pull")
            dir.mkdirs()
            val file = uniqueFileIn(dir, fileName)
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
            SaveResult.Success
        }
    } catch (e: Exception) {
        SaveResult.Failure
    }
}

/** §3.4 "Copy image": cacheDir/clip/, emptied on each copy but never by the share cleanup. */
suspend fun copyCardToClipboard(context: Context, bitmap: Bitmap, fileName: String) {
    val uri = withContext(Dispatchers.IO) { writePngToCacheDir(context, bitmap, fileName, CACHE_CLIP_DIR) }
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newUri(context.contentResolver, "Hadith card", uri))
}

private fun directImageIntent(uri: Uri, packageName: String, text: String? = null): Intent =
    imageIntent(uri).apply {
        setPackage(packageName)
        if (text != null) putExtra(Intent.EXTRA_TEXT, text)
    }

/** §3.4/S2: image + text to WhatsApp, then Business WhatsApp, then the web's text-only wa.me link. */
fun shareToWhatsApp(context: Context, uri: Uri, shareText: String) {
    try {
        context.startActivity(directImageIntent(uri, "com.whatsapp", shareText))
    } catch (e: ActivityNotFoundException) {
        try {
            context.startActivity(directImageIntent(uri, "com.whatsapp.w4b", shareText))
        } catch (e2: ActivityNotFoundException) {
            val url = "https://wa.me/?text=" + Uri.encode(shareText)
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}

/** §3.4/S2: image only (Facebook drops prefilled text), then the web's sharer.php link. */
fun shareToFacebook(context: Context, uri: Uri) {
    try {
        context.startActivity(directImageIntent(uri, "com.facebook.katana"))
    } catch (e: ActivityNotFoundException) {
        val url = "https://www.facebook.com/sharer/sharer.php?u=https%3A%2F%2Fhadithpull.online"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

sealed interface InstagramShareResult {
    data object Sent : InstagramShareResult
    data class FellBackToSave(val result: SaveResult) : InstagramShareResult
}

/** §3.4/S2: Instagram's own picker, then the web's save-and-toast fallback. */
suspend fun shareToInstagram(context: Context, bitmap: Bitmap, uri: Uri, fileName: String): InstagramShareResult =
    try {
        context.startActivity(directImageIntent(uri, "com.instagram.android"))
        InstagramShareResult.Sent
    } catch (e: ActivityNotFoundException) {
        InstagramShareResult.FellBackToSave(saveCardToDevice(context, bitmap, fileName))
    }
