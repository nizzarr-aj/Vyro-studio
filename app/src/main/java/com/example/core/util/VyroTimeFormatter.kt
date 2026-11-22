package com.example.core.util

import java.util.Locale
import kotlin.math.roundToInt

object VyroTimeFormatter {
    fun formatTimecode(ms: Long): String {
        val clamped = ms.coerceAtLeast(0L)
        val totalSeconds = clamped / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        val centis = (clamped % 1000L) / 10L
        return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centis)
    }

    fun formatDurationShort(ms: Long): String {
        val clamped = ms.coerceAtLeast(0L)
        val totalSeconds = (clamped / 1000.0).roundToInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun formatMb(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f", mb)
    }

    fun estimateExportMb(durationMs: Long, mbps: Int): String {
        val seconds = (durationMs.coerceAtLeast(1000L)) / 1000.0
        val megabytes = (seconds * mbps) / 8.0
        return String.format(Locale.US, "%.1f", megabytes.coerceAtLeast(0.5))
    }
}
