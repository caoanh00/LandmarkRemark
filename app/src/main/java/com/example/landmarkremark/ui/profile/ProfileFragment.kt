package com.example.landmarkremark.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.landmarkremark.databinding.FragmentProfileBinding
import com.example.landmarkremark.ui.main.MainActivity
import com.example.landmarkremark.ui.main.MainViewModel
import com.example.landmarkremark.ui.signin.SignInActivity
import com.example.landmarkremark.utilities.SharedPreferenceUtils

class ProfileFragment : Fragment() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: FragmentProfileBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = context as MainActivity
        mainViewModel = ViewModelProvider(activity).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileEmailTxt.text = SharedPreferenceUtils.getEmail()

        binding.profileSignOut.setOnClickListener {
            mainViewModel.signOut()
            SharedPreferenceUtils.clearAccessToken()
            SharedPreferenceUtils.clearAccount()

            val intent = Intent(context, SignInActivity::class.java)
            context?.startActivity(intent)
        }
    }
}