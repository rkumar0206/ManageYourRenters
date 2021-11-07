package com.rohitthebest.manageyourrenters.data.apiModels

data class Expense(
    val id: Long,
    val amount: Double,
    val created: Long,
    val modified: Long,
    val spentOn: String,
    val uid: String,
    val key: String
) {

    constructor() : this(
        0L,
        0.0,
        0L,
        0L,
        "",
        "",
        ""
    )
}