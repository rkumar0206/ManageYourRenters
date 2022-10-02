package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters.addContentFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.*
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.databinding.AddPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.others.Constants.RENTER_PAYMENT_CONFIRMATION_BILL_KEY
import com.rohitthebest.manageyourrenters.ui.fragments.SupportingDocumentDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlin.math.abs

private const val TAG = "AddPaymentFragment"

@AndroidEntryPoint
class AddPaymentFragment : Fragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener,
    CompoundButton.OnCheckedChangeListener,
    SupportingDocumentDialogFragment.OnBottomSheetDismissListener,
    ShowBillForConfirmationBottomSheetFragment.OnPaymentConfirmationBottomSheetDismissListener {

    private val paymentViewModel: RenterPaymentViewModel by viewModels()

    private var _binding: FragmentAddPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddPaymentLayoutBinding
    private var receivedRenter: Renter? = null
    private var currentTimestamp = 0L
    private lateinit var supportingDocmtHelperModel: SupportingDocumentHelperModel
    private lateinit var payment: RenterPayment
    private lateinit var currencyList: List<String>
    private lateinit var yearListFrom: ArrayList<Int>
    private lateinit var yearListTo: ArrayList<Int>
    private var currencySymbol: String = ""
    private var periodType: BillPeriodType = BillPeriodType.BY_MONTH
    private var delayCalculateBillJob: Job? = null
    private var lastPaymentInfo: RenterPayment? = null
    private var duesOrAdvanceAmount: Double = 0.0
    private var isPaymentAdded = false

    private var previousNumberOfMonths = 1
    private var baseHouseRent = 0.0
    private var baseParkingRent = 0.0


    //if selected by_month period type
    private var selectedFromMonthNumber: Int = 1
    private var selectedToMonthNumber: Int = 1
    private var selectedFromYear: Int = 0
    private var selectedToYear: Int = 0
    private var numberOfMonths: Int = 1

    //if selected by_date period type
    private var fromDateTimestamp: Long = 0L
    private var tillDateTimeStamp: Long = 0L
    private var numberOfDays: Int = 0

    //electricity vars
    private var previousReading: Double = 0.0
    private var currentReading: Double = 0.0
    private var rate: Double = 0.0
    private var difference: Double = 0.0
    private var totalElectricBill: Double = 0.0

    //total rent vars
    private var parkingBill: Double = 0.0
    private var houseRent: Double = 0.0
    private var extraBillAmount: Double = 0.0
    private var amountPaid: Double = 0.0
    private var netDemand: Double = 0.0

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

        supportingDocmtHelperModel = SupportingDocumentHelperModel()

        includeBinding = binding.include

        //List of currency symbols of different places
        currencyList = resources.getStringArray(R.array.currency_symbol).toList()
        setUpCurrencySymbolSpinner()
        currencySymbol = currencyList[0]

        currentTimestamp = System.currentTimeMillis()
        setDateAndTimeInTextViews()

        // for period type by_month
        populateYearListFrom(WorkingWithDateAndTime.getCurrentYear())
        populateYearListTo(WorkingWithDateAndTime.getCurrentYear())
        setUpMonthSpinners()
        setUpYearSpinners()

        initializeByMonthField()

        // for period type by_date
        fromDateTimestamp = currentTimestamp
        tillDateTimeStamp = currentTimestamp
        numberOfDays = 0
        setNumberOfDays()
        includeBinding.fromDateTV.setDateInTextView(fromDateTimestamp)
        includeBinding.tillDateTV.setDateInTextView(tillDateTimeStamp)


        includeBinding.duesOfLastPaymentTV.text =
            getString(R.string.duesOfLastPayment_message)

        getMessage()
        initListeners()
        textWatcher()
    }

    private fun setUpYearSpinners() {

        includeBinding.fromYearSpinner.setListToSpinner(
            requireContext(), yearListFrom, { position ->
                selectedFromYear = yearListFrom[position]
                setNumberOfMonthsInTextView()
            }, {}
        )
        includeBinding.toYearSpinner.setListToSpinner(
            requireContext(), yearListTo, { position ->
                selectedToYear = yearListTo[position]
                setNumberOfMonthsInTextView()
            }, {}
        )
    }

    private fun setNumberOfMonthsInTextView() {

        numberOfMonths = WorkingWithDateAndTime.calculateNumberOfMonthsInBetween(
            Pair(selectedFromMonthNumber, selectedFromYear),
            Pair(selectedToMonthNumber, selectedToYear)
        )

        includeBinding.byMonthErrorTV.text = if (numberOfMonths <= 0) {

            includeBinding.byMonthErrorTV.changeTextColor(requireContext(), R.color.color_orange)
            "Please enter a valid month range"
        } else {
            includeBinding.byMonthErrorTV.changeTextColor(requireContext(), R.color.color_green)
            modifyRent(numberOfMonths)
            "Number of months : $numberOfMonths"
        }
    }

    private fun modifyRent(numberOfMonths: Int) {

        if (periodType == BillPeriodType.BY_MONTH) {
            var houseRent = if (includeBinding.houseRentET.editText.isTextValid())
                includeBinding.houseRentET.editText?.text.toString().toDouble()
            else
                0.0

            var parkingRent = if (includeBinding.parkingET.editText.isTextValid())
                includeBinding.parkingET.editText?.text.toString().toDouble()
            else
                0.0

            if (houseRent > 0.0 && parkingRent > 0.0) {

                val diff: Int

                if (previousNumberOfMonths < numberOfMonths) {
                    diff = numberOfMonths - previousNumberOfMonths
                    houseRent += (baseHouseRent * diff)
                    parkingRent += (baseParkingRent * diff)

                } else if (previousNumberOfMonths > numberOfMonths) {
                    diff = previousNumberOfMonths - numberOfMonths
                    houseRent -= (baseHouseRent * diff)
                    parkingRent -= (baseParkingRent * diff)
                }

                if (previousNumberOfMonths != numberOfMonths) {
                    previousNumberOfMonths = numberOfMonths
                    includeBinding.houseRentET.editText?.setText((houseRent).toString())
                    includeBinding.parkingET.editText?.setText((parkingRent).toString())
                }
            }
        }
    }

    private fun setUpMonthSpinners() {

        val monthList = resources.getStringArray(R.array.months).toList()

        includeBinding.fromMonthSelectSpinner.setListToSpinner(
            requireContext(), monthList,
            { position ->
                selectedFromMonthNumber = position + 1
                setNumberOfMonthsInTextView()
            }, {}
        )

        includeBinding.toMonthSelectSpinner.setListToSpinner(
            requireContext(), monthList,
            { position ->
                selectedToMonthNumber = position + 1
                setNumberOfMonthsInTextView()
            },
            {}
        )
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

            lastPaymentInfo?.let { lastPayment ->

                // currency symbol
                currencySymbol = lastPayment.currencySymbol
                includeBinding.moneySymbolSpinner.setSelection(currencyList.indexOf(currencySymbol))

                if (lastPayment.billPeriodInfo.billPeriodType == BillPeriodType.BY_MONTH) {

                    baseHouseRent = getBaseHouseRentBasedOnLastPayment()
                    baseParkingRent = getBaseParkingRentBasedOnLastPayment()

                    includeBinding.houseRentET.editText?.setText(baseHouseRent.toString())
                    includeBinding.parkingET.editText?.setText(baseParkingRent.toString())
                    includeBinding.periodTypeRG.check(includeBinding.byMonthRB.id)
                    showByMonthAndHideByDateView()
                    initializeByMonthField()

                } else if (lastPayment.billPeriodInfo.billPeriodType == BillPeriodType.BY_DATE) {

                    includeBinding.houseRentET.editText?.setText(lastPayment.houseRent.toString())
                    includeBinding.parkingET.editText?.setText(lastPayment.parkingRent.toString())
                    includeBinding.periodTypeRG.check(includeBinding.byDateRB.id)
                    hideByMonthAndShowByDateView()
                    initialiseByDateField()
                }


                if (lastPayment.isElectricityBillIncluded) {

                    includeBinding.previousReadingET.setText(lastPayment.electricityBillInfo?.currentReading.toString())
                    includeBinding.currentReadingET.setText((lastPayment.electricityBillInfo?.currentReading.toString()))
                    includeBinding.rateET.setText(lastPayment.electricityBillInfo?.rate.toString())
                }

                if (lastPayment.extras?.fieldName.isValid()) {

                    includeBinding.extraFieldNameET.setText(lastPayment.extras?.fieldName)
                    includeBinding.extraAmountET.setText(lastPayment.extras?.fieldAmount.toString())
                }

                initializeLastPaymentsDuesAndAdvance()
            }

            amountPaid = calculateTotalBill().toDouble()
            includeBinding.amountPaidET.editText?.setText("$amountPaid")
        }

        binding.progressBar.hide()
    }

    private fun getBaseParkingRentBasedOnLastPayment(): Double {

        lastPaymentInfo?.let {

            return (it.parkingRent) / (it.billPeriodInfo.renterBillMonthType?.numberOfMonths!!)
        }
        return 0.0
    }

    private fun getBaseHouseRentBasedOnLastPayment(): Double {
        lastPaymentInfo?.let {

            return (it.houseRent) / (it.billPeriodInfo.renterBillMonthType?.numberOfMonths!!)
        }
        return 0.0
    }

    private fun setDateAndTimeInTextViews() {

        includeBinding.dateTV.text =
            getString(
                R.string.payment_date,
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    currentTimestamp
                )
            )

        includeBinding.timeTV.text =
            getString(
                R.string.time_,
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    currentTimestamp, "hh:mm a"
                )
            )
    }

    private fun initializeLastPaymentsDuesAndAdvance() {

        when {
            receivedRenter?.dueOrAdvanceAmount!! < 0.0 -> {

                includeBinding.duesOfLastPaymentTV.changeTextColor(
                    requireContext(),
                    R.color.color_orange
                )
                includeBinding.duesOfLastPaymentTV.text =
                    getString(
                        R.string.dues_from_last_payment,
                        "${lastPaymentInfo?.currencySymbol} ${
                            abs(receivedRenter?.dueOrAdvanceAmount!!)
                        }"
                    )

                duesOrAdvanceAmount = receivedRenter?.dueOrAdvanceAmount!!
            }
            receivedRenter?.dueOrAdvanceAmount!! > 0.0 -> {

                includeBinding.duesOfLastPaymentTV.changeTextColor(
                    requireContext(),
                    R.color.color_green
                )
                includeBinding.duesOfLastPaymentTV.text =
                    getString(
                        R.string.paid_in_advance_in_last_payment,
                        "${lastPaymentInfo?.currencySymbol}${receivedRenter?.dueOrAdvanceAmount}"
                    )

                duesOrAdvanceAmount = receivedRenter?.dueOrAdvanceAmount!!
            }
            else -> {

                includeBinding.duesOfLastPaymentTV.text =
                    getString(R.string.duesOfLastPayment_message)
            }
        }
    }

    private fun initialiseByDateField() {

        fromDateTimestamp = lastPaymentInfo?.billPeriodInfo?.renterBillDateType?.toBillDate!!
        tillDateTimeStamp = currentTimestamp

        includeBinding.fromDateTV.setDateInTextView(fromDateTimestamp)
        includeBinding.tillDateTV.setDateInTextView(tillDateTimeStamp)

        numberOfDays =
            WorkingWithDateAndTime.calculateNumberOfDays(fromDateTimestamp, tillDateTimeStamp)

        setNumberOfDays()
    }

    private fun initializeByMonthField() {

        lastPaymentInfo?.let { lastPayment ->

            lastPayment.billPeriodInfo.renterBillMonthType?.let { renterBillMonthType ->

                // from
                selectedFromMonthNumber =
                    if (renterBillMonthType.toBillMonth == 12) {

                        if (renterBillMonthType.toBillYear + 1 != WorkingWithDateAndTime.getCurrentYear()
                        ) {
                            // setting fromYear to the next year
                            selectedFromYear = renterBillMonthType.toBillYear + 1
                        }
                        1
                    } else {

                        selectedFromYear = renterBillMonthType.toBillYear
                        renterBillMonthType.toBillMonth + 1
                    }

                if (selectedFromMonthNumber == 12 &&
                    renterBillMonthType.toBillYear != WorkingWithDateAndTime.getCurrentYear()
                ) {
                    selectedFromYear = yearListFrom[1] // previous year
                }

                // to
                selectedToMonthNumber = selectedFromMonthNumber
                selectedToYear = selectedFromYear

            }
        }

        populateYearListFrom(selectedFromYear)
        populateYearListTo(selectedToYear)
        setUpYearSpinners()
        includeBinding.fromMonthSelectSpinner.setSelection(selectedFromMonthNumber - 1)
        includeBinding.toMonthSelectSpinner.setSelection(selectedToMonthNumber - 1)
        includeBinding.fromYearSpinner.setSelection(yearListFrom.indexOf(selectedFromYear))
        includeBinding.toYearSpinner.setSelection(yearListFrom.indexOf(selectedToYear))
        setNumberOfMonthsInTextView()
    }

    private fun populateYearListFrom(selectedYear: Int) {

        yearListFrom = java.util.ArrayList()

        for (year in selectedYear downTo selectedYear - 5) {

            yearListFrom.add(year)
        }
    }

    private fun populateYearListTo(selectedYear: Int) {

        yearListTo = java.util.ArrayList()

        for (year in selectedYear until (selectedYear + 5)) {

            yearListTo.add(year)
        }
    }

    private fun setUpCurrencySymbolSpinner() {

        includeBinding.moneySymbolSpinner.setListToSpinner(
            requireContext(),
            currencyList,
            { position ->
                currencySymbol = currencyList[position]
                calculateTotalBill()
            }, {}
        )
    }

    private fun initListeners() {

        includeBinding.addSupportingDocCB.isChecked = false

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        includeBinding.seeTotalBtn.setOnClickListener(this)

        includeBinding.fromDateTV.setOnClickListener(this)
        includeBinding.dateRangePickerBtn.setOnClickListener(this)
        includeBinding.tillDateTV.setOnClickListener(this)

        includeBinding.periodTypeRG.setOnCheckedChangeListener(this)
        includeBinding.dateContainer.setOnClickListener(this)

        includeBinding.addSupportingDocCB.setOnCheckedChangeListener(this)
        includeBinding.viewEditSupportingDoc.setOnClickListener(this)

        binding.toolbar.menu.findItem(R.id.menuSaveRenterPayment).setOnMenuItemClickListener {

            validateFormAndShowBillPreviewInBottomSheet()

            true
        }
    }

    private fun validateFormAndShowBillPreviewInBottomSheet() {

        if (validateForm()) {

            if (periodType == BillPeriodType.BY_DATE) {

                lifecycleScope.launch {

                    val isValid = paymentViewModel.validateByDateField(
                        receivedRenter?.key!!,
                        RenterBillDateType(fromDateTimestamp, tillDateTimeStamp, numberOfDays)
                    )

                    if (!isValid) {

                        includeBinding.byDateErrorMessageTV.changeTextColor(
                            requireContext(),
                            R.color.color_orange
                        )
                        includeBinding.byDateErrorMessageTV.text =
                            getString(R.string.renter_payment_date_range_error_message)
                    } else {

                        calculateTotalBill()
                        showBillInBottomSheet()
                    }
                }

            } else {

                lifecycleScope.launch {

                    val isValid = paymentViewModel.validateByMonthField(
                        receivedRenter?.key!!,
                        RenterBillMonthType(
                            selectedFromMonthNumber,
                            selectedFromYear,
                            selectedToMonthNumber,
                            selectedToYear,
                            numberOfMonths
                        )
                    )

                    if (!isValid) {

                        includeBinding.byMonthErrorTV.changeTextColor(
                            requireContext(),
                            R.color.color_orange
                        )
                        includeBinding.byMonthErrorTV.text =
                            getString(R.string.renter_payment_month_range_error_message)
                    } else {

                        calculateTotalBill()
                        showBillInBottomSheet()
                    }
                }

            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        hideKeyBoard(requireActivity())

        when (buttonView?.id) {

            includeBinding.addSupportingDocCB.id -> {

                if (isChecked) {

                    includeBinding.viewEditSupportingDoc.show()
                    supportingDocmtHelperModel = SupportingDocumentHelperModel()
                    supportingDocmtHelperModel.modelName = getString(R.string.renter_payments)
                    showSupportDocumentBottomSheetDialog()
                } else {

                    includeBinding.viewEditSupportingDoc.hide()
                }
            }
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            includeBinding.seeTotalBtn.id -> {

                includeBinding.amountPaidET.editText?.setText(calculateTotalBill())
            }

            includeBinding.dateContainer.id -> {

                Functions.showDateAndTimePickerDialog(
                    requireContext(),
                    WorkingWithDateAndTime.convertMillisecondsToCalendarInstance(currentTimestamp),
                    false,
                    if (lastPaymentInfo != null) lastPaymentInfo?.created!! else 0L
                ) { calendar ->

                    currentTimestamp = calendar.timeInMillis
                    setDateAndTimeInTextViews()
                }

            }
            includeBinding.viewEditSupportingDoc.id -> {

                showSupportDocumentBottomSheetDialog()
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

        if (includeBinding.extraFieldNameET.isTextValid()
            && !includeBinding.extraAmountET.isTextValid()
        ) {

            includeBinding.extraAmountET.requestFocus()
            includeBinding.extraAmountET.error = "Required"
            return false
        }

        if (includeBinding.extraAmountET.isTextValid() &&
            !includeBinding.extraFieldNameET.isTextValid()
        ) {

            includeBinding.extraFieldNameET.requestFocus()
            includeBinding.extraFieldNameET.error = "Required"
            return false
        }

        if (periodType == BillPeriodType.BY_DATE) {

            if (includeBinding.byDateErrorMessageTV.isVisible
                && includeBinding.byDateErrorMessageTV.currentTextColor == ContextCompat.getColor(
                    requireContext(),
                    R.color.color_orange
                )
            ) {
                return false
            }

        } else {

            if (includeBinding.byMonthErrorTV.isVisible
                && includeBinding.byMonthErrorTV.currentTextColor == ContextCompat.getColor(
                    requireContext(),
                    R.color.color_orange
                )
            ) {
                return false
            }
        }

        return includeBinding.houseRentET.error == null
    }

    private fun showSupportDocumentBottomSheetDialog() {

        val bundle = Bundle()
        bundle.putString(
            Constants.SUPPORTING_DOCUMENT_HELPER_MODEL_KEY,
            supportingDocmtHelperModel.convertToJsonString()
        )

        requireActivity().supportFragmentManager.let {

            SupportingDocumentDialogFragment.newInstance(bundle)
                .apply {
                    show(it, TAG)
                }.setOnBottomSheetDismissListener(this)
        }
    }

    override fun onBottomSheetDismissed(
        isDocumentAdded: Boolean,
        supportingDocumentHelperModel: SupportingDocumentHelperModel
    ) {

        if (!isDocumentAdded) {
            includeBinding.addSupportingDocCB.isChecked = false
        } else {

            supportingDocmtHelperModel = supportingDocumentHelperModel
        }
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

                numberOfDays =
                    WorkingWithDateAndTime.calculateNumberOfDays(dates.first!!, dates.second!!)

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

    private fun calculateTotalBill(shouldUpdateAmountPaidTV: Boolean = false): String {

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

        extraBillAmount = if (includeBinding.extraAmountET.isTextValid()) {

            includeBinding.extraAmountET.text.toString().trim().toDouble()
        } else {
            0.0
        }

        netDemand =
            ((totalElectricBill + parkingBill + houseRent + extraBillAmount) - duesOrAdvanceAmount)

        includeBinding.totalTV.text =
            getString(R.string.currency_amount, currencySymbol, String.format("%.2f", netDemand))

        amountPaid = if (!includeBinding.amountPaidET.editText?.isTextValid()!!
        ) {

            includeBinding.amountPaidET.editText?.setText("0.0")
            updateAmountPaidET("0.0")
            0.0
        } else {
            if (shouldUpdateAmountPaidTV) {

                includeBinding.amountPaidET.editText?.setText(netDemand.toString())
                updateAmountPaidET(netDemand.toString())
            }
            includeBinding.amountPaidET.editText?.text.toString().trim().toDouble()
        }

        return netDemand.toString()
    }

    private fun showBillInBottomSheet() {

        initPayment()

        val bundle = Bundle()
        bundle.putString(
            RENTER_PAYMENT_CONFIRMATION_BILL_KEY,
            payment.convertToJsonString()
        )

        requireActivity().supportFragmentManager.let {

            ShowBillForConfirmationBottomSheetFragment.newInstance(bundle)
                .apply {
                    show(it, TAG)
                }.setOnPaymentConfirmationBottomSheetDismissListener(this)
        }
    }

    private fun initPayment() {

        if (!isPaymentAdded) {

            val billInfo = RenterBillPeriodInfo(
                periodType,
                if (periodType == BillPeriodType.BY_MONTH) {
                    RenterBillMonthType(
                        selectedFromMonthNumber,
                        selectedFromYear,
                        selectedToMonthNumber,
                        selectedToYear,
                        numberOfMonths
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
                selectedFromYear
            )

            val isElectricBillIncluded = (includeBinding.previousReadingET.isTextValid()
                    && includeBinding.currentReadingET.isTextValid()
                    && includeBinding.rateET.isTextValid())


            val electricityBillInfo = if (isElectricBillIncluded) {
                RenterElectricityBillInfo(
                    previousReading,
                    currentReading,
                    rate,
                    difference,
                    totalElectricBill
                )
            } else {
                null
            }

            payment = RenterPayment(
                key = generateKey(appendString = "_${getUid()}"),
                created = currentTimestamp,
                modified = currentTimestamp,
                renterKey = receivedRenter?.key!!,
                currencySymbol = currencySymbol,
                billPeriodInfo = billInfo,
                isElectricityBillIncluded = isElectricBillIncluded,
                electricityBillInfo = electricityBillInfo,
                houseRent = houseRent,
                parkingRent = parkingBill,
                extras = if (includeBinding.extraFieldNameET.text.toString().trim().isValid()) {
                    RenterPaymentExtras(
                        includeBinding.extraFieldNameET.text.toString().trim(),
                        extraBillAmount
                    )
                } else {
                    null
                },
                netDemand = netDemand,
                amountPaid = amountPaid,
                note = includeBinding.addNoteET.text.toString().trim(),
                uid = getUid()!!,
                isSynced = true,
                isSupportingDocAdded = includeBinding.addSupportingDocCB.isChecked,
                supportingDocument = if (includeBinding.addSupportingDocCB.isChecked
                    && supportingDocmtHelperModel.documentType == DocumentType.URL
                ) {
                    SupportingDocument(
                        supportingDocmtHelperModel.documentName,
                        supportingDocmtHelperModel.documentUrl,
                        supportingDocmtHelperModel.documentType
                    )
                } else {
                    null
                }
            )
        }
    }

    override fun onPaymentConfirmationBottomSheetDismissed(isSaveBtnClicked: Boolean) {

        if (isSaveBtnClicked) {

            if (payment.isSupportingDocAdded
                && supportingDocmtHelperModel.documentType != DocumentType.URL
            ) {

                // if the document type is not URL, then we need internet connection to upload the uri
                if (!Functions.isInternetAvailable(requireContext())) {
                    Functions.showToast(
                        requireContext(),
                        getString(R.string.internet_required_message_for_uploading_doc),
                        Toast.LENGTH_LONG
                    )
                    return
                }

                paymentViewModel.insertPayment(
                    payment,
                    supportingDocmtHelperModel
                )
            } else {

                paymentViewModel.insertPayment(payment, null)
            }

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

        includeBinding.houseRentET.editText?.onTextChangedListener { s ->
            if (s?.isEmpty()!!) {

                includeBinding.houseRentET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {
                if (periodType == BillPeriodType.BY_MONTH) {
                    baseHouseRent = (includeBinding.houseRentET.editText?.text.toString()
                        .toDouble()) / numberOfMonths
                }
                includeBinding.houseRentET.error = null
            }
            calculateTotalBillJob()
        }

        includeBinding.parkingET.editText?.onTextChangedListener { s ->
            if (!s?.isEmpty()!!) {
                if (periodType == BillPeriodType.BY_MONTH) {
                    baseParkingRent = (includeBinding.parkingET.editText?.text.toString()
                        .toDouble()) / numberOfMonths
                }
            }
            calculateTotalBillJob()
        }

        includeBinding.extraAmountET.onTextChangedListener {
            calculateTotalBillJob()
            if (it?.isNotEmpty()!!) {
                includeBinding.extraAmountET.error = null
            }
        }

        includeBinding.extraFieldNameET.onTextChangedListener {
            if (it?.isNotEmpty()!!) {
                includeBinding.extraFieldNameET.error = null
            }
        }

        includeBinding.previousReadingET.onTextChangedListener {
            calculateTotalBillJob()
        }

        includeBinding.currentReadingET.onTextChangedListener {
            calculateTotalBillJob()
        }

        includeBinding.rateET.onTextChangedListener {
            calculateTotalBillJob()
        }

        includeBinding.amountPaidET.editText?.onTextChangedListener { s ->

            updateAmountPaidET(s.toString())
        }
    }

    private fun updateAmountPaidET(amountPaid: String) {

        if (amountPaid.trim().isNotEmpty()) {
            when {
                amountPaid.trim().toDouble() < netDemand -> {

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

    private fun calculateTotalBillJob() {

        try {
            if (delayCalculateBillJob != null && delayCalculateBillJob!!.isActive) {

                delayCalculateBillJob!!.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

            delayCalculateBillJob = lifecycleScope.launch {

                delay(300)
                calculateTotalBill(true)
            }
        }
    }

    private fun showByMonthAndHideByDateView() {

        try {
            periodType = BillPeriodType.BY_MONTH
            includeBinding.byDateCL.hide()
            includeBinding.byMonthCL.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideByMonthAndShowByDateView() {

        try {
            periodType = BillPeriodType.BY_DATE
            includeBinding.byDateCL.show()
            includeBinding.byMonthCL.hide()
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