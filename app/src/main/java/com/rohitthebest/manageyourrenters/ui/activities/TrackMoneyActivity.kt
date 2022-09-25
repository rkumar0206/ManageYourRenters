package com.rohitthebest.manageyourrenters.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.ActivityTrackMoneyBinding
import com.rohitthebest.manageyourrenters.others.Constants.SHORTCUT_FRAGMENT_NAME_KEY
import com.rohitthebest.manageyourrenters.utils.isValid
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackMoneyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackMoneyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_ManageYourRenters)

        binding = ActivityTrackMoneyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentName = intent.getStringExtra(SHORTCUT_FRAGMENT_NAME_KEY)

        if (fragmentName.isValid()) {

            Log.d("shortcut", "onCreate: fragmentName : $fragmentName")

            val navHostFragment =
                supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment

            val navController = navHostFragment.navController

            val bundle = Bundle()
            bundle.putString("shortcutFragmentNameKey", fragmentName)
            navController.setGraph(R.navigation.track_money_navigation, bundle)
        }

    }
}