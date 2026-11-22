package com.example.media.engine

import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Brightness
import androidx.media3.effect.Contrast
import androidx.media3.effect.HslAdjustment
import androidx.media3.effect.RgbAdjustment
import androidx.media3.effect.RgbFilter
import androidx.media3.effect.ScaleAndRotateTransformation
import com.example.core.model.AppliedEffect
import com.example.core.model.EffectKind
import com.example.data.repository.EffectsCatalogRepository

@OptIn(UnstableApi::class)
object Media3EffectMapper {

    fun toMedia3Effects(appliedEffects: List<AppliedEffect>): List<Effect> {
        return appliedEffects
            .sortedBy { it.orderIndex }
            .flatMap { applied ->
                val preset = EffectsCatalogRepository.findById(applied.presetId) ?: return@flatMap emptyList()
                mapPreset(preset.kind, applied.intensity)
            }
    }

    fun mapPreset(kind: EffectKind, intensity: Float): List<Effect> {
        val strength = intensity.coerceIn(0.1f, 1.0f)
        return when (kind) {
            EffectKind.GRAYSCALE -> listOf(RgbFilter.createGrayscaleFilter())
            EffectKind.INVERT -> listOf(RgbFilter.createInvertedFilter())
            EffectKind.HIGH_CONTRAST -> listOf(Contrast(0.35f * strength))
            EffectKind.WARM_CINEMA -> listOf(
                RgbAdjustment.Builder()
                    .setRedScale(1f + 0.18f * strength)
                    .setGreenScale(1f + 0.05f * strength)
                    .setBlueScale(1f - 0.14f * strength)
                    .build(),
                Contrast(0.12f * strength)
            )
            EffectKind.CYBER_NEON -> listOf(
                RgbAdjustment.Builder()
                    .setRedScale(1f + 0.15f * strength)
                    .setGreenScale(1f - 0.08f * strength)
                    .setBlueScale(1f + 0.28f * strength)
                    .build(),
                Contrast(0.22f * strength)
            )
            EffectKind.NOIR_SHADOW -> listOf(
                Brightness(-0.18f * strength),
                Contrast(0.28f * strength)
            )
            EffectKind.SATURATION_BOOST -> listOf(
                HslAdjustment.Builder()
                    .adjustSaturation(45f * strength)
                    .build()
            )
            EffectKind.VIVID_HUE -> listOf(
                HslAdjustment.Builder()
                    .adjustHue(28f * strength)
                    .adjustSaturation(30f * strength)
                    .build()
            )
            EffectKind.ROTATE_90 -> listOf(
                ScaleAndRotateTransformation.Builder()
                    .setRotationDegrees(90f)
                    .build()
            )
            EffectKind.MIRROR_FLIP -> listOf(
                ScaleAndRotateTransformation.Builder()
                    .setScale(-1f, 1f)
                    .build()
            )
        }
    }
}
