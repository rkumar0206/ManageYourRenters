package com.rohitthebest.manageyourrenters.database.typeConverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.data.*
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment

class TypeConvertersForDatabase {

    @TypeConverter
    fun fromRenterBillPeriodInfoToString(renterBillPeriodInfo: RenterBillPeriodInfo?): String? {

        return Gson().toJson(renterBillPeriodInfo)
    }

    @TypeConverter
    fun fromStringToRenterBillPeriodInfo(str: String): RenterBillPeriodInfo? {

        return Gson().fromJson(str, object : TypeToken<RenterBillPeriodInfo>() {}.type)
    }

    @TypeConverter
    fun fromRenterElectricityBillInfoToString(renterElectricityBillInfo: RenterElectricityBillInfo?): String? {

        return Gson().toJson(renterElectricityBillInfo)
    }

    @TypeConverter
    fun fromStringToRenterElectricityBillInfo(str: String): RenterElectricityBillInfo? {

        return Gson().fromJson(str, object : TypeToken<RenterElectricityBillInfo>() {}.type)
    }

    @TypeConverter
    fun fromRenterPaymentExtrasToString(renterPaymentExtras: RenterPaymentExtras?): String? {

        return Gson().toJson(renterPaymentExtras)
    }

    @TypeConverter
    fun fromStringToRenterPaymentExtras(str: String): RenterPaymentExtras? {

        return Gson().fromJson(str, object : TypeToken<RenterPaymentExtras>() {}.type)
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

    @TypeConverter
    fun fromRenterToString(renter: Renter): String {

        return Gson().toJson(renter)
    }

    @TypeConverter
    fun fromStringToRenter(str: String): Renter {

        return Gson().fromJson(str, object : TypeToken<Renter>() {}.type)
    }

    @TypeConverter
    fun fromRenterPaymentToString(renterPayment: RenterPayment): String {

        return Gson().toJson(renterPayment)
    }

    @TypeConverter
    fun fromStringToRenterPayment(str: String): RenterPayment {

        return Gson().fromJson(str, object : TypeToken<RenterPayment>() {}.type)
    }

    @TypeConverter
    fun fromMapOfLongDoubleToString(map: Map<Long, Double>): String {

        return Gson().toJson(map)
    }

    @TypeConverter
    fun fromStringToMapOfLongDouble(str: String): Map<Long, Double> {

        return Gson().fromJson(str, object : TypeToken<Map<Long, Double>>() {}.type)
    }
}