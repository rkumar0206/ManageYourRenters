package com.rohitthebest.manageyourrenters.data

import androidx.room.ColumnInfo

// this class will be used for sending the key and the isSynced value of the borrower payment from
// the borrower payment database
data class KeyAndIsSyncedTuple(
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "isSynced") val isSynced: Boolean
)