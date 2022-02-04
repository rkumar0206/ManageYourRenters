package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters.addContentFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.*
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.databinding.AddPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private const val TAG = "AddPaymentFragment"

@AndroidEntryPoint
class AddPaymentFragment : Fragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private val paymentViewModel: RenterPaymentViewModel by viewModels()

    private var _binding: FragmentAddPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddPaymentLayoutBinding

    private var receivedRenter: Renter? = null
    private var currentTimestamp = 0L

    private lateinit var monthList: List<String>
    private lateinit var currencyList: List<String>
    private lateinit var yearList: ArrayList<Int>

    private var periodType: BillPeriodType = BillPeriodType.BY_MONTH
    private var billMonth: String = ""
    private var billMonthNumber = 1
    private var currencySymbol: String = ""
    private var selectedYear: Int = 0

    //if selected by_date method
    private var fromDateTimestamp: Long = 0L
    private var tillDateTimeStamp: Long = 0L
    private var numberOfDays: Int = 0

    private var lastPaymentInfo: RenterPayment? = null
    private var duesOrAdvanceAmount: Double = 0.0
    private var presentDue: Double? = 0.0
    private var presentPaidInAdvance: Double? = 0.0

    private var isPaymentAdded = false

    private lateinit var workingWithDateAndTime: WorkingWithDateAndTime

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.show()

        workingWithDateAndTime = WorkingWithDateAndTime()

        includeBinding = binding.include

        monthList = ArrayList()          //List of months
        currencyList = ArrayList()       //List of currency Symbols

        //List of months
        monthList = resources.getStringArray(R.array.months).toList()

        //List of currency symbols of different places
        currencyList = resources.getStringArray(R.array.currency_symbol).toList()

        setUpSpinnerMonth() //Setting the from(month) and till(month) spinners
        setUpCurrencySymbolList()

        selectedYear =
            workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                System.currentTimeMillis(),
                "yyyy"
            )?.toInt()!!

        yearList = populateYearList(selectedYear)

        setUpSpinnerYear()

        getMessage()
        initListeners()
        textWatcher()
    }

    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    AddPaymentFragmentArgs.fromBundle(it)
                }

                receivedRenter = convertJSONtoRenter(args?.renterInfoMessage)
                getLastPaymentInfo()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLastPaymentInfo() {

        paymentViewModel.getLastRenterPayment(receivedRenter?.key!!)
            .observe(viewLifecycleOwner) { payment ->

                if ((payment != null) && !isPaymentAdded) {

                    lastPaymentInfo = payment

                    Log.d(TAG, "getLastPaymentInfo: $lastPaymentInfo")

                    lifecycleScope.launch {

                        delay(100)

                        withContext(Dispatchers.Main) {

                            initialChanges()
                        }
                    }

                } else {

                    initialChanges()
                    binding.progressBar.hide()
                }
            }
    }


    private fun initialChanges() {

        Log.i(TAG, "initialChanges: ")

        receivedRenter?.let {

            includeBinding.renterNameTV.text = it.name

            currentTimestamp = System.currentTimeMillis()

            setDateAndTimeInTextViews()

            periodType = BillPeriodType.BY_MONTH

            billMonth = monthList[0]
            billMonthNumber = 1

            fromDateTimestamp = currentTimestamp
            tillDateTimeStamp = currentTimestamp
            numberOfDays = 0
            includeBinding.byDateErrorMessageTV.text = numberOfDays.toString()

            includeBinding.fromDateTV.setDateInTextView(fromDateTimestamp)

            includeBinding.tillDateTV.setDateInTextView(tillDateTimeStamp)

            if (lastPaymentInfo != null) {

                if (lastPaymentInfo!!.billPeriodInfo.billPeriodType == BillPeriodType.BY_MONTH) {

                    showByMonthAndHideByDateView()
                    initializeByMonthField()

                } else if (lastPaymentInfo!!.billPeriodInfo.billPeriodType == BillPeriodType.BY_DATE) {

                    includeBinding.periodTypeRG.check(includeBinding.byDateRB.id)
                    hideByMonthAndShowByDateView()
                    initialiseByDateField()
                }

                includeBinding.houseRentET.editText?.setText(lastPaymentInfo?.houseRent.toString())

                if (lastPaymentInfo?.isElectricityBillIncluded != false) {

                    includeBinding.previousReadingET.setText(lastPaymentInfo?.electricityBillInfo?.currentReading.toString())

                    includeBinding.currentReadingET.setText((lastPaymentInfo?.electricityBillInfo?.currentReading.toString()))

                    includeBinding.rateET.setText(lastPaymentInfo?.electricityBillInfo?.rate.toString())

                    calculateTotalBill()
                }

                includeBinding.parkingET.editText?.setText(lastPaymentInfo?.parkingRent.toString())

                initializeLastPaymentsDuesAndAdvance()

            } else {

                currencySymbol = currencyList[0]

                calculateTotalBill()

                includeBinding.duesOfLastPaymentTV.text =
                    getString(R.string.duesOfLastPayment_message)
            }
        }

        binding.progressBar.hide()
    }

    @SuppressLint("SetTextI18n")
    private fun setDateAndTimeInTextViews() {

        includeBinding.dateTV.text = "Payment Date : ${
            workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                currentTimestamp
            )
        }"

        includeBinding.timeTV.text = "Time : ${
            workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                currentTimestamp,
                "hh:mm a"
            )
        }"
    }

    @SuppressLint("SetTextI18n")
    private fun initializeLastPaymentsDuesAndAdvance() {

        Log.d(TAG, "initializeLastPaymentsDuesAndAdvance: ")

        when {
            receivedRenter?.dueOrAdvanceAmount!! < 0.0 -> {

                includeBinding.duesOfLastPaymentTV.changeTextColor(
                    requireContext(),
                    R.color.color_orange
                )
                includeBinding.duesOfLastPaymentTV.text =
                    "Dues from last payments : + ${lastPaymentInfo?.currencySymbol} ${
                        abs(receivedRenter?.dueOrAdvanceAmount!!)
                    }"

                duesOrAdvanceAmount = receivedRenter?.dueOrAdvanceAmount!!
            }
            receivedRenter?.dueOrAdvanceAmount!! > 0.0 -> {

                includeBinding.duesOfLastPaymentTV.changeTextColor(
                    requireContext(),
                    R.color.color_green
                )
                includeBinding.duesOfLastPaymentTV.text =
                    "Paid in advance in last payments : - ${lastPaymentInfo?.currencySymbol}${receivedRenter?.dueOrAdvanceAmount}"

                duesOrAdvanceAmount = receivedRenter?.dueOrAdvanceAmount!!
            }
            else -> {

                includeBinding.duesOfLastPaymentTV.text =
                    "There are no dues and no money given in advance."
            }
        }

        calculateTotalBill()

    }

    private fun initialiseByDateField() {

        fromDateTimestamp = lastPaymentInfo?.billPeriodInfo?.renterBillDateType?.toBillDate!!
        tillDateTimeStamp = currentTimestamp

        includeBinding.fromDateTV.setDateInTextView(fromDateTimestamp)

        includeBinding.tillDateTV.setDateInTextView(tillDateTimeStamp)

        numberOfDays = calculateNumberOfDays(fromDateTimestamp, tillDateTimeStamp)

        setNumberOfDays()
    }

    private fun initializeByMonthField() {

        billMonthNumber =
            if (lastPaymentInfo?.billPeriodInfo?.renterBillMonthType?.forBillMonth!! + 1 > 12) {
                1
            } else {

                lastPaymentInfo?.billPeriodInfo?.renterBillMonthType!!.forBillMonth + 1
            }

        includeBinding.monthSelectSpinner.setSelection(billMonthNumber - 1)

        // setting the selected year to be the year of november if this
        // payment is for month of december, because if the payment of
        // december is paid in the month of january of the next year
        // then the selected year will be increased by 1

        if (billMonthNumber == 12) {

            selectedYear = lastPaymentInfo?.billPeriodInfo?.billYear!!
            includeBinding.selectYearSpinner.setSelection(1)
        }
    }

    private fun setUpSpinnerMonth() {

        includeBinding.monthSelectSpinner.let { spinner ->

            spinner.adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                monthList
            )

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {

                    spinner.setSelection(0)
                    billMonth = monthList[0]
                    billMonthNumber = 1
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    spinner.setSelection(position)
                    billMonth = monthList[position]
                    billMonthNumber = position + 1
                }
            }
        }

    }

    private fun setUpSpinnerYear() {

        includeBinding.selectYearSpinner.apply {

            adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                yearList
            )

            onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                        setSelection(position)
                        selectedYear = yearList[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                        setSelection(0)
                        selectedYear = yearList[0]
                    }
                }

        }
    }

    private fun populateYearList(selectedYear: Int): ArrayList<Int> {

        val yearList = ArrayList<Int>()

        for (year in selectedYear downTo selectedYear - 5) {

            yearList.add(year)
        }

        return yearList
    }

    private fun setUpCurrencySymbolList() {

        includeBinding.moneySymbolSpinner.setCurrencySymbol(

            requireContext(),
        ) { position ->

            currencySymbol = currencyList[position]
            calculateTotalBill()
        }
    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
        includeBinding.seeTotalBtn.setOnClickListener(this)
        includeBinding.calculateElectrictyBtn.setOnClickListener(this)

        includeBinding.fromDateTV.setOnClickListener(this)
        includeBinding.dateRangePickerBtn.setOnClickListener(this)
        includeBinding.tillDateTV.setOnClickListener(this)

        includeBinding.periodTypeRG.setOnCheckedChangeListener(this)
        includeBinding.dateContainer.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {

        when (v?.id) {

            binding.saveBtn.id -> {


                if (validateForm()) {

                    calculateTotalBill()
                    showBillInBottomSheet()
                }
            }

            includeBinding.seeTotalBtn.id -> {

                includeBinding.amountPaidET.editText?.setText(calculateTotalBill())
            }

            includeBinding.calculateElectrictyBtn.id -> {

                calculateElectricBill()
            }

            includeBinding.dateContainer.id -> {

                Functions.showDateAndTimePickerDialog(
                    requireContext(),
                    workingWithDateAndTime.convertMillisecondsToCalendarInstance(currentTimestamp),
                    false,
                    if (lastPaymentInfo != null) lastPaymentInfo?.created!! else 0L
                ) { calendar ->

                    currentTimestamp = calendar.timeInMillis
                    setDateAndTimeInTextViews()
                }

            }

            binding.backBtn.id -> {

                requireActivity().onBackPressed()
            }

        }

        if (v?.id == includeBinding.dateRangePickerBtn.id || v?.id == includeBinding.fromDateTV.id
            || v?.id == includeBinding.tillDateTV.id
        ) {

            showDateRangePickerDialog()
        }
    }

    private fun validateForm(): Boolean {

        if (includeBinding.houseRentET.editText?.text.toString().trim().isEmpty()) {

            includeBinding.houseRentET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        return includeBinding.houseRentET.error == null
    }

    private fun showDateRangePickerDialog() {

        val endDate = if (fromDateTimestamp > tillDateTimeStamp) {

            fromDateTimestamp
        } else {

            tillDateTimeStamp
        }

        Functions.showDateRangePickerDialog(
            fromDateTimestamp,
            endDate,
            {
                requireActivity().supportFragmentManager
            },
            { dates ->

                fromDateTimestamp = dates.first
                tillDateTimeStamp = dates.second
                includeBinding.fromDateTV.setDateInTextView(dates.first)
                includeBinding.tillDateTV.setDateInTextView(dates.second)

                numberOfDays = calculateNumberOfDays(dates.first!!, dates.second!!)

                setNumberOfDays()
            },
            true
        )
    }

    private fun setNumberOfDays() {

        includeBinding.byDateErrorMessageTV.text = when {
            numberOfDays > 0 -> {

                includeBinding.byDateErrorMessageTV.changeTextColor(
                    requireContext(),
                    R.color.color_green
                )

                "Number of Days : $numberOfDays"
            }
            numberOfDays < 0 -> {

                includeBinding.byDateErrorMessageTV.changeTextColor(
                    requireContext(),
                    R.color.color_orange
                )

                "Please enter a valid date"
            }
            else -> {

                includeBinding.byDateErrorMessageTV.changeTextColor(
                    requireContext(),
                    R.color.color_green
                )

                numberOfDays = 0
                getString(R.string.same_day)
            }
        }

    }

    private fun calculateNumberOfDays(startDate: Long, endDate: Long): Int {

        return ((endDate - startDate) / (1000 * 60 * 60 * 24)).toInt()

    }

    //electricity vars
    private var previousReading: Double = 0.0
    private var currentReading: Double = 0.0
    private var rate: Double = 0.0
    private var difference: Double = 0.0
    private var totalElectricBill: Double = 0.0

    private fun calculateElectricBill(): Double {

        previousReading = if (includeBinding.previousReadingET.text.toString().trim() == "") {

            0.0
        } else {

            includeBinding.previousReadingET.text.toString().trim().toDouble()
        }

        currentReading = if (includeBinding.currentReadingET.text.toString().trim() == "") {

            0.0
        } else {

            includeBinding.currentReadingET.text.toString().trim().toDouble()
        }

        if (previousReading > currentReading) {

            includeBinding.electricityErrorTextTV.changeTextColor(
                requireContext(),
                R.color.color_orange
            )
            includeBinding.electricityErrorTextTV.text =
                getString(R.string.electricity_error_message)
            return 0.0
        }

        rate = if (includeBinding.rateET.text.toString().trim() == "") {

            0.0
        } else {

            includeBinding.rateET.text.toString().trim().toDouble()
        }

        difference = currentReading - previousReading
        totalElectricBill = difference * rate

        includeBinding.electricityErrorTextTV.changeTextColor(requireContext(), R.color.color_green)
        includeBinding.electricityErrorTextTV.text =
            getString(R.string.total, String.format("%.2f", totalElectricBill))

        return totalElectricBill
    }

    //total rent vars
    private var parkingBill: Double = 0.0
    private var houseRent: Double = 0.0
    private var extraBillAmount: Double = 0.0
    private var amountPaid: Double = 0.0
    private var netDemand: Double = 0.0

    @SuppressLint("SetTextI18n")
    private fun calculateTotalBill(): String {

        calculateElectricBill()

        parkingBill = if (includeBinding.parkingET.editText?.text.toString().trim() != "") {

            includeBinding.parkingET.editText?.text.toString().trim().toDouble()

        } else {

            0.0
        }

        houseRent = if (includeBinding.houseRentET.editText?.text.toString().trim() != "") {

            includeBinding.houseRentET.editText?.text.toString().trim().toDouble()
        } else {
            0.0
        }

        extraBillAmount = if (includeBinding.extraAmountET.text.toString().trim() != "") {

            includeBinding.extraAmountET.text.toString().trim().toDouble()
        } else {
            0.0
        }

        netDemand =
            ((totalElectricBill + parkingBill + houseRent + extraBillAmount) - duesOrAdvanceAmount)

        includeBinding.totalTV.text = "$currencySymbol ${String.format("%.2f", netDemand)}"

        amountPaid = if (includeBinding.amountPaidET.editText?.text.toString().trim() == ""
            || includeBinding.amountPaidET.editText?.text.toString().trim() == "0.0"
        ) {

            includeBinding.amountPaidET.editText?.setText(netDemand.toString())
            netDemand
        } else {

            includeBinding.amountPaidET.editText?.text.toString().trim().toDouble()
        }

        return netDemand.toString()
    }

    private fun showBillInBottomSheet() {

        try {
            MaterialDialog(requireContext(), BottomSheet()).show {

                title(text = "Your Bill")

                customView(
                    R.layout.show_bill_layout,
                    scrollable = true,
                    noVerticalPadding = true
                )

                initializeValues(this.getCustomView())
            }.positiveButton(text = "Add Payment") {

                initPayment()

            }.negativeButton(text = "Edit") {

                it.dismiss()
            }
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initializeValues(customView: View) {

        //renter info
        customView.findViewById<TextView>(R.id.showBill_renterName).text = receivedRenter?.name
        customView.findViewById<TextView>(R.id.showBill_renterMobile).text =
            receivedRenter?.mobileNumber
        customView.findViewById<TextView>(R.id.showBill_renterAddress).text =
            receivedRenter?.address

        //billing parameter
        customView.findViewById<TextView>(R.id.showBill_billDate)
            .setDateInTextView(currentTimestamp)
        customView.findViewById<TextView>(R.id.showBill_billTime)
            .setDateInTextView(currentTimestamp, "hh:mm a")
        customView.findViewById<TextView>(R.id.showBill_billPeriod).text =
            if (includeBinding.periodTypeRG.checkedRadioButtonId == includeBinding.byMonthRB.id) {

                "$billMonth, $selectedYear"
            } else {
                "${
                    includeBinding.fromDateTV.text.toString().trim()
                } to ${includeBinding.tillDateTV.text.toString().trim()}"
            }

        //electricity
        customView.findViewById<TextView>(R.id.showBill_previousReading).text =
            "${String.format("%.2f", previousReading)} unit(s)"
        customView.findViewById<TextView>(R.id.showBill_currentReading).text =
            "${String.format("%.2f", currentReading)} unit(s)"
        customView.findViewById<TextView>(R.id.showBill_rate).text =
            "${String.format("%.2f", rate)} per/unit"
        customView.findViewById<TextView>(R.id.showBill_difference).text =
            "${String.format("%.2f", difference)} unit(s)"
        customView.findViewById<TextView>(R.id.showBill_electricity_total).text =
            "$currencySymbol ${String.format("%.2f", totalElectricBill)}"

        //total rent
        customView.findViewById<TextView>(R.id.showBill_houseRent).text =
            "$currencySymbol ${String.format("%.2f", houseRent)}"

        customView.findViewById<TextView>(R.id.showBill_parking).text =
            "$currencySymbol ${String.format("%.2f", parkingBill)}"

        customView.findViewById<TextView>(R.id.showBill_electricity).text =
            "$currencySymbol ${String.format("%.2f", totalElectricBill)}"

        customView.findViewById<TextView>(R.id.showBill_extraFieldName).text =
            if (includeBinding.extraFieldNameET.text.toString().trim() == "") {

                "Extra"
            } else {
                includeBinding.extraFieldNameET.text.toString().trim()
            }

        customView.findViewById<TextView>(R.id.showBill_extraFieldAmount).text =
            if (includeBinding.extraAmountET.text.toString().trim() == "") {

                "$currencySymbol 0.0"
            } else {

                "$currencySymbol ${
                    String.format(
                        "%.2f",
                        includeBinding.extraAmountET.text.toString().trim().toDouble()
                    )
                }"
            }

        customView.findViewById<TextView>(R.id.showBill_AmountPaid).text =
            "$currencySymbol ${String.format("%.2f", amountPaid)}"

        when {
            duesOrAdvanceAmount < 0.0 -> {

                customView.findViewById<TextView>(R.id.showBill_dueOfLastPayAmount).text =
                    "+ $currencySymbol ${String.format("%.2f", abs(duesOrAdvanceAmount))}"

                customView.findViewById<TextView>(R.id.showBill_paidInAdvanceInlastPayAmount).text =
                    "- $currencySymbol 0.0"

            }
            duesOrAdvanceAmount > 0.0 -> {

                customView.findViewById<TextView>(R.id.showBill_dueOfLastPayAmount).text =
                    "+ $currencySymbol 0.0"

                customView.findViewById<TextView>(R.id.showBill_paidInAdvanceInlastPayAmount).text =
                    "- $currencySymbol ${String.format("%.2f", duesOrAdvanceAmount)}"
            }
            else -> {

                customView.findViewById<TextView>(R.id.showBill_dueOfLastPayAmount).text =
                    "+ $currencySymbol 0.0"

                customView.findViewById<TextView>(R.id.showBill_paidInAdvanceInlastPayAmount).text =
                    "- $currencySymbol 0.0"
            }
        }

        customView.findViewById<TextView>(R.id.showBill_dueAmount).text =
            when {
                amountPaid < netDemand -> {

                    presentDue = netDemand - amountPaid

                    "$currencySymbol ${String.format("%.2f", presentDue)}"
                }
                amountPaid > netDemand -> {

                    presentPaidInAdvance = amountPaid - netDemand

                    customView.findViewById<TextView>(R.id.show_billDueOrArrearTV).text =
                        getString(R.string.paid_in_advance)

                    "$currencySymbol ${String.format("%.2f", presentPaidInAdvance)}"
                }
                else -> {
                    "$currencySymbol 0.0"
                }
            }

        customView.findViewById<TextView>(R.id.showBill_netDemand).text =
            "$currencySymbol ${netDemand.format(2)}"
    }

    private fun initPayment() {

        if (!isPaymentAdded) {

            val billInfo = RenterBillPeriodInfo(
                periodType,
                if (periodType == BillPeriodType.BY_MONTH) {
                    RenterBillMonthType(
                        billMonthNumber,
                        billMonthNumber,
                        1
                    )
                } else {
                    null
                },
                if (periodType == BillPeriodType.BY_DATE) {

                    RenterBillDateType(
                        fromDateTimestamp,
                        tillDateTimeStamp,
                        numberOfDays
                    )
                } else {
                    null
                },
                selectedYear
            )

            val isElectricBillIncluded = calculateElectricBill() != 0.0

            val electricityBillInfo = RenterElectricityBillInfo(
                previousReading,
                currentReading,
                rate,
                difference,
                totalElectricBill
            )

            val payment = RenterPayment(
                generateKey(appendString = "_${getUid()}"),
                currentTimestamp,
                currentTimestamp,
                receivedRenter?.key!!,
                currencySymbol,
                billInfo,
                isElectricBillIncluded,
                if (isElectricBillIncluded) {
                    electricityBillInfo
                } else {
                    null
                },
                houseRent,
                parkingBill,
                if (includeBinding.extraFieldNameET.text.toString().trim().isValid()) {
                    RenterPaymentExtras(
                        includeBinding.extraFieldNameET.text.toString().trim(),
                        extraBillAmount
                    )
                } else {
                    null
                },
                netDemand,
                amountPaid,
                includeBinding.addNoteET.text.toString().trim(),
                getUid()!!,
                true
            )

            paymentViewModel.insertPayment(requireContext(), payment)

            requireActivity().onBackPressed()
        }
    }


    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        if (checkedId == includeBinding.byMonthRB.id) {

            showByMonthAndHideByDateView()
        } else {

            hideByMonthAndShowByDateView()
        }

    }

    private fun textWatcher() {

        includeBinding.houseRentET.editText?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.isEmpty()!!) {

                    includeBinding.houseRentET.error = EDIT_TEXT_EMPTY_MESSAGE
                } else {

                    includeBinding.houseRentET.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        includeBinding.amountPaidET.editText?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.toString()?.trim()?.isNotEmpty()!!) {

                    when {

                        s.toString().trim().toDouble() < netDemand -> {

                            includeBinding.amountPaidET.editText?.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.color_orange
                                )
                            )
                        }

                        else -> {

                            includeBinding.amountPaidET.editText?.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.color_green
                                )
                            )
                        }

                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showByMonthAndHideByDateView() {

        try {
            periodType = BillPeriodType.BY_MONTH
            includeBinding.byDateCL.hide()
            includeBinding.monthSelectSpinner.show()
            includeBinding.selectYearSpinner.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideByMonthAndShowByDateView() {

        try {
            periodType = BillPeriodType.BY_DATE
            includeBinding.byDateCL.show()
            includeBinding.monthSelectSpinner.hide()
            includeBinding.selectYearSpinner.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        try {
            hideKeyBoard(requireActivity())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        _binding = null
    }

}