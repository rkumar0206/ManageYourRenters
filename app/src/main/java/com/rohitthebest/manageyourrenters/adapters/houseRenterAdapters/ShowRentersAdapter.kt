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
import com.rohitthebest.manageyourrenters.databinding.AdapterRenterBinding
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeTextColor

class ShowRentersAdapter :
    ListAdapter<Renter, ShowRentersAdapter.RenterViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class RenterViewHolder(val binding: AdapterRenterBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        init {

            binding.renterAdapterCV.setOnClickListener(this)
            binding.adapterRenterEditBtn.setOnClickListener(this)
            //binding.adapterRenterMobileTV.setOnClickListener(this)
            binding.adapterRenterStatusBtn.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun setData(renter: Renter) {

            binding.apply {

                if (renter.dueOrAdvanceAmount < 0.0) {

                    binding.adapterRenterNameTV.changeTextColor(
                        binding.root.context,
                        R.color.color_orange
                    )

                } else {

                    binding.adapterRenterNameTV.changeTextColor(
                        binding.root.context,
                        R.color.primaryTextColor
                    )
                }

                binding.adapterRenterNameTV.text = renter.name
                binding.adapterRoomNumTV.text = renter.roomNumber
                binding.adapterRenterTimeTV.text = "Added on : ${
                    WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                        renter.timeStamp
                    )
                }"
                //binding.adapterRenterMobileTV.text = renter.mobileNumber
                //binding.adapterRenterEmailTV.text = renter.emailId
//                binding.adapterDocumnetNameTV.text = if (renter.otherDocumentName != "") {
//
//                    "${renter.otherDocumentName} : "
//                } else {
//
//                    "Other document : "
//                }
                //binding.adapterRenterDocNumTV.text = renter.otherDocumentNumber
                //binding.adapterRenterAddressTV.text = renter.address

                when (renter.status) {

                    StatusEnum.ACTIVE -> adapterRenterStatusBtn.setImageResource(R.drawable.ic_baseline_status_active)
                    StatusEnum.INACVTIVE -> adapterRenterStatusBtn.setImageResource(R.drawable.ic_baseline_status_inactive)
                }

                if (renter.isSynced == binding.root.context.getString(R.string.t)) {

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

        override fun onClick(v: View?) {

            when (v?.id) {

                binding.renterAdapterCV.id -> {

                    if (checkForNullability(absoluteAdapterPosition)) {

                        mListener!!.onRenterClicked(getItem(absoluteAdapterPosition))
                    }
                }

                binding.adapterRenterEditBtn.id -> {

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

//                binding.adapterRenterMobileTV.id -> {
//
//                    if (checkForNullability(absoluteAdapterPosition)) {
//
//                        mListener!!.onMobileNumberClicked(
//                            getItem(absoluteAdapterPosition).mobileNumber,
//                            binding.adapterRenterMobileTV
//                        )
//                    }
//                }
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
            AdapterRenterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

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
        fun onMobileNumberClicked(mobileNumber: String, view: View)
        fun onStatusButtonClicked(renter: Renter, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {

        mListener = listener
    }

}