package com.mentricstudios.cidquest.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Adaptive anchored banner (the current AdMob-recommended banner format —
 * taller and higher-eCPM than the old fixed 320x50) pinned wherever this is
 * placed in the layout. Used on menu/browsing screens only; never shown
 * during actual maze gameplay so it can't overlap the board or bump a swipe.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            adUnitId = AdIds.BANNER
            val displayMetrics = context.resources.displayMetrics
            val adWidthPixels = displayMetrics.widthPixels.toFloat()
            val density = displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth))
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(Unit) {
        onDispose { adView.destroy() }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { adView }
    )
}
