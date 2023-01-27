package com.rohitthebest.manageyourrenters.database.typeConverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.data.*
import com.rohitthebest.manageyourrenters.database.model.MonthlyPaymentDateTimeInfo
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.utils.convertJsonToObject
import com.rohitthebest.manageyourrenters.utils.convertToJsonString
import com.rohitthebest.manageyourrenters.utils.isValid

class TypeConvertersForDatabase {

    @TypeConverter
    fun fromRenterBillPeriodInfoToString(renterBillPeriodInfo: RenterBillPeriodInfo?): String? {

        return renterBillPeriodInfo.convertToJsonString()
    }

    @TypeConverter
    fun fromStringToRenterBillPeriodInfo(str: String): RenterBillPeriodInfo? {

        return str.convertJsonToObject(RenterBillPeriodInfo::class.java)
    }

    @TypeConverter
    fun fromRenterElectricityBillInfoToString(renterElectricityBillInfo: RenterElectricityBillInfo?): String? {

        return renterElectricityBillInfo.convertToJsonString()
    }

    @TypeConverter
    fun fromStringToRenterElectricityBillInfo(str: String): RenterElectricityBillInfo? {

        return str.convertJsonToObject(RenterElectricityBillInfo::class.java)
    }

    @TypeConverter
    fun fromRenterPaymentExtrasToString(renterPaymentExtras: RenterPaymentExtras?): String? {

        return renterPaymentExtras.convertToJsonString()
    }

    @TypeConverter
    fun fromStringToRenterPaymentExtras(str: String): RenterPaymentExtras? {

        return str.convertJsonToObject(RenterPaymentExtras::class.java)
    }

    @TypeConverter
    fun fromInterestToString(interest: Interest?): String? {

        return interest.convertToJsonString()
    }

    @TypeConverter
    fun fromStringToInterest(str: String): Interest? {

        return str.convertJsonToObject(Interest::class.java)
    }

    @TypeConverter
    fun fromSupportingDocumentToString(supportingDocument: SupportingDocument?): String? {

        return supportingDocument.convertToJsonString()
    }

    @TypeConverter
    fun fromStringToSupportingDocument(str: String?): SupportingDocument? {

        return str?.convertJsonToObject(SupportingDocument::class.java)
    }

    @TypeConverter
    fun fromRenterToString(renter: Renter): String {

        return renter.convertToJsonString()!!
    }

    @TypeConverter
    fun fromStringToRenter(str: String): Renter {

        return str.convertJsonToObject(Renter::class.java)!!
    }

    @TypeConverter
    fun fromRenterPaymentToString(renterPayment: RenterPayment): String {

        return renterPayment.convertToJsonString()!!
    }

    @TypeConverter
    fun fromStringToRenterPayment(str: String): RenterPayment {

        return str.convertJsonToObject(RenterPayment::class.java)!!
    }

    @TypeConverter
    fun fromStatusEnumToString(status: StatusEnum): String {

        return Gson().toJson(status)
    }

    @TypeConverter
    fun fromStringToStatusEnum(str: String): StatusEnum {

        if (!str.isValid()) {
            return StatusEnum.ACTIVE
        }
        return Gson().fromJson(str, object : TypeToken<StatusEnum>() {}.type)
    }

    @TypeConverter
    fun fromMapOfLongDoubleToString(map: Map<Long, Double>): String {

        return Gson().toJson(map)
    }

    @TypeConverter
    fun fromStringToMapOfLongDouble(str: String): Map<Long, Double> {

        return Gson().fromJson(str, object : TypeToken<Map<Long, Double>>() {}.type)
    }

    @TypeConverter
    fun fromMonthlyPaymentDateTimeInfoToString(monthlyPaymentDateTimeInfo: MonthlyPaymentDateTimeInfo?): String? {

        return monthlyPaymentDateTimeInfo.convertToJsonString()
    }

    @TypeConverter
    fun fromStringToMonthlyPaymentDateTimeInfo(str: String?): MonthlyPaymentDateTimeInfo? {

        return str?.convertJsonToObject(MonthlyPaymentDateTimeInfo::class.java)
    }

    @TypeConverter
    fun fromSetToString(paymentMethodKeySet: Set<String>?): String {

        return Gson().toJson(paymentMethodKeySet)
    }

    @TypeConverter
    fun fromStringToSet(str: String?): Set<String>? {

        return if (str == null) null else Gson().fromJson(
            str,
            object : TypeToken<Set<String>?>() {}.type
        )
    }
}