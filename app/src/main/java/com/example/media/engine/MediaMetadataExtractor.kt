package com.example.media.engine

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.core.model.MediaPickCandidate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaMetadataExtractor(private val context: Context) {

    suspend fun extractMetadata(uri: Uri): MediaPickCandidate = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Photo Picker URIs may grant temporary read access without persistable flag.
        } catch (_: UnsupportedOperationException) {
            // Ignore when provider does not support persistable URI permissions.
        }

        val mimeType = context.contentResolver.getType(uri) ?: "video/mp4"
        val isVideo = mimeType.startsWith("video/")
        val displayName = queryDisplayName(uri) ?: if (isVideo) "Clip.mp4" else "Frame.jpg"

        var durationMs = if (isVideo) 5_000L else 4_000L
        var width = 1920
        var height = 1080

        if (isVideo) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val wStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val hStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                durationMs = durStr?.toLongOrNull()?.coerceAtLeast(1_000L) ?: 5_000L
                width = wStr?.toIntOrNull()?.coerceAtLeast(1) ?: 1920
                height = hStr?.toIntOrNull()?.coerceAtLeast(1) ?: 1080
            } catch (_: Exception) {
                // Keep safe defaults when metadata header is incomplete
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {
                }
            }
        } else {
            try {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, opts)
                }
                if (opts.outWidth > 0 && opts.outHeight > 0) {
                    width = opts.outWidth
                    height = opts.outHeight
                }
            } catch (_: Exception) {
            }
        }

        MediaPickCandidate(
            uriString = uri.toString(),
            displayName = displayName,
            mimeType = mimeType,
            isVideo = isVideo,
            durationMs = durationMs,
            width = width,
            height = height
        )
    }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx) else null
                    } else null
                }
        } catch (_: Exception) {
            null
        }
    }
}
