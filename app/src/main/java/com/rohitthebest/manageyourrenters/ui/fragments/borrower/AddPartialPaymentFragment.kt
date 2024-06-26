package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.annotation.SuppressLint
import android.graphics.Color
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.borrowerAdapters.PartialPaymentAdapter
import com.rohitthebest.manageyourrenters.data.InterestCalculatorFields
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.database.model.PartialPayment
import com.rohitthebest.manageyourrenters.databinding.AddPartialPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddPartialPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PartialPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.calculateInterestAndAmount
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.calculateNumberOfDays
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showDateAndTimePickerDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime.getCalendarInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    private lateinit var oldPartialPaymentList: List<PartialPayment>
    private lateinit var addedPartialPaymentList: ArrayList<PartialPayment>
    private lateinit var removedPartialPaymentList: ArrayList<PartialPayment>
    private var dueLeftAmount = 0.0

    private var isRefreshEnabled = true

    private var mListener: OnPartialPaymentDismiss? = null

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

        oldPartialPaymentList = emptyList()
        addedPartialPaymentList = ArrayList()
        removedPartialPaymentList = ArrayList()

        selectedDate = System.currentTimeMillis()
        initDate()

        getMessage()
        textWatcher()
    }

    private fun initDate() {

        includeBinding.addPartialPaymentDateTV.setDateInTextView(
            selectedDate,
            "dd-MM-yyyy, hh:mm a"
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
            .observe(viewLifecycleOwner) { borrowerPayment ->

                receivedBorrowerPayment = borrowerPayment
                partialPaymentAdapter = PartialPaymentAdapter(borrowerPayment.currencySymbol)
                setUpRecyclerView()

                updateUI()
                getBorrowerPartialPayments()
            }
    }

    private fun getBorrowerPartialPayments() {

        partialPaymentViewModel.getPartialPaymentByBorrowerPaymentKey(receivedBorrowerPaymentKey)
            .observe(viewLifecycleOwner) { partialPaymentList ->

                if (isRefreshEnabled) {

                    oldPartialPaymentList = partialPaymentList

                    partialPaymentList.forEach {

                        addedPartialPaymentList.add(it)
                    }

                    partialPaymentAdapter.submitList(addedPartialPaymentList)
                    calculateDueAmount()

                    isRefreshEnabled = false
                }
            }
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

           dismiss()
        }

        binding.addPartialFragmentToolbar.menu.findItem(R.id.menu_save_btn)
            .setOnMenuItemClickListener {

                saveChangesToTheDatabase()
                true
            }
    }

    override fun onClick(v: View?) {

        if (v?.id == includeBinding.addPartialPaymentDateBtn.id
            || v?.id == includeBinding.addPartialPaymentDateTV.id
        ) {

            showDateAndTimePickerDialog(
                requireContext(),
                selectedDate.getCalendarInstance(),
                false,
                0L,
            ) { calendar ->

                selectedDate = calendar.timeInMillis
                initDate()
            }
        }

        when (v?.id) {

            includeBinding.addPartialPaymentBtn.id -> {

                if (includeBinding.addPartialPaymentAmountET.editText?.isTextValid()!!) {

                    addToPartialPaymentList(
                        includeBinding.addPartialPaymentAmountET.editText?.text.toString()
                            .toDouble()
                    )

                } else {

                    includeBinding.addPartialPaymentAmountET.error = EDIT_TEXT_EMPTY_MESSAGE
                }
            }
        }

        hideKeyBoard(requireActivity())
    }

    private fun addToPartialPaymentList(amount: Double) {

        val partialPayment = PartialPayment()

        partialPayment.apply {

            created = selectedDate
            borrowerId = receivedBorrowerPayment?.borrowerId!!
            borrowerPaymentKey = receivedBorrowerPaymentKey
            this.amount = amount
            uid = receivedBorrowerPayment?.uid!!
            isSynced = false
            key = generateKey("_${receivedBorrowerPayment?.uid}")
        }

        includeBinding.addPartialPaymentAmountET.editText?.setText("")

        addedPartialPaymentList.add(0, partialPayment)
        partialPaymentAdapter.notifyItemInserted(0)

        calculateDueAmount()
    }

    private fun calculateDueAmount() {

        receivedBorrowerPayment?.let { payment ->

            val totalDue = payment.amountTakenOnRent
            val totalAmountPaid = getTotalPaidAmountFromAllThePartialPayment()

            dueLeftAmount = totalDue - totalAmountPaid

            if (payment.isInterestAdded && payment.interest != null && !payment.isDueCleared) {

                val interestAndAmount = calculateInterestAndAmount(
                    InterestCalculatorFields(
                        0L, payment.amountTakenOnRent, payment.interest!!,
                        calculateNumberOfDays(payment.created, System.currentTimeMillis())
                    )
                )

                dueLeftAmount += interestAndAmount.first
            }

            handleTheDue()
        }
    }

    private fun handleTheDue() {

        includeBinding.markAsDoneCB.isChecked = dueLeftAmount <= 0.0

        updateDueAmountTV()

        if (addedPartialPaymentList.isEmpty()) {

            showNoPartialPaymentAddedTV(true)
        } else {

            showNoPartialPaymentAddedTV(false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDueAmountTV() {

        includeBinding.dueLeftAmoutTV.text =
            "${receivedBorrowerPayment?.currencySymbol} ${dueLeftAmount.format(2)}"

        if (dueLeftAmount <= 0.0) {

            includeBinding.dueLeftAmoutTV.changeTextColor(
                requireContext(),
                R.color.color_green
            )
        } else {

            includeBinding.dueLeftAmoutTV.changeTextColor(
                requireContext(),
                R.color.color_orange
            )
        }

    }


    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        when (buttonView?.id) {

            includeBinding.markAsDoneCB.id -> {

                if (isChecked) {

                    // disabling the ui below this checkbox
                    shouldEnableAddingPartialPayment(false)

                    if (dueLeftAmount != 0.0 && dueLeftAmount > 0.0) {

                        addToPartialPaymentList(dueLeftAmount)
                    }

                } else {

                    if (dueLeftAmount <= 0.0) {

                        if (addedPartialPaymentList.isNotEmpty()) {

                            MaterialAlertDialogBuilder(requireContext())
                                .setMessage("Are you sure you want to delete all the partial payments.")
                                .setTitle("Delete all partial payments")
                                .setPositiveButton("Yes") { dialog, _ ->

                                    addedPartialPaymentList.forEach {

                                        if (oldPartialPaymentList.contains(it)) {

                                            removedPartialPaymentList.add(it)
                                        }
                                    }

                                    addedPartialPaymentList.clear()
                                    partialPaymentAdapter.notifyDataSetChanged()
                                    calculateDueAmount()

                                    shouldEnableAddingPartialPayment(true)
                                    dialog.dismiss()
                                }
                                .setNegativeButton("No") { dialog, _ ->

                                    includeBinding.markAsDoneCB.isChecked = true
                                    dialog.dismiss()
                                }
                                .create()
                                .show()

                        }
                    } else {

                        shouldEnableAddingPartialPayment(true)
                    }
                }
            }
        }
    }

    private fun saveChangesToTheDatabase() {

        if (removedPartialPaymentList.any { it.isSynced }
            && !isInternetAvailable(requireContext())
        ) {

            showToast(
                requireContext(),
                "You need internet connection for saving the changes."
            )
            return
        }

        val borrowerPayment = receivedBorrowerPayment?.copy()

        borrowerPayment?.let { payment ->

            calculateDueAmount()

            if (includeBinding.markAsDoneCB.isChecked != payment.isDueCleared) {

                if (includeBinding.markAsDoneCB.isChecked) {

                    payment.isDueCleared = true
                    payment.dueLeftAmount = 0.0
                } else {

                    payment.isDueCleared = false
                    payment.dueLeftAmount = dueLeftAmount
                }

            } else {

                if (dueLeftAmount >= 0.0 && dueLeftAmount != payment.dueLeftAmount) {

                    payment.isDueCleared = false
                    payment.dueLeftAmount = dueLeftAmount
                }
            }

            borrowerPaymentViewModel.updateBorrowerPayment(
                receivedBorrowerPayment!!,
                borrowerPayment
            )

            savePartialPaymentsToFireStore()
        }
    }

    private fun savePartialPaymentsToFireStore() {

        Log.d(TAG, "savePartialPaymentsToFireStore: ")

        val listToBeAddedToTheDatabase =
            (oldPartialPaymentList + addedPartialPaymentList).toSet().toList()

        Log.d(TAG, "savePartialPaymentsToFireStore: $listToBeAddedToTheDatabase")

        if (listToBeAddedToTheDatabase.isNotEmpty()) {

            if (isInternetAvailable(requireContext())) {

                val notSyncedList = listToBeAddedToTheDatabase.filter { !it.isSynced }

                notSyncedList.forEach {

                    it.isSynced = true
                }

                uploadListOfDataToFireStore(
                    requireContext(),
                    collection = FirestoreCollectionsConstants.PARTIAL_PAYMENTS,
                    fromPartialPaymentListToString(notSyncedList)
                )
            }

            partialPaymentViewModel.insertAllPartialPayment(listToBeAddedToTheDatabase)
        }

        if (removedPartialPaymentList.isNotEmpty()) {

            val syncedList = removedPartialPaymentList.filter { it.isSynced }

            if (syncedList.isNotEmpty()) {

                // delete this list from the firestore
                deleteAllDocumentsUsingKeyFromFirestore(
                    requireContext(),
                    getString(R.string.partialPayments),
                    convertStringListToJSON(syncedList.map { it.key })
                )
            }

            partialPaymentViewModel.deleteAllByProvideList(
                removedPartialPaymentList.map { it.key }
            )
        }

        lifecycleScope.launch {

            delay(200)
            if (mListener != null) mListener!!.onPartialPaymentDismissed()
            dismiss()
        }
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

        // adding the keys of the partial payment (isSynced = true) which is going to removed so that
        // it can also be removed from the cloud database
        if (oldPartialPaymentList.contains(partialPayment)) {

            removedPartialPaymentList.add(partialPayment)
        }

        addedPartialPaymentList.remove(partialPayment)
        partialPaymentAdapter.notifyItemRemoved(position)

        calculateDueAmount()

        //partialPaymentViewModel.deletePartialPayment(partialPayment)
        hideKeyBoard(requireActivity())
    }

    private fun getTotalPaidAmountFromAllThePartialPayment(): Double {

        val totalPaid = addedPartialPaymentList.fold(0.0) { acc, partialPayment ->

            acc + partialPayment.amount
        }

        Log.d(TAG, "checkTotalDueAmountAndHandleShowingAlertDialog: $totalPaid")

        return totalPaid
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
                    R.color.color_white
                )
            )
            includeBinding.addPartialPaymentBtn.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dark_blue
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
            includeBinding.addPartialPaymentBtn.setTextColor(Color.BLACK)
            includeBinding.addPartialPaymentBtn.setBackgroundColor(colorGrey)
            includeBinding.noPayemtAddedTV.changeTextColor(requireContext(), R.color.colorGrey)

            //todo : also change the color of recyclerview list items
        }
    }

    interface OnPartialPaymentDismiss {

        fun onPartialPaymentDismissed()
    }

    fun setOnPartialPaymentDialogDismissListener(listener: OnPartialPaymentDismiss) {

        this.mListener = listener
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): AddPartialPaymentFragment {
            val fragment = AddPartialPaymentFragment()
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
