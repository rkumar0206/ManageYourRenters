package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.ItemExpenseCategoryBinding
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.setImageToImageViewUsingGlide
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.show

class ExpenseCategoryAdapter :
    ListAdapter<ExpenseCategory, ExpenseCategoryAdapter.ExpenseCategoryViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class ExpenseCategoryViewHolder(val binding: ItemExpenseCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.rootL.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }

            binding.expenseCategoryMenuBtn.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onMenuBtnClicked(getItem(absoluteAdapterPosition))
                }
            }
        }

        fun setData(expenseCategory: ExpenseCategory?) {

            expenseCategory?.let { expenseCat ->

                binding.apply {

                    if (expenseCat.imageUrl.isValid()) {

                        setImageToImageViewUsingGlide(
                            binding.root.context,
                            expenseCategoryIV,
                            expenseCat.imageUrl,
                            {},
                            {}
                        )
                    } else {

                        Glide.with(binding.root)
                            .load(R.drawable.gradient_blue)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(expenseCategoryIV)
                    }

                    expenseCategoryNameTV.text = expenseCat.categoryName

                    if (expenseCat.categoryDescription.isValid()) {

                        expenseCategoryDescriptionTV.show()
                        expenseCategoryDescriptionTV.text = expenseCat.categoryDescription
                    } else {

                        expenseCategoryDescriptionTV.hide()
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
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: ExpenseCategory,
                newItem: ExpenseCategory
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseCategoryViewHolder {

        val binding =
            ItemExpenseCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ExpenseCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseCategoryViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(expenseCategory: ExpenseCategory)
        fun onMenuBtnClicked(expenseCategory: ExpenseCategory)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

