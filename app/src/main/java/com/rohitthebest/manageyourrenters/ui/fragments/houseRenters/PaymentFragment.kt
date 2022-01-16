package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters.ShowPaymentAdapter
import com.rohitthebest.manageyourrenters.database.model.Payment
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentPaymentBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs

private const val TAG = "PaymentFragment"

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class PaymentFragment : Fragment(), View.OnClickListener, ShowPaymentAdapter.OnClickListener {

    private val renterViewModel: RenterViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by viewModels()

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private var receivedRenter: Renter? = null

    private lateinit var paymentKeyList: List<String>
    private lateinit var paymentAdapter: ShowPaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paymentAdapter = ShowPaymentAdapter()
        paymentKeyList = emptyList()

        getMessage()
        initListener()
        setUpRecyclerView()
    }

    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    PaymentFragmentArgs.fromBundle(it)
                }

                val renterKey = args?.renterInfoMessage
                getTheRenter(renterKey)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTheRenter(renterKey: String?) {

        renterViewModel.getRenterByKey(renterKey!!).observe(viewLifecycleOwner) { renter ->

            receivedRenter = renter

            showProgressBar()

            lifecycleScope.launch {

                delay(300)

                withContext(Dispatchers.Main) {

                    getPaymentListOfRenter()
                }
            }
        }
    }

    private fun getPaymentListOfRenter() {

        try {

            paymentViewModel.getAllPaymentsListOfRenter(receivedRenter?.key!!)
                .observe(viewLifecycleOwner) { paymentList ->

                    if (paymentList.isNotEmpty()) {

                        hideNoPaymentsTV()
                        initializeSearchView(paymentList)

                        paymentKeyList =
                            paymentList.filter { payment -> payment.isSynced == getString(R.string.t) }
                                .map { pay ->

                                    pay.key
                                }

                        updateCurrentDueOrAdvanceTV()

                        paymentAdapter.submitList(paymentList)

                    } else {

                        showNoPaymentsTV()
                    }

                    hideProgressBar()
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrentDueOrAdvanceTV() {

        when {

            receivedRenter?.dueOrAdvanceAmount!! < 0.0 -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_orange)
                binding.dueOrAdvancedTV.text =
                    "Current due of ${receivedRenter?.name} : ${abs(receivedRenter?.dueOrAdvanceAmount!!)}"
            }
            receivedRenter?.dueOrAdvanceAmount!! > 0.0 -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_green)
                binding.dueOrAdvancedTV.text =
                    "Current advance of ${receivedRenter?.name} : ${receivedRenter?.dueOrAdvanceAmount}"
            }
            else -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_green)
                binding.dueOrAdvancedTV.text =
                    "There is no due or advance of ${receivedRenter?.name}"
            }
        }
    }

    private fun initializeSearchView(paymentList: List<Payment>?) {

        try {

            binding.paymentSV.onTextChangedListener { s ->

                if (s?.isEmpty()!!) {

                    binding.paymentRV.scrollToPosition(0)
                    paymentAdapter.submitList(paymentList)
                } else {

                    val filteredList = paymentList?.filter { payment ->

                        var from: String? = ""
                        var till: String? = ""

                        if (payment.bill?.billPeriodType == getString(R.string.by_date)) {

                            from =
                                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                    payment.bill?.billDateFrom
                                )

                            till =
                                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                    payment.bill?.billDateTill
                                )
                        }

                        payment.bill?.billMonth?.lowercase(Locale.ROOT)?.contains(

                            s.toString().trim().lowercase(Locale.ROOT)
                        )!! ||
                                from?.contains(s.toString().trim())!!
                                ||
                                till?.contains(s.toString().trim())!!

                    }

                    paymentAdapter.submitList(filteredList)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpRecyclerView() {

        try {

            binding.paymentRV.apply {

                adapter = paymentAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                changeVisibilityOfFABOnScrolled(binding.addPyamentFAB)
            }

            paymentAdapter.setOnClickListener(this)

        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    override fun onPaymentClick(payment: Payment) {

        //showToast(requireContext(), payment.id.toString())

        val action = PaymentFragmentDirections.actionPaymentFragmentToRenterBillFragment(
            paymentKey = payment.key
        )

        findNavController().navigate(action)

    }

    override fun onSyncClicked(payment: Payment) {

        if (isInternetAvailable(requireContext())) {

            if (payment.isSynced == getString(R.string.t)) {

                showToast(requireContext(), "Already Synced")
            } else {

                payment.isSynced = getString(R.string.t)

                uploadDocumentToFireStore(
                    requireContext(),
                    convertPaymentToJSONString(payment),
                    getString(R.string.renters),
                    payment.key
                )

                paymentViewModel.insertPayment(payment)
            }

        } else {

            showNoInternetMessage(requireContext())
        }

    }


    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ==================================================================================================
    // todo : requires change
    // lookup the code and make the necessary changes
    override fun onDeleteClicked(payment: Payment) {

        showAlertDialogForDeletion(
            requireContext(),
            { dialog ->

                // checking if the payment is synced, if it's not, then deleting it from
                // only the local database
                if (payment.isSynced == getString(R.string.f)) {

                    deletePayment(payment)
                } else {

                    if (isInternetAvailable(requireContext())) {

                        deletePayment(payment)
                    } else {
                        showNoInternetMessage(requireContext())
                    }
                }
                dialog.dismiss()

            },
            { dialog ->

                dialog.dismiss()
            }
        )

/*
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Are you sure?")
            .setMessage(getString(R.string.delete_warning_message))
            .setPositiveButton("Delete") { dialogInterface, _ ->

                if (payment.isSynced == getString(R.string.f)) {

                    deletePayment(payment)
                } else {

                    if (isInternetAvailable(requireContext())) {

                        deletePayment(payment)
                    } else {
                        showNoInternetMessage(requireContext())
                    }
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()
*/

    }

    private fun deletePayment(payment: Payment) {

        paymentViewModel.deletePayment(payment)
        updateRenterDuesOrAdvanceTextView()

        getPaymentListOfRenter()

        var isUndoClicked = false

        binding.paymentCoordL.showSnackbarWithActionAndDismissListener(
            "Payment deleted",
            "Undo",
            {
                isUndoClicked = true

                paymentViewModel.insertPayment(payment)
                updateRenterDuesOrAdvanceTextView()
                getPaymentListOfRenter()

                showToast(requireContext(), "Payment restored...")
            },
            {
                if (!isUndoClicked && payment.isSynced == getString(R.string.t)) {

                    //updateRenterDuesOrAdvance()
                    updateRenterInDatabase(receivedRenter!!)

                    deleteDocumentFromFireStore(
                        context = requireContext(),
                        collection = getString(R.string.payments),
                        documentKey = payment.key
                    )
                }

            }
        )
    }


    override fun onMessageBtnClicked(paymentMessage: String) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Message")
            .setMessage(
                if (paymentMessage != "") {
                    paymentMessage
                } else {
                    "No message..."
                }
            )
            .setPositiveButton("Ok") { dialogInterface, _ ->

                dialogInterface.dismiss()
            }
            .create()
            .show()
    }

    private fun updateRenterDuesOrAdvanceTextView() {

        var dueOrAdvance: Double
        paymentViewModel.getAllPaymentsListOfRenter(receivedRenter?.key!!).observe(
            viewLifecycleOwner
        ) {

            try {
                val payment = it.first()

                dueOrAdvance = (payment.amountPaid?.toDouble()
                    ?.minus(payment.totalRent.toDouble())!!)


                receivedRenter!!.dueOrAdvanceAmount = dueOrAdvance
                renterViewModel.insertRenter(receivedRenter!!)

                //editRenterInDatabase(receivedRenter!!)

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateRenterInDatabase(renter: Renter) {

        if (isInternetAvailable(requireContext())) {

            if (renter.isSynced == getString(R.string.t)) {

                //update on firestore
                val map = HashMap<String, Any?>()
                map["dueOrAdvanceAmount"] = renter.dueOrAdvanceAmount

                updateDocumentOnFireStore(
                    requireContext(),
                    map,
                    getString(R.string.renters),
                    renter.key!!
                )

                renterViewModel.insertRenter(renter)
            } else {

                renter.isSynced = getString(R.string.t)

                //insert on firestore
                uploadDocumentToFireStore(
                    requireContext(),
                    convertRenterToJSONString(renter),
                    getString(R.string.renters),
                    renter.key!!
                )

                renterViewModel.insertRenter(renter)
            }

        } else {

            renter.isSynced = getString(R.string.f)
            renterViewModel.insertRenter(renter)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {

        binding.addPyamentFAB.setOnClickListener(this)
        binding.deleteAllPaymentsBtn.setOnClickListener(this)
        binding.paymentBackBtn.setOnClickListener(this)

        binding.paymentRV.changeVisibilityOfFABOnScrolled(
            binding.addPyamentFAB
        )
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.addPyamentFAB.id -> {

                try {

                    val action =
                        PaymentFragmentDirections.actionPaymentFragmentToAddPaymentFragment(
                            convertRenterToJSONString(receivedRenter!!)
                        )
                    findNavController().navigate(action)
                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }

            binding.deleteAllPaymentsBtn.id -> {

                if (binding.noPaymentsTV.visibility == View.VISIBLE) {

                    showToast(requireContext(), "No Payments added!!!")
                } else {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete all payments?")
                        .setMessage(getString(R.string.delete_warning_message))
                        .setPositiveButton("Delete All") { dialogInterface, _ ->

                            if (isInternetAvailable(requireContext())) {

                                deleteAllPayments()
                            } else {
                                showNoInternetMessage(requireContext())
                            }
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->

                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }

            binding.paymentBackBtn.id -> {

                requireActivity().onBackPressed()
            }
        }
    }

    private fun deleteAllPayments() {

        deleteFromCloud()

        paymentViewModel.deleteAllPaymentsOfRenter(receivedRenter?.key!!)

        showToast(requireContext(), "Deleted all the payments of ${receivedRenter?.name}")
    }

    private fun deleteFromCloud() {

        if (paymentKeyList.isNotEmpty()) {

            deleteAllDocumentsUsingKeyFromFirestore(
                requireContext(),
                getString(R.string.payments),
                convertStringListToJSON(paymentKeyList)
            )
        }

        receivedRenter?.dueOrAdvanceAmount = 0.0

        updateRenterInDatabase(receivedRenter!!)
    }

    private fun showNoPaymentsTV() {

        try {

            binding.noPaymentsTV.show()
            binding.paymentRV.hide()
            binding.paymentAppBarLL.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideNoPaymentsTV() {

        try {

            binding.noPaymentsTV.hide()
            binding.paymentRV.show()
            binding.paymentAppBarLL.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showProgressBar() {

        try {

            binding.paymentFragProgressBar.show()
            binding.paymentRV.hide()
        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {

        try {

            binding.paymentFragProgressBar.hide()
            binding.paymentRV.show()
        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }

}