package com.rohitthebest.manageyourrenters.data

import androidx.room.ColumnInfo

data class KeyAndIsSyncedTuple(
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "isSynced") val isSynced: Boolean
)