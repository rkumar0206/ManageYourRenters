package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Payment
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentRenterBillBinding
import com.rohitthebest.manageyourrenters.databinding.ShowBillLayoutBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.saveBitmapToCacheDirectoryAndShare
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showMobileNumberOptionMenu
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
@SuppressLint("SetTextI18n")
class RenterBillFragment : Fragment(R.layout.fragment_renter_bill) {

    private var _binding: FragmentRenterBillBinding? = null
    private val binding get() = _binding!!

    private val paymentViewModel: PaymentViewModel by viewModels()
    private val renterViewModel: RenterViewModel by viewModels()

    private lateinit var receivedRenter: Renter
    private lateinit var receivedPayment: Payment

    private var receivedPaymentKey = ""

    private lateinit var includeBinding: ShowBillLayoutBinding
    private lateinit var workingWithDateAndTime: WorkingWithDateAndTime

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRenterBillBinding.bind(view)

        includeBinding = binding.include
        workingWithDateAndTime = WorkingWithDateAndTime()

        getMessage()

        intiListeners()

    }

    private fun intiListeners() {

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }


        binding.toolbar.menu.findItem(R.id.menu_share_bill).setOnMenuItemClickListener {

            val bitmap = includeBinding.root.loadBitmap()

            lifecycleScope.launch {

                saveBitmapToCacheDirectoryAndShare(requireActivity(), bitmap)

            }

            true
        }

        binding.toolbar.menu.findItem(R.id.menu_save_bill).setOnMenuItemClickListener {

            val bitmap = includeBinding.root.loadBitmap()

            val fileName =
                if (receivedPayment.bill?.billPeriodType == getString(R.string.by_month)) {

                    "${receivedRenter.name}_${receivedPayment.bill?.billMonth}"
                } else {

                    "${receivedRenter.name}_" + "${
                        workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            receivedPayment.bill?.billDateFrom
                        )
                    }" +
                            "_to_${
                                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    receivedPayment.bill?.billDateTill
                                )
                            }"

                }

            bitmap.saveToStorage(requireContext(), fileName)

            showToast(requireContext(), "Payment saved to phone storage.")

            true
        }
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let { bundle ->

                RenterBillFragmentArgs.fromBundle(bundle)
            }

            args?.let { arg ->

                receivedPaymentKey = arg.paymentKey!!

                if (receivedPaymentKey.isValid()) {

                    getPaymentFromDatabase()

                } else {

                    showToast(requireContext(), "Something went wrong...")
                    requireActivity().onBackPressed()
                }

            }

        }

    }

    private fun getPaymentFromDatabase() {

        paymentViewModel.getPaymentByPaymentKey(receivedPaymentKey)
            .observe(viewLifecycleOwner, { payment ->

                receivedPayment = payment

                getRenterInfoFromDatabase()
            })
    }

    private fun getRenterInfoFromDatabase() {

        renterViewModel.getRenterByKey(receivedPayment.renterKey)
            .observe(viewLifecycleOwner, { renter ->

                receivedRenter = renter

                binding.toolbar.title = "${renter.name} - payment"

                includeBinding.showBillRenterMobile.setOnClickListener {

                    showMobileNumberOptionMenu(
                        requireActivity(),
                        includeBinding.showBillRenterMobile,
                        receivedRenter.mobileNumber
                    )
                }
                initializeValuesToBill()
            })
    }

    private fun initializeValuesToBill() {

        setRenterInfo()

        setBillingParameters()

        setElectricFields()

        setDuesOrAdvanceOdLastPayment()

        setExtraFields()

        setDuesOrAdvance()

        setTotalRent()
    }

    //[Start of setting fields in bills textViews]

    private fun setRenterInfo() {

        //renter info
        includeBinding.showBillRenterName.text = receivedRenter.name
        includeBinding.showBillRenterMobile.text = receivedRenter.mobileNumber
        includeBinding.showBillRenterAddress.text = receivedRenter.address

    }

    private fun setBillingParameters() {

        //billing parameter
        includeBinding.showBillBillDate
            .setDateInTextView(receivedPayment.timeStamp)
        includeBinding.showBillBillTime
            .setDateInTextView(receivedPayment.timeStamp, "hh:mm a")
        includeBinding.showBillBillPeriod.text =
            if (receivedPayment.bill?.billPeriodType == getString(R.string.by_month)) {

                "${receivedPayment.bill!!.billMonth}, ${receivedPayment.bill!!.billYear}"
            } else {

                "${workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(receivedPayment.bill?.billDateFrom)}" +
                        " to ${
                            workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                receivedPayment.bill?.billDateTill
                            )
                        }"
            }
    }

    private fun setElectricFields() {

        //electricity
        includeBinding.showBillPreviousReading.text =
            "${String.format("%.2f", receivedPayment.electricBill?.previousReading)} unit(s)"
        includeBinding.showBillCurrentReading.text =
            "${String.format("%.2f", receivedPayment.electricBill?.currentReading)} unit(s)"
        includeBinding.showBillRate.text =
            "${String.format("%.2f", receivedPayment.electricBill?.rate)} per/unit"
        includeBinding.showBillDifference.text =
            "${String.format("%.2f", receivedPayment.electricBill?.differenceInReading)} unit(s)"
        includeBinding.showBillElectricityTotal.text =
            "${receivedPayment.bill?.currencySymbol} ${receivedPayment.electricBill?.totalElectricBill}"

    }

    private fun setExtraFields() {

        //Extra
        includeBinding.showBillExtraFieldName.text =
            if (receivedPayment.extraFieldName == "") {

                "Extra"
            } else {
                receivedPayment.extraFieldName
            }

        includeBinding.showBillExtraFieldAmount.text =
            if (receivedPayment.extraAmount == "") {

                "${receivedPayment.bill?.currencySymbol} 0.0"
            } else {

                "${receivedPayment.bill?.currencySymbol} ${receivedPayment.extraAmount}"
            }
    }

    private fun setDuesOrAdvanceOdLastPayment() {

        val dueOfLastPayment = getDuesOfLastPayment()

        when {

            dueOfLastPayment > 0.0 -> {

                //due
                includeBinding.showBillDueOfLastPayAmount.text =
                    "${receivedPayment.bill?.currencySymbol} $dueOfLastPayment"

                includeBinding.showBillPaidInAdvanceInlastPayAmount.text =
                    "${receivedPayment.bill?.currencySymbol} 0.0"

            }

            dueOfLastPayment < 0.0 -> {

                //advance
                includeBinding.showBillDueOfLastPayAmount.text =
                    "${receivedPayment.bill?.currencySymbol} 0.0"

                includeBinding.showBillPaidInAdvanceInlastPayAmount.text =
                    "${receivedPayment.bill?.currencySymbol} $dueOfLastPayment"
            }

            else -> {
                includeBinding.showBillDueOfLastPayAmount.text =
                    "${receivedPayment.bill?.currencySymbol} 0.0"

                includeBinding.showBillPaidInAdvanceInlastPayAmount.text =
                    "${receivedPayment.bill?.currencySymbol} 0.0"
            }
        }
    }

    private fun setDuesOrAdvance() {

        val dueOrAdvance =
            receivedPayment.amountPaid?.toDouble()?.minus(receivedPayment.totalRent.toDouble())!!

        includeBinding.showBillDueAmount.text =

            when {

                dueOrAdvance < 0.0 -> {

                    //due
                    "${receivedPayment.bill?.currencySymbol} ${
                        String.format(
                            "%.2f",
                            abs(dueOrAdvance)
                        )
                    }"
                }

                dueOrAdvance > 0.0 -> {

                    includeBinding.showBillDueOrArrearTV.text =
                        "Paid in advance"
                    "${receivedPayment.bill?.currencySymbol} ${String.format("%.2f", dueOrAdvance)}"
                }
                else -> {

                    "${receivedPayment.bill?.currencySymbol} 0.0"
                }
            }

    }

    private fun setTotalRent() {

        //total rent
        includeBinding.showBillHouseRent.text =
            "${receivedPayment.bill?.currencySymbol} ${receivedPayment.houseRent}"

        includeBinding.showBillParking.text =
            "${receivedPayment.bill?.currencySymbol} ${receivedPayment.parkingRent}"

        includeBinding.showBillElectricity.text =
            "${receivedPayment.bill?.currencySymbol} ${receivedPayment.electricBill?.totalElectricBill}"

        includeBinding.showBillAmountPaid.text =
            "${receivedPayment.bill?.currencySymbol} ${receivedPayment.amountPaid}"

        includeBinding.showBillNetDemand.text =
            "${receivedPayment.bill?.currencySymbol} ${receivedPayment.totalRent}"
    }

    //[END of setting fields in bills textViews]

    private fun getDuesOfLastPayment(): Double {

        val houseRent = receivedPayment.houseRent.toDouble()
        val parking = if (receivedPayment.isTakingParkingBill == getString(R.string.t))
            receivedPayment.parkingRent?.toDouble()!!
        else
            0.0
        val electricBill =
            if (receivedPayment.electricBill?.isTakingElectricBill == getString(R.string.t))
                receivedPayment.electricBill?.totalElectricBill?.toDouble()!!
            else
                0.0
        val extra = if (receivedPayment.extraAmount != "")
            receivedPayment.extraAmount?.toDouble()!!
        else
            0.0

        return receivedPayment.totalRent.toDouble() - (houseRent + parking + electricBill + extra)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
