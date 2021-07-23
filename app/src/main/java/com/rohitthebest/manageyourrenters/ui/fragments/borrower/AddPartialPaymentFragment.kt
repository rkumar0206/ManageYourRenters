package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.database.model.PartialPayment
import com.rohitthebest.manageyourrenters.databinding.AddPartialPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddPartialPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PartialPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import com.rohitthebest.manageyourrenters.utils.changeTextColor
import com.rohitthebest.manageyourrenters.utils.isTextValid
import com.rohitthebest.manageyourrenters.utils.setDateInTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPartialPaymentFragment : BottomSheetDialogFragment(),
    CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private var _binding: FragmentAddPartialPaymentBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: AddPartialPaymentLayoutBinding

    private val borrowerPaymentViewModel by viewModels<BorrowerPaymentViewModel>()
    private val partialPaymentViewModel by viewModels<PartialPaymentViewModel>()

    private var receivedBorrowerPayment: BorrowerPayment? = null
    private var receivedBorrowerPaymentKey: String = ""

    private var selectedDate = 0L

    private var isPaymentMarkedAsDone = false
    private lateinit var addedPartialPaymentList: List<PartialPayment>
    private lateinit var removedPartialPaymentKeyList: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_partial_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddPartialPaymentBinding.bind(view)
        includeBinding = binding.includeLayout

        initListeners()

        addedPartialPaymentList = ArrayList()
        removedPartialPaymentKeyList = ArrayList()

        selectedDate = System.currentTimeMillis()
        initDate()

        getMessage()
        textWatcher()
    }

    private fun initDate() {

        includeBinding.addPartialPaymentDateTV.setDateInTextView(
            selectedDate,
            "dd-MMMM-yyyy"
        )
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let {

                AddPartialPaymentFragmentArgs.fromBundle(it)
            }

            receivedBorrowerPaymentKey = args?.borrowerPaymentMessage!!

            getBorrowerPayment()
            getBorrowerPartialPayments()
        }
    }

    private fun getBorrowerPayment() {

        borrowerPaymentViewModel.getBorrowerPaymentByKey(receivedBorrowerPaymentKey)
            .observe(viewLifecycleOwner, { borrowerPayment ->

                receivedBorrowerPayment = borrowerPayment
                updateUI()
            })
    }

    private fun getBorrowerPartialPayments() {

        partialPaymentViewModel.getPartialPaymentByBorrowerPaymentKey(receivedBorrowerPaymentKey)
            .observe(viewLifecycleOwner, { partialPaymentList ->

                if (partialPaymentList.isNotEmpty()) {

                    //todo : submit the list to the adapter
                    showNoPartialPaymentAddedTV(false)
                } else {

                    showNoPartialPaymentAddedTV(true)
                }
            })
    }

    private fun updateUI() {

        receivedBorrowerPayment?.let { borrowerPayment ->

            if (borrowerPayment.isDueCleared) {

                includeBinding.markAsDoneCB.isChecked = true
                isPaymentMarkedAsDone = true
            }
        }
    }

    private fun initListeners() {

        includeBinding.addPartialPaymentDateBtn.setOnClickListener(this)
        includeBinding.addPartialPaymentBtn.setOnClickListener(this)
        includeBinding.addPartialPaymentDateTV.setOnClickListener(this)
        includeBinding.markAsDoneCB.setOnCheckedChangeListener(this)

        binding.addPartialFragmentToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }
    }

    override fun onClick(v: View?) {

        if (v?.id == includeBinding.addPartialPaymentDateBtn.id
            || v?.id == includeBinding.addPartialPaymentDateTV.id
        ) {

            showCalendarDialog(
                selectedDate,
                {
                    requireActivity().supportFragmentManager
                },
                {
                    selectedDate = it
                    initDate()
                }
            )
        }

        when (v?.id) {

            includeBinding.addPartialPaymentBtn.id -> {

                if (includeBinding.addPartialPaymentAmountET.editText?.isTextValid()!!) {

                    //todo : add the partial payment and clear the text
                } else {

                    includeBinding.addPartialPaymentAmountET.error = EDIT_TEXT_EMPTY_MESSAGE
                }
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        when (buttonView?.id) {

            includeBinding.markAsDoneCB.id -> {

                if (isChecked) {

                    // disabling the ui below this checkbox
                    shouldEnableAddingPartialPayment(false)
                    // todo : update the is due cleared variable of borrower payment
                    //todo : show the delete payment button
                } else {
                    //enabling the ui below the checkbox
                    shouldEnableAddingPartialPayment(true)

                    //todo : check weather the sum of partial payment is greater than or equal to the due amount and take appropriate action

                }
            }
        }
    }

    private fun textWatcher() {

        includeBinding.addPartialPaymentAmountET.editText?.addTextChangedListener { s ->

            if (s?.isNotEmpty()!!) {

                includeBinding.addPartialPaymentAmountET.error = null
            }
        }
    }

    private fun showNoPartialPaymentAddedTV(isVisible: Boolean) {

        includeBinding.noPayemtAddedTV.isVisible = isVisible
    }

    private fun shouldEnableAddingPartialPayment(isEnable: Boolean) {

        val colorGrey = ContextCompat.getColor(requireContext(), R.color.colorGrey)

        includeBinding.addPartialPaymentDateBtn.isEnabled = isEnable
        includeBinding.addPartialPaymentDateTV.isEnabled = isEnable
        includeBinding.addPartialPaymentAmountET.isEnabled = isEnable
        includeBinding.addPartialPaymentAmountET.editText?.isEnabled = isEnable
        includeBinding.addPartialPaymentBtn.isEnabled = isEnable
        includeBinding.addPartialPaymentRV.isEnabled = isEnable

        if (isEnable) {

            includeBinding.addPatialPaymentHeadingTV.changeTextColor(
                requireContext(),
                R.color.primaryTextColor
            )
            includeBinding.addPartialPaymentAmountET.editText?.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primaryTextColor
                )
            )
            includeBinding.addPartialPaymentBtn.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.purple_500
                )
            )
            includeBinding.noPayemtAddedTV.changeTextColor(
                requireContext(),
                R.color.primaryTextColor
            )
        } else {

            includeBinding.addPatialPaymentHeadingTV.changeTextColor(
                requireContext(),
                R.color.colorGrey
            )
            includeBinding.addPartialPaymentAmountET.editText?.setTextColor(colorGrey)
            includeBinding.addPartialPaymentBtn.setTextColor(colorGrey)
            includeBinding.noPayemtAddedTV.changeTextColor(requireContext(), R.color.colorGrey)

            //todo : also change the color of recyclerview list items
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
