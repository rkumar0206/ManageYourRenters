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
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.changeTextColor
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import kotlinx.android.synthetic.main.adapter_show_payment.view.*

class ShowPaymentAdapter :
    ListAdapter<Payment, ShowPaymentAdapter.ShowPaymentViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class ShowPaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        @SuppressLint("SetTextI18n")
        fun setData(payment: Payment?) {

            itemView.apply {

                payment?.let {

                    //Period
                    if (it.bill?.billPeriodType == context.getString(R.string.by_month)) {

                        paymentAdapter_billPeriodTV.text =
                            "${it.bill!!.billMonth}, ${it.bill!!.billYear}"
                    } else {

                        if (it.bill?.billDateFrom == it.bill?.billDateTill) {

                            paymentAdapter_billPeriodTV.text =
                                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                    it.bill?.billDateTill
                                )
                        } else {

                            paymentAdapter_billPeriodTV.textSize = 18.0f
                            paymentAdapter_billPeriodTV.text =
                                "From : ${
                                    WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                        it.bill?.billDateFrom
                                    )
                                }\nTo      :  ${
                                    WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                        it.bill?.billDateTill
                                    )
                                }"
                        }
                    }

                    //issue date
                    paymentAdapter_issueDateTV.text =
                        "Issue date : ${
                            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                it.timeStamp
                            )
                        }"

                    //house rent and extra
                    if ((it.houseRent == "0.0" || it.houseRent == "0") &&
                        (it.extraAmount != "0.0" || it.extraAmount != "0")
                    ) {

                        paymentAdapter_neetDemandTV.text =
                            "${it.extraFieldName} : ${it.extraAmount}"

                    } else {

                        paymentAdapter_neetDemandTV.text = "Net demand : ${it.totalRent}"
                    }

                    //total rent
                    if (it.amountPaid?.toDouble()!! < it.totalRent.toDouble()) {

                        paymentAdapter_amountPaidTV.changeTextColor(context, R.color.color_orange)
                    } else {

                        paymentAdapter_amountPaidTV.changeTextColor(context, R.color.color_green)
                    }

                    paymentAdapter_amountPaidTV.text = "Amount paid : ${it.amountPaid}"

                    if (it.isSynced == context.getString(R.string.t)) {

                        itemView.paymentAdapter_syncBtn.setImageResource(R.drawable.ic_baseline_sync_24_green)
                    } else {

                        itemView.paymentAdapter_syncBtn.setImageResource(R.drawable.ic_baseline_sync_24)
                    }

                }
            }
        }

        init {

            itemView.setOnClickListener(this)
            itemView.paymentAdapter_syncBtn.setOnClickListener(this)
            itemView.paymentAdapter_deleteBtn.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            when (v?.id) {

                itemView.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onPaymentClick(getItem(absoluteAdapterPosition))
                    }
                }

                itemView.paymentAdapter_syncBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onSyncClicked(getItem(absoluteAdapterPosition))
                    }
                }

                itemView.paymentAdapter_deleteBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onDeleteClicked(getItem(absoluteAdapterPosition))
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
