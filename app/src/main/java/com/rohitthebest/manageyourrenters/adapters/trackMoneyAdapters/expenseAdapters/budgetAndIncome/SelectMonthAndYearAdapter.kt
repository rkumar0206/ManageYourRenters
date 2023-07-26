package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.AdapterSelectMonthAndYearBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime

class SelectMonthAndYearAdapter :
    ListAdapter<String, SelectMonthAndYearAdapter.SelectMonthAndYearViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class SelectMonthAndYearViewHolder(val binding: AdapterSelectMonthAndYearBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var monthList: List<String> = emptyList()

        init {

            monthList = binding.root.context.resources.getStringArray(R.array.months).toList()

            binding.monthYearRB.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onMonthAndYearClicked(getItem(absoluteAdapterPosition))
                }
            }
        }

        fun setData(monthAndYearString: String?) {

            monthAndYearString?.let { monthYearString ->

                binding.apply {

                    val monthAndYear =
                        WorkingWithDateAndTime.extractMonthAndYearFromMonthAndYearString(
                            monthYearString
                        )

                    val month = monthList[monthAndYear.first]

                    monthYearRB.text = binding.root.context.getString(
                        R.string.month_and_year,
                        month,
                        monthAndYear.second.toString()
                    )
                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<String>() {

            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectMonthAndYearViewHolder {

        val binding = AdapterSelectMonthAndYearBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SelectMonthAndYearViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectMonthAndYearViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onMonthAndYearClicked(monthAndYear: String)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

