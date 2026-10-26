package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class SignageThemePreset(
    val key: String,
    val displayName: String,
    val tagline: String,
    val description: String,
    val badgeEmoji: String,
    val previewColors: List<Color>,
    val backgroundBrush: Brush,
    val surfaceColor: Color,
    val surfaceBorderColor: Color,
    val headerCardColor: Color,
    val headerBorderColor: Color,
    val primaryAccentColor: Color,
    val secondaryAccentColor: Color,
    val textPrimaryColor: Color,
    val textSecondaryColor: Color,
    val textMutedColor: Color,
    val tickerBgColor: Color,
    val tickerTextColor: Color,
    val bellBadgeBgColor: Color,
    val bellBadgeTextColor: Color,
    val currentScheduleHighlightColor: Color,
    val isDark: Boolean
) {
    ROYAL_ACADEMIC(
        key = "ROYAL_ACADEMIC",
        displayName = "Royal Academic",
        tagline = "Biru Resmi & Emas Kemendikbud",
        description = "Standar resmi nasional berwibawa, kontras tinggi, dan prestisius untuk SMA / SMK / Sekolah Unggulan.",
        badgeEmoji = "🎓",
        previewColors = listOf(Color(0xFF1E3A8A), Color(0xFFF59E0B), Color(0xFF0F172A), Color(0xFFFFFFFF)),
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
        ),
        surfaceColor = Color(0xFF1E293B),
        surfaceBorderColor = Color(0xFF334155),
        headerCardColor = Color(0xFF1E293B),
        headerBorderColor = Color(0xFF3B82F6),
        primaryAccentColor = Color(0xFF3B82F6),
        secondaryAccentColor = Color(0xFFF59E0B),
        textPrimaryColor = Color(0xFFF8FAFC),
        textSecondaryColor = Color(0xFFCBD5E1),
        textMutedColor = Color(0xFF94A3B8),
        tickerBgColor = Color(0xFF0A0F1D),
        tickerTextColor = Color(0xFFFDE047),
        bellBadgeBgColor = Color(0xFF2563EB),
        bellBadgeTextColor = Color(0xFFFFFFFF),
        currentScheduleHighlightColor = Color(0xFF1D4ED8),
        isDark = true
    ),

    ISLAMIC_EMERALD(
        key = "ISLAMIC_EMERALD",
        displayName = "Islamic Emerald",
        tagline = "Hijau Zamrud & Emas Elegan",
        description = "Nuansa Islami yang tenang, teduh, dan agung untuk Madrasah, Pesantren, atau Sekolah Islam Terpadu.",
        badgeEmoji = "🕌",
        previewColors = listOf(Color(0xFF0D5C3A), Color(0xFFD4AF37), Color(0xFF06281B), Color(0xFFF9F9F6)),
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF052116), Color(0xFF0A3A27), Color(0xFF041911))
        ),
        surfaceColor = Color(0xFF0D452F),
        surfaceBorderColor = Color(0xFF19704D),
        headerCardColor = Color(0xFF0D452F),
        headerBorderColor = Color(0xFFD4AF37),
        primaryAccentColor = Color(0xFF10B981),
        secondaryAccentColor = Color(0xFFD4AF37),
        textPrimaryColor = Color(0xFFF9F9F6),
        textSecondaryColor = Color(0xFFD1FAE5),
        textMutedColor = Color(0xFF86EFAC),
        tickerBgColor = Color(0xFF03160E),
        tickerTextColor = Color(0xFFFDE047),
        bellBadgeBgColor = Color(0xFFD4AF37),
        bellBadgeTextColor = Color(0xFF06281B),
        currentScheduleHighlightColor = Color(0xFF047857),
        isDark = true
    ),

    CYBER_TECH(
        key = "CYBER_TECH",
        displayName = "Cyber Tech Blue",
        tagline = "Futuristik Gelap & Neon Cyan",
        description = "Gaya dashboard pusat kendali futuristik beraksen neon cerah untuk SMK Teknologi, Lab Komputer, dan Robotika.",
        badgeEmoji = "⚡",
        previewColors = listOf(Color(0xFF00F0FF), Color(0xFF0B132B), Color(0xFF1C2541), Color(0xFF38BDF8)),
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF030712), Color(0xFF0B132B), Color(0xFF020617))
        ),
        surfaceColor = Color(0xFF0E1A38),
        surfaceBorderColor = Color(0xFF0284C7),
        headerCardColor = Color(0xFF0E1A38),
        headerBorderColor = Color(0xFF00F0FF),
        primaryAccentColor = Color(0xFF00F0FF),
        secondaryAccentColor = Color(0xFF38BDF8),
        textPrimaryColor = Color(0xFFF0FDF4),
        textSecondaryColor = Color(0xFF7DD3FC),
        textMutedColor = Color(0xFF38BDF8),
        tickerBgColor = Color(0xFF020817),
        tickerTextColor = Color(0xFF00F0FF),
        bellBadgeBgColor = Color(0xFF0284C7),
        bellBadgeTextColor = Color(0xFF020817),
        currentScheduleHighlightColor = Color(0xFF0369A1),
        isDark = true
    ),

    VIBRANT_FRIENDLY(
        key = "VIBRANT_FRIENDLY",
        displayName = "Vibrant Friendly",
        tagline = "Ceria, Hangat & Ramah Murid",
        description = "Warna cerah ceria penuh semangat belajar untuk SD, MI, PAUD, atau Sekolah Ramah Anak.",
        badgeEmoji = "🎈",
        previewColors = listOf(Color(0xFF0284C7), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFFE0F2FE)),
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0C3857), Color(0xFF13506F), Color(0xFF08263B))
        ),
        surfaceColor = Color(0xFF174C6B),
        surfaceBorderColor = Color(0xFF38BDF8),
        headerCardColor = Color(0xFF174C6B),
        headerBorderColor = Color(0xFFF59E0B),
        primaryAccentColor = Color(0xFF38BDF8),
        secondaryAccentColor = Color(0xFFF59E0B),
        textPrimaryColor = Color(0xFFFFFFFF),
        textSecondaryColor = Color(0xFFBAE6FD),
        textMutedColor = Color(0xFFFDE68A),
        tickerBgColor = Color(0xFF082236),
        tickerTextColor = Color(0xFFFEF08A),
        bellBadgeBgColor = Color(0xFFF97316),
        bellBadgeTextColor = Color(0xFFFFFFFF),
        currentScheduleHighlightColor = Color(0xFF0284C7),
        isDark = true
    ),

    OLED_MINIMALIST(
        key = "OLED_MINIMALIST",
        displayName = "Minimalist OLED Dark",
        tagline = "Hitam Pekat Arsitektural & Hemat Daya",
        description = "Tampilan monokrom kontras tinggi, hemat energi TV layar OLED/LED, dan fokus maksimal pada foto & video.",
        badgeEmoji = "🌙",
        previewColors = listOf(Color(0xFF000000), Color(0xFF18181B), Color(0xFF27272A), Color(0xFFFFFFFF)),
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF000000), Color(0xFF09090B), Color(0xFF000000))
        ),
        surfaceColor = Color(0xFF121214),
        surfaceBorderColor = Color(0xFF27272A),
        headerCardColor = Color(0xFF121214),
        headerBorderColor = Color(0xFF3F3F46),
        primaryAccentColor = Color(0xFFE4E4E7),
        secondaryAccentColor = Color(0xFFA1A1AA),
        textPrimaryColor = Color(0xFFFFFFFF),
        textSecondaryColor = Color(0xFFD4D4D8),
        textMutedColor = Color(0xFF71717A),
        tickerBgColor = Color(0xFF000000),
        tickerTextColor = Color(0xFFFAFAFA),
        bellBadgeBgColor = Color(0xFF27272A),
        bellBadgeTextColor = Color(0xFFFFFFFF),
        currentScheduleHighlightColor = Color(0xFF27272A),
        isDark = true
    );

    companion object {
        fun fromKey(key: String?): SignageThemePreset {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: ROYAL_ACADEMIC
        }
    }
}
