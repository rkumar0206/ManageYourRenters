package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.databinding.AddEditMonthlyPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEditMonthlyPaymentBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

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
    private lateinit var workingWithDateAndTime: WorkingWithDateAndTime

    private var receivedMonthlyPaymentKey = ""
    private lateinit var receivedMonthlyPayment: MonthlyPayment
    private var isMessageReceivedForEditing = false

    private lateinit var yearList: ArrayList<Int>

    private var selectedFromMonth: String = ""
    private var selectedToMonth: String = ""
    private var selectedFromYear: Int = 0
    private var selectedToYear: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditMonthlyPaymentBinding.bind(view)

        includeBinding = binding.includeLayout

        workingWithDateAndTime = WorkingWithDateAndTime()

        paymentDate = Calendar.getInstance()

        updateSelectedDateTextView()

        populateYearList(workingWithDateAndTime.getCurrentYear())
        setUpMonthSpinners()
        setUpYearSpinners()

        // todo : populate by date for and till textViews
        // todo : pre-populate all the fields based on the last payment

        getMessage()

        initListeners()

        //textWatchers()
    }

    private fun populateYearList(selectedYear: Int) {

        yearList = ArrayList()

        for (year in selectedYear downTo selectedYear - 5) {

            yearList.add(year)
        }
    }

    private fun setUpYearSpinners() {

        includeBinding.fromYearSpinner.setListToSpinner(
            requireContext(), yearList, { position -> selectedFromYear = yearList[position] }, {}
        )
        includeBinding.toYearSpinner.setListToSpinner(
            requireContext(), yearList, { position -> selectedToYear = yearList[position] }, {}
        )
    }

    private fun setUpMonthSpinners() {

        val monthList = resources.getStringArray(R.array.months).toList()

        includeBinding.fromMonthSelectSpinner.setListToSpinner(
            requireContext(), monthList, {},
            { month -> selectedFromMonth = month as String }
        )

        includeBinding.toMonthSelectSpinner.setListToSpinner(
            requireContext(), monthList, {},
            { month -> selectedToMonth = month as String }
        )
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        // todo :  on save menu click validate and save the monthly payment

        includeBinding.monthlyPaymentDateTV.setOnClickListener(this)
        includeBinding.monthlyPaymentDateIB.setOnClickListener(this)

        // todo : handle hiding and showing of by month and by date on the change of radio button
        includeBinding.periodTypeRG.setOnCheckedChangeListener(this)
    }

    override fun onClick(v: View?) {

        if (includeBinding.monthlyPaymentDateTV.id == v?.id || includeBinding.monthlyPaymentDateIB.id == v?.id) {

            Functions.showDateAndTimePickerDialog(
                requireContext(),
                paymentDate,
                false
            ) { paymentDate ->

                this.paymentDate = paymentDate
                updateSelectedDateTextView()
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        when (checkedId) {

            includeBinding.byDateRB.id -> {

                includeBinding.byDateCL.show()
                includeBinding.byMonthCL.hide()
            }

            includeBinding.byMonthRB.id -> {

                includeBinding.byMonthCL.show()
                includeBinding.byDateCL.hide()
            }
        }

    }

    private fun updateSelectedDateTextView() {

        includeBinding.monthlyPaymentDateTV.text =
            workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
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

            if (receivedMonthlyPaymentKey != "") {

                isMessageReceivedForEditing = true

                getMonthlyPayment()
            }

            lifecycleScope.launch {

                delay(300)
                getMonthlyPaymentCategory()
            }
        }
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
                    WorkingWithDateAndTime().convertMillisecondsToCalendarInstance(
                        receivedMonthlyPayment.created
                    )
                updateSelectedDateTextView()
                monthlyPaymentAmountET.editText?.setText(receivedMonthlyPayment.amount.toString())
                monthlyPaymentNoteET.setText(receivedMonthlyPayment.message)
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
