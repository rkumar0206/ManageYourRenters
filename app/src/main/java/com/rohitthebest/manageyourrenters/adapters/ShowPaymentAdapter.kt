package com.rohitthebest.manageyourrenters.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.entity.Payment
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import kotlinx.android.synthetic.main.adapter_show_payment.view.*

class ShowPaymentAdapter :
    ListAdapter<Payment, ShowPaymentAdapter.ShowPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class ShowPaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun setData(payment: Payment?) {

            itemView.apply {

                payment?.let {

                    if (it.bill?.billPeriodType == context.getString(R.string.by_month)) {

                        paymentAdapter_billPeriodTV.text =
                            "${it.bill!!.billMonth}, ${it.bill!!.billYear}"
                    } else {

                        paymentAdapter_billPeriodTV.textSize = 18.0f
                        paymentAdapter_billPeriodTV.text =
                            "From : ${
                                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                    it.bill?.billDateFrom
                                )
                            }\nTo :  ${
                                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                    it.bill?.billDateTill
                                )
                            }"
                    }

                    paymentAdapter_issueDateTV.text =
                        WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                            it.timeStamp
                        )

                    paymentAdapter_neetDemandTV.text = it.totalRent
                    paymentAdapter_amountPaidTV.text = it.amountPaid

                }
            }
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

        return ShowPaymentViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_show_payment, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ShowPaymentViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onPaymentClick(payment: Payment)
        fun onSyncClicked(payment: Payment)
        fun onDeleteClicked(payment: Payment)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}
