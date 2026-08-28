package com.mentricstudios.cidquest.ads

/**
 * All AdMob ad unit IDs used by the app, kept in one place so nothing is
 * ever hard-coded inline at a call site.
 *
 * BANNER, INTERSTITIAL, and NATIVE_ADVANCED are the real Cid Quest ad unit
 * IDs. REWARDED is still the older value from before this ID set was
 * provided — if that one's since changed too, send the new one and it's a
 * one-line swap here.
 *
 * NATIVE_ADVANCED is stored but not wired up to anything yet — a native ad
 * needs its own custom layout (headline/image/CTA rendered in the app's own
 * style, not a fixed Google-provided view like Banner/Interstitial), so
 * that's a real design decision — where it should appear and what it should
 * look like — worth confirming before building it, not something to guess at.
 */
object AdIds {
    const val BANNER = "ca-app-pub-9019700052213764/4634304560"
    const val INTERSTITIAL = "ca-app-pub-9019700052213764/9338951857"
    const val NATIVE_ADVANCED = "ca-app-pub-9019700052213764/8124169038"
    const val REWARDED = "ca-app-pub-9019700052213764/6792187327"
}
