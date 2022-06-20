package com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.databinding.AdapterShowPaymentBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeTextColor
import com.rohitthebest.manageyourrenters.utils.format
import com.rohitthebest.manageyourrenters.utils.isValid

private const val TAG = "ShowPaymentAdapter"

class ShowPaymentAdapter(private val monthList: List<String>) :
    ListAdapter<RenterPayment, ShowPaymentAdapter.ShowPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class ShowPaymentViewHolder(val binding: AdapterShowPaymentBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        init {

            binding.root.setOnClickListener(this)
            binding.paymentAdapterMenuBtn.setOnClickListener(this)
            binding.paymentAdapterMessageBtn.setOnClickListener(this)
        }

        fun setData(payment: RenterPayment) {

            val context = binding.root.context

            binding.apply {

                Log.d(TAG, "setData: absoluteAdapterPosition : $absoluteAdapterPosition")

                //Period
                if (payment.billPeriodInfo.billPeriodType == BillPeriodType.BY_MONTH) {

                    paymentAdapterBillPeriodTV.text = context.getString(
                        R.string.month_year,
                        monthList[payment.billPeriodInfo.renterBillMonthType?.forBillMonth!! - 1],
                        payment.billPeriodInfo.billYear.toString()
                    )

                } else {

                    if (payment.billPeriodInfo.renterBillDateType?.fromBillDate
                        == payment.billPeriodInfo.renterBillDateType?.toBillDate
                    ) {

                        paymentAdapterBillPeriodTV.text =
                            WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                payment.billPeriodInfo.renterBillDateType?.toBillDate
                            )
                    } else {

                        paymentAdapterBillPeriodTV.textSize = 18.0f
                        paymentAdapterBillPeriodTV.text =
                            "From : ${
                                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.billPeriodInfo.renterBillDateType?.fromBillDate
                                )
                            }\nTo      : ${
                                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.billPeriodInfo.renterBillDateType?.toBillDate
                                )
                            }"
                    }
                }

                //issue date
                paymentAdapterIssueDateTV.text =
                    "Payment date : ${
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            payment.created
                        )
                    }\n" +
                            "Payment time : ${
                                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
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

                    binding.root.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.color_green
                        )
                    )
                } else {

                    binding.root.setCardBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.color_orange
                        )
                    )
                }
            }
        }

        override fun onClick(v: View?) {

            when (v?.id) {

                binding.root.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onPaymentClick(getItem(absoluteAdapterPosition))
                    }
                }

                binding.paymentAdapterMenuBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onMenuBtnClicked(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
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
        fun onMenuBtnClicked(payment: RenterPayment, position: Int)
        fun onMessageBtnClicked(paymentMessage: String)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}
