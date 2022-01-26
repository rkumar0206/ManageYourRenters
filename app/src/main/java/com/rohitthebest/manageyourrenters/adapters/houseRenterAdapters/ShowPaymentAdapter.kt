package com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Payment
import com.rohitthebest.manageyourrenters.databinding.AdapterShowPaymentBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeTextColor
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show

class ShowPaymentAdapter :
    ListAdapter<Payment, ShowPaymentAdapter.ShowPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null
    private lateinit var workingWithDateAndTime: WorkingWithDateAndTime

    inner class ShowPaymentViewHolder(val binding: AdapterShowPaymentBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        @SuppressLint("SetTextI18n")
        fun setData(payment: Payment) {

            workingWithDateAndTime = WorkingWithDateAndTime()

            binding.apply {

                if (absoluteAdapterPosition == 0) {

                    paymentAdapterDeleteBtn.show()
                } else {

                    paymentAdapterDeleteBtn.hide()
                }

                //Period
                if (payment.bill?.billPeriodType == binding.root.context.getString(R.string.by_month)) {

                    paymentAdapterBillPeriodTV.text =
                        "${payment.bill!!.billMonth}, ${payment.bill!!.billYear}"
                } else {

                    if (payment.bill?.billDateFrom == payment.bill?.billDateTill) {

                        paymentAdapterBillPeriodTV.text =
                            workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                payment.bill?.billDateTill
                            )
                    } else {

                        paymentAdapterBillPeriodTV.textSize = 18.0f
                        paymentAdapterBillPeriodTV.text =
                            "From : ${
                                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.bill?.billDateFrom
                                )
                            }\nTo      : ${
                                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.bill?.billDateTill
                                )
                            }"
                    }
                }

                //issue date
                paymentAdapterIssueDateTV.text =
                    "Payment date : ${
                        workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            payment.timeStamp
                        )
                    }\n" +
                            "Payment time : ${
                                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.timeStamp, "hh:mm a"
                                )
                            }"

                //house rent and extra
                if ((payment.houseRent == "0.0" || payment.houseRent == "0") &&
                    (payment.extraAmount != "0.0" || payment.extraAmount != "0")
                ) {

                    if (payment.extraFieldName?.trim()?.isEmpty()!!) {

                        paymentAdapterNetDemandTV.text =
                            "Net demand : ${payment.currencySymbol} ${payment.extraAmount}"
                    } else {

                        paymentAdapterNetDemandTV.text =
                            "${payment.extraFieldName} : ${payment.currencySymbol} ${payment.extraAmount}"
                    }

                } else {

                    paymentAdapterNetDemandTV.text =
                        "Net demand : ${payment.currencySymbol} ${payment.totalRent}"
                }

                //total rent
                if (payment.amountPaid?.toDouble()!! < payment.totalRent.toDouble()) {

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
                    "Amount paid : ${payment.currencySymbol} ${payment.amountPaid}"

                if (payment.isSynced == binding.root.context.getString(R.string.t)) {

                    paymentAdapterSyncBtn.setImageResource(R.drawable.ic_baseline_sync_24_green)
                } else {

                    paymentAdapterSyncBtn.setImageResource(R.drawable.ic_baseline_sync_24)
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

                        getItem(absoluteAdapterPosition).messageOrNote?.let { message ->

                            mListener!!.onMessageBtnClicked(
                                message
                            )
                        }
                    }
                }

            }
        }

        private fun checkForNullability(position: Int): Boolean {

            return position != RecyclerView.NO_POSITION &&
                    mListener != null
        }

    }

    class DiffUtilCallback : DiffUtil.ItemCallback<Payment>() {

        override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {

            return oldItem.id == newItem.id
                    && oldItem.timeStamp == newItem.timeStamp
                    && oldItem.key == newItem.key
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

        fun onPaymentClick(payment: Payment)
        fun onSyncClicked(payment: Payment)
        fun onDeleteClicked(payment: Payment)
        fun onMessageBtnClicked(paymentMessage: String)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}
