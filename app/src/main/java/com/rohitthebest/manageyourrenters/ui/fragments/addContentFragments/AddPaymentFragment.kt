package com.rohitthebest.manageyourrenters.ui.fragments.addContentFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.datepicker.MaterialDatePicker
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.entity.Payment
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.databinding.AddPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.fragments.PaymentFragmentArgs
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_renter.*

@AndroidEntryPoint
class AddPaymentFragment : Fragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private val paymentViewModel: PaymentViewModel by viewModels()

    private var _binding: FragmentAddPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddPaymentLayoutBinding

    private var receivedRenter: Renter? = null
    private var currentTimestamp = 0L

    private var monthList: List<String>? = null
    private var currencyList: List<String>? = null

    private var billMonth: String? = null
    private var billMonthNumber = 1
    private var currencySymbol: String? = null

    //if selected by_date method
    private var fromDateTimestamp: Long? = null
    private var tillDateTimeStamp: Long? = null
    private var numberOfDays: String? = ""

    private var lastPaymentInfo: Payment? = null

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

                        lastPaymentInfo = it.last()
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
                initialChanges()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initialChanges() {

        receivedRenter?.let {

            includeBinding.renterNameTV.text = it.name

            currentTimestamp = System.currentTimeMillis()

            includeBinding.dateTV.text = "Date : ${
                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                    currentTimestamp
                )
            }"

            includeBinding.timeTV.text = "Time : ${
                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                    currentTimestamp,
                    "hh:mm:ss"
                )
            }"

            if (lastPaymentInfo != null) {

                if (lastPaymentInfo!!.bill?.billPeriodType == getString(R.string.by_month)) {

                    initializeByMonthField()

                } else if (lastPaymentInfo!!.bill?.billPeriodType == getString(R.string.by_date)) {

                    initialiseByDateField()

                }
            } else {

                billMonth = monthList?.get(0)
                billMonthNumber = 1

                fromDateTimestamp = currentTimestamp
                tillDateTimeStamp = currentTimestamp
                numberOfDays = getString(R.string.same_day)

                includeBinding.byDateErrorMessageTV.text = numberOfDays

                includeBinding.fromDateTV.setDateInTextView(fromDateTimestamp)

                includeBinding.tillDateTV.setDateInTextView(tillDateTimeStamp)
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

    override fun onClick(v: View?) {

        when (v?.id) {


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

    private fun showDateRangePickerDialog() {

        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setSelection(Pair(fromDateTimestamp, tillDateTimeStamp))
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


    }

    private fun showByMonthAndHideByDateView() {

        try {
            includeBinding.byDateCL.hide()
            includeBinding.monthSelectSpinner.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideByMonthAndShowByDateView() {

        try {
            includeBinding.byDateCL.show()
            includeBinding.monthSelectSpinner.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun TextView.setDateInTextView(timeStamp: Long?) {

        this.text = WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
            timeStamp
        )

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}