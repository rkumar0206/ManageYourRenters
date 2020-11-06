package com.rohitthebest.manageyourrenters.ui.fragments.addContentFragments

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
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.datepicker.MaterialDatePicker
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.entity.Payment
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.database.entity.dataClasses.BillInfo
import com.rohitthebest.manageyourrenters.database.entity.dataClasses.ElectricityBillInfo
import com.rohitthebest.manageyourrenters.databinding.AddPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.fragments.PaymentFragmentArgs
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertPaymentToJSONString
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper.Companion.updateDocumentOnFireStore
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper.Companion.uploadDocumentToFireStore
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.toStringM
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlin.random.Random

@AndroidEntryPoint
class AddPaymentFragment : Fragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private val TAG = "AddPaymentFragment"

    private val paymentViewModel: PaymentViewModel by viewModels()

    private var _binding: FragmentAddPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddPaymentLayoutBinding

    private var receivedRenter: Renter? = null
    private var currentTimestamp = 0L

    private var monthList: List<String>? = null
    private var currencyList: List<String>? = null

    private var periodType: String = ""
    private var billMonth: String? = null
    private var billMonthNumber = 1
    private var currencySymbol: String? = null

    //if selected by_date method
    private var fromDateTimestamp: Long? = null
    private var tillDateTimeStamp: Long? = null
    private var numberOfDays: String? = ""

    private var lastPaymentInfo: Payment? = null
    private var isDueOrPaidInAdvance: String? = ""
    private var duesFromLastPayment: Double? = 0.0
    private var paidInAdvanceFromLastPayment: Double? = 0.0
    private var presentDue: Double? = 0.0
    private var presentPaidInAdvance: Double? = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.show()

        includeBinding = binding.include

        monthList = ArrayList()          //List of months
        currencyList = ArrayList()       //List of currency Symbols

        //List of months
        monthList = resources.getStringArray(R.array.months).toList()

        //List of currency symbols of different places
        currencyList = resources.getStringArray(R.array.currency_symbol).toList()

        setUpSpinnerMonth() //Setting the from(month) and till(month) spinners
        setUpCurrencySymbolList()

        getMessage()
        initListeners()
        textWatcher()

        getLastPaymentInfo()
    }

    private fun getLastPaymentInfo() {

        try {
            paymentViewModel.getAllPaymentsListOfRenter(receivedRenter?.key!!)
                .observe(viewLifecycleOwner, {

                    if (it.isNotEmpty()) {

                        Log.i(TAG, "getLastPaymentInfo: ")
                        lastPaymentInfo = it.first()
                        Log.d(TAG, "getLastPaymentInfo: $lastPaymentInfo")

                        GlobalScope.launch {

                            delay(100)

                            withContext(Dispatchers.Main) {

                                initialChanges()
                            }
                        }
                    } else {

                        initialChanges()
                        binding.progressBar.hide()
                    }

                })

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    PaymentFragmentArgs.fromBundle(it)
                }

                receivedRenter = ConversionWithGson.convertJSONtoRenter(args?.renterInfoMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initialChanges() {

        Log.i(TAG, "initialChanges: ")

        receivedRenter?.let {

            includeBinding.renterNameTV.text = it.name

            currentTimestamp = System.currentTimeMillis()

            includeBinding.dateTV.text = "Bill Issue Date : ${
                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                    currentTimestamp
                )
            }"

            includeBinding.timeTV.text = "Time : ${
                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                    currentTimestamp,
                    "hh:mm a"
                )
            }"

            periodType = getString(R.string.by_month)

            billMonth = monthList?.get(0)
            billMonthNumber = 1

            fromDateTimestamp = currentTimestamp
            tillDateTimeStamp = currentTimestamp
            numberOfDays = getString(R.string.same_day)
            includeBinding.byDateErrorMessageTV.text = numberOfDays

            includeBinding.fromDateTV.setDateInTextView(fromDateTimestamp)

            includeBinding.tillDateTV.setDateInTextView(tillDateTimeStamp)

            if (lastPaymentInfo != null) {

                if (lastPaymentInfo!!.bill?.billPeriodType == getString(R.string.by_month)) {

                    showByMonthAndHideByDateView()
                    initializeByMonthField()

                } else if (lastPaymentInfo!!.bill?.billPeriodType == getString(R.string.by_date)) {

                    includeBinding.periodTypeRG.check(includeBinding.byDateRB.id)
                    hideByMonthAndShowByDateView()
                    initialiseByDateField()
                }

                includeBinding.houseRentET.editText?.setText(lastPaymentInfo?.houseRent)

                if (lastPaymentInfo?.electricBill?.isTakingElectricBill != getString(R.string.f)) {

                    includeBinding.previousReadingET.setText(lastPaymentInfo?.electricBill?.currentReading.toString())

                    includeBinding.currentReadingET.setText((lastPaymentInfo?.electricBill?.currentReading.toString()))

                    includeBinding.rateET.setText(lastPaymentInfo?.electricBill?.rate.toString())

                    calculateTotalBill()
                }

                if (lastPaymentInfo?.isTakingParkingBill != getString(R.string.f)) {

                    includeBinding.parkingET.editText?.setText(lastPaymentInfo?.parkingRent)
                }

                initializeLastPaymentsDuesAndAdvance()

            } else {

                currencySymbol = currencyList?.get(0)

                calculateTotalBill()

                includeBinding.duesOfLatsPaymentTV.text =
                    "There are no dues and no money given in advance."
            }
        }

        binding.progressBar.hide()
    }

    @SuppressLint("SetTextI18n")
    private fun initializeLastPaymentsDuesAndAdvance() {

        when (lastPaymentInfo?.isDueOrPaidInAdvance) {

            getString(R.string.due) -> {

                includeBinding.duesOfLatsPaymentTV.changeTextColor(R.color.color_orange)
                includeBinding.duesOfLatsPaymentTV.text =
                    "Dues of last payments : + ${lastPaymentInfo?.bill?.currencySymbol}${lastPaymentInfo?.dueAmount}"

                duesFromLastPayment = lastPaymentInfo?.dueAmount?.toDouble()

                calculateTotalBill()
            }
            getString(R.string.paid_in_advance) -> {

                includeBinding.duesOfLatsPaymentTV.changeTextColor(R.color.color_green)
                includeBinding.duesOfLatsPaymentTV.text =
                    "Paid in advance in last payments : - ${lastPaymentInfo?.bill?.currencySymbol}${lastPaymentInfo?.paidInAdvanceAmount}"

                paidInAdvanceFromLastPayment = lastPaymentInfo?.paidInAdvanceAmount?.toDouble()

                calculateTotalBill()
            }
            else -> {

                includeBinding.duesOfLatsPaymentTV.text =
                    "There are no dues and no money given in advance."
            }
        }

    }

    private fun initialiseByDateField() {

        fromDateTimestamp = lastPaymentInfo!!.bill?.billDateTill
        tillDateTimeStamp = currentTimestamp

        includeBinding.fromDateTV.setDateInTextView(fromDateTimestamp)

        includeBinding.tillDateTV.setDateInTextView(tillDateTimeStamp)

        numberOfDays = calculateNumberOfDays(fromDateTimestamp!!, tillDateTimeStamp!!)

        setNumberOfDays()
    }

    private fun initializeByMonthField() {

        billMonthNumber = lastPaymentInfo?.bill?.billMonthNumber!! + 1

        includeBinding.monthSelectSpinner.setSelection(billMonthNumber - 1)

    }

    private fun setUpSpinnerMonth() {

        includeBinding.monthSelectSpinner.let { spinner ->

            if (monthList != null) {
                spinner.adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.support_simple_spinner_dropdown_item,
                    monthList!!
                )

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                        spinner.setSelection(0)
                        billMonth = monthList!![0]
                        billMonthNumber = 1
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                        spinner.setSelection(position)
                        billMonth = monthList!![position]
                        billMonthNumber = position + 1
                    }
                }
            }
        }

    }

    private fun setUpCurrencySymbolList() {

        includeBinding.moneySymbolSpinner.let { spinner ->

            if (currencyList != null) {
                spinner.adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.support_simple_spinner_dropdown_item,
                    currencyList!!
                )

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                        spinner.setSelection(0)
                        currencySymbol = currencyList!![0]
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                        spinner.setSelection(position)
                        currencySymbol = currencyList!![position]
                        calculateTotalBill()
                        //showToast(requireContext(), "${currencyList!![position]} is selected..")
                    }
                }
            }
        }

    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
        includeBinding.seeTotalBtn.setOnClickListener(this)


        includeBinding.fromDateTV.setOnClickListener(this)
        includeBinding.dateRangePickerBtn.setOnClickListener(this)
        includeBinding.tillDateTV.setOnClickListener(this)

        includeBinding.periodTypeRG.setOnCheckedChangeListener(this)
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

        val endDate = if (fromDateTimestamp!! > tillDateTimeStamp!!) {

            fromDateTimestamp
        } else {

            tillDateTimeStamp
        }

        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setSelection(Pair(fromDateTimestamp, endDate))
            .setTitleText("Select date range")
            .build()

        builder.show(requireActivity().supportFragmentManager, "date_range_picker")


        builder.addOnPositiveButtonClickListener {

            fromDateTimestamp = it.first
            tillDateTimeStamp = it.second
            includeBinding.fromDateTV.setDateInTextView(it.first)
            includeBinding.tillDateTV.setDateInTextView(it.second)

            numberOfDays = calculateNumberOfDays(it.first!!, it.second!!)

            setNumberOfDays()
        }
    }

    private fun setNumberOfDays() {

        includeBinding.byDateErrorMessageTV.text = when {
            numberOfDays!!.toInt() > 0 -> {

                includeBinding.byDateErrorMessageTV.changeTextColor(R.color.color_green)

                "Number of Days : $numberOfDays"
            }
            numberOfDays!!.toInt() < 0 -> {

                includeBinding.byDateErrorMessageTV.changeTextColor(R.color.color_orange)

                "Please enter a valid date"
            }
            else -> {

                includeBinding.byDateErrorMessageTV.changeTextColor(R.color.color_green)

                numberOfDays = getString(R.string.same_day)
                getString(R.string.same_day)
            }
        }

    }

    private fun TextView.changeTextColor(color: Int) {

        this.setTextColor(ContextCompat.getColor(requireContext(), color))
    }

    private fun calculateNumberOfDays(startDate: Long, endDate: Long): String {

        return ((endDate - startDate) / (1000 * 60 * 60 * 24)).toInt().toString()

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

            includeBinding.electricityErrorTextTV.changeTextColor(R.color.color_orange)
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

        includeBinding.electricityErrorTextTV.changeTextColor(R.color.color_green)
        includeBinding.electricityErrorTextTV.text =
            getString(R.string.total, String.format("%.2f", totalElectricBill))

        return totalElectricBill
    }

    //total rent vars
    private var parkingBill: Double = 0.0
    private var houseRent: Double = 0.0
    private var extraBillAmount: Double = 0.0
    private var amountPaid: Double = 0.0
    private var totalRent: Double = 0.0

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

        totalRent =
            ((totalElectricBill + parkingBill + houseRent + extraBillAmount + duesFromLastPayment!!) - paidInAdvanceFromLastPayment!!)

        includeBinding.totalTV.text = "$currencySymbol ${String.format("%.2f", totalRent)}"

        amountPaid = if (includeBinding.amountPaidET.editText?.text.toString().trim() == "") {

            includeBinding.amountPaidET.editText?.setText(totalRent.toString())
            totalRent
        } else {

            includeBinding.amountPaidET.editText?.text.toString().trim().toDouble()
        }

        return totalRent.toString()
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

                //updating last payment (i.e. due and paid in advance should be 0.0 of last payment)
                updateLastPayment()

                saveToDatabase()

            }.negativeButton(text = "Edit") {

                it.dismiss()
            }
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun updateLastPayment() {

        if (lastPaymentInfo?.isDueOrPaidInAdvance == getString(R.string.due) ||
            lastPaymentInfo?.isDueOrPaidInAdvance == getString(R.string.paid_in_advance)
        ) {

            val map = HashMap<String, Any?>()

            if (lastPaymentInfo?.dueAmount?.toDouble() != 0.0) {

                map["dueAmount"] = "0.0"
            }

            if (lastPaymentInfo?.paidInAdvanceAmount?.toDouble() != 0.0) {

                map["paidInAdvanceAmount"] = "0.0"
            }

            //fireStore saves isDueOrPaidInAdvance as dueOrPaidInAdvance
            map["dueOrPaidInAdvance"] = ""

            lastPaymentInfo?.isDueOrPaidInAdvance = ""
            lastPaymentInfo?.dueAmount = "0.0"
            lastPaymentInfo?.paidInAdvanceAmount = "0.0"

            if (isInternetAvailable(requireContext())) {

                if (lastPaymentInfo?.isSynced == getString(R.string.t)) {

                    updateDocumentOnFireStore(
                        requireContext(),
                        map,
                        getString(R.string.payments),
                        lastPaymentInfo?.key!!
                    )

                    paymentViewModel.insertPayment(lastPaymentInfo!!)
                } else {

                    lastPaymentInfo?.isSynced = getString(R.string.t)

                    uploadDocumentToFireStore(
                        requireContext(),
                        convertPaymentToJSONString(lastPaymentInfo!!),
                        getString(R.string.payments),
                        lastPaymentInfo?.key!!
                    )

                    paymentViewModel.insertPayment(lastPaymentInfo!!)
                }

            } else {

                lastPaymentInfo?.isSynced = getString(R.string.f)
                paymentViewModel.insertPayment(lastPaymentInfo!!)
            }

        }
    }

    private fun saveToDatabase() {

        if (periodType == getString(R.string.by_month)) {

            fromDateTimestamp = 0L
            tillDateTimeStamp = 0L
            numberOfDays = ""
        } else {

            billMonth = ""
            billMonthNumber = 0
        }

        val billInfo = BillInfo(
            periodType,
            fromDateTimestamp,
            tillDateTimeStamp,
            numberOfDays,
            billMonth,
            billMonthNumber,
            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                currentTimestamp,
                "yyyy"
            )?.toInt(),
            currencySymbol
        )

        val isTakingElectricBill = if (calculateElectricBill() != 0.0) {

            getString(R.string.t)
        } else {

            getString(R.string.f)
        }

        val electricityBillInfo = ElectricityBillInfo(
            isTakingElectricBill,
            previousReading,
            currentReading,
            rate,
            difference,
            totalElectricBill.toString()
        )

        val payment = Payment(

            currentTimestamp,
            receivedRenter?.key!!,
            billInfo,
            electricityBillInfo,
            houseRent.toString(),
            if (parkingBill == 0.0) {
                getString(R.string.f)
            } else {
                getString(R.string.t)
            },
            parkingBill.toString(),
            includeBinding.extraFieldNameET.text.toString().trim(),
            extraBillAmount.toString(),
            amountPaid.toString(),
            isDueOrPaidInAdvance.toString(),
            presentDue.toString(),
            presentPaidInAdvance.toString(),
            includeBinding.addNoteET.text.toString().trim(),
            totalRent.toString(),
            getUid()!!,
            "${System.currentTimeMillis().toStringM(69)}_${
                Random.nextLong(
                    100,
                    9223372036854775
                ).toStringM(69)
            }_${getUid()}",
            getString(R.string.f)
        )


        if (isInternetAvailable(requireContext())) {

            payment.isSynced = getString(R.string.t)

            addToFireStore(payment)

            paymentViewModel.insertPayment(payment)
            requireActivity().onBackPressed()
        } else {

            payment.isSynced = getString(R.string.f)

            paymentViewModel.insertPayment(payment)
            requireActivity().onBackPressed()
        }
    }

    private fun addToFireStore(payment: Payment) {

        uploadDocumentToFireStore(
            requireContext(),
            convertPaymentToJSONString(payment),
            getString(R.string.payments),
            payment.key
        )

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

                "$billMonth, ${
                    WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                        currentTimestamp,
                        "yyyy"
                    )
                }"
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

        customView.findViewById<TextView>(R.id.showBill_dueOfLastPayAmount).text =
            "+ $currencySymbol ${String.format("%.2f", duesFromLastPayment)}"

        customView.findViewById<TextView>(R.id.showBill_paidInAdvanceInlastPayAmount).text =
            "- $currencySymbol ${String.format("%.2f", paidInAdvanceFromLastPayment)}"

        customView.findViewById<TextView>(R.id.showBill_dueAmount).text =
            when {
                amountPaid < totalRent -> {
                    isDueOrPaidInAdvance = getString(R.string.due)

                    presentPaidInAdvance = 0.0
                    presentDue = totalRent - amountPaid

                    "$currencySymbol ${String.format("%.2f", presentDue)}"
                }
                amountPaid > totalRent -> {

                    isDueOrPaidInAdvance = getString(R.string.paid_in_advance)

                    presentDue = 0.0
                    presentPaidInAdvance = amountPaid - totalRent

                    customView.findViewById<TextView>(R.id.show_billDueOrArrearTV).text =
                        getString(R.string.paid_in_advance)

                    "$currencySymbol ${String.format("%.2f", presentPaidInAdvance)}"
                }
                else -> {
                    isDueOrPaidInAdvance = ""
                    "$currencySymbol 0.0"
                }
            }

        customView.findViewById<TextView>(R.id.showBill_netDemand).text =
            "$currencySymbol $totalRent"
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

                        s.toString().trim().toDouble() < totalRent -> {

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
            periodType = getString(R.string.by_month)
            includeBinding.byDateCL.hide()
            includeBinding.monthSelectSpinner.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideByMonthAndShowByDateView() {

        try {
            periodType = getString(R.string.by_date)
            includeBinding.byDateCL.show()
            includeBinding.monthSelectSpinner.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun TextView.setDateInTextView(timeStamp: Long?, pattern: String = "dd-MM-yyyy") {

        this.text = WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
            timeStamp, pattern
        )

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