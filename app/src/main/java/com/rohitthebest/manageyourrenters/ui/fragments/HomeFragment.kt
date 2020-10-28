package com.rohitthebest.manageyourrenters.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentHomeBinding
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.closeKeyboard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showKeyboard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import kotlinx.coroutines.*

class HomeFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isSearchViewVisible = false
    private var mAuth: FirebaseAuth? = null


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

        mAuth = Firebase.auth

        initListeners()
    }

    override fun onStart() {
        super.onStart()

        if (mAuth?.currentUser != null) {

            if (isInternetAvailable(requireContext())) {

                if (mAuth?.currentUser!!.photoUrl != null) {

                    Glide.with(this)
                        .load(mAuth?.currentUser!!.photoUrl)
                        .into(binding.profileImage)
                }

            } else {

                showNoInternetMessage(requireContext())
            }

        }
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

            binding.addRenterFAB.id -> {

                if(isInternetAvailable(requireContext())) {

                    findNavController().navigate(R.id.action_homeFragment_to_addRenterFragment)
                }else {

                    showNoInternetMessage(requireContext())
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