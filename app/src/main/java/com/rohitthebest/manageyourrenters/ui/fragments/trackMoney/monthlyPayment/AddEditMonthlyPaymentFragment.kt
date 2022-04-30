package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

import android.os.Bundle
import android.view.View
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
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AddEditMonthlyPaymentFragment : Fragment(R.layout.fragment_add_edit_monthly_payment) {

    private var _binding: FragmentAddEditMonthlyPaymentBinding? = null
    private val binding get() = _binding!!

    private val monthlyPaymentViewModel by viewModels<MonthlyPaymentViewModel>()
    private val monthlyPaymentCategoryViewModel by viewModels<MonthlyPaymentCategoryViewModel>()

    private lateinit var includeBinding: AddEditMonthlyPaymentLayoutBinding

    private lateinit var receivedMonthlyPaymentCategoryKey: String
    private lateinit var receivedMonthlyPaymentCategory: MonthlyPaymentCategory

    private lateinit var selectedDate: Calendar

    private var receivedMonthlyPaymentKey = ""
    private lateinit var receivedMonthlyPayment: MonthlyPayment
    private var isMessageReceivedForEditing = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditMonthlyPaymentBinding.bind(view)

        includeBinding = binding.includeLayout

        selectedDate = Calendar.getInstance()

        updateSelectedDateTextView()

        getMessage()

        //initListeners()

        //textWatchers()
    }

    private fun updateSelectedDateTextView() {

        includeBinding.monthlyPaymentDateTV.text =
            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                selectedDate.timeInMillis, "dd-MM-yyyy hh:mm a"
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

                selectedDate =
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
