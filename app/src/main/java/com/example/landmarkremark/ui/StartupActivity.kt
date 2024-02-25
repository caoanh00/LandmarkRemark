package com.example.landmarkremark.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.landmarkremark.databinding.ActivityStartupBinding
import com.example.landmarkremark.ui.main.MainActivity
import com.example.landmarkremark.repository.MainRepository
import com.example.landmarkremark.ui.signin.SignInActivity
import com.example.landmarkremark.utilities.SharedPreferenceUtils

class StartupActivity : AppCompatActivity() {

    private var isGoingToSignInActivity: Boolean = false

    private lateinit var binding: ActivityStartupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (MainRepository.getFirebaseUser() != null) {
            // User is signed in, load locations with user Id
            MainRepository.getLocations()
        } else {
            // No user is signed in
            isGoingToSignInActivity = SharedPreferenceUtils.getUserId().isNullOrBlank()

        }

        if(!SharedPreferenceUtils.getRememberMe()) {
            SharedPreferenceUtils.clearAccessToken()
            isGoingToSignInActivity = true
        }

        if (isGoingToSignInActivity) {
            // Go to SignInActivity
            val intent = Intent(this@StartupActivity, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }else {
            val intent = Intent(this@StartupActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}