package com.rohitthebest.manageyourrenters.utils

import android.annotation.SuppressLint
import android.util.Log
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

@SuppressLint("SimpleDateFormat")
object WorkingWithDateAndTime {

    private val TAG = "WorkingWithDateAndTime"

    fun convertTimeStampToDateOrTimePattern(timestamps: Timestamp?, pattern: String): String? {

        //converting timestamp to Date format
        val date = timestamps?.time?.let { Date(it) }

        //Converting date to the required pattern
        val formattedDate = SimpleDateFormat(pattern)

        return formattedDate.format(date!!)
    }

    @Throws(ParseException::class)
    fun getDateFromDateInString(dateInString: String?, pattern: String): Date? {

        val dateFormat = SimpleDateFormat(pattern)
        var myDate: Date? = null

        if (dateInString != null) {
            myDate = dateFormat.parse(dateInString)
        }

        return myDate
    }

    @Throws(ParseException::class)
    fun getTimeInMillisFromDateInString(dateInString: String, pattern: String): Long? {

        val myDate = getDateFromDateInString(dateInString, pattern)

        return myDate?.time
    }

    fun convertDateToPattern(date: Date?, pattern: String? = "dd-MM-yyyy"): String? {

        var sdf: SimpleDateFormat? = null
        if (pattern != null && pattern != "") {
            sdf = SimpleDateFormat(pattern)
        }
        return if (date != null) {
            sdf?.format(date)
        } else {
            null
        }
    }

    fun getCurrentYear(): Int {

        return Calendar.getInstance().get(Calendar.YEAR)
    }

    fun getCurrentMonth(): Int {

        return Calendar.getInstance().get(Calendar.MONTH)
    }

    fun calculateNumberOfDays(startDate: Long, endDate: Long): Int {

        return ((endDate - startDate) / (1000 * 60 * 60 * 24)).toInt()

    }

    /**
     * @param startMonthYear : pair of starting month and year ex : Pair(7, 2022)
     * @param endMonthYear : pair of ending month and year ex : Pair(8, 2022)
     * @return numberOfMonths : ex : 1
     */
    fun calculateNumberOfMonthsInBetween(
        startMonthYear: Pair<Int, Int>,
        endMonthYear: Pair<Int, Int>
    ): Int {

        val start = Calendar.getInstance()
        start.set(startMonthYear.second, startMonthYear.first - 1, 1)
        val end = Calendar.getInstance()
        end.set(endMonthYear.second, endMonthYear.first - 1, 1)

        val yearsInBetween = end.get(Calendar.YEAR) - start.get(Calendar.YEAR)
        val monthDiff = end.get(Calendar.MONTH) - start.get(Calendar.MONTH)

        return yearsInBetween * 12 + monthDiff + 1
    }

    fun getTimeInMillisForYearMonthAndDate(yearMonthDay: Triple<Int, Int, Int>): Long {

        val cal = Calendar.getInstance()
        cal.set(yearMonthDay.first, yearMonthDay.second - 1, yearMonthDay.third)
        return cal.timeInMillis
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
        pattern: String = "dd-MM-yyyy"
    ): String? {

        val timeStamp = timeInMillis?.let { Timestamp(it) }
        return if (pattern != "") {
            convertTimeStampToDateOrTimePattern(timeStamp, pattern)
        } else {
            null
        }
    }

    /**
     * Used for converting time in milliseconds to the calendar instance
     */

    fun convertMillisecondsToCalendarInstance(
        timeInMillis: Long
    ): Calendar {

        val timeStamp = Timestamp(timeInMillis)

        val date = Date(timeStamp.time)

        val cal = Calendar.getInstance()
        cal.time = date
        return cal
    }

    fun Long.getCalendarInstance(): Calendar = convertMillisecondsToCalendarInstance(this)

    /**
     * Get the date (day), month, year from the time in milliseconds
     */

    fun getDateMonthYearByTimeInMillis(
        timeInMillis: Long
    ): Triple<Int, Int, Int> {

        try {

            val date = convertMillisecondsToDateAndTimePattern(timeInMillis, "dd")?.toInt()!!
            val month = convertMillisecondsToDateAndTimePattern(timeInMillis, "MM")?.toInt()!!
            val year = convertMillisecondsToDateAndTimePattern(timeInMillis, "yyyy")?.toInt()!!

            return Triple(date, month, year)

        } catch (e: Exception) {

            e.printStackTrace()
        }

        return Triple(1, 1, 1)
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


        val month = convertMillisecondsToDateAndTimePattern(timeInMillis, "MM")?.toInt()
        val year = convertMillisecondsToDateAndTimePattern(timeInMillis, "yyyy")?.toInt()

        val firstDayCal = Calendar.getInstance()
        val lastDayCal = Calendar.getInstance()

        if (year != null && month != null) {

            firstDayCal.set(year, month - 1, 1, 0, 0, 0)

            val dayInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)

            Log.d(TAG, "getMillisecondsOfStartAndEndDayOfMonth: Day In month : $dayInMonth")

            lastDayCal.set(year, month - 1, dayInMonth, 0, 0, 0)


            return Pair(firstDayCal.timeInMillis, lastDayCal.timeInMillis)
        }

        return Pair(timeInMillis, timeInMillis)
    }

    /**
     * used for getting the milliseconds of the first day and the last day of any week
     *
     * @param timeInMillis
     * @return Pair(firstDayMilliseconds, lastDayMilliseconds)
     */

    fun getMillisecondsOfStartAndEndOfWeek(
        timeInMillis: Long
    ): Pair<Long, Long> {

        val dateMonthYear = getDateMonthYearByTimeInMillis(timeInMillis)

        val firstCal = Calendar.getInstance()
        firstCal.set(
            dateMonthYear.third, dateMonthYear.second - 1, dateMonthYear.first,
            0, 0, 0
        )

        val dayOfWeek = firstCal.get(Calendar.DAY_OF_WEEK)

        val lastCal = Calendar.getInstance()
        lastCal.set(
            dateMonthYear.third, dateMonthYear.second - 1, dateMonthYear.first,
            0, 0, 0
        )

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

        return Pair(firstCal.timeInMillis, lastCal.timeInMillis)
    }

    fun getMonthAndYearString(month: Int, year: Int): String {

        return "${month}_${year}"
    }

    fun extractMonthAndYearFromMonthAndYearString(monthYearString: String): Pair<Int, Int> {

        return try {

            val pattern = "(\\d+)_(\\d+)"
            val regex: Pattern = Pattern.compile(pattern)
            val matcher: Matcher = regex.matcher(monthYearString)

            if (matcher.find()) {

                val month = matcher.group(1)?.toInt()
                val year = matcher.group(2)?.toInt()

                return if (month != null && year != null) {

                    Pair(month, year)
                } else {
                    Pair(0, 0)
                }

            } else {
                return Pair(0, 0)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Pair(0, 0)
        }

    }


    fun getNextMonth(selectedMonth: Int): Int {

        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, selectedMonth)
        cal.add(Calendar.MONTH, 1)
        return cal.get(Calendar.MONTH)
    }

    fun getPreviousMonth(selectedMonth: Int): Int {

        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, selectedMonth)
        cal.add(Calendar.MONTH, -1)
        return cal.get(Calendar.MONTH)
    }

    fun getMillisecondsOfStartAndEndDayOfMonthForGivenMonthAndYear(
        month: Int,
        year: Int
    ): Pair<Long, Long> {

        val calendar = Calendar.getInstance()
        calendar.set(year, month, 5)
        val dateInMillis = calendar.timeInMillis
        return getMillisecondsOfStartAndEndDayOfMonth(dateInMillis)
    }

    /**
     * @param month - month (0-11)
     * @param year  - year (Ex: 2023)
     *
     * @return - Returns the number of days in a give month
     */
    fun getNumberOfDaysInMonth(month: Int, year: Int): Int {

        val yearMonth = YearMonth.of(year, month + 1)
        return yearMonth.lengthOfMonth()
    }

    /**
     * @param month - month (0-11)
     * @param year  - year (Ex: 2023)
     *
     * @return - Returns the number of days left in the month (Int)
     */
    fun getNumberOfDaysLeftInAnyMonth(month: Int, year: Int): Int {

        val currentYear = getCurrentYear()
        val currentMonth = getCurrentMonth()
        val numberOfDaysInMonth = getNumberOfDaysInMonth(month, year)

        if (year < currentYear) return 0
        if (year > currentYear) return numberOfDaysInMonth

        if (month < currentMonth) return 0
        if (month > currentMonth) return numberOfDaysInMonth

        // year = current year and month = current month
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        return numberOfDaysInMonth - currentDay
    }

    /**
     * @param month - month (0-11)
     * @param year  - year (Ex: 2023)
     *
     * @return - Returns list of Pair object for which the
     * first value is the start milliseconds of the day (example : if the date is 20-09-2023, then it's value will the milliseconds of (20-09-2023 00:00:00)),
     * second value is the end milliseconds of the day (example : if the date is 20-09-2023, then it's value will the milliseconds of (20-09-2023 24:00:00))
     */
    fun getStartMillisecondOfAllDaysInMonth(month: Int, year: Int): List<Pair<Long, Long>> {

        val numberOfDays = getNumberOfDaysInMonth(month, year)

        val startCalendar = Calendar.getInstance()
        val endCalendar = Calendar.getInstance()

        val startingMillisOfDays = ArrayList<Pair<Long, Long>>()

        for (i in 1..numberOfDays) {

            startCalendar.set(year, month, i, 0, 0)
            endCalendar.set(year, month, i, 24, 0)
            startingMillisOfDays.add(Pair(startCalendar.time.time, endCalendar.time.time))
        }

        return startingMillisOfDays
    }

    /**
     * @param month - month (0-11)
     * @param year  - year (Ex: 2023)
     *
     * @return - Returns the list of a pair object for which the first value is the int value of the DAY_OF_WEEK and the second value is String value i.e., human readable form
     */
    fun getDayOfWeeksForEntireMonth(month: Int, year: Int): List<Pair<Int, String>> {

        val calendar = Calendar.getInstance()
        val numberOfDays = getNumberOfDaysInMonth(month, year)

        val daysList = ArrayList<Pair<Int, String>>()

        for (i in 1..numberOfDays) {

            calendar.set(year, month, i)
            val dayOfWeekInt = calendar.get(Calendar.DAY_OF_WEEK)
            val dayOfWeekString = SimpleDateFormat("EEEE").format(calendar.time)

            daysList.add(Pair(dayOfWeekInt, dayOfWeekString))
        }

        return daysList;
    }

}