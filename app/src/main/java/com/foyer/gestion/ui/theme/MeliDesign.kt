package com.foyer.gestion.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ── Palette Meli Melo ────────────────────────────────────────────────────────
object MeliColors {
    val BgDark    = Color(0xFF0D2B25)
    val Accent    = Color(0xFF25D197)
    val CardMint  = Color(0xFF8EDFD0)
    val CardYellow= Color(0xFFF5D87A)
    val CardPink  = Color(0xFFF4A3A3)
    val CardBlue  = Color(0xFFB8D8E8)
    val CardTeal  = Color(0xFF9FE3CE)
    val CardPeach = Color(0xFFF5C5A3)
    val White     = Color(0xFFFFFFFF)
    val TextDark  = Color(0xFF1A1A1A)
    val TextMuted = Color(0xFF888888)
    val RedAlert  = Color(0xFFE53935)
    val GreenOk   = Color(0xFF2E7D52)
}

// ── Shapes ───────────────────────────────────────────────────────────────────
object MeliShapes {
    val card    = RoundedCornerShape(20.dp)
    val bigCard = RoundedCornerShape(24.dp)
    val input   = RoundedCornerShape(14.dp)
    val fab     = RoundedCornerShape(16.dp)
    val pill    = RoundedCornerShape(50.dp)
}
