# Cid Quest release shrinking rules.
#
# Most of what's here is defensive: modern AndroidX/Compose/Media3 AARs ship
# their own consumer-rules.pro that R8 merges in automatically, so this file
# doesn't need to (and shouldn't try to) re-describe every library's
# internals. What it does add is a small set of commonly-needed keep rules
# for things R8 can't always see are still needed — reflection-based ad
# loading, and anything from this app's own code that R8's static analysis
# might otherwise consider unreachable and strip.

# --- This app's own code -----------------------------------------------
# Data classes/enums referenced by name-based lookups (route args, level
# lookups) rather than direct calls in some places — keep their shape
# intact rather than risk a stripped/renamed field breaking a lookup.
-keep class com.mentricstudios.cidquest.game.** { *; }
-keep class com.mentricstudios.cidquest.navigation.** { *; }

# --- Google Mobile Ads (play-services-ads) ------------------------------
# AdMob's SDK loads some classes via reflection for mediation/adapters that
# aren't statically referenced anywhere in this app's code, so R8's
# reachability analysis alone can't be trusted to keep them.
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# play-services-ads references android.media.LoudnessCodecController, which
# only exists on Android 15 (API 35) — one level above this project's
# compileSdk 34. The SDK feature-detects it at runtime and handles its
# absence fine on older devices, but R8 can't verify a class that isn't in
# the SDK it's compiling against, so it needs to be told that's expected
# rather than erroring out.
-dontwarn android.media.LoudnessCodecController
-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener

# --- Media3 (ExoPlayer) --------------------------------------------------
# Extractors/decoders are looked up by class name at runtime.
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# --- Kotlin coroutines ----------------------------------------------------
-dontwarn kotlinx.coroutines.**
