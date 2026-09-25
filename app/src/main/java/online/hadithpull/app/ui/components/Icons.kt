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
    @DrawableRes val bookmarkFilled = R.drawable.ic_bookmark_filled
    @DrawableRes val openBook = R.drawable.ic_open_book
    @DrawableRes val infoCircle = R.drawable.ic_info_circle
    @DrawableRes val check = R.drawable.ic_check
    @DrawableRes val pencil = R.drawable.ic_pencil
    @DrawableRes val bin = R.drawable.ic_bin
    @DrawableRes val copy = R.drawable.ic_copy
    @DrawableRes val upload = R.drawable.ic_upload
    @DrawableRes val openInNew = R.drawable.ic_open_in_new
    @DrawableRes val whatsapp = R.drawable.ic_whatsapp
    @DrawableRes val facebook = R.drawable.ic_facebook
    @DrawableRes val instagram = R.drawable.ic_instagram
    @DrawableRes val github = R.drawable.ic_github
    @DrawableRes val linkedin = R.drawable.ic_linkedin
}
