package com.rohitthebest.manageyourrenters.adapters.unsplashAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.data.UnsplashPhoto
import com.rohitthebest.manageyourrenters.databinding.ItemUnsplashPhotoBinding
import com.rohitthebest.manageyourrenters.utils.Functions

private const val TAG = "UnsplashSearchResultsAd"

class UnsplashSearchResultsAdapter() :
    PagingDataAdapter<UnsplashPhoto, UnsplashSearchResultsAdapter.UnsplashSearchViewHolder>(
        DiffUtilCallback()
    ) {

    private var mListener: OnClickListener? = null

    inner class UnsplashSearchViewHolder(val binding: ItemUnsplashPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(unsplashPhoto: UnsplashPhoto?) {

            unsplashPhoto?.let { photo ->

                binding.apply {

                    Functions.setImageToImageViewUsingGlide(
                        binding.root.context,
                        unsplashPhotoIV,
                        photo.urls.small,
                        {},
                        {}
                    )
                }
            }
        }


        init {

            binding.root.setOnClickListener {

                if (checkForNullability()) {

                    getItem(absoluteAdapterPosition)?.let { unsplashPhoto ->
                        mListener!!.onImageClicked(
                            unsplashPhoto
                        )
                    }
                }
            }
        }

        private fun checkForNullability(): Boolean {

            return absoluteAdapterPosition != RecyclerView.NO_POSITION && mListener != null
        }

    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<UnsplashPhoto>() {
            override fun areItemsTheSame(oldItem: UnsplashPhoto, newItem: UnsplashPhoto): Boolean =
                oldItem.urls == newItem.urls

            override fun areContentsTheSame(
                oldItem: UnsplashPhoto,
                newItem: UnsplashPhoto
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: UnsplashSearchViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnsplashSearchViewHolder {

        val binding = ItemUnsplashPhotoBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return UnsplashSearchViewHolder(binding)
    }

    interface OnClickListener {

        fun onImageClicked(unsplashPhoto: UnsplashPhoto)
    }

    fun setOnClickListener(listener: OnClickListener) {

        mListener = listener
    }
}