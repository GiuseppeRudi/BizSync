package com.bizsync.ui.theme

import androidx.compose.ui.graphics.Color


object BizSyncColors {
    // Primary colors
    val Primary = Color(0xFF2563EB)
    val PrimaryVariant = Color(0xFF1E40AF)
    val Secondary = Color(0xFF64748B)

    // Background colors
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1F5F9)

    // Text colors
    val OnBackground = Color(0xFF1E293B)
    val OnSurface = Color(0xFF334155)
    val OnSurfaceVariant = Color(0xFF64748B)

    // Card gradients - più professionali
    val CardGradients = listOf(
        listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)), // Blue
        listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)), // Purple
        listOf(Color(0xFF06B6D4), Color(0xFF0891B2)), // Cyan
        listOf(Color(0xFF10B981), Color(0xFF059669)), // Emerald
        listOf(Color(0xFFF59E0B), Color(0xFFD97706)), // Amber
        listOf(Color(0xFFEF4444), Color(0xFFDC2626)), // Red
        listOf(Color(0xFF6B7280), Color(0xFF4B5563)), // Gray
        listOf(Color(0xFF8B5A2B), Color(0xFF92400E))  // Brown
    )
}