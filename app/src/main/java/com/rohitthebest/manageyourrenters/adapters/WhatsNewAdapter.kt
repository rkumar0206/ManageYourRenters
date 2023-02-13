package com.rohitthebest.manageyourrenters.adapters

import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.StyleType
import com.rohitthebest.manageyourrenters.data.WhatsNew
import com.rohitthebest.manageyourrenters.databinding.AdapterWhatsNewBinding
import com.rohitthebest.manageyourrenters.utils.*

class WhatsNewAdapter :
    ListAdapter<WhatsNew, WhatsNewAdapter.WhatsNewViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class WhatsNewViewHolder(val binding: AdapterWhatsNewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {

            binding.whatsNewTV.setOnClickListener {

                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }

            binding.whatsNewExpandBtn.setOnClickListener {
                if (mListener != null && absoluteAdapterPosition != RecyclerView.NO_POSITION) {

                    mListener!!.onItemClick(getItem(absoluteAdapterPosition))
                }
            }
        }

        fun setData(whatsNew: WhatsNew?) {

            whatsNew?.let { new ->

                binding.apply {

                    if (new.styleType != null && new.styleType == StyleType.HTML) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            whatsNewTV.text = Html.fromHtml(
                                new.feature, HtmlCompat.FROM_HTML_MODE_COMPACT
                            )
                        } else {
                            whatsNewTV.text = Html.fromHtml(
                                new.feature
                            )
                        }

                    } else {

                        when {

                            new.feature.endsWith("-heading") || (new.textStyle.isValid() && new.textStyle.equals(
                                "heading"
                            )) -> {

                                changeTextStyle(
                                    color = R.color.primaryTextColor,
                                    typeface = Typeface.BOLD,
                                    textSize = 20f,
                                    text = new.feature.replace("-heading", "")
                                )

                            }

                            new.feature.endsWith("-critical") || (new.textStyle.isValid() && new.textStyle.equals(
                                "critical"
                            )) -> {

                                changeTextStyle(
                                    R.color.color_orange,
                                    Typeface.BOLD,
                                    20f,
                                    new.feature.replace("-critical", "")
                                )
                            }
                            new.feature.startsWith("https") || new.feature.startsWith("http") -> {

                                changeTextStyle(
                                    R.color.blue_text_color,
                                    Typeface.NORMAL,
                                    16f,
                                    new.feature
                                )
                                whatsNewTV.underline()
                            }

                            else -> {

                                if (new.textStyle.isValid()) {

                                    val typeface = when {

                                        new.textStyle!!.contains("B") && new.textStyle!!.contains("I") -> {
                                            Typeface.BOLD_ITALIC
                                        }

                                        new.textStyle!!.contains("B") -> {
                                            Typeface.BOLD
                                        }

                                        new.textStyle!!.contains("I") -> {
                                            Typeface.ITALIC
                                        }

                                        else -> Typeface.NORMAL
                                    }
                                    changeTextStyle(
                                        if (new.textStyle!!.contains("B")) R.color.primaryTextColor else R.color.secondaryTextColor,
                                        typeface,
                                        16f,
                                        new.feature
                                    )

                                    if (new.textStyle!!.contains("U")) {
                                        whatsNewTV.underline()
                                    }

                                } else {
                                    changeTextStyle(
                                        R.color.secondaryTextColor,
                                        Typeface.NORMAL,
                                        16f,
                                        new.feature
                                    )
                                }
                            }
                        }
                    }

                    if (new.image.isValid()) {

                        whatsNewExpandBtn.show()
                    } else {
                        whatsNewExpandBtn.hide()
                    }
                }
            }
        }

        private fun changeTextStyle(color: Int, typeface: Int, textSize: Float, text: String) {

            binding.apply {

                whatsNewTV.changeTextColor(
                    binding.root.context,
                    color
                )
                whatsNewTV.setTypeface(null, typeface)
                whatsNewTV.textSize = textSize
                whatsNewTV.text = text
            }
        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<WhatsNew>() {

            override fun areItemsTheSame(oldItem: WhatsNew, newItem: WhatsNew): Boolean =
                oldItem.feature == newItem.feature

            override fun areContentsTheSame(oldItem: WhatsNew, newItem: WhatsNew): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhatsNewViewHolder {

        val binding =
            AdapterWhatsNewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return WhatsNewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WhatsNewViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(whatsNew: WhatsNew)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}

