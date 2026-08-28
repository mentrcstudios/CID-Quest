package com.mentricstudios.cidquest.navigation

object Routes {
    const val STUDIO_SPLASH = "studio_splash"
    const val LOADING = "loading"
    const val AGE_GATE = "age_gate"
    const val HOME = "home"
    const val GAME = "game/{categoryName}/{levelNumber}"

    fun game(categoryName: String, levelNumber: Int) = "game/$categoryName/$levelNumber"
}
