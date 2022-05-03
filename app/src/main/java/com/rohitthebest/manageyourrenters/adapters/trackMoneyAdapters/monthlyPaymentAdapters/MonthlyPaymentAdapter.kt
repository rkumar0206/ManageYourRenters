package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.monthlyPaymentAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.utils.MDUtil.getStringArray
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import com.rohitthebest.manageyourrenters.databinding.ItemMonthlyPaymentBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime

class MonthlyPaymentAdapter :
    ListAdapter<MonthlyPayment, MonthlyPaymentAdapter.MonthlyPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null
    private lateinit var workingWithDateAndTime: WorkingWithDateAndTime
    private lateinit var monthList: List<String>

    inner class MonthlyPaymentViewHolder(val binding: ItemMonthlyPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            workingWithDateAndTime = WorkingWithDateAndTime()

            monthList = binding.root.context.getStringArray(R.array.months).toList()

            binding.rootL.setOnClickListener {
                if (checkForNullability(absoluteAdapterPosition)) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
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
                            workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                payment.created
                            )
                        }\n" +
                                "Payment time : ${
                                    workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
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
                                "From : $fromMonthYear" +
                                        "\nTo      : $toMonthYear"
                        }

                    } else {

                        monthlyPaymentPaymentForTV.text =
                            "From : ${
                                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.monthlyPaymentDateTimeInfo?.fromBillDate
                                )
                            }\nTo      : ${
                                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.monthlyPaymentDateTimeInfo?.toBillDate
                                )
                            }"
                    }
                }
            }
        }

        private fun checkForNullability(position: Int): Boolean {

            return position != RecyclerView.NO_POSITION &&
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
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

