package com.example.media.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.core.model.AspectRatioPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.math.min

interface ProjectThumbnailGenerator {
    suspend fun generateInitialThumbnail(
        projectId: Long,
        title: String,
        aspectRatio: AspectRatioPreset,
        fps: Int,
        templateDrawableRes: Int? = null
    ): String?

    suspend fun captureMediaThumbnail(
        projectId: Long,
        mediaUriString: String,
        isVideo: Boolean
    ): String?

    suspend fun copyThumbnail(sourcePath: String?, targetProjectId: Long): String?

    suspend fun deleteThumbnail(thumbnailPath: String?)
}

class AndroidProjectThumbnailGenerator(
    private val context: Context
) : ProjectThumbnailGenerator {

    private val sequence = AtomicLong(0L)

    private val thumbnailsDir: File by lazy {
        File(context.filesDir, "thumbnails").apply { mkdirs() }
    }

    override suspend fun generateInitialThumbnail(
        projectId: Long,
        title: String,
        aspectRatio: AspectRatioPreset,
        fps: Int,
        templateDrawableRes: Int?
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = Bitmap.createBitmap(THUMB_WIDTH, THUMB_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            if (templateDrawableRes != null) {
                val decoded = decodeSampledResource(templateDrawableRes, THUMB_WIDTH, THUMB_HEIGHT)
                if (decoded != null) {
                    drawCenterCroppedBitmap(canvas, decoded, THUMB_WIDTH, THUMB_HEIGHT)
                    decoded.recycle()
                    drawVignetteAndFrame(canvas, aspectRatio, fps)
                } else {
                    drawFuturisticSlate(canvas, title, aspectRatio, fps)
                }
            } else {
                drawFuturisticSlate(canvas, title, aspectRatio, fps)
            }

            val outputFile = createOutputFile(projectId, "png")
            FileOutputStream(outputFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            bitmap.recycle()
            outputFile.absolutePath
        }.getOrNull()
    }

    override suspend fun captureMediaThumbnail(
        projectId: Long,
        mediaUriString: String,
        isVideo: Boolean
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(mediaUriString)
            val sourceBitmap = if (isVideo) {
                extractVideoFrame(uri)
            } else {
                decodeSampledImageUri(uri, THUMB_WIDTH, THUMB_HEIGHT)
            } ?: return@withContext null

            val targetBitmap = Bitmap.createBitmap(THUMB_WIDTH, THUMB_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(targetBitmap)
            drawCenterCroppedBitmap(canvas, sourceBitmap, THUMB_WIDTH, THUMB_HEIGHT)
            sourceBitmap.recycle()

            val outputFile = createOutputFile(projectId, "jpg")
            FileOutputStream(outputFile).use { stream ->
                targetBitmap.compress(Bitmap.CompressFormat.JPEG, 88, stream)
            }
            targetBitmap.recycle()
            outputFile.absolutePath
        }.getOrNull()
    }

    override suspend fun copyThumbnail(sourcePath: String?, targetProjectId: Long): String? =
        withContext(Dispatchers.IO) {
            if (sourcePath.isNullOrBlank()) return@withContext null
            runCatching {
                val sourceFile = File(sourcePath)
                if (!sourceFile.exists() || !sourceFile.isFile) return@withContext null
                val ext = sourceFile.extension.ifBlank { "png" }
                val destFile = createOutputFile(targetProjectId, ext)
                sourceFile.copyTo(destFile, overwrite = true)
                destFile.absolutePath
            }.getOrNull()
        }

    override suspend fun deleteThumbnail(thumbnailPath: String?) = withContext(Dispatchers.IO) {
        if (thumbnailPath.isNullOrBlank()) return@withContext
        runCatching {
            val target = File(thumbnailPath)
            val baseCanonical = thumbnailsDir.canonicalPath
            val targetCanonical = target.canonicalPath
            if (targetCanonical.startsWith(baseCanonical + File.separator) && target.exists()) {
                target.delete()
            }
        }
        Unit
    }

    private fun createOutputFile(projectId: Long, extension: String): File {
        thumbnailsDir.mkdirs()
        val id = sequence.incrementAndGet()
        return File(thumbnailsDir, "thumb_${projectId}_${System.currentTimeMillis()}_$id.$extension")
    }

    private fun extractVideoFrame(uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.getFrameAtTime(500_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime
        } catch (_: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun decodeSampledImageUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOpts)
        } ?: return null

        if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, reqWidth, reqHeight)
        }
        val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)
        } ?: return null

        val rotationDegrees = readExifRotation(uri)
        if (rotationDegrees == 0) return rawBitmap

        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        if (rotated != rawBitmap) {
            rawBitmap.recycle()
        }
        return rotated
    }

    private fun readExifRotation(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                when (ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun decodeSampledResource(resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resId, boundsOpts)
        if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, reqWidth, reqHeight)
        }
        return BitmapFactory.decodeResource(context.resources, resId, decodeOpts)
    }

    private fun calculateInSampleSize(srcWidth: Int, srcHeight: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun drawCenterCroppedBitmap(
        canvas: Canvas,
        source: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ) {
        val scale = max(
            targetWidth.toFloat() / source.width.toFloat(),
            targetHeight.toFloat() / source.height.toFloat()
        )
        val scaledWidth = source.width * scale
        val scaledHeight = source.height * scale
        val dx = (targetWidth - scaledWidth) / 2f
        val dy = (targetHeight - scaledHeight) / 2f

        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(dx, dy)
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(source, matrix, paint)
    }

    private fun drawVignetteAndFrame(
        canvas: Canvas,
        aspectRatio: AspectRatioPreset,
        fps: Int
    ) {
        val w = THUMB_WIDTH.toFloat()
        val h = THUMB_HEIGHT.toFloat()

        val scrimPaint = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                h,
                intArrayOf(
                    Color.argb(65, 6, 6, 10),
                    Color.argb(20, 6, 6, 10),
                    Color.argb(190, 6, 6, 10)
                ),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, scrimPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            shader = LinearGradient(
                0f,
                0f,
                w,
                h,
                COLOR_ELECTRIC_PURPLE,
                COLOR_NEON_BLUE,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(RectF(8f, 8f, w - 8f, h - 8f), 20f, 20f, borderPaint)

        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 248, 250, 252)
            textSize = 22f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("${aspectRatio.ratioLabel} • $fps FPS", w / 2f, h - 32f, badgePaint)
    }

    private fun drawFuturisticSlate(
        canvas: Canvas,
        title: String,
        aspectRatio: AspectRatioPreset,
        fps: Int
    ) {
        val w = THUMB_WIDTH.toFloat()
        val h = THUMB_HEIGHT.toFloat()

        // Base deep obsidian gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                w,
                h,
                COLOR_DEEP_BLACK,
                COLOR_GRAPHITE,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // Electric purple and neon blue radial ambient halos
        val purpleGlow = Paint().apply {
            shader = RadialGradient(
                w * 0.2f,
                h * 0.2f,
                w * 0.55f,
                Color.argb(90, 139, 92, 246),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.2f, h * 0.2f, w * 0.55f, purpleGlow)

        val blueGlow = Paint().apply {
            shader = RadialGradient(
                w * 0.85f,
                h * 0.85f,
                w * 0.50f,
                Color.argb(75, 56, 189, 248),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.85f, h * 0.85f, w * 0.50f, blueGlow)

        // Subtle studio grid lines
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 40, 45, 66)
            strokeWidth = 1.5f
        }
        val stepX = w / 8f
        val stepY = h / 5f
        for (i in 1..7) {
            canvas.drawLine(stepX * i, 0f, stepX * i, h, gridPaint)
        }
        for (j in 1..4) {
            canvas.drawLine(0f, stepY * j, w, stepY * j, gridPaint)
        }

        // Centered proportional aspect-ratio cinema frame
        val maxBoxW = w * 0.62f
        val maxBoxH = h * 0.60f
        val targetRatio = aspectRatio.floatRatio.coerceIn(0.45f, 2.4f)
        val boxW: Float
        val boxH: Float
        if (maxBoxW / maxBoxH > targetRatio) {
            boxH = maxBoxH
            boxW = boxH * targetRatio
        } else {
            boxW = maxBoxW
            boxH = boxW / targetRatio
        }
        val left = (w - boxW) / 2f
        val top = (h - boxH) / 2f - 10f
        val frameRect = RectF(left, top, left + boxW, top + boxH)

        val frameFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                frameRect.left,
                frameRect.top,
                frameRect.right,
                frameRect.bottom,
                Color.argb(55, 139, 92, 246),
                Color.argb(40, 56, 189, 248),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(frameRect, 18f, 18f, frameFill)

        val frameStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            shader = LinearGradient(
                frameRect.left,
                frameRect.top,
                frameRect.right,
                frameRect.bottom,
                COLOR_ELECTRIC_PURPLE_BRIGHT,
                COLOR_NEON_BLUE,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(frameRect, 18f, 18f, frameStroke)

        // Aspect ratio label inside frame
        val specPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = min(30f, boxH * 0.24f)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            aspectRatio.ratioLabel,
            frameRect.centerX(),
            frameRect.centerY() + specPaint.textSize * 0.35f,
            specPaint
        )

        // Bottom project spec strip
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_NEON_BLUE
            textSize = 20f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val cleanTitle = title.trim().take(22)
        val caption = if (cleanTitle.isNotEmpty()) "$cleanTitle • $fps FPS" else "$fps FPS"
        canvas.drawText(caption, w / 2f, h - 26f, subPaint)
    }

    private companion object {
        const val THUMB_WIDTH = 640
        const val THUMB_HEIGHT = 400
        const val COLOR_DEEP_BLACK = 0xFF06060A.toInt()
        const val COLOR_GRAPHITE = 0xFF12141D.toInt()
        const val COLOR_ELECTRIC_PURPLE = 0xFF8B5CF6.toInt()
        const val COLOR_ELECTRIC_PURPLE_BRIGHT = 0xFFA855F7.toInt()
        const val COLOR_NEON_BLUE = 0xFF38BDF8.toInt()
        const val COLOR_TEXT_PRIMARY = 0xFFF8FAFC.toInt()
    }
}
