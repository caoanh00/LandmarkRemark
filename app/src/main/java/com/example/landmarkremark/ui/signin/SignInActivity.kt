package com.example.landmarkremark.ui.signin;

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.landmarkremark.R
import com.example.landmarkremark.databinding.ActivitySignInBinding
import com.example.landmarkremark.interfaces.IOnCompleteListener
import com.example.landmarkremark.ui.main.MainActivity
import com.example.landmarkremark.repository.MainRepository
import com.example.landmarkremark.ui.signup.SignUpActivity
import com.example.landmarkremark.utilities.SharedPreferenceUtils
import com.example.landmarkremark.utilities.Utils
import com.example.landmarkremark.widgets.LoadingDialog
import com.google.android.material.snackbar.Snackbar

class SignInActivity : AppCompatActivity() {
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)
        setListeners()
        MainRepository.clear()
    }

    private fun setListeners() {
        binding.signInContainer.setOnClickListener {
            clearFocus()
        }

        binding.signInCheckboxTxt.setOnClickListener {
            binding.signInCheckbox.isChecked = !binding.signInCheckbox.isChecked
        }

        binding.signInRegister.setOnClickListener {
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.signInBtn.setOnClickListener {
            signIn()
        }

        binding.signInPassword.setOnKeyListener { _, keyCode, event ->
            if (Utils.isEnterPressed(keyCode, event)) {
                signIn()
                return@setOnKeyListener true
            } else {
                return@setOnKeyListener false
            }
        }
    }

    private fun clearFocus() {
        Utils.hideKeyboard(this)
        binding.signInEmail.clearFocus()
        binding.signInPassword.clearFocus()
    }

    /**
     * Check email and password is in correct format before posting to server. It will then display
     * any known errors to the user and if successful then start StartIpActivity in a new stack.
     */
    private fun signIn() {
        // Hide keyboard and clear focus
        clearFocus()

        // Check if email is blank
        if (binding.signInEmail.text.isNullOrBlank()) {
            Snackbar.make(binding.signInContainer, R.string.user_email_blank, Snackbar.LENGTH_LONG)
                .show()
            return
        }

        // Check if email format is correct
        if (Utils.isEmailInvalid(binding.signInEmail.text.toString())) {
            Snackbar.make(
                binding.signInContainer,
                R.string.user_email_incorrect,
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        // Check if password is blank
        if (binding.signInPassword.text.isNullOrBlank()) {
            Snackbar.make(
                binding.signInContainer,
                R.string.user_password_blank, Snackbar.LENGTH_LONG
            )
                .show()
            return
        }

        // Check if password length is correct
        if (binding.signInPassword.text.toString().length < 6) {
            Snackbar.make(
                binding.signInContainer,
                R.string.user_password_length_limit,
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        loadingDialog.show(false)
        MainRepository.signInWithEmailAndPassword(binding.signInEmail.text.toString(), binding.signInPassword.text.toString(), this, object:
            IOnCompleteListener {
            override fun onSuccess() {
                SharedPreferenceUtils.setEmail(binding.signInEmail.text.toString())
                SharedPreferenceUtils.setPassword(binding.signInPassword.text.toString())
                SharedPreferenceUtils.setRememberMe(binding.signInCheckbox.isChecked)
                postUserLogin()
            }

            override fun onError(err: Exception?) {
                val error = getString(R.string.sign_in_fail, err.toString())
                Toast.makeText(this@SignInActivity, error, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun postUserLogin() {
        MainRepository.getFirebaseUser()?.let { firebaseUser ->
            SharedPreferenceUtils.setUserId(firebaseUser.uid)
        }

        val intent = Intent(this@SignInActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}