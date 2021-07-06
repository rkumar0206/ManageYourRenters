package com.rohitthebest.manageyourrenters.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rohitthebest.manageyourrenters.databinding.ActivityIndividualRentersBinding

class IndividualRentersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndividualRentersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndividualRentersBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}