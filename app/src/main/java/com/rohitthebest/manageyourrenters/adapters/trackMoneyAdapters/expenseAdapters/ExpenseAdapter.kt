package com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.database.model.apiModels.Expense
import com.rohitthebest.manageyourrenters.databinding.ItemExpenseBinding
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.show
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class ExpenseAdapter(val categoryName: String = "") :
    ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class ExpenseViewHolder(val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.rootL.setOnClickListener {

                if (checkForNullability()) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }

            binding.expenseMenuBtn.setOnClickListener {

                if (checkForNullability()) {

                    mListener!!.onMenuBtnClicked(getItem(absoluteAdapterPosition))
                }
            }

        }

        @SuppressLint("SetTextI18n")
        fun setData(expense: Expense?) {

            expense?.let { exp ->

                binding.apply {

                    val p = PrettyTime()
                    expenseDateTV.text = p.format(Date(exp.created))

                    expenseAmountTV.text = exp.amount.toString()

                    if (exp.spentOn.isValid()) {

                        expenseSpentOnTV.show()
                        expenseSpentOnTV.text = "Spent on : ${exp.spentOn}"
                    } else {

                        if (categoryName.isValid()) {

                            expenseSpentOnTV.show()
                            expenseSpentOnTV.text = "Spent on : $categoryName"
                        } else {

                            expenseSpentOnTV.hide()
                        }
                    }
                }
            }
        }

        private fun checkForNullability(): Boolean {

            return absoluteAdapterPosition != RecyclerView.NO_POSITION && mListener != null
        }

    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<Expense>() {

            override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {

        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(expense: Expense)
        fun onMenuBtnClicked(expense: Expense)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}
