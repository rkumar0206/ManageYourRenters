package com.rohitthebest.manageyourrenters.data

import com.bumptech.glide.load.ImageHeaderParser.ImageType
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AppUpdate(
    var version: String,
    var apk_url: String,
    var whatsNew: ArrayList<WhatsNew>?
) {

    @Exclude
    fun isEmpty(): Boolean {

        return version == "" && apk_url == "" && (whatsNew == null || whatsNew!!.isEmpty())
    }

    constructor() : this(
        "",
        "",
        null
    )

    //constructor(version: String, apk_url: String) : this()
}

data class WhatsNew(
    var feature: String,
    var image: String? = "",
    var imageType: ImageType? = ImageType.JPEG,
    var textStyle: String? = "",    // example: "B" or "I" or "heading" or "critical"
    var styleType: StyleType? = StyleType.NORMAL
) {

    constructor() : this(
        "",
        "",
        ImageType.JPEG,
        "",
        StyleType.NORMAL
    )
}

enum class StyleType {
    NORMAL,
    HTML
}
