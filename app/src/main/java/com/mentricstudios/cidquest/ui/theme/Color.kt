package com.mentricstudios.cidquest.ui.theme

import androidx.compose.ui.graphics.Color

// Cyberpunk Neon palette for CID Quest
// Midnight base + electric cyan / hot pink accents
val BackgroundTop = Color(0xFF070913)      // Midnight
val BackgroundBottom = Color(0xFF0F1428)   // Deeper navy for gradient depth
val AccentTeal = Color(0xFF00F0FF)         // Neon Cyan (primary glow)
val AccentOrange = Color(0xFFFF0055)       // Neon Pink (secondary / danger)
val TextPrimary = Color(0xFFEAFBFF)        // Icy near-white with a cyan tint
val TextSecondary = Color(0xFF7A88B8)      // Muted blue-grey

// Category card colors — each category gets its own neon signature
val CategoryClassic = Color(0xFF00F0FF)    // Neon Cyan
val CategoryIce = Color(0xFF33C6FF)        // Electric Ice-Blue
val CategoryDarkness = Color(0xFF2A2E4A)   // Deep Violet-Navy
val CategoryTraps = Color(0xFFFFB300)      // Neon Amber (warning)
val CategoryLightning = Color(0xFFB026FF)  // Neon Violet

val CardLocked = Color(0xFF11142B)
val LockGrey = Color(0xFF4A5578)

// Ice Floor in-level palette — the board itself gets a real frosty look
// (pale ice-white walls over a deep frozen-lake gradient) instead of reusing
// Classic's neon-cyan wireframe look.
val IceWallColor = Color(0xFFE3F7FF)       // Pale ice-white (corridor walls)
val IceBoardTop = Color(0xFF0A2436)        // Deep frozen-lake teal (board bg top)
val IceBoardBottom = Color(0xFF123B52)     // Slightly lighter frozen teal (board bg bottom)
val IceSparkle = Color(0xFFBFF0FF)         // Soft frost-glint specks on the floor

// Darkness in-level palette — wisp light pickups get their own warm firefly
// gold so they read as "collectible" at a glance, distinct from the cool
// cyan/violet used for the goal beacon and everything else in the category.
val WispGold = Color(0xFFFFD873)
