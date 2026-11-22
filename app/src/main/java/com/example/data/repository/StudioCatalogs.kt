package com.example.data.repository

import com.example.R
import com.example.core.model.AspectRatioPreset
import com.example.core.model.ColorProfile
import com.example.core.model.EffectCategory
import com.example.core.model.EffectKind
import com.example.core.model.ProjectTemplate
import com.example.core.model.ResolutionPreset
import com.example.core.model.TemplateCategory
import com.example.core.model.VideoEffectPreset

object EffectsCatalogRepository {
    val presets: List<VideoEffectPreset> = listOf(
        VideoEffectPreset(
            id = "fx_cyber_neon",
            name = "Cyber Neon LUT",
            description = "Electric violet highlights with cool cyan shadow separation via Media3 RGB shader.",
            category = EffectCategory.COLOR,
            kind = EffectKind.CYBER_NEON,
            defaultIntensity = 0.85f,
            badge = "LUT"
        ),
        VideoEffectPreset(
            id = "fx_warm_cinema",
            name = "Anamorphic Warm",
            description = "Golden tungsten midtones and subtle teal low-end color balance.",
            category = EffectCategory.COLOR,
            kind = EffectKind.WARM_CINEMA,
            defaultIntensity = 0.80f,
            badge = "FILM"
        ),
        VideoEffectPreset(
            id = "fx_vivid_hue",
            name = "Chromatic Pulse",
            description = "Shifts hue and amplifies spectral vibrancy using GPU HSL transformation.",
            category = EffectCategory.COLOR,
            kind = EffectKind.VIVID_HUE,
            defaultIntensity = 0.75f,
            badge = "HSL"
        ),
        VideoEffectPreset(
            id = "fx_saturation_boost",
            name = "HDR Pop",
            description = "Punchy saturation lift tailored for high-energy reels and night footage.",
            category = EffectCategory.COLOR,
            kind = EffectKind.SATURATION_BOOST,
            defaultIntensity = 0.70f,
            badge = "VIVID"
        ),
        VideoEffectPreset(
            id = "fx_high_contrast",
            name = "Obsidian Contrast",
            description = "Deepens blacks and sharpens specular highlights with Media3 Contrast shader.",
            category = EffectCategory.TONE,
            kind = EffectKind.HIGH_CONTRAST,
            defaultIntensity = 0.65f,
            badge = "TONE"
        ),
        VideoEffectPreset(
            id = "fx_noir_shadow",
            name = "Noir Eclipse",
            description = "Moody low-key exposure curve for dramatic atmospheric sequences.",
            category = EffectCategory.TONE,
            kind = EffectKind.NOIR_SHADOW,
            defaultIntensity = 0.60f,
            badge = "DARK"
        ),
        VideoEffectPreset(
            id = "fx_grayscale",
            name = "Silver Halide Mono",
            description = "True luminance-weighted monochrome conversion via Media3 RgbFilter.",
            category = EffectCategory.TONE,
            kind = EffectKind.GRAYSCALE,
            defaultIntensity = 1.0f,
            badge = "MONO"
        ),
        VideoEffectPreset(
            id = "fx_invert",
            name = "X-Ray Negative",
            description = "Full RGB color inversion shader for futuristic glitch transitions.",
            category = EffectCategory.TONE,
            kind = EffectKind.INVERT,
            defaultIntensity = 1.0f,
            badge = "FX"
        ),
        VideoEffectPreset(
            id = "fx_rotate_90",
            name = "90° Optical Rotate",
            description = "Hardware matrix rotation for vertical/horizontal clip realignment.",
            category = EffectCategory.OPTICS,
            kind = EffectKind.ROTATE_90,
            defaultIntensity = 1.0f,
            badge = "90°"
        ),
        VideoEffectPreset(
            id = "fx_mirror_flip",
            name = "Horizontal Mirror",
            description = "Reflects frame horizontally across the vertical optical axis.",
            category = EffectCategory.OPTICS,
            kind = EffectKind.MIRROR_FLIP,
            defaultIntensity = 1.0f,
            badge = "FLIP"
        )
    )

    fun findById(id: String): VideoEffectPreset? = presets.firstOrNull { it.id == id }
}

object TemplateCatalogRepository {
    val templates: List<ProjectTemplate> = listOf(
        ProjectTemplate(
            id = "tpl_neon_reel",
            title = "Neon Pulse Reel",
            subtitle = "High-contrast cyberpunk vertical timeline engineered for 60fps kinetic cuts.",
            category = TemplateCategory.REELS,
            aspectRatio = AspectRatioPreset.RATIO_9_16,
            fps = 60,
            resolution = ResolutionPreset.FHD_1080,
            colorProfile = ColorProfile.REC_709,
            previewDrawable = R.drawable.img_template_neon,
            presetEffectIds = listOf("fx_cyber_neon", "fx_high_contrast"),
            tag = "9:16 • 60 FPS"
        ),
        ProjectTemplate(
            id = "tpl_anamorphic_road",
            title = "Anamorphic Scope 21:9",
            subtitle = "Ultra-wide 24fps filmic canvas with warm tungsten grade and deep shadows.",
            category = TemplateCategory.CINEMA,
            aspectRatio = AspectRatioPreset.RATIO_21_9,
            fps = 24,
            resolution = ResolutionPreset.UHD_4K,
            colorProfile = ColorProfile.HDR10_HLG,
            previewDrawable = R.drawable.img_template_cinema,
            presetEffectIds = listOf("fx_warm_cinema", "fx_noir_shadow"),
            tag = "21:9 • 24 FPS"
        ),
        ProjectTemplate(
            id = "tpl_prism_studio",
            title = "VYRO Prism Master",
            subtitle = "Balanced 16:9 widescreen studio configuration with HDR saturation punch.",
            category = TemplateCategory.CINEMA,
            aspectRatio = AspectRatioPreset.RATIO_16_9,
            fps = 30,
            resolution = ResolutionPreset.FHD_1080,
            colorProfile = ColorProfile.REC_709,
            previewDrawable = R.drawable.img_hero_studio,
            presetEffectIds = listOf("fx_saturation_boost"),
            tag = "16:9 • 30 FPS"
        ),
        ProjectTemplate(
            id = "tpl_noir_editorial",
            title = "Silver Editorial 4:5",
            subtitle = "Monochrome silver-halide portrait look built for fashion & architecture feeds.",
            category = TemplateCategory.EDITORIAL,
            aspectRatio = AspectRatioPreset.RATIO_4_5,
            fps = 24,
            resolution = ResolutionPreset.FHD_1080,
            colorProfile = ColorProfile.REC_709,
            previewDrawable = R.drawable.img_template_cinema,
            presetEffectIds = listOf("fx_grayscale", "fx_high_contrast"),
            tag = "4:5 • MONO"
        ),
        ProjectTemplate(
            id = "tpl_square_pulse",
            title = "Chromatic Square Cut",
            subtitle = "1:1 social showcase with vibrant HSL hue shift and crisp 60fps cadence.",
            category = TemplateCategory.REELS,
            aspectRatio = AspectRatioPreset.RATIO_1_1,
            fps = 60,
            resolution = ResolutionPreset.FHD_1080,
            colorProfile = ColorProfile.REC_709,
            previewDrawable = R.drawable.img_template_neon,
            presetEffectIds = listOf("fx_vivid_hue"),
            tag = "1:1 • 60 FPS"
        )
    )

    fun findById(id: String): ProjectTemplate? = templates.firstOrNull { it.id == id }
}
