package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
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

    private lateinit var paymentAdapter: ShowPaymentAdapter

    private var rvStateParcelable: Parcelable? = null

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

        showProgressBar()

        getMessage()
        initListener()
        setUpRecyclerView()

        getRvState()
    }

    private fun getRvState() {

        paymentViewModel.renterPaymentRvState.observe(viewLifecycleOwner) { parcelable ->

            parcelable?.let {

                rvStateParcelable = it
            }
        }

    }


    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    PaymentFragmentArgs.fromBundle(it)
                }

                getTheRenter(args?.renterInfoMessage)

                lifecycleScope.launch {

                    delay(300)
                    getPaymentListOfRenter()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTheRenter(renterKey: String?) {

        renterViewModel.getRenterByKey(renterKey!!).observe(viewLifecycleOwner) { renter ->

            Log.d(TAG, "getTheRenter: ")

            receivedRenter = renter

            updateCurrentDueOrAdvanceTV()

        }
    }

    private fun getPaymentListOfRenter() {

        try {

            paymentViewModel.getAllPaymentsListOfRenter(receivedRenter?.key!!)
                .observe(viewLifecycleOwner) { paymentList ->

                    Log.d(TAG, "getPaymentListOfRenter: ")

                    if (paymentList.isNotEmpty()) {

                        hideNoPaymentsTV()
                        initializeSearchView(paymentList)

                    } else {

                        showNoPaymentsTV()
                    }

                    paymentAdapter.submitList(paymentList)
                    binding.paymentRV.layoutManager?.onRestoreInstanceState(rvStateParcelable)
                    rvStateParcelable = null
                    hideProgressBar()

                    paymentAdapter.notifyItemChanged(0)
                    paymentAdapter.notifyItemChanged(1)
                    paymentAdapter.notifyItemChanged(2)

                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrentDueOrAdvanceTV() {

        Log.d(TAG, "updateCurrentDueOrAdvanceTV: ")

        val currentDueOrAdvance =
            "${receivedRenter?.name} : ${abs(receivedRenter?.dueOrAdvanceAmount!!).format(2)}"

        when {

            receivedRenter?.dueOrAdvanceAmount!! < 0.0 -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_orange)
                binding.dueOrAdvancedTV.text =
                    "Current due of $currentDueOrAdvance"
            }
            receivedRenter?.dueOrAdvanceAmount!! > 0.0 -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_green)
                binding.dueOrAdvancedTV.text =
                    "Current advance of $currentDueOrAdvance"
            }
            else -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_green)
                binding.dueOrAdvancedTV.text =
                    "There is no due / advance of ${receivedRenter?.name}"
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
                paymentViewModel.updatePayment(requireContext(), payment)
            }

        } else {

            showNoInternetMessage(requireContext())
        }

    }

    override fun onDeleteClicked(payment: Payment) {

        showAlertDialogForDeletion(
            requireContext(),
            { dialog ->

                if (isInternetAvailable(requireContext())) {

                    paymentViewModel.deletePayment(requireContext(), payment)

                } else {
                    showNoInternetMessage(requireContext())
                }

                dialog.dismiss()

            },
            { dialog ->

                dialog.dismiss()
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

                    deleteAllPaymentsAfterWarningMessage()
                }
            }

            binding.paymentBackBtn.id -> {

                requireActivity().onBackPressed()
            }
        }
    }

    private fun deleteAllPaymentsAfterWarningMessage() {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete all payments?")
            .setMessage(getString(R.string.delete_warning_message))
            .setPositiveButton("Delete All") { dialogInterface, _ ->

                if (isInternetAvailable(requireContext())) {

                    paymentViewModel.deleteAllPaymentsOfRenter(
                        requireContext(),
                        receivedRenter?.key!!
                    )

                    showToast(
                        requireContext(),
                        "Deleted all the payments of ${receivedRenter?.name}"
                    )
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
        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {

        try {

            binding.paymentFragProgressBar.hide()
        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        paymentViewModel.saveRenterPaymentRvState(binding.paymentRV.layoutManager?.onSaveInstanceState())

        hideKeyBoard(requireActivity())

        _binding = null
    }

}