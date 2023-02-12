package com.rohitthebest.manageyourrenters.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.databinding.ActivityShowImageBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.setImageToImageViewUsingGlide
import com.rohitthebest.manageyourrenters.utils.convertJsonToObject
import com.rohitthebest.manageyourrenters.utils.downloadFileFromUrl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowImageBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_ManageYourRenters)
        binding = ActivityShowImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val document =
            intent.getStringExtra(Constants.GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION)
                ?.convertJsonToObject(SupportingDocument::class.java)

        setImageToImageViewUsingGlide(
            this,
            binding.imageIV,
            document?.documentUrl,
            {
                binding.imageIV.setImageResource(R.drawable.ic_outline_error_outline_24)
            },
            {}
        )

        binding.downloadImageFAB.setOnClickListener {
            downloadFileFromUrl(
                this,
                document?.documentUrl,
                document?.documentName
            )
        }
    }
}