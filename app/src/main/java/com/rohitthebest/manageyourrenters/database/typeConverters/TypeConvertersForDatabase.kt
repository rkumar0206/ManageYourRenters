package com.rohitthebest.manageyourrenters.database.typeConverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.database.entity.dataClasses.BillInfo
import com.rohitthebest.manageyourrenters.database.entity.dataClasses.ElectricityBillInfo

class TypeConvertersForDatabase {

    val gson = Gson()

    @TypeConverter
    fun convertFromBillInfoToString(billInfo: BillInfo): String {

        return gson.toJson(billInfo)
    }

    @TypeConverter
    fun convertFromStringToBillInfo(billInfoString: String): BillInfo {

        val type = object : TypeToken<BillInfo>() {}.type

        return gson.fromJson(billInfoString, type)
    }

    @TypeConverter
    fun convertFromElectricBillInfoToString(electricBillInfo: ElectricityBillInfo): String {

        return gson.toJson(electricBillInfo)
    }

    @TypeConverter
    fun convertFromStringToElectricBillInfo(electricBillInfoString: String): ElectricityBillInfo {

        val type = object : TypeToken<ElectricityBillInfo>() {}.type

        return gson.fromJson(electricBillInfoString, type)
    }
}