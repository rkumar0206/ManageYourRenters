package com.rohitthebest.manageyourrenters.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rohitthebest.manageyourrenters.databinding.ActivityBorrowerBinding

class BorrowerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBorrowerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBorrowerBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}