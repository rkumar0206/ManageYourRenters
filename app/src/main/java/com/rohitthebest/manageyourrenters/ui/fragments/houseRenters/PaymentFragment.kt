package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters.ShowPaymentAdapter
import com.rohitthebest.manageyourrenters.database.model.Payment
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentPaymentBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertPaymentToJSONString
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertRenterToJSONString
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertStringListToJSON
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper.Companion.deleteAllDocumentsUsingKey
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper.Companion.updateDocumentOnFireStore
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper.Companion.uploadDocumentToFireStore
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.changeTextColor
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.setDateInTextView
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class PaymentFragment : Fragment(), View.OnClickListener, ShowPaymentAdapter.OnClickListener {

    private val renterViewModel: RenterViewModel by viewModels()
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
    ): View {

        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = ShowPaymentAdapter()
        paymentKeyList = emptyList()

        getMessage()
        initListener()
    }

    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    PaymentFragmentArgs.fromBundle(it)
                }

                val renterKey = args?.renterInfoMessage
                getTheRenter(renterKey)
                //receivedRenter = convertJSONtoRenter(args?.renterInfoMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTheRenter(renterKey: String?) {

        renterViewModel.getRenterByKey(renterKey!!).observe(viewLifecycleOwner) {

            receivedRenter = it

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
                .observe(viewLifecycleOwner) {

                    if (it.isNotEmpty()) {

                        hideNoPaymentsTV()
                        initializeSearchView(it)

                        paymentKeyList =
                            it.filter { payment -> payment.isSynced == getString(R.string.t) }
                                .map { pay ->

                                    pay.key
                                }

                        updateCurrentDueOrAdvanceTV()

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

        //showToast(requireContext(), payment.id.toString())

        MaterialDialog(requireContext(), BottomSheet())
            .show {

                title(text = "BILL")

                customView(
                    R.layout.show_bill_layout,
                    scrollable = true,
                    noVerticalPadding = true
                )

                initializeValuesToBill(getCustomView(), payment)
            }
    }

    private fun initializeValuesToBill(customView: View, payment: Payment) {

        setRenterInfo(customView)

        setBillingParameters(customView, payment)

        setElectricFields(customView, payment)

        setDuesOrAdvanceOdLastPayment(customView, payment)

        setExtraFields(customView, payment)

        setDuesOrAdvance(customView, payment)

        setTotalRent(customView, payment)
    }

    //[Start of setting fields in bills textViews]

    private fun setRenterInfo(customView: View) {

        //renter info
        customView.findViewById<TextView>(R.id.showBill_renterName).text = receivedRenter?.name
        customView.findViewById<TextView>(R.id.showBill_renterMobile).text =
            receivedRenter?.mobileNumber
        customView.findViewById<TextView>(R.id.showBill_renterAddress).text =
            receivedRenter?.address

    }

    private fun setBillingParameters(customView: View, payment: Payment) {

        //billing parameter
        customView.findViewById<TextView>(R.id.showBill_billDate)
            .setDateInTextView(payment.timeStamp)
        customView.findViewById<TextView>(R.id.showBill_billTime)
            .setDateInTextView(payment.timeStamp, "hh:mm a")
        customView.findViewById<TextView>(R.id.showBill_billPeriod).text =
            if (payment.bill?.billPeriodType == getString(R.string.by_month)) {

                "${payment.bill!!.billMonth}, ${
                    WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                        payment.timeStamp,
                        "yyyy"
                    )
                }"
            } else {

                "${WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(payment.bill?.billDateFrom)}" +
                        " to ${
                            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                payment.bill?.billDateTill
                            )
                        }"
            }
    }

    private fun setElectricFields(customView: View, payment: Payment) {

        //electricity
        customView.findViewById<TextView>(R.id.showBill_previousReading).text =
            "${String.format("%.2f", payment.electricBill?.previousReading)} unit(s)"
        customView.findViewById<TextView>(R.id.showBill_currentReading).text =
            "${String.format("%.2f", payment.electricBill?.currentReading)} unit(s)"
        customView.findViewById<TextView>(R.id.showBill_rate).text =
            "${String.format("%.2f", payment.electricBill?.rate)} per/unit"
        customView.findViewById<TextView>(R.id.showBill_difference).text =
            "${String.format("%.2f", payment.electricBill?.differenceInReading)} unit(s)"
        customView.findViewById<TextView>(R.id.showBill_electricity_total).text =
            "${payment.bill?.currencySymbol} ${payment.electricBill?.totalElectricBill}"

    }

    private fun setExtraFields(customView: View, payment: Payment) {

        //Extra
        customView.findViewById<TextView>(R.id.showBill_extraFieldName).text =
            if (payment.extraFieldName == "") {

                "Extra"
            } else {
                payment.extraFieldName
            }

        customView.findViewById<TextView>(R.id.showBill_extraFieldAmount).text =
            if (payment.extraAmount == "") {

                "${payment.bill?.currencySymbol} 0.0"
            } else {

                "${payment.bill?.currencySymbol} ${payment.extraAmount}"
            }
    }

    private fun setDuesOrAdvanceOdLastPayment(customView: View, payment: Payment) {

        val dueOfLastPayment = getDuesOfLastPayment(payment)

        when {

            dueOfLastPayment > 0.0 -> {

                //due
                customView.findViewById<TextView>(R.id.showBill_dueOfLastPayAmount).text =
                    "${payment.bill?.currencySymbol} $dueOfLastPayment"

                customView.findViewById<TextView>(R.id.showBill_paidInAdvanceInlastPayAmount).text =
                    "${payment.bill?.currencySymbol} 0.0"

            }

            dueOfLastPayment < 0.0 -> {

                //advance
                customView.findViewById<TextView>(R.id.showBill_dueOfLastPayAmount).text =
                    "${payment.bill?.currencySymbol} 0.0"

                customView.findViewById<TextView>(R.id.showBill_paidInAdvanceInlastPayAmount).text =
                    "${payment.bill?.currencySymbol} $dueOfLastPayment"
            }

            else -> {
                customView.findViewById<TextView>(R.id.showBill_dueOfLastPayAmount).text =
                    "${payment.bill?.currencySymbol} 0.0"

                customView.findViewById<TextView>(R.id.showBill_paidInAdvanceInlastPayAmount).text =
                    "${payment.bill?.currencySymbol} 0.0"
            }
        }
    }

    private fun setDuesOrAdvance(customView: View, payment: Payment) {

        val dueOrAdvance = payment.amountPaid?.toDouble()?.minus(payment.totalRent.toDouble())!!

        customView.findViewById<TextView>(R.id.showBill_dueAmount).text =

            when {

                dueOrAdvance < 0.0 -> {

                    //due
                    "${payment.bill?.currencySymbol} ${String.format("%.2f", abs(dueOrAdvance))}"
                }

                dueOrAdvance > 0.0 -> {

                    customView.findViewById<TextView>(R.id.show_billDueOrArrearTV).text =
                        "Paid in advance"
                    "${payment.bill?.currencySymbol} ${String.format("%.2f", dueOrAdvance)}"
                }
                else -> {

                    "${payment.bill?.currencySymbol} 0.0"
                }
            }

    }

    private fun setTotalRent(customView: View, payment: Payment) {

        //total rent
        customView.findViewById<TextView>(R.id.showBill_houseRent).text =
            "${payment.bill?.currencySymbol} ${payment.houseRent}"

        customView.findViewById<TextView>(R.id.showBill_parking).text =
            "${payment.bill?.currencySymbol} ${payment.parkingRent}"

        customView.findViewById<TextView>(R.id.showBill_electricity).text =
            "${payment.bill?.currencySymbol} ${payment.electricBill?.totalElectricBill}"

        customView.findViewById<TextView>(R.id.showBill_AmountPaid).text =
            "${payment.bill?.currencySymbol} ${payment.amountPaid}"

        customView.findViewById<TextView>(R.id.showBill_netDemand).text =
            "${payment.bill?.currencySymbol} ${payment.totalRent}"
    }

    //[END of setting fields in bills textViews]

    private fun getDuesOfLastPayment(payment: Payment): Double {

        val houseRent = payment.houseRent.toDouble()
        val parking = if (payment.isTakingParkingBill == getString(R.string.t))
            payment.parkingRent?.toDouble()!!
        else
            0.0
        val electricBill = if (payment.electricBill?.isTakingElectricBill == getString(R.string.t))
            payment.electricBill?.totalElectricBill?.toDouble()!!
        else
            0.0
        val extra = if (payment.extraAmount != "")
            payment.extraAmount?.toDouble()!!
        else
            0.0

        return payment.totalRent.toDouble() - (houseRent + parking + electricBill + extra)
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
        updateRenterDuesOrAdvance()

        getPaymentListOfRenter()

        var isUndoClicked = false

        Snackbar.make(binding.paymentCoordL, "Payment deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {

                isUndoClicked = true

                paymentViewModel.insertPayment(payment)
                updateRenterDuesOrAdvance()
                getPaymentListOfRenter()

                showToast(requireContext(), "Payment restored...")
            }
            .addCallback(object : Snackbar.Callback() {

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {

                    if (!isUndoClicked && payment.isSynced == getString(R.string.t)) {

                        //updateRenterDuesOrAdvance()
                        editRenterInDatabase(receivedRenter!!)

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

    private fun updateRenterDuesOrAdvance() {

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

    private fun editRenterInDatabase(renter: Renter) {

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

        receivedRenter?.dueOrAdvanceAmount = 0.0

        editRenterInDatabase(receivedRenter!!)
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