package com.rohitthebest.manageyourrenters.utils

import android.annotation.SuppressLint
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class WorkingWithDateAndTime {

    fun convertTimeStampToDateOrTime(timestamps: Timestamp?, pattern: String): String? {

        //converting timestamp to Date format
        val date = timestamps?.time?.let { Date(it) }

        //Converting date to the required pattern
        val formattedDate = SimpleDateFormat(pattern)

        return formattedDate.format(date!!)
    }

    fun getDateFromDateInString(dateInString: String?, pattern: String): Date? {

        val dateFormat = SimpleDateFormat(pattern)
        var myDate: Date? = null

        try {
            if (dateInString != null) {
                myDate = dateFormat.parse(dateInString)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return myDate
    }

    fun getTimeInMillisFromDateInString(dateInString: String?, pattern: String?): Long? {

        val myDate = pattern?.let { getDateFromDateInString(dateInString, it) }

        return myDate?.time

    }

    fun convertDateToPattern(date: Date?, pattern: String? = "dd-MM-yyyy"): String? {

        var sdf: SimpleDateFormat? = null
        if (pattern != null || pattern != "") {
            sdf = SimpleDateFormat(pattern!!)
        }
        return if (date != null) {
            sdf?.format(date)
        } else {
            null
        }
    }

    fun convertMillisecondsToDateAndTimePattern(
        timeInMillis: Long? = System.currentTimeMillis(),
        pattern: String? = "dd-MM-yyyy"
    ): String? {

        val timeStamp = timeInMillis?.let { Timestamp(it) }
        return if (pattern != "") {
            convertTimeStampToDateOrTime(timeStamp, pattern!!)
        } else {
            null
        }
    }

}