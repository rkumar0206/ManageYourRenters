package com.rohitthebest.manageyourrenters.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.ShowPaymentAdapter
import com.rohitthebest.manageyourrenters.database.entity.Payment
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentPaymentBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertJSONtoRenter
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertPaymentToJSONString
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertRenterToJSONString
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertStringListToJSON
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper.Companion.deleteAllDocumentsUsingKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.changeTextColor
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*

@AndroidEntryPoint
class PaymentFragment : Fragment(), View.OnClickListener, ShowPaymentAdapter.OnClickListener {

    private val paymentViewModel: PaymentViewModel by viewModels()

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private var receivedRenter: Renter? = null

    private lateinit var paymentKeyList: List<String>

    private lateinit var mAdapter: ShowPaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = ShowPaymentAdapter()
        paymentKeyList = emptyList()

        getMessage()
        initListener()

        showProgressBar()

        GlobalScope.launch {

            delay(300)

            withContext(Dispatchers.Main) {

                getPaymentListOfRenter()
            }
        }
    }

    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    PaymentFragmentArgs.fromBundle(it)
                }

                receivedRenter = convertJSONtoRenter(args?.renterInfoMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getPaymentListOfRenter() {

        try {

            paymentViewModel.getAllPaymentsListOfRenter(receivedRenter?.key!!)
                .observe(viewLifecycleOwner) {

                    if (it.isNotEmpty()) {

                        hideNoPaymentsTV()
                        initializeSearchView(it)

                        paymentKeyList =
                            it.filter { payment -> payment.isSynced == getString(R.string.t) }
                                .map { pay ->

                                    pay.key
                                }

                        updateCurrentDueOrAdvanceTV(it.first())

                    } else {

                        showNoPaymentsTV()
                    }

                    setUpRecyclerView(it)
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrentDueOrAdvanceTV(lastPayment: Payment) {

        when (lastPayment.isDueOrPaidInAdvance) {

            getString(R.string.due) -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_orange)
                binding.dueOrAdvancedTV.text =
                    "Current due of ${receivedRenter?.name} : ${lastPayment.dueAmount}"
            }
            getString(R.string.paid_in_advance) -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_green)
                binding.dueOrAdvancedTV.text =
                    "Current advance of ${receivedRenter?.name} : ${lastPayment.paidInAdvanceAmount}"
            }
            else -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_green)
                binding.dueOrAdvancedTV.text =
                    "There is no due or advance of ${receivedRenter?.name}"
            }
        }
    }

    private fun initializeSearchView(it: List<Payment>?) {

        try {

            binding.paymentSV.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (s?.isEmpty()!!) {

                        setUpRecyclerView(it)
                    } else {


                        val filteredList = it?.filter { payment ->

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

                            payment.bill?.billMonth?.toLowerCase(Locale.ROOT)?.contains(

                                s.toString().trim().toLowerCase(Locale.ROOT)
                            )!! ||
                                    from?.contains(s.toString().trim())!!
                                    ||
                                    till?.contains(s.toString().trim())!!

                        }

                        setUpRecyclerView(filteredList)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpRecyclerView(paymentList: List<Payment>?) {

        try {

            paymentList?.let {

                mAdapter.submitList(it)

                binding.paymentRV.apply {

                    adapter = mAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    setHasFixedSize(true)
                }

            }

            mAdapter.setOnClickListener(this)

        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }

        hideProgressBar()
    }

    override fun onPaymentClick(payment: Payment) {

        //todo : show bottom sheet
    }

    override fun onSyncClicked(payment: Payment) {

        if (isInternetAvailable(requireContext())) {

            if (payment.isSynced == getString(R.string.t)) {

                showToast(requireContext(), "Already Synced")
            } else {

                payment.isSynced = getString(R.string.t)

                FirebaseServiceHelper.uploadDocumentToFireStore(
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

    override fun onDeleteClicked(payment: Payment) {

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

    private fun deletePayment(payment: Payment) {

        paymentViewModel.deletePayment(payment)

        var isUndoClicked = false

        Snackbar.make(binding.paymentCoordL, "Payment deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {

                isUndoClicked = true

                paymentViewModel.insertPayment(payment)
                showToast(requireContext(), "Payment restored...")
            }
            .addCallback(object : Snackbar.Callback() {

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {

                    if (!isUndoClicked && payment.isSynced == getString(R.string.t)) {

                        FirebaseServiceHelper.deleteDocumentFromFireStore(
                            context = requireContext(),
                            collection = getString(R.string.payments),
                            documentKey = payment.key
                        )
                    }
                }
            })
            .show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {

        binding.addPyamentFAB.setOnClickListener(this)
        binding.deleteAllPaymentsBtn.setOnClickListener(this)
        binding.paymentBackBtn.setOnClickListener(this)

        binding.paymentRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                try {
                    if (dy > 0 && binding.addPyamentFAB.visibility == View.VISIBLE) {

                        binding.addPyamentFAB.hide()
                    } else if (dy < 0 && binding.addPyamentFAB.visibility != View.VISIBLE) {

                        binding.addPyamentFAB.show()

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })


        /*
                if (binding.paymentSV.visibility == View.VISIBLE) {

                    binding.paymentSV.requestFocus()
                    showKeyboard(requireActivity(), binding.paymentSV)
                } else {

                    hideKeyBoard(requireActivity())
                    binding.paymentSV.setText("")
                }
*/

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
                        .setTitle("Are you sure?")
                        .setMessage(getString(R.string.delete_warning_message))
                        .setPositiveButton("Delete") { dialogInterface, _ ->

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

            deleteAllDocumentsUsingKey(
                requireContext(),
                getString(R.string.payments),
                convertStringListToJSON(paymentKeyList)
            )
        }

    }

    private fun showNoPaymentsTV() {

        try {

            binding.noPaymentsTV.show()
            binding.paymentRV.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideNoPaymentsTV() {

        try {

            binding.noPaymentsTV.hide()
            binding.paymentRV.show()
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