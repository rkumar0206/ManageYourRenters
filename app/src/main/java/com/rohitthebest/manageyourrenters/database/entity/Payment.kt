package com.rohitthebest.manageyourrenters.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.rohitthebest.manageyourrenters.database.entity.dataClasses.BillInfo
import com.rohitthebest.manageyourrenters.database.entity.dataClasses.ElectricityBillInfo

@IgnoreExtraProperties
@Entity(tableName = "payment_table")
data class Payment(
    var timeStamp : Long? = System.currentTimeMillis(),
    var renterKey : String,
    var bill : BillInfo?,
    var electricBill : ElectricityBillInfo?,
    var houseRent : String,
    var parkingRent : String?,
    var extraFieldName : String?,
    var extraAmount : String?,
    var dueAmount : String?,
    var messageOrNote : String?,
    var totalRent : String,
    var uid : String,
    var key : String,
    var isSynced : String = "false"
) {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    var id : Int? = null

    constructor() : this(
        System.currentTimeMillis(),
        "",
        BillInfo(),
        ElectricityBillInfo(),
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    )
}