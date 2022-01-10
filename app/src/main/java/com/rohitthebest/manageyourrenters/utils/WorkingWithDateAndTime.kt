package com.rohitthebest.manageyourrenters.utils

import android.annotation.SuppressLint
import android.util.Log
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class WorkingWithDateAndTime {

    private val TAG = "WorkingWithDateAndTime"

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

    fun getTimeInMillisFromDateInString(dateInString: String, pattern: String): Long? {

        val myDate = getDateFromDateInString(dateInString, pattern)

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
        timeInMillis: Long
    ): Calendar {

        val timeStamp = Timestamp(timeInMillis)

        val date = Date(timeStamp.time)

        val cal = Calendar.getInstance()
        cal.time = date
        return cal
    }


    /**
     * used for getting the milliseconds of the first day and the last day of any month
     *
     * @param timeInMillis
     * @return Pair(firstDayMilliseconds, lastDayMilliseconds)
     */

    fun getMillisecondsOfStartAndEndDayOfMonth(
        timeInMillis: Long
    ): Pair<Long, Long> {

        Log.d(TAG, "getMillisecondsOfStartAndEndDayOfMonth: Given time : $timeInMillis")

        val month = convertMillisecondsToDateAndTimePattern(timeInMillis, "MM")?.toInt()
        val year = convertMillisecondsToDateAndTimePattern(timeInMillis, "yyyy")?.toInt()

        Log.d(TAG, "getMillisecondsOfStartAndEndDayOfMonth: Month : $month")
        Log.d(TAG, "getMillisecondsOfStartAndEndDayOfMonth: Year : $year")

        val firstDayCal = Calendar.getInstance()
        val lastDayCal = Calendar.getInstance()

        if (year != null && month != null) {

            firstDayCal.set(year, month - 1, 1)

            val dayInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)

            Log.d(TAG, "getMillisecondsOfStartAndEndDayOfMonth: Day In month : $dayInMonth")

            lastDayCal.set(year, month - 1, dayInMonth)

            Log.d(
                TAG,
                "getMillisecondsOfStartAndEndDayOfMonth: firstDate : ${
                    convertMillisecondsToDateAndTimePattern(
                        firstDayCal.timeInMillis
                    )
                }"
            )
            Log.d(
                TAG,
                "getMillisecondsOfStartAndEndDayOfMonth: lastDate : ${
                    convertMillisecondsToDateAndTimePattern(
                        lastDayCal.timeInMillis
                    )
                }"
            )

            Log.d(
                TAG,
                "getMillisecondsOfStartAndEndDayOfMonth: Pair : " + firstDayCal.timeInMillis + ", ${lastDayCal.timeInMillis}"
            )

            return Pair(firstDayCal.timeInMillis, lastDayCal.timeInMillis)
        }

        return Pair(timeInMillis, timeInMillis);
    }

    fun getMillisecondsOfStartAndEndOfWeek(
        timeInMillis: Long
    ): Pair<Long, Long> {

        val firstCal = convertMillisecondsToCalendarInstance(timeInMillis)

        val dayOfWeek = firstCal.get(Calendar.DAY_OF_WEEK)

        Log.d(
            TAG,
            "getMillisecondsOfStartAndEndOfWeek: Day : ${firstCal.get(Calendar.DAY_OF_WEEK)}"
        )

        val lastCal = convertMillisecondsToCalendarInstance(timeInMillis)

        when (dayOfWeek) {
            1 -> {

                // sunday
                firstCal.add(Calendar.DATE, -6) // till monday
            }

            2 -> {

                // monday
                lastCal.add(Calendar.DATE, 6) // till sunday
            }

            3 -> {

                //tuesday

                firstCal.add(Calendar.DATE, -1) // monday
                lastCal.add(Calendar.DATE, 5) // sunday
            }

            4 -> {
                // wednesday
                firstCal.add(Calendar.DATE, -2) // monday
                lastCal.add(Calendar.DATE, 4) // sunday

            }

            5 -> {

                // thursday
                firstCal.add(Calendar.DATE, -3) // monday
                lastCal.add(Calendar.DATE, 3) // sunday

            }

            6 -> {

                // friday
                firstCal.add(Calendar.DATE, -4) // monday
                lastCal.add(Calendar.DATE, 2) // sunday
            }

            7 -> {

                // saturday
                firstCal.add(Calendar.DATE, -5) // monday
                lastCal.add(Calendar.DATE, 1) // sunday

            }

            else -> {
            }
        }

        Log.d(
            TAG, "getMillisecondsOfStartAndEndOfWeek: firstDate : ${
                convertMillisecondsToDateAndTimePattern(
                    firstCal.timeInMillis
                )
            }"
        )

        Log.d(
            TAG, "getMillisecondsOfStartAndEndOfWeek: lastDate : ${
                convertMillisecondsToDateAndTimePattern(
                    lastCal.timeInMillis
                )
            }"
        )

        Log.d(
            TAG,
            "getMillisecondsOfStartAndEndOfWeek: Pair : ${firstCal.timeInMillis}, ${lastCal.timeInMillis}"
        )


        return Pair(firstCal.timeInMillis, lastCal.timeInMillis);
    }

}