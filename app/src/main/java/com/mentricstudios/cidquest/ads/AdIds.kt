package com.mentricstudios.cidquest.ads

/**
 * All AdMob ad unit IDs used by the app, kept in one place so nothing is
 * ever hard-coded inline at a call site.
 *
 * These are real ad unit IDs (publisher "9019700052213764"). They are only
 * live once the app is signed with the same certificate registered to this
 * AdMob account and the AdMob App ID in AndroidManifest.xml is the real one
 * (see the comment there) — until then, AdMob will simply serve nothing /
 * test fills for these units, so it's safe to ship this file as-is.
 */
object AdIds {
    const val BANNER = "ca-app-pub-9019700052213764/4269397547"
    const val INTERSTITIAL = "ca-app-pub-9019700052213764/7170893051"
    const val REWARDED = "ca-app-pub-9019700052213764/6792187327"
}
