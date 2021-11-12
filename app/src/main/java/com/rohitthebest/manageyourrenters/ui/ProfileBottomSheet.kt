package com.rohitthebest.manageyourrenters.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.UserInfoWithSignOutLayoutBinding

class ProfileBottomSheet() : BottomSheetDialogFragment() {

    private var _binding: UserInfoWithSignOutLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAuth: FirebaseAuth

    private var mListener: OnItemClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.user_info_with_sign_out_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = UserInfoWithSignOutLayoutBinding.bind(view)

        mAuth = Firebase.auth

        updateUI()

        binding.signOutBtn.setOnClickListener {

            if (mListener != null) {

                mListener?.onSignOutBtnClicked()
            }
        }
    }


    private fun updateUI() {

        if (mAuth.currentUser != null) {

            try {

                if (mAuth.currentUser!!.photoUrl != null) {

                    Glide.with(this)
                        .load(mAuth.currentUser!!.photoUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.profileImageIV)
                }

                binding.userNameTV.text = mAuth.currentUser?.displayName
                binding.userEmailTV.text = mAuth.currentUser?.email

            } catch (e: Exception) {

                e.printStackTrace()
            }

        }
    }

    interface OnItemClickListener {

        fun onSignOutBtnClicked()
    }

    fun setOnClickListener(listener: OnItemClickListener) {

        mListener = listener
    }

    companion object {

        @JvmStatic
        fun newInstance(bundle: Bundle?): ProfileBottomSheet {

            val fragment = ProfileBottomSheet()
            fragment.arguments = bundle
            return fragment
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}