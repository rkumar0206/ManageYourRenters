package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.borrowerAdapters.PartialPaymentAdapter
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.database.model.PartialPayment
import com.rohitthebest.manageyourrenters.databinding.AddPartialPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddPartialPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PartialPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import com.rohitthebest.manageyourrenters.utils.changeTextColor
import com.rohitthebest.manageyourrenters.utils.isTextValid
import com.rohitthebest.manageyourrenters.utils.setDateInTextView
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AddPartialPaymentFragme"

@AndroidEntryPoint
class AddPartialPaymentFragment : BottomSheetDialogFragment(),
    CompoundButton.OnCheckedChangeListener, View.OnClickListener,
    PartialPaymentAdapter.OnClickListener {

    private var _binding: FragmentAddPartialPaymentBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: AddPartialPaymentLayoutBinding

    private val borrowerPaymentViewModel by viewModels<BorrowerPaymentViewModel>()
    private val partialPaymentViewModel by viewModels<PartialPaymentViewModel>()

    private var receivedBorrowerPayment: BorrowerPayment? = null
    private var receivedBorrowerPaymentKey: String = ""

    private var selectedDate = 0L

    private lateinit var partialPaymentAdapter: PartialPaymentAdapter
    private var isPaymentMarkedAsDone = false
    private lateinit var removedPartialPaymentKeyList: ArrayList<String>
    private var dueLeftAmount = 0.0;

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

            Log.d(TAG, "getMessage: $receivedBorrowerPaymentKey")
            getBorrowerPayment()
        }
    }

    private fun getBorrowerPayment() {

        borrowerPaymentViewModel.getBorrowerPaymentByKey(receivedBorrowerPaymentKey)
            .observe(viewLifecycleOwner, { borrowerPayment ->

                receivedBorrowerPayment = borrowerPayment
                dueLeftAmount = borrowerPayment.dueLeftAmount

                Log.d(TAG, "getBorrowerPayment: Due left amount : $dueLeftAmount")
                partialPaymentAdapter = PartialPaymentAdapter(borrowerPayment.currencySymbol)
                setUpRecyclerView()

                updateUI()
                getBorrowerPartialPayments()
                getTheTotalPartialPaymentSumAndHandleTheDue()
            })
    }

    private fun setUpRecyclerView() {

        includeBinding.addPartialPaymentRV.apply {

            setHasFixedSize(true)
            adapter = partialPaymentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        partialPaymentAdapter.setOnClickListener(this)
    }

    override fun onDeleteBtnClick(partialPayment: PartialPayment, position: Int) {

        dueLeftAmount += partialPayment.amount

        // adding the keys of the partial payment (isSynced = true) which is going to removed so that
        // it can also be removed from the cloud database
        if (partialPayment.isSynced) {

            removedPartialPaymentKeyList.add(partialPayment.key)
        }

        partialPaymentViewModel.deletePartialPayment(partialPayment)
        hideKeyBoard(requireActivity())
    }

    private fun getBorrowerPartialPayments() {

        partialPaymentViewModel.getPartialPaymentByBorrowerPaymentKey(receivedBorrowerPaymentKey)
            .observe(viewLifecycleOwner, { partialPaymentList ->

                partialPaymentAdapter.submitList(partialPaymentList)

                if (partialPaymentList.isNotEmpty()) {

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

                    initPartialPaymentAndAddToDatabase()

                } else {

                    includeBinding.addPartialPaymentAmountET.error = EDIT_TEXT_EMPTY_MESSAGE
                }
            }
        }

        hideKeyBoard(requireActivity())
    }

    private fun initPartialPaymentAndAddToDatabase() {

        val partialPayment = PartialPayment()

        partialPayment.apply {

            created = selectedDate
            borrowerId = receivedBorrowerPayment?.borrowerId!!
            borrowerPaymentKey = receivedBorrowerPaymentKey
            amount = includeBinding.addPartialPaymentAmountET.editText?.text.toString()
                .toDouble()
            uid = receivedBorrowerPayment?.uid!!
            isSynced = false
            key = generateKey("_${receivedBorrowerPayment?.uid}")
        }

        includeBinding.addPartialPaymentAmountET.editText?.setText("")

        partialPaymentViewModel.insertPartialPayment(partialPayment)

        dueLeftAmount -= partialPayment.amount

    }

    private fun getTheTotalPartialPaymentSumAndHandleTheDue() {

        try {
            partialPaymentViewModel.getTheSumOfPartialPaymentsOfBorrowerPayment(
                receivedBorrowerPaymentKey
            ).observe(viewLifecycleOwner, { totalPartialPayment ->

                Log.d(
                    TAG,
                    "getTheTotalPartialPaymentSumAndHandleTheDue: Total Due : $totalPartialPayment"
                )
                Log.d(TAG, "getTheTotalPartialPaymentSumAndHandleTheDue: Due Left : $dueLeftAmount")

                if (totalPartialPayment != null) {

                    includeBinding.markAsDoneCB.isChecked =
                        totalPartialPayment >= receivedBorrowerPayment?.dueLeftAmount!!
                } else {

                    includeBinding.markAsDoneCB.isChecked = false
                }
            })
        } catch (e: NullPointerException) {

            e.printStackTrace()
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
