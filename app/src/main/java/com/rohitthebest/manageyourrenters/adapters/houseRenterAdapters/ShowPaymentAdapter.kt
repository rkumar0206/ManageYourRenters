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
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.changeTextColor
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime

class ShowPaymentAdapter :
    ListAdapter<Payment, ShowPaymentAdapter.ShowPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class ShowPaymentViewHolder(val binding: AdapterShowPaymentBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        @SuppressLint("SetTextI18n")
        fun setData(payment: Payment?) {

            binding.apply {

                payment?.let {

                    if (absoluteAdapterPosition == 0) {

                        paymentAdapterDeleteBtn.show()
                    } else {

                        paymentAdapterDeleteBtn.hide()
                    }

                    //Period
                    if (it.bill?.billPeriodType == binding.root.context.getString(R.string.by_month)) {

                        paymentAdapterBillPeriodTV.text =
                            "${it.bill!!.billMonth}, ${it.bill!!.billYear}"
                    } else {

                        if (it.bill?.billDateFrom == it.bill?.billDateTill) {

                            paymentAdapterBillPeriodTV.text =
                                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                    it.bill?.billDateTill
                                )
                        } else {

                            paymentAdapterBillPeriodTV.textSize = 18.0f
                            paymentAdapterBillPeriodTV.text =
                                "From : ${
                                    WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                        it.bill?.billDateFrom
                                    )
                                }\nTo      : ${
                                    WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                        it.bill?.billDateTill
                                    )
                                }"
                        }
                    }

                    //issue date
                    paymentAdapterIssueDateTV.text =
                        "Payment date : ${
                            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                it.timeStamp
                            )
                        }"

                    //house rent and extra
                    if ((it.houseRent == "0.0" || it.houseRent == "0") &&
                        (it.extraAmount != "0.0" || it.extraAmount != "0")
                    ) {

                        if(it.extraFieldName?.trim()?.isEmpty()!!){

                            paymentAdapterNetDemandTV.text =
                                "Net demand : ${it.extraAmount}"
                        }else {

                            paymentAdapterNetDemandTV.text =
                                "${it.extraFieldName} : ${it.extraAmount}"
                        }

                    } else {

                        paymentAdapterNetDemandTV.text = "Net demand : ${it.totalRent}"
                    }

                    //total rent
                    if (it.amountPaid?.toDouble()!! < it.totalRent.toDouble()) {

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

                    paymentAdapterAmountPaidTV.text = "Amount paid : ${it.amountPaid}"

                    if (it.isSynced == binding.root.context.getString(R.string.t)) {

                        paymentAdapterSyncBtn.setImageResource(R.drawable.ic_baseline_sync_24_green)
                    } else {

                        paymentAdapterSyncBtn.setImageResource(R.drawable.ic_baseline_sync_24)
                    }

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

                binding.paymentAdapterDeleteBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        getItem(absoluteAdapterPosition).messageOrNote?.let {

                            mListener!!.onMessageBtnClicked(
                                it
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

        holder.setData(getItem(position))
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
