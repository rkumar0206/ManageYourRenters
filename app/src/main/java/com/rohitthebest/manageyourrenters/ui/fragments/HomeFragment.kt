package com.rohitthebest.manageyourrenters.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.databinding.FragmentHomeBinding
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.closeKeyboard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showKeyboard
import kotlinx.coroutines.*

class HomeFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isSearchViewVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
    }

    private fun initListeners() {

        binding.searchRenterBtn.setOnClickListener(this)
        binding.addRenterFAB.setOnClickListener(this)
        binding.profileImage.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.searchRenterBtn.id -> {

                if (!isSearchViewVisible) {

                    showSearchView()
                } else {

                    hideSearchView()
                }
            }
        }

    }

    private fun showSearchView() {

        isSearchViewVisible = !isSearchViewVisible

        binding.renterSV.show()
        binding.renterSV.animate().translationY(0f).alpha(1f).setDuration(350).start()

        binding.searchET.requestFocus()

        showKeyboard(requireActivity(), binding.searchET)
    }

    private fun hideSearchView() {

        isSearchViewVisible = !isSearchViewVisible

        binding.renterSV.animate().translationY(-50f).alpha(0f).setDuration(350).start()

        GlobalScope.launch {

            delay(360)

            closeKeyboard(requireActivity())
            withContext(Dispatchers.Main) {

                binding.renterSV.hide()
                binding.searchET.setText("")
            }
        }
    }

}