package com.rohitthebest.manageyourrenters.utils

import android.annotation.SuppressLint
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class WorkingWithDateAndTime {

    fun convertTimeStampToDateOrTimePattern(timestamps: Timestamp?, pattern: String): String? {

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

    /**
     * converts milliseconds in any date pattern
     *
     * @param timeInMillis
     * @param pattern
     * pattern types :
     *  1. dd-M-yyyy hh:mm:ss	        02-1-2018 06:07:59
     *  2. dd MMMM yyyy	                02 January 2018
     *  3. dd MMMM yyyy zzzz	        02 January 2018 India Standard Time
     *  4. E, dd MMM yyyy HH:mm:ss z	Tue, 02 Jan 2018 18:07:59 IST
     */

    fun convertMillisecondsToDateAndTimePattern(
        timeInMillis: Long? = System.currentTimeMillis(),
        pattern: String? = "dd-MM-yyyy"
    ): String? {

        val timeStamp = timeInMillis?.let { Timestamp(it) }
        return if (pattern != "") {
            convertTimeStampToDateOrTimePattern(timeStamp, pattern!!)
        } else {
            null
        }
    }

    fun convertMillisecondsToCalendarInstance(
        timeInMillis: Long?
    ): Calendar {

        val timeStamp = timeInMillis?.let { Timestamp(it) }

        val date = timeStamp?.time?.let { Date(it) }

        val cal = Calendar.getInstance()
        cal.time = date!!
        return cal
    }

}