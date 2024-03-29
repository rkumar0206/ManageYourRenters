package com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.StatusEnum
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.AdapterRenterV2Binding
import com.rohitthebest.manageyourrenters.utils.changeTextColor

private const val TAG = "ShowRentersAdapter"
class ShowRentersAdapter :
    ListAdapter<Renter, ShowRentersAdapter.RenterViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class RenterViewHolder(val binding: AdapterRenterV2Binding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        init {

            binding.renterAdapterCV.setOnClickListener(this)
            binding.renterDetailsBtn.setOnClickListener(this)
            binding.renterMenuBtn.setOnClickListener(this)
            binding.renterPaymentsBtn.setOnClickListener(this)
            binding.adapterRenterStatusBtn.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun setData(renter: Renter) {

            binding.apply {

                if (renter.status == StatusEnum.ACTIVE) {
                    adapterRenterStatusBtn.setImageResource(R.drawable.ic_baseline_status_active)
                } else {
                    adapterRenterStatusBtn.setImageResource(R.drawable.ic_baseline_status_inactive)
                }

                if (renter.dueOrAdvanceAmount < 0.0) {

                    adapterRenterNameTV.changeTextColor(
                        root.context,
                        R.color.color_orange
                    )

                } else {

                    adapterRenterNameTV.changeTextColor(
                        root.context,
                        R.color.primaryTextColor
                    )
                }

                adapterRenterNameTV.text = renter.name
                adapterRoomNumTV.text = renter.roomNumber

                if (renter.isSynced == binding.root.context.getString(R.string.t)) {

                    root.setCardBackgroundColor(
                        ContextCompat.getColor(
                            root.context,
                            R.color.color_green
                        )
                    )
                } else {

                    root.setCardBackgroundColor(
                        ContextCompat.getColor(
                            root.context,
                            R.color.color_orange
                        )
                    )
                }
            }
        }

        override fun onClick(v: View?) {

            when (v?.id) {

                binding.renterAdapterCV.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {
                        mListener!!.onRenterClicked(getItem(absoluteAdapterPosition))
                    }
                }

                binding.renterMenuBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {
                        mListener!!.onMenuButtonClicked(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }

                }
                binding.adapterRenterStatusBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {
                        mListener!!.onStatusButtonClicked(
                            getItem(absoluteAdapterPosition),
                            absoluteAdapterPosition
                        )
                    }
                }

                binding.renterDetailsBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {
                        mListener!!.onDetailsButtonClicked(getItem(absoluteAdapterPosition))
                    }
                }

                binding.renterPaymentsBtn.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {
                        mListener!!.onPaymentButtonClicked(getItem(absoluteAdapterPosition))
                    }
                }
            }
        }

        private fun checkForNullability(position: Int): Boolean {

            return position != RecyclerView.NO_POSITION &&
                    mListener != null
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<Renter>() {

        override fun areItemsTheSame(oldItem: Renter, newItem: Renter): Boolean {

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Renter, newItem: Renter): Boolean {

            return oldItem.id == newItem.id
                    && oldItem.timeStamp == newItem.timeStamp
                    && oldItem.key == newItem.key
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RenterViewHolder {

        val binding =
            AdapterRenterV2Binding.inflate(LayoutInflater.from(parent.context), parent, false)

        return RenterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RenterViewHolder, position: Int) {

        getItem(position)?.let {

            holder.setData(it)
        }
    }

    interface OnClickListener {

        fun onRenterClicked(renter: Renter)
        fun onMenuButtonClicked(renter: Renter, position: Int)
        fun onStatusButtonClicked(renter: Renter, position: Int)
        fun onDetailsButtonClicked(renter: Renter)
        fun onPaymentButtonClicked(renter: Renter)
    }

    fun setOnClickListener(listener: OnClickListener) {

        mListener = listener
    }

}