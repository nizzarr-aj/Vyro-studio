package com.example.core.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.R

enum class AspectRatioPreset(
    val id: String,
    @StringRes val labelRes: Int,
    val ratioLabel: String,
    val widthRatio: Int,
    val heightRatio: Int
) {
    RATIO_9_16("9:16", R.string.aspect_9_16, "9:16", 9, 16),
    RATIO_16_9("16:9", R.string.aspect_16_9, "16:9", 16, 9),
    RATIO_1_1("1:1", R.string.aspect_1_1, "1:1", 1, 1),
    RATIO_4_5("4:5", R.string.aspect_4_5, "4:5", 4, 5),
    RATIO_21_9("21:9", R.string.aspect_21_9, "21:9", 21, 9);

    val floatRatio: Float get() = widthRatio.toFloat() / heightRatio.toFloat()

    companion object {
        fun fromId(id: String): AspectRatioPreset =
            entries.firstOrNull { it.id == id } ?: RATIO_16_9
    }
}

enum class FrameRateOption(val fps: Int, val label: String) {
    FPS_24(24, "24 fps"),
    FPS_30(30, "30 fps"),
    FPS_60(60, "60 fps");

    companion object {
        fun fromFps(fps: Int): FrameRateOption =
            entries.firstOrNull { it.fps == fps } ?: FPS_30
    }
}

enum class ResolutionPreset(
    val id: String,
    val shortEdge: Int,
    val label: String,
    val detail: String
) {
    HD_720("720p", 720, "720p HD", "1280×720"),
    FHD_1080("1080p", 1080, "1080p FHD", "1920×1080"),
    UHD_4K("4k", 2160, "4K UHD", "3840×2160");

    companion object {
        fun fromId(id: String): ResolutionPreset =
            entries.firstOrNull { it.id == id } ?: FHD_1080
    }
}

enum class ColorProfile(val id: String, val label: String) {
    REC_709("rec709", "Rec.709 SDR"),
    HDR10_HLG("hdr10", "HDR10 HLG");

    companion object {
        fun fromId(id: String): ColorProfile =
            entries.firstOrNull { it.id == id } ?: REC_709
    }
}

enum class VideoCodecOption(
    val id: String,
    val mimeType: String,
    val label: String
) {
    H264("h264", "video/avc", "H.264 / AVC"),
    H265("h265", "video/hevc", "H.265 / HEVC");

    companion object {
        fun fromId(id: String): VideoCodecOption =
            entries.firstOrNull { it.id == id } ?: H264
    }
}

enum class BitratePreset(
    val id: String,
    val mbps: Int,
    @StringRes val labelRes: Int
) {
    STANDARD("standard", 8, R.string.bitrate_standard),
    HIGH("high", 18, R.string.bitrate_high),
    MASTER("master", 35, R.string.bitrate_master);
}

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val isRtl: Boolean
) {
    ENGLISH("en", "English", false),
    FRENCH("fr", "Français", false),
    ARABIC("ar", "العربية", true);

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

data class TimelineClip(
    val id: Long = 0,
    val projectId: Long,
    val uriString: String,
    val displayName: String,
    val isVideo: Boolean,
    val durationMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = durationMs,
    val width: Int = 1920,
    val height: Int = 1080,
    val orderIndex: Int = 0,
    val playbackSpeed: Float = 1.0f
) {
    val effectiveDurationMs: Long
        get() = ((trimEndMs - trimStartMs).coerceAtLeast(500L) / playbackSpeed).toLong()
}

enum class EffectCategory {
    COLOR,
    TONE,
    OPTICS
}

enum class EffectKind {
    GRAYSCALE,
    INVERT,
    HIGH_CONTRAST,
    WARM_CINEMA,
    CYBER_NEON,
    NOIR_SHADOW,
    SATURATION_BOOST,
    VIVID_HUE,
    ROTATE_90,
    MIRROR_FLIP
}

data class VideoEffectPreset(
    val id: String,
    val name: String,
    val description: String,
    val category: EffectCategory,
    val kind: EffectKind,
    val defaultIntensity: Float = 1.0f,
    val badge: String
)

data class AppliedEffect(
    val id: Long = 0,
    val projectId: Long,
    val presetId: String,
    val intensity: Float = 1.0f,
    val orderIndex: Int = 0
)

data class VideoProject(
    val id: Long = 0,
    val title: String,
    val aspectRatio: AspectRatioPreset = AspectRatioPreset.RATIO_16_9,
    val fps: Int = 30,
    val resolution: ResolutionPreset = ResolutionPreset.FHD_1080,
    val colorProfile: ColorProfile = ColorProfile.REC_709,
    val isMuted: Boolean = false,
    val templateId: String? = null,
    val thumbnailPath: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val clips: List<TimelineClip> = emptyList(),
    val appliedEffects: List<AppliedEffect> = emptyList()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.effectiveDurationMs }
}

enum class TemplateCategory {
    REELS,
    CINEMA,
    EDITORIAL
}

data class ProjectTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: TemplateCategory,
    val aspectRatio: AspectRatioPreset,
    val fps: Int,
    val resolution: ResolutionPreset,
    val colorProfile: ColorProfile,
    @DrawableRes val previewDrawable: Int,
    val presetEffectIds: List<String>,
    val tag: String
)

data class UserPreferences(
    val creatorName: String = "Alex Mercer",
    val creatorHandle: String = "@vyro.director",
    val language: AppLanguage = AppLanguage.ENGLISH,
    val defaultResolution: ResolutionPreset = ResolutionPreset.FHD_1080,
    val defaultFps: Int = 30,
    val preferHevc: Boolean = false
)

sealed interface ExportJobState {
    data object Idle : ExportJobState
    data object Preparing : ExportJobState
    data class Exporting(val progressPercent: Int, val elapsedMs: Long) : ExportJobState
    data class Completed(val outputPath: String, val sizeBytes: Long) : ExportJobState
    data class Failed(val message: String) : ExportJobState
}

data class MediaPickCandidate(
    val uriString: String,
    val displayName: String,
    val mimeType: String,
    val isVideo: Boolean,
    val durationMs: Long,
    val width: Int,
    val height: Int
)
