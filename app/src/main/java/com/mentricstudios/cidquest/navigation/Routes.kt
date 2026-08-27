package com.mentricstudios.cidquest.navigation

object Routes {
    const val STUDIO_SPLASH = "studio_splash"
    const val LOADING = "loading"
    const val TERMS = "terms"
    const val AGE_GATE = "age_gate"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val SHOP = "shop"
    const val LEVELS = "levels/{categoryName}"
    const val GAME = "game/{categoryName}/{levelNumber}"

    fun levels(categoryName: String) = "levels/$categoryName"
    fun game(categoryName: String, levelNumber: Int) = "game/$categoryName/$levelNumber"
}
