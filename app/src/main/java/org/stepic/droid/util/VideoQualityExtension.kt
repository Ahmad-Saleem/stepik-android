package org.stepic.droid.util

import org.stepic.droid.model.VideoUrl

fun VideoUrl.greaterThanMaxQuality(): Boolean {
    try {
        val qualityInt = Integer.parseInt(this.quality)
        return qualityInt > AppConstants.MAX_QUALITY_INT
    } catch (exception: Exception) {
        return true
    }
}

