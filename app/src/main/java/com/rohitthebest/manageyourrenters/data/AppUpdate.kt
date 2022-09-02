package com.rohitthebest.manageyourrenters.data

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AppUpdate(
    var version: String,
    var apk_url: String,
    var whatsNew: HashMap<String, String>?
) {
    fun isEmpty(): Boolean {

        return version == "" && apk_url == "" && whatsNew == null
    }

    constructor() : this(
        "",
        "",
        null
    )

    constructor(version: String, apk_url: String) : this()
}
