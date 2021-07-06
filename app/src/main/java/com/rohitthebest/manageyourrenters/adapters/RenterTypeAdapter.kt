package com.rohitthebest.manageyourrenters.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.data.RenterTypes
import com.rohitthebest.manageyourrenters.databinding.RenterTypeItemLayoutBinding

class RenterTypeAdapter :
    ListAdapter<RenterTypes, RenterTypeAdapter.RenterTypeViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class RenterTypeViewHolder(val binding: RenterTypeItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.root.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }
        }

        fun setData(renterType: RenterTypes?) {

            renterType?.let {

                binding.apply {

                    renterTypeImage.setImageResource(it.image)
                    renterTypeTV.text = it.renterType
                }
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<RenterTypes>() {

            override fun areItemsTheSame(oldItem: RenterTypes, newItem: RenterTypes): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: RenterTypes, newItem: RenterTypes): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RenterTypeViewHolder {

        val binding =
            RenterTypeItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return RenterTypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RenterTypeViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(renterType: RenterTypes)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

