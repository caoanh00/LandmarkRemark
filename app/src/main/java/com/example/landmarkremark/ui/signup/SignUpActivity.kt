package com.example.landmarkremark.ui.signup

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.landmarkremark.R
import com.example.landmarkremark.databinding.ActivitySignUpBinding
import com.example.landmarkremark.interfaces.IOnCompleteListener
import com.example.landmarkremark.repository.MainRepository
import com.example.landmarkremark.utilities.SharedPreferenceUtils
import com.example.landmarkremark.utilities.Utils
import com.example.landmarkremark.widgets.LoadingDialog
import com.google.android.material.snackbar.Snackbar


class SignUpActivity : AppCompatActivity() {
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)
        setListeners()
    }

    private fun setListeners() {
        binding.signUpContainer.setOnClickListener {
            clearFocus()
        }

        binding.signUpBtn.setOnClickListener {
            signUp()
        }

        binding.signUpPassword.setOnKeyListener { _, keyCode, event ->
            if (Utils.isEnterPressed(keyCode, event)) {
                signUp()
                return@setOnKeyListener true
            } else {
                return@setOnKeyListener false
            }
        }
    }

    private fun clearFocus() {
        Utils.hideKeyboard(this)
        binding.signUpEmail.clearFocus()
        binding.signUpPassword.clearFocus()
    }

    private fun signUp() {
        // Hide keyboard and clear focus
        clearFocus()

        // Check if email is blank
        if (binding.signUpEmail.text.isNullOrBlank()) {
            Snackbar.make(binding.signUpContainer, R.string.user_email_blank, Snackbar.LENGTH_LONG)
                .show()
            return
        }

        // Check if email format is correct
        if (Utils.isEmailInvalid(binding.signUpEmail.text.toString())) {
            Snackbar.make(
                binding.signUpContainer,
                R.string.user_email_incorrect,
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        // Check if password is blank
        if (binding.signUpPassword.text.isNullOrBlank()) {
            Snackbar.make(
                binding.signUpContainer,
                R.string.user_password_blank, Snackbar.LENGTH_LONG
            )
                .show()
            return
        }

        // Check if password length is correct
        if (binding.signUpPassword.text.toString().length !in 6..32) {
            Snackbar.make(
                binding.signUpContainer,
                R.string.user_password_length_limit,
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        loadingDialog.show(false)

        MainRepository.createUserWithEmailAndPassword(binding.signUpEmail.text.toString(), binding.signUpPassword.text.toString(), this, object:
            IOnCompleteListener {
                override fun onSuccess() {
                    SharedPreferenceUtils.setEmail(binding.signUpEmail.text.toString())
                    SharedPreferenceUtils.setPassword(binding.signUpPassword.text.toString())

                    SharedPreferenceUtils.setRememberMe(true)
                    Toast.makeText(this@SignUpActivity, getString(R.string.sign_up_success), Toast.LENGTH_LONG).show()

                    finish()
                }

                override fun onError(err: Exception?) {
                    val error = getString(R.string.sign_up_fail, err.toString())
                    Toast.makeText(this@SignUpActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}