package com.rohitthebest.manageyourrenters.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rohitthebest.manageyourrenters.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HouseRentersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


}