package com.rohitthebest.manageyourrenters.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rohitthebest.manageyourrenters.databinding.ActivityTrackMoneyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackMoneyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackMoneyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackMoneyBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}