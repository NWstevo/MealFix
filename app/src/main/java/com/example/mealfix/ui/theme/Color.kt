package com.example.mealfix.ui.theme

import androidx.compose.ui.graphics.Color

// "Green life" palette — a ladder of green intensities from deep forest down to
// pale mint, plus a fresher lime-green for accents. Named by intensity (900 = darkest,
// 50 = lightest) so it's easy to tell at a glance how strong a shade is.

val Green900 = Color(0xFF0F3D12) // deep forest — dark-mode background / high-contrast text
val Green800 = Color(0xFF1B5E20) // dark green — text on light containers
val Green700 = Color(0xFF2E7D32) // rich green
val Green600 = Color(0xFF388E3C)
val Green500 = Color(0xFF4CAF50) // main "life" green — primary in light mode
val Green400 = Color(0xFF66BB6A)
val Green300 = Color(0xFF81C784) // light green — primary in dark mode, pops on dark bg
val Green200 = Color(0xFFA5D6A7)
val Green100 = Color(0xFFC8E6C9) // pale green — containers
val Green50 = Color(0xFFE8F5E9)  // near-white green — light background tint

// A slightly yellow-leaning green (lime) for a second, fresher intensity —
// keeps things in the same green family while adding a bit of energy/variety.
val Lime400 = Color(0xFF9CCC65)
val Lime100 = Color(0xFFDCEDC8)

// Dark-mode surfaces: a dark, slightly green-tinted near-black rather than pure
// black/grey, so the "life" feeling carries into dark mode too.
val DarkGreenBackground = Color(0xFF0D1F0F)
val DarkGreenSurface = Color(0xFF132514)
val DarkGreenContainer = Color(0xFF1E4620)
