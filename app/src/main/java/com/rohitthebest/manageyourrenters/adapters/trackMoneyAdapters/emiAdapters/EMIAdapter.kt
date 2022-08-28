package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.emiAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.databinding.AdapterEmiBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime

class EMIAdapter : ListAdapter<EMI, EMIAdapter.EMIViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class EMIViewHolder(val binding: AdapterEmiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.emiAdapterRootLayout.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }

            binding.emiItemMenuBtn.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onMenuBtnClicked(
                        getItem(absoluteAdapterPosition),
                        absoluteAdapterPosition
                    )
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun setData(emi: EMI?) {

            emi?.let { e ->

                binding.apply {

                    emiNameTV.text = e.emiName
                    emiAddedOnTV.text = "Added on : ${
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            e.created
                        )
                    }"

                    emiAmountPaidTV.text =
                        "${e.currencySymbol} ${e.amountPaid} / ${e.currencySymbol} ${e.amountPaidPerMonth * e.totalMonths}"

                    monthsCompletedTV.text = "${e.monthsCompleted} / ${e.totalMonths}"

                    if (e.isSynced) {

                        root.setCardBackgroundColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                R.color.color_green
                            )
                        )
                    } else {

                        root.setCardBackgroundColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                R.color.color_orange
                            )
                        )
                    }
                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<EMI>() {

            override fun areItemsTheSame(oldItem: EMI, newItem: EMI): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: EMI, newItem: EMI): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EMIViewHolder {

        val binding = AdapterEmiBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return EMIViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EMIViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(emi: EMI)
        fun onMenuBtnClicked(emi: EMI, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

