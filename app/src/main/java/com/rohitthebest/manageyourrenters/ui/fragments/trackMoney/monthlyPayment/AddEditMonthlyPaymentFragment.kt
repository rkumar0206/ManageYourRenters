package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.database.model.MonthlyPayment
import com.rohitthebest.manageyourrenters.database.model.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.database.model.MonthlyPaymentDateTimeInfo
import com.rohitthebest.manageyourrenters.databinding.AddEditMonthlyPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEditMonthlyPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "AddEditMonthlyPaymentFragment"

@AndroidEntryPoint
class AddEditMonthlyPaymentFragment : Fragment(R.layout.fragment_add_edit_monthly_payment),
    View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private var _binding: FragmentAddEditMonthlyPaymentBinding? = null
    private val binding get() = _binding!!

    private val monthlyPaymentViewModel by viewModels<MonthlyPaymentViewModel>()
    private val monthlyPaymentCategoryViewModel by viewModels<MonthlyPaymentCategoryViewModel>()

    private lateinit var includeBinding: AddEditMonthlyPaymentLayoutBinding

    private lateinit var receivedMonthlyPaymentCategoryKey: String
    private lateinit var receivedMonthlyPaymentCategory: MonthlyPaymentCategory

    private lateinit var paymentDate: Calendar

    private var receivedMonthlyPaymentKey = ""
    private lateinit var receivedMonthlyPayment: MonthlyPayment
    private var isMessageReceivedForEditing = false

    private lateinit var yearList: ArrayList<Int>

    private var selectedFromMonthNumber: Int = 1
    private var selectedToMonthNumber: Int = 1
    private var selectedFromYear: Int = 0
    private var selectedToYear: Int = 0

    private var periodType: BillPeriodType = BillPeriodType.BY_MONTH
    private var fromDateTimestamp: Long = 0L
    private var tillDateTimeStamp: Long = 0L
    private var numberOfDays: Int = 0
    private var numberOfMonths: Int = 1

    private var lastPaymentInfo: MonthlyPayment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditMonthlyPaymentBinding.bind(view)

        includeBinding = binding.includeLayout

        paymentDate = Calendar.getInstance()

        updateSelectedPaymentDateTextView()

        populateYearList(WorkingWithDateAndTime.getCurrentYear())
        setUpMonthSpinners()
        setUpYearSpinners()

        populateByDateLayoutFields()
        populateByMonthLayoutFields()

        getMessage()

        initListeners()

        textWatchers()
    }

    private fun populateYearList(selectedYear: Int) {

        yearList = ArrayList()

        for (year in selectedYear downTo selectedYear - 5) {

            yearList.add(year)
        }
    }

    private fun setUpYearSpinners() {

        includeBinding.fromYearSpinner.setListToSpinner(
            requireContext(), yearList, { position ->
                selectedFromYear = yearList[position]
                setNumberOfMonthsInTextView()
            }, {}
        )
        includeBinding.toYearSpinner.setListToSpinner(
            requireContext(), yearList, { position ->
                selectedToYear = yearList[position]
                setNumberOfMonthsInTextView()
            }, {}
        )
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

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            if (validateForm()) {

                initMonthlyPayment()
            }

            true
        }

        includeBinding.monthlyPaymentDateTV.setOnClickListener(this)
        includeBinding.monthlyPaymentDateIB.setOnClickListener(this)
        includeBinding.dateRangePickerBtn.setOnClickListener(this)
        includeBinding.fromDateTV.setOnClickListener(this)
        includeBinding.tillDateTV.setOnClickListener(this)

        includeBinding.periodTypeRG.setOnCheckedChangeListener(this)
    }

    private fun initMonthlyPayment() {

        val monthlyPaymentDateTimeInfo: MonthlyPaymentDateTimeInfo

        val monthlyPayment: MonthlyPayment = if (!isMessageReceivedForEditing) {

            monthlyPaymentDateTimeInfo = MonthlyPaymentDateTimeInfo()

            // create
            MonthlyPayment().apply {
                key = Functions.generateKey("_${getUid()}", 50)
                categoryKey = receivedMonthlyPaymentCategoryKey
                id = 0
                uid = getUid()!!
            }

        } else {

            // update
            monthlyPaymentDateTimeInfo = receivedMonthlyPayment.monthlyPaymentDateTimeInfo!!.copy()
            receivedMonthlyPayment.copy()
        }

        monthlyPaymentDateTimeInfo.apply {

            this.paymentPeriodType = periodType
            if (periodType == BillPeriodType.BY_MONTH) {

                this.forBillMonth = selectedFromMonthNumber
                this.forBillYear = selectedFromYear
                this.toBillMonth = selectedToMonthNumber
                this.toBillYear = selectedToYear
                this.numberOfMonths = this@AddEditMonthlyPaymentFragment.numberOfMonths
            } else {

                this.fromBillDate = fromDateTimestamp
                this.toBillDate = tillDateTimeStamp
                this.numberOfDays = this@AddEditMonthlyPaymentFragment.numberOfDays
            }
        }

        monthlyPayment.amount =
            includeBinding.monthlyPaymentAmountET.editText?.text.toString().toDouble()
        monthlyPayment.created = paymentDate.timeInMillis
        monthlyPayment.message = includeBinding.monthlyPaymentNoteET.text.toString().trim()
        monthlyPayment.modified = System.currentTimeMillis()
        monthlyPayment.monthlyPaymentDateTimeInfo = monthlyPaymentDateTimeInfo

        monthlyPayment.isSynced = isInternetAvailable(requireContext())
        saveMonthlyPaymentToDatabase(monthlyPayment)
    }

    private fun saveMonthlyPaymentToDatabase(monthlyPayment: MonthlyPayment) {

        val oldValue = receivedMonthlyPaymentCategory.copy()
        receivedMonthlyPaymentCategory.modified = System.currentTimeMillis()

        monthlyPaymentCategoryViewModel.updateMonthlyPaymentCategory(
            oldValue,
            receivedMonthlyPaymentCategory
        )

        if (!isMessageReceivedForEditing) {

            monthlyPaymentViewModel.insertMonthlyPayment(monthlyPayment)
        } else {

            monthlyPaymentViewModel.updateMonthlyPayment(receivedMonthlyPayment, monthlyPayment)
        }

        Log.d(TAG, "saveMonthlyPaymentToDatabase: $monthlyPayment")

        requireActivity().onBackPressed()
    }

    private fun validateForm(): Boolean {

        if (!includeBinding.monthlyPaymentAmountET.editText.isTextValid()) {

            includeBinding.monthlyPaymentAmountET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (periodType == BillPeriodType.BY_MONTH) {

            if (numberOfMonths <= 0) {

                setNumberOfMonthsInTextView()
                return false
            }
        } else {

            if (numberOfDays < 0) {

                setNumberOfDaysInTextView()
                return false
            }
        }
        return true
    }

    override fun onClick(v: View?) {

        if (includeBinding.monthlyPaymentDateTV.id == v?.id || includeBinding.monthlyPaymentDateIB.id == v?.id) {

            Functions.showDateAndTimePickerDialog(
                requireContext(),
                paymentDate,
                false
            ) { paymentDate ->

                this.paymentDate = paymentDate
                updateSelectedPaymentDateTextView()
            }
        }

        if (v?.id == includeBinding.dateRangePickerBtn.id || v?.id == includeBinding.fromDateTV.id
            || v?.id == includeBinding.tillDateTV.id
        ) {

            showDateRangePickerDialog()
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        when (checkedId) {

            includeBinding.byDateRB.id -> {

                periodType = BillPeriodType.BY_DATE
                showByDateLayout()
            }

            includeBinding.byMonthRB.id -> {

                periodType = BillPeriodType.BY_MONTH
                showByMonthLayout()
            }
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

                setNumberOfDaysInTextView()
            },
            true
        )
    }


    private fun showByMonthLayout() {
        includeBinding.byMonthCL.show()
        includeBinding.byDateCL.hide()
    }

    private fun showByDateLayout() {
        includeBinding.byDateCL.show()
        includeBinding.byMonthCL.hide()
    }

    private fun updateSelectedPaymentDateTextView() {

        includeBinding.monthlyPaymentDateTV.text =
            WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                paymentDate.timeInMillis, "dd-MM-yyyy hh:mm a"
            )
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let { bundle ->

                AddEditMonthlyPaymentFragmentArgs.fromBundle(bundle)
            }

            receivedMonthlyPaymentCategoryKey = args?.monthlyPaymentCategoryKey!!

            receivedMonthlyPaymentKey = args.monthlyPaymentKey!!

            if (receivedMonthlyPaymentKey.isValid()) {

                isMessageReceivedForEditing = true
                getMonthlyPayment()
            }

            lifecycleScope.launch {

                delay(300)
                getMonthlyPaymentCategory()
                if (!isMessageReceivedForEditing) {

                    initialUIChangesBasedOnLastPayment()
                }
            }
        }
    }

    private fun initialUIChangesBasedOnLastPayment() {

        monthlyPaymentViewModel.getLastMonthlyPayment(receivedMonthlyPaymentCategoryKey)
            .observe(viewLifecycleOwner) { payment ->

                lastPaymentInfo = payment

                Log.d(TAG, "LastPayment: $lastPaymentInfo")

                if (lastPaymentInfo != null) {

                    var amount = lastPaymentInfo?.amount!!

                    lastPaymentInfo?.monthlyPaymentDateTimeInfo?.let { dateTimeInfo ->

                        if (dateTimeInfo.paymentPeriodType == BillPeriodType.BY_MONTH) {

                            periodType = BillPeriodType.BY_MONTH
                            includeBinding.periodTypeRG.check(includeBinding.byMonthRB.id)
                            populateByMonthLayoutFields()

                            amount /= lastPaymentInfo?.monthlyPaymentDateTimeInfo?.numberOfMonths!!

                        } else {
                            periodType = BillPeriodType.BY_DATE
                            includeBinding.periodTypeRG.check(includeBinding.byDateRB.id)
                            populateByDateLayoutFields()
                        }
                    }
                    includeBinding.monthlyPaymentAmountET.editText?.setText(amount.toString())
                }
            }
    }

    private fun populateByDateLayoutFields() {

        when {
            lastPaymentInfo != null -> {

                fromDateTimestamp = lastPaymentInfo?.monthlyPaymentDateTimeInfo?.toBillDate!!
                tillDateTimeStamp = System.currentTimeMillis()
            }
            isMessageReceivedForEditing -> {

                if (this::receivedMonthlyPayment.isInitialized) {

                    fromDateTimestamp =
                        receivedMonthlyPayment.monthlyPaymentDateTimeInfo?.fromBillDate!!
                    tillDateTimeStamp =
                        receivedMonthlyPayment.monthlyPaymentDateTimeInfo?.toBillDate!!
                }
            }
            else -> {

                fromDateTimestamp = System.currentTimeMillis()
                tillDateTimeStamp = System.currentTimeMillis()
            }
        }

        numberOfDays =
            WorkingWithDateAndTime.calculateNumberOfDays(fromDateTimestamp, tillDateTimeStamp)

        includeBinding.fromDateTV.setDateInTextView(fromDateTimestamp)
        includeBinding.tillDateTV.setDateInTextView(tillDateTimeStamp)
        setNumberOfDaysInTextView()
    }

    private fun populateByMonthLayoutFields() {

        when {
            lastPaymentInfo != null -> {
                // from
                selectedFromMonthNumber =
                    if (lastPaymentInfo?.monthlyPaymentDateTimeInfo?.toBillMonth == 12) {

                        if (lastPaymentInfo?.monthlyPaymentDateTimeInfo?.toBillYear!! + 1
                            != WorkingWithDateAndTime.getCurrentYear()
                        ) {

                            // increase year by 1
                            selectedFromYear =
                                lastPaymentInfo?.monthlyPaymentDateTimeInfo?.toBillYear!! + 1
                            populateYearList(selectedFromYear)
                            setUpYearSpinners()
                        }

                        1
                    } else {

                        selectedFromYear = lastPaymentInfo?.monthlyPaymentDateTimeInfo?.toBillYear!!
                        lastPaymentInfo?.monthlyPaymentDateTimeInfo?.toBillMonth!! + 1
                    }

                if (selectedFromMonthNumber == 12) {

                    if (lastPaymentInfo?.monthlyPaymentDateTimeInfo?.toBillYear != WorkingWithDateAndTime.getCurrentYear()) {

                        selectedFromYear = yearList[1] // previous year
                    }
                }

                // to
                selectedToMonthNumber = selectedFromMonthNumber
                selectedToYear = selectedFromYear
            }
            isMessageReceivedForEditing -> {

                if (this::receivedMonthlyPayment.isInitialized) {

                    selectedFromMonthNumber =
                        receivedMonthlyPayment.monthlyPaymentDateTimeInfo?.forBillMonth!!
                    selectedToMonthNumber =
                        receivedMonthlyPayment.monthlyPaymentDateTimeInfo?.toBillMonth!!
                    selectedFromYear =
                        receivedMonthlyPayment.monthlyPaymentDateTimeInfo?.forBillYear!!
                    selectedToYear = receivedMonthlyPayment.monthlyPaymentDateTimeInfo?.toBillYear!!
                }
            }
            else -> {

                Log.d(TAG, "populateByMonthLayoutFields: No Last payment or for editing")

                selectedFromMonthNumber = WorkingWithDateAndTime.getCurrentMonth() + 1
                selectedToMonthNumber = WorkingWithDateAndTime.getCurrentMonth() + 1
                selectedFromYear = WorkingWithDateAndTime.getCurrentYear()
                selectedToYear = WorkingWithDateAndTime.getCurrentYear()
            }
        }

        includeBinding.fromMonthSelectSpinner.setSelection(selectedFromMonthNumber - 1)
        includeBinding.toMonthSelectSpinner.setSelection(selectedToMonthNumber - 1)
        includeBinding.fromYearSpinner.setSelection(yearList.indexOf(selectedFromYear))
        includeBinding.toYearSpinner.setSelection(yearList.indexOf(selectedToYear))
        setNumberOfMonthsInTextView()
    }

    private fun getMonthlyPayment() {

        monthlyPaymentViewModel.getMonthlyPaymentByKey(receivedMonthlyPaymentKey)
            .observe(viewLifecycleOwner) { monthlyPayment ->

                receivedMonthlyPayment = monthlyPayment
                updateUI()
            }
    }

    private fun updateUI() {

        if (this::receivedMonthlyPayment.isInitialized) {

            includeBinding.apply {

                paymentDate =
                    WorkingWithDateAndTime.convertMillisecondsToCalendarInstance(
                        receivedMonthlyPayment.created
                    )
                updateSelectedPaymentDateTextView()
                monthlyPaymentAmountET.editText?.setText(receivedMonthlyPayment.amount.toString())
                monthlyPaymentNoteET.setText(receivedMonthlyPayment.message)

                if (receivedMonthlyPayment.monthlyPaymentDateTimeInfo?.paymentPeriodType == BillPeriodType.BY_MONTH) {

                    periodType = BillPeriodType.BY_MONTH
                    includeBinding.periodTypeRG.check(includeBinding.byMonthRB.id)
                    populateByMonthLayoutFields()

                } else {
                    periodType = BillPeriodType.BY_DATE
                    includeBinding.periodTypeRG.check(includeBinding.byDateRB.id)
                    populateByDateLayoutFields()
                }

            }
        }
    }

    private fun getMonthlyPaymentCategory() {

        if (this::receivedMonthlyPaymentCategoryKey.isInitialized) {

            monthlyPaymentCategoryViewModel.getMonthlyPaymentCategoryUsingKey(
                receivedMonthlyPaymentCategoryKey
            )
                .observe(viewLifecycleOwner) { category ->

                    receivedMonthlyPaymentCategory = category

                    binding.toolbar.title = "Add ${category.categoryName} payment"
                }
        }
    }

    private fun setNumberOfDaysInTextView() {

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
            "Number of months : $numberOfMonths"
        }
    }

    private fun textWatchers() {

        includeBinding.monthlyPaymentAmountET.editText?.onTextChangedListener { s ->

            if (!s?.toString().isValid()) {

                includeBinding.monthlyPaymentAmountET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.monthlyPaymentAmountET.error = null
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }
}
