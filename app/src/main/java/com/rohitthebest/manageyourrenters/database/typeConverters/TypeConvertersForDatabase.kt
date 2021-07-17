package com.rohitthebest.manageyourrenters.database.typeConverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.data.BillInfo
import com.rohitthebest.manageyourrenters.data.ElectricityBillInfo
import com.rohitthebest.manageyourrenters.data.Interest
import com.rohitthebest.manageyourrenters.data.SupportingDocument

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

    @TypeConverter
    fun fromInterestToString(interest: Interest?): String? {

        return Gson().toJson(interest)
    }

    @TypeConverter
    fun fromStringToInterest(str: String): Interest? {

        return Gson().fromJson(str, object : TypeToken<Interest?>() {}.type)
    }

    @TypeConverter
    fun fromSupportingDocumentToString(supportingDocument: SupportingDocument?): String? {

        return Gson().toJson(supportingDocument)
    }

    @TypeConverter
    fun fromStringToSupportingDocument(str: String): SupportingDocument? {

        return Gson().fromJson(str, object : TypeToken<SupportingDocument?>() {}.type)
    }
}