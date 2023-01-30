package com.rohitthebest.manageyourrenters.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.ItemSelectPaymentMethodBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.hide

class SelectPaymentMethodAdapter :
    ListAdapter<PaymentMethod, SelectPaymentMethodAdapter.PaymentMethodViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class PaymentMethodViewHolder(val binding: ItemSelectPaymentMethodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.root.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onItemClick(
                        getItem(absoluteAdapterPosition),
                        absoluteAdapterPosition
                    )
                }
            }
        }

        fun setData(paymentMethod: PaymentMethod?) {

            paymentMethod?.let { pm ->

                binding.apply {

                    paymentMethodName.text = pm.paymentMethod

                    val color = ContextCompat.getColor(root.context, R.color.blue_text_color)

                    if (pm.isSelected) {

                        addRemovePaymentMethodSelection.animate().rotation(45f).setDuration(800)
                            .start()
                        root.strokeColor = color
                        paymentMethodName.setBackgroundColor(Functions.getBackgroundColor(color))
                    } else {

                        addRemovePaymentMethodSelection.animate().rotation(0f).setDuration(800)
                            .start()
                        root.strokeColor =
                            ContextCompat.getColor(root.context, R.color.divider_color)
                        paymentMethodName.setBackgroundColor(Color.WHITE)
                    }

                    if (pm.key == Constants.ADD_PAYMENT_METHOD_KEY) {

                        addRemovePaymentMethodSelection.hide()
                        paymentMethodName.setTextColor(
                            ContextCompat.getColor(
                                root.context,
                                R.color.colorGrey
                            )
                        )

                        paymentMethodName.setBackgroundColor(
                            Functions.getBackgroundColor(
                                ContextCompat.getColor(root.context, R.color.colorGrey)
                            )
                        )

                        root.elevation = 15.0f
                    }

                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<PaymentMethod>() {

            override fun areItemsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod): Boolean =
                oldItem.key == newItem.key

            override fun areContentsTheSame(
                oldItem: PaymentMethod,
                newItem: PaymentMethod
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {

        val binding =
            ItemSelectPaymentMethodBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return PaymentMethodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(paymentMethod: PaymentMethod, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

