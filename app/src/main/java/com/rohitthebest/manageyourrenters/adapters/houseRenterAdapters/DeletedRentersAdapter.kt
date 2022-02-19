package com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.DeletedRenter
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.databinding.AdapterDeletedRenterBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeTextColor
import com.rohitthebest.manageyourrenters.utils.format
import kotlin.math.abs


class DeletedRentersAdapter :
    ListAdapter<DeletedRenter, DeletedRentersAdapter.DeletedRenterViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class DeletedRenterViewHolder(val binding: AdapterDeletedRenterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.renterInfoBtn.setOnClickListener {
                if (checkForNullability()) {

                    mListener!!.onRenterInfoBtnClicked(getItem(absoluteAdapterPosition).renterInfo)
                }
            }

            binding.lastPaymentInfoBtn.setOnClickListener {
                if (checkForNullability()) {

                    mListener!!.onLastPaymentInfoBtnClicked(getItem(absoluteAdapterPosition).lastPaymentInfo)
                }
            }

            binding.deleteBtn.setOnClickListener {
                if (checkForNullability()) {

                    mListener!!.onDeleteBtnClicked(getItem(absoluteAdapterPosition))
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun setData(deletedRenter: DeletedRenter?) {

            deletedRenter?.let { theDeletedRenter ->

                binding.apply {

                    renterNameTV.text = theDeletedRenter.renterInfo.name
                    deletedOnTV.text = "Deleted on : ${
                        WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                            theDeletedRenter.created
                        )
                    }"

                    roomNoTV.text = theDeletedRenter.renterInfo.roomNumber

                    dueOrAdvanceTV.text = when {
                        theDeletedRenter.renterInfo.dueOrAdvanceAmount < 0.0 -> {

                            dueOrAdvanceTV.changeTextColor(
                                binding.root.context,
                                R.color.color_orange
                            )

                            "Due : ${abs(theDeletedRenter.renterInfo.dueOrAdvanceAmount).format(2)}"
                        }
                        theDeletedRenter.renterInfo.dueOrAdvanceAmount > 0.0 -> {

                            dueOrAdvanceTV.changeTextColor(
                                binding.root.context,
                                R.color.color_green
                            )

                            "Advance : ${theDeletedRenter.renterInfo.dueOrAdvanceAmount.format(2)}"
                        }
                        else -> {

                            dueOrAdvanceTV.changeTextColor(
                                binding.root.context,
                                R.color.color_green
                            )
                            "No Due / Advance"
                        }
                    }

                }
            }
        }

        private fun checkForNullability() =
            mListener != null && (absoluteAdapterPosition != RecyclerView.NO_POSITION)
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<DeletedRenter>() {

            override fun areItemsTheSame(oldItem: DeletedRenter, newItem: DeletedRenter): Boolean =
                oldItem.key == newItem.key

            override fun areContentsTheSame(
                oldItem: DeletedRenter,
                newItem: DeletedRenter
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeletedRenterViewHolder {

        val binding =
            AdapterDeletedRenterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return DeletedRenterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeletedRenterViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onRenterInfoBtnClicked(deletedRenter: Renter)
        fun onLastPaymentInfoBtnClicked(deletedRenter: RenterPayment)
        fun onDeleteBtnClicked(deletedRenter: DeletedRenter)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}


