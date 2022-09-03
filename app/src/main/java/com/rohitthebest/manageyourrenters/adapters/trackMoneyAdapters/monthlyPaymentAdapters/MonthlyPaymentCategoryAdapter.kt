package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.monthlyPaymentAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.databinding.ItemMonthlyPaymentCategoryBinding
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.setImageToImageViewUsingGlide
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.show

class MonthlyPaymentCategoryAdapter :
    ListAdapter<MonthlyPaymentCategory, MonthlyPaymentCategoryAdapter.MonthlyPaymentCategoryViewHolder>(
        DiffUtilCallback()
    ) {

    private var mListener: OnClickListener? = null

    inner class MonthlyPaymentCategoryViewHolder(val binding: ItemMonthlyPaymentCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.rootL.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }

            binding.monthlyPaymentCategoryMenuBtn.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onMenuBtnClicked(
                        getItem(absoluteAdapterPosition),
                        absoluteAdapterPosition
                    )
                }
            }
        }

        fun setData(monthlyPaymentCategory: MonthlyPaymentCategory?) {

            monthlyPaymentCategory?.let { monthlyPaymentCat ->

                binding.apply {

                    if (monthlyPaymentCat.imageUrl.isValid()) {

                        setImageToImageViewUsingGlide(
                            binding.root.context,
                            monthlyPaymentCategoryIV,
                            monthlyPaymentCat.imageUrl,
                            {},
                            {}
                        )
                    } else {

                        Glide.with(binding.root)
                            .load(R.drawable.gradient_blue)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(monthlyPaymentCategoryIV)
                    }

                    monthlyPaymentCategoryNameTV.text = monthlyPaymentCat.categoryName

                    if (monthlyPaymentCat.categoryDescription.isValid()) {

                        monthlyPaymentCategoryDescriptionTV.show()
                        monthlyPaymentCategoryDescriptionTV.text =
                            monthlyPaymentCat.categoryDescription
                    } else {

                        monthlyPaymentCategoryDescriptionTV.hide()
                    }

                    if (monthlyPaymentCat.isSynced) {

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
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<MonthlyPaymentCategory>() {

            override fun areItemsTheSame(
                oldItem: MonthlyPaymentCategory,
                newItem: MonthlyPaymentCategory
            ): Boolean =
                oldItem.key == newItem.key

            override fun areContentsTheSame(
                oldItem: MonthlyPaymentCategory,
                newItem: MonthlyPaymentCategory
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MonthlyPaymentCategoryViewHolder {

        val binding =
            ItemMonthlyPaymentCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return MonthlyPaymentCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonthlyPaymentCategoryViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(monthlyPaymentCategory: MonthlyPaymentCategory)
        fun onMenuBtnClicked(monthlyPaymentCategory: MonthlyPaymentCategory, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

