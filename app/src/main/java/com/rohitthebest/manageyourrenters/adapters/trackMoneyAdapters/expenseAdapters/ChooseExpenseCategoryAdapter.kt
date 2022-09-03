package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.AdapterChooseExpenseCategoryBinding
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.isValid

class ChooseExpenseCategoryAdapter :
    ListAdapter<ExpenseCategory, ChooseExpenseCategoryAdapter.ChooseExpenseCategoryViewHolder>(
        DiffUtilCallback()
    ) {

    private var mListener: OnClickListener? = null

    inner class ChooseExpenseCategoryViewHolder(val binding: AdapterChooseExpenseCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.rootL.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }
        }

        fun setData(data: ExpenseCategory?) {

            data?.let { expenseCategory ->

                binding.apply {

                    if (expenseCategory.imageUrl.isValid()) {

                        Functions.setImageToImageViewUsingGlide(
                            binding.root.context,
                            expenseCategoryIV,
                            expenseCategory.imageUrl,
                            {},
                            {}
                        )
                    } else {

                        Glide.with(binding.root)
                            .load(R.drawable.gradient_blue)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(expenseCategoryIV)
                    }

                    expenseCategoryNameTV.text = expenseCategory.categoryName

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
                oldItem.id == newItem.id

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
    ): ChooseExpenseCategoryViewHolder {

        val binding = AdapterChooseExpenseCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ChooseExpenseCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChooseExpenseCategoryViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(expenseCategory: ExpenseCategory)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

