package com.rohitthebest.manageyourrenters.ui.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.ImageHeaderParser
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.WhatsNew
import com.rohitthebest.manageyourrenters.databinding.ActivityShowImageBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.setImageToImageViewUsingGlide
import com.rohitthebest.manageyourrenters.utils.convertJsonToObject
import com.rohitthebest.manageyourrenters.utils.downloadFileFromUrl
import com.rohitthebest.manageyourrenters.utils.isValid
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowImageBinding
    private var imageUrl: String = ""
    private var imageName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_ManageYourRenters)
        binding = ActivityShowImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        imageName = System.currentTimeMillis().toString()

        val tag =
            intent.getStringExtra(Constants.GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION2)

        if (tag.isValid()) {

            when (tag) {

                getString(R.string.whats_new) -> {

                    handleImageReceivedFromWhatsNew()
                }

                else -> {

                    handleImageReceivedFromSupportingDocument()
                }
            }
        } else {
            handleImageReceivedFromSupportingDocument()
        }


        binding.downloadImageFAB.setOnClickListener {
            downloadFileFromUrl(
                this,
                imageUrl,
                imageName
            )
        }

    }

    private fun handleImageReceivedFromSupportingDocument() {

        val document =
            intent.getStringExtra(Constants.GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION)
                ?.convertJsonToObject(SupportingDocument::class.java)

        imageUrl = document?.documentUrl ?: ""
        imageName = document?.documentName ?: System.currentTimeMillis().toString()

        binding.imageIV.setImage()
    }

    private fun handleImageReceivedFromWhatsNew() {

        // todo

        val whatsNew =
            intent.getStringExtra(Constants.GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION)
                ?.convertJsonToObject(WhatsNew::class.java)

        imageUrl = whatsNew?.image ?: ""

        if (whatsNew?.imageType != null && whatsNew.imageType != ImageHeaderParser.ImageType.GIF) {
            binding.imageIV.setImage()
        } else {

            Glide.with(this)
                .asGif()
                .load(imageUrl)
                .placeholder(R.drawable.baseline_loading_24)
                .error(R.drawable.ic_outline_error_outline_24)
                .into(binding.imageIV)
        }

    }

    private fun ImageView.setImage() {

        if (!imageUrl.isValid()) onBackPressedDispatcher.onBackPressed()

        setImageToImageViewUsingGlide(
            applicationContext,
            this,
            imageUrl,
            {
                binding.imageIV.setImageResource(R.drawable.ic_outline_error_outline_24)
            },
            {}
        )
    }
}