package online.hadithpull.app.ui.components

import androidx.annotation.DrawableRes
import online.hadithpull.app.R

/**
 * Vector drawables ported from the web SVG paths (sun/moon/bookmark, from index.html) or,
 * where the web has no equivalent (open book, info circle — nav-tab-only icons), hand-drawn
 * to match their stroke style. See res/drawable/ic_*.xml for the path data.
 */
object HadithIcons {
    @DrawableRes val sun = R.drawable.ic_sun
    @DrawableRes val moon = R.drawable.ic_moon
    @DrawableRes val bookmark = R.drawable.ic_bookmark
    @DrawableRes val openBook = R.drawable.ic_open_book
    @DrawableRes val infoCircle = R.drawable.ic_info_circle
    @DrawableRes val check = R.drawable.ic_check
    @DrawableRes val pencil = R.drawable.ic_pencil
    @DrawableRes val bin = R.drawable.ic_bin
}
