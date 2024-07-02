package com.rohitthebest.manageyourrenters.data

data class ImportLimitPerUser(
    val uid: String,
    val limit: Int,
    val modified: Long = System.currentTimeMillis()
) {
    constructor() : this("", 0, System.currentTimeMillis())
}
