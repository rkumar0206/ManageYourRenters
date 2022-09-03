package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.ItemDeepAnalyzeExpenseCategoryBinding
import com.rohitthebest.manageyourrenters.utils.Functions

class DeepAnalyzeExpenseCategoryAdapter :
    ListAdapter<ExpenseCategory, DeepAnalyzeExpenseCategoryAdapter.DeepAnalyzeExpenseCategoryViewHolder>(
        DiffUtilCallback()
    ) {

    private var mListener: OnClickListener? = null

    inner class DeepAnalyzeExpenseCategoryViewHolder(val binding: ItemDeepAnalyzeExpenseCategoryBinding) :
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

        fun setData(expenseCategory: ExpenseCategory?) {

            expenseCategory?.let { expenseCat ->

                binding.apply {

                    expenseCategoryNameTV.text = expenseCat.categoryName

                    val color = ContextCompat.getColor(root.context, R.color.blue_text_color)

                    if (expenseCat.isSelected) {

                        addRemoveExpenseCategorySelection.animate().rotation(45f).setDuration(800)
                            .start()
                        root.strokeColor = color
                        expenseCategoryNameTV.setBackgroundColor(Functions.getBackgroundColor(color))
                    } else {

                        addRemoveExpenseCategorySelection.animate().rotation(0f).setDuration(800)
                            .start()
                        root.strokeColor =
                            ContextCompat.getColor(root.context, R.color.divider_color)
                        expenseCategoryNameTV.setBackgroundColor(Color.WHITE)
                    }
                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<ExpenseCategory>() {

            override fun areItemsTheSame(
                oldItem: ExpenseCategory,
                newItem: ExpenseCategory
            ): Boolean =
                oldItem.key == newItem.key

            override fun areContentsTheSame(
                oldItem: ExpenseCategory,
                newItem: ExpenseCategory
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeepAnalyzeExpenseCategoryViewHolder {

        val binding = ItemDeepAnalyzeExpenseCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return DeepAnalyzeExpenseCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeepAnalyzeExpenseCategoryViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(expenseCategory: ExpenseCategory, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

