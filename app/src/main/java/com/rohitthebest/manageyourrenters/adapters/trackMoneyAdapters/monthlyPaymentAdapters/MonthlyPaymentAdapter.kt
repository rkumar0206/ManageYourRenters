package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.monthlyPaymentAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.database.model.MonthlyPayment
import com.rohitthebest.manageyourrenters.databinding.ItemMonthlyPaymentBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime

class MonthlyPaymentAdapter :
    ListAdapter<MonthlyPayment, MonthlyPaymentAdapter.MonthlyPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null
    private lateinit var monthList: List<String>

    inner class MonthlyPaymentViewHolder(val binding: ItemMonthlyPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            monthList = binding.root.context.resources.getStringArray(R.array.months).toList()

            binding.rootL.setOnClickListener {
                if (checkForNullability()) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }

            binding.monthlyPaymentMessageBtn.setOnClickListener {
                if (checkForNullability()) {

                    mListener!!.onMessageBtnClicked(getItem(absoluteAdapterPosition).message)
                }
            }

            binding.monthlyPaymentMenuBtn.setOnClickListener {
                if (checkForNullability()) {

                    mListener!!.onMenuBtnClicked(
                        getItem(absoluteAdapterPosition),
                        absoluteAdapterPosition
                    )
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun setData(monthlyPayment: MonthlyPayment?) {

            monthlyPayment?.let { payment ->

                binding.apply {

                    monthlyPaymentAmountPaidTV.text =
                        binding.root.context.getString(
                            R.string.amount_paid_with_value,
                            String.format("%.2f", payment.amount)
                        )

                    monthlyPaymentPaymentDateAndTimeTV.text =
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

                    if (payment.monthlyPaymentDateTimeInfo?.paymentPeriodType == BillPeriodType.BY_MONTH) {

                        val fromMonthYear =
                            "${monthList[payment.monthlyPaymentDateTimeInfo?.forBillMonth!! - 1]}, " +
                                    "${payment.monthlyPaymentDateTimeInfo?.forBillYear}"

                        val toMonthYear =
                            "${monthList[payment.monthlyPaymentDateTimeInfo?.toBillMonth!! - 1]}, " +
                                    "${payment.monthlyPaymentDateTimeInfo?.toBillYear}"

                        if (fromMonthYear == toMonthYear) {

                            monthlyPaymentPaymentForTV.text =
                                "For : $fromMonthYear"
                        } else {

                            monthlyPaymentPaymentForTV.text =
                                "From : $fromMonthYear\nTo      : $toMonthYear"
                        }

                    } else {

                        monthlyPaymentPaymentForTV.text =
                            "From : ${
                                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.monthlyPaymentDateTimeInfo?.fromBillDate
                                )
                            }\nTo      : ${
                                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.monthlyPaymentDateTimeInfo?.toBillDate
                                )
                            }"
                    }
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
        }

        private fun checkForNullability(): Boolean {

            return absoluteAdapterPosition != RecyclerView.NO_POSITION &&
                    mListener != null
        }

    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<MonthlyPayment>() {

            override fun areItemsTheSame(
                oldItem: MonthlyPayment,
                newItem: MonthlyPayment
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: MonthlyPayment,
                newItem: MonthlyPayment
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlyPaymentViewHolder {

        val binding =
            ItemMonthlyPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MonthlyPaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonthlyPaymentViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(monthlyPayment: MonthlyPayment)
        fun onMessageBtnClicked(message: String)
        fun onMenuBtnClicked(monthlyPayment: MonthlyPayment, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

