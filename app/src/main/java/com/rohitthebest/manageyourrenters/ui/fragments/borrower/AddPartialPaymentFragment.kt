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
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.database.model.PartialPayment
import com.rohitthebest.manageyourrenters.databinding.AddPartialPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddPartialPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PartialPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
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
    private val borrowerViewModel by viewModels<BorrowerViewModel>()

    private var receivedBorrowerPayment: BorrowerPayment? = null
    private var receivedBorrowerPaymentKey: String = ""

    private var selectedDate = 0L

    private lateinit var partialPaymentAdapter: PartialPaymentAdapter
    private var isPaymentMarkedAsDone = false

    private lateinit var oldPartialPaymentList: List<PartialPayment>
    private lateinit var addedPartialPaymentList: ArrayList<PartialPayment>
    private lateinit var removedPartialPaymentList: ArrayList<PartialPayment>
    private var dueLeftAmount = 0.0

    private var isRefereshEnabled = true

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

                if (isRefereshEnabled) {

                    oldPartialPaymentList = partialPaymentList

                    partialPaymentList.forEach {

                        addedPartialPaymentList.add(it)
                    }

                    partialPaymentAdapter.submitList(addedPartialPaymentList)
                    calculateDueAmount()
                    //handleTheDue()

                    isRefereshEnabled = false
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

        val totalDue = receivedBorrowerPayment?.amountTakenOnRent!!
        val totalAmountPaid = getTotalPaidAmountFromAllThePartialPayment()

        dueLeftAmount = totalDue - totalAmountPaid
        handleTheDue()
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
            "${receivedBorrowerPayment?.currencySymbol} $dueLeftAmount"

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

        receivedBorrowerPayment?.let { borrowerPayment ->

            calculateDueAmount()

            val map = HashMap<String, Any?>()

            if (includeBinding.markAsDoneCB.isChecked != borrowerPayment.isDueCleared) {

                receivedBorrowerPayment!!.isDueCleared = includeBinding.markAsDoneCB.isChecked

                map["dueCleared"] = receivedBorrowerPayment!!.isDueCleared

                if (includeBinding.markAsDoneCB.isChecked) {

                    receivedBorrowerPayment!!.dueLeftAmount = 0.0
                    map["dueLeftAmount"] = 0.0

                } else {

                    receivedBorrowerPayment!!.dueLeftAmount = dueLeftAmount
                    map["dueLeftAmount"] = dueLeftAmount
                }

            } else {

                if (dueLeftAmount >= 0.0 && dueLeftAmount != borrowerPayment.dueLeftAmount) {

                    Log.d(TAG, "saveChangesToTheDatabase: dueLeftAmount : $dueLeftAmount")

                    receivedBorrowerPayment!!.dueLeftAmount = dueLeftAmount
                    map["dueLeftAmount"] = dueLeftAmount
                }
            }

            //borrower payment was already synced - need to be updated
            if (receivedBorrowerPayment!!.isSynced) {

                if (isInternetAvailable(requireContext())) {

                    if (map.isNotEmpty()) {

                        updateDocumentOnFireStore(
                            requireContext(),
                            map,
                            getString(R.string.borrowerPayments),
                            receivedBorrowerPayment!!.key
                        )
                    }

                } else {

                    if (map.isNotEmpty()) {

                        receivedBorrowerPayment!!.isSynced = false
                    }
                }
            } else {

                //not synced - needs to be uploaded

                if (isInternetAvailable(requireContext())) {

                    receivedBorrowerPayment!!.isSynced = true

                    Log.d(TAG, "saveChangesToTheDatabase: upload payment to firestore")

                    uploadDocumentToFireStore(
                        requireContext(),
                        getString(R.string.borrowerPayments),
                        borrowerPayment.key
                    )
                }

            }

            borrowerPaymentViewModel.updateBorrowerPayment(receivedBorrowerPayment!!)
            savePartialPaymentsToFireStore()
            getAndUpdateTotalDueAmountOfBorrower()

        }
    }

    private fun getAndUpdateTotalDueAmountOfBorrower() {

        Log.d(TAG, "getAndUpdateTotalDueAmountOfBorrower: ")

        borrowerPaymentViewModel.getTotalDueOfTheBorrower(receivedBorrowerPayment?.borrowerKey!!)
            .observe(viewLifecycleOwner) { totalDue ->

                Log.d(TAG, "getAndUpdateTotalDueAmountOfBorrower: Total due : $totalDue")
                if (totalDue == null) {

                    updateBorrowerDueAmount(0.0)
                } else {

                    updateBorrowerDueAmount(totalDue)
                }
            }
    }

    var isBorrowerUpdateEnabled = true

    private fun updateBorrowerDueAmount(totalDue: Double?) {

        Log.d(TAG, "updateBorrowerDueAmount: ")

        borrowerViewModel.getBorrowerByKey(receivedBorrowerPayment?.borrowerKey!!)
            .observe(viewLifecycleOwner) { borrower ->

                if (isBorrowerUpdateEnabled) {
                    Log.d(TAG, "updateBorrowerDueAmount: ${borrower.name}")

                    borrower.totalDueAmount = totalDue!!
                    val map = HashMap<String, Any?>()
                    map["totalDueAmount"] = totalDue

                    //already synced - need to update
                    if (borrower.isSynced) {

                        if (isInternetAvailable(requireContext())) {

                            updateDocumentOnFireStore(
                                requireContext(),
                                map,
                                getString(R.string.borrowers),
                                borrower.key
                            )
                        } else {

                            borrower.isSynced = false
                        }
                    } else {

                        // borrower not synced - need to be uploaded
                        if (isInternetAvailable(requireContext())) {

                            borrower.isSynced = true

                            uploadDocumentToFireStore(
                                requireContext(),
                                getString(R.string.borrowers),
                                borrower.key
                            )
                        }
                    }

                    borrowerViewModel.updateBorrower(requireContext(), borrower)

                    isBorrowerUpdateEnabled = false
                }
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
                    collection = getString(R.string.partialPayments),
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
            requireActivity().onBackPressed()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
