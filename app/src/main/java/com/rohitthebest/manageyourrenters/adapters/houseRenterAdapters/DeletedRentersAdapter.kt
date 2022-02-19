package com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.database.model.DeletedRenter
import com.rohitthebest.manageyourrenters.databinding.AdapterDeletedRenterBinding


class DeletedRentersAdapter :
    ListAdapter<DeletedRenter, DeletedRentersAdapter.DeletedRenterViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class DeletedRenterViewHolder(val binding: AdapterDeletedRenterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

        }

        fun setData(deletedRenter: DeletedRenter?) {

            deletedRenter?.let { theDeletedRenter ->

                binding.apply {

                    renterNameTV.text = theDeletedRenter.renterInfo.name
                }
            }
        }
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

        fun onItemClick(deletedReneter: DeletedRenter)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}


