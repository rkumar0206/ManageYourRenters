package com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.databinding.AdapterShowPaymentBinding
import com.rohitthebest.manageyourrenters.utils.*

private const val TAG = "ShowPaymentAdapter"

class ShowPaymentAdapter(private val monthList: List<String>) :
    ListAdapter<RenterPayment, ShowPaymentAdapter.ShowPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null
    private lateinit var workingWithDateAndTime: WorkingWithDateAndTime

    inner class ShowPaymentViewHolder(val binding: AdapterShowPaymentBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        @SuppressLint("SetTextI18n")
        fun setData(payment: RenterPayment) {

            workingWithDateAndTime = WorkingWithDateAndTime()

            binding.apply {

                Log.d(TAG, "setData: absoluteAdapterPosition : $absoluteAdapterPosition")

                //Period
                if (payment.billPeriodInfo.billPeriodType == BillPeriodType.BY_MONTH) {

                    paymentAdapterBillPeriodTV.text =
                        "${monthList[payment.billPeriodInfo.renterBillMonthType?.forBillMonth!! - 1]}, ${payment.billPeriodInfo.billYear}"
                } else {

                    if (payment.billPeriodInfo.renterBillDateType?.fromBillDate
                        == payment.billPeriodInfo.renterBillDateType?.toBillDate
                    ) {

                        paymentAdapterBillPeriodTV.text =
                            workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                payment.billPeriodInfo.renterBillDateType?.toBillDate
                            )
                    } else {

                        paymentAdapterBillPeriodTV.textSize = 18.0f
                        paymentAdapterBillPeriodTV.text =
                            "From : ${
                                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.billPeriodInfo.renterBillDateType?.fromBillDate
                                )
                            }\nTo      : ${
                                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.billPeriodInfo.renterBillDateType?.toBillDate
                                )
                            }"
                    }
                }

                //issue date
                paymentAdapterIssueDateTV.text =
                    "Payment date : ${
                        workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            payment.created
                        )
                    }\n" +
                            "Payment time : ${
                                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.created, "hh:mm a"
                                )
                            }"

                //house rent and extra
                if (payment.houseRent == 0.0 && (payment.extras?.fieldAmount != null && payment.extras?.fieldAmount != 0.0)) {

                    if (!payment.extras?.fieldName?.trim().isValid()) {

                        paymentAdapterNetDemandTV.text =
                            "Net demand : ${payment.currencySymbol} ${
                                payment.extras?.fieldAmount?.format(
                                    2
                                )
                            }"
                    } else {

                        paymentAdapterNetDemandTV.text =
                            "${payment.extras?.fieldName} : ${payment.currencySymbol} ${
                                payment.extras?.fieldAmount?.format(
                                    2
                                )
                            }"
                    }

                } else {

                    paymentAdapterNetDemandTV.text =
                        "Net demand : ${payment.currencySymbol} ${
                            payment.netDemand.format(2)
                        }"
                }

                //total rent
                if (payment.amountPaid < payment.netDemand) {

                    paymentAdapterAmountPaidTV.changeTextColor(
                        binding.root.context,
                        R.color.color_orange
                    )
                } else {

                    paymentAdapterAmountPaidTV.changeTextColor(
                        binding.root.context,
                        R.color.color_green
                    )
                }

                paymentAdapterAmountPaidTV.text =
                    "Amount paid : ${payment.currencySymbol} ${
                        payment.amountPaid.format(2)
                    }"

                if (payment.isSynced) {

                    paymentAdapterSyncBtn.setImageResource(R.drawable.ic_baseline_sync_24_green)
                } else {

                    paymentAdapterSyncBtn.setImageResource(R.drawable.ic_baseline_sync_24)
                }

                if (absoluteAdapterPosition == 0) {

                    paymentAdapterDeleteBtn.show()
                } else {

                    paymentAdapterDeleteBtn.hide()
                }
            }
        }

        init {

            binding.root.setOnClickListener(this)
            binding.paymentAdapterSyncBtn.setOnClickListener(this)
            binding.paymentAdapterDeleteBtn.setOnClickListener(this)
            binding.paymentAdapterMessageBtn.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            when (v?.id) {

                binding.root.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onPaymentClick(getItem(absoluteAdapterPosition))
                    }
                }

                binding.paymentAdapterSyncBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onSyncClicked(getItem(absoluteAdapterPosition))
                    }
                }

                binding.paymentAdapterDeleteBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onDeleteClicked(getItem(absoluteAdapterPosition))
                    }
                }

                binding.paymentAdapterMessageBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onMessageBtnClicked(
                            getItem(absoluteAdapterPosition).note
                        )
                    }
                }

            }
        }

        private fun checkForNullability(position: Int): Boolean {

            return position != RecyclerView.NO_POSITION &&
                    mListener != null
        }

    }

    class DiffUtilCallback : DiffUtil.ItemCallback<RenterPayment>() {

        override fun areItemsTheSame(oldItem: RenterPayment, newItem: RenterPayment): Boolean {

            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: RenterPayment, newItem: RenterPayment): Boolean {

            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowPaymentViewHolder {

        val binding =
            AdapterShowPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ShowPaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShowPaymentViewHolder, position: Int) {

        getItem(position)?.let { payment ->

            holder.setData(payment)
        }
    }

    interface OnClickListener {

        fun onPaymentClick(payment: RenterPayment)
        fun onSyncClicked(payment: RenterPayment)
        fun onDeleteClicked(payment: RenterPayment)
        fun onMessageBtnClicked(paymentMessage: String)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}
