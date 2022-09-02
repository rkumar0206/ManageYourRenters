package com.rohitthebest.manageyourrenters.data

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AppUpdate(
    var version: String,
    var apk_url: String,
    var whatsNew: ArrayList<WhatsNew>?
) {

    fun isEmpty(): Boolean {

        return version == "" && apk_url == "" && (whatsNew == null || whatsNew!!.isEmpty())
    }

    constructor() : this(
        "",
        "",
        null
    )

    constructor(version: String, apk_url: String) : this()
}

data class WhatsNew(
    var feature: String,
    var image: String?
) {

    constructor() : this(
        "",
        ""
    )
}
