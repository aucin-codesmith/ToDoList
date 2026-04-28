package com.app.todolist.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.todolist.R
import com.app.todolist.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputLayout
import android.content.Intent
import com.app.todolist.ui.home.HomeActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInputListeners()
        setupClickListeners()
    }

    // ─── Input validation listeners ───────────────────────────────────────────

    private fun setupInputListeners() {
        // Clear error on email input
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) binding.tilEmail.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Clear error on password input
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) binding.tilPassword.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ─── Click listeners ──────────────────────────────────────────────────────

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        binding.btnForgotPassword.setOnClickListener {
            onForgotPasswordClicked()
        }

        binding.btnRegister.setOnClickListener {
            onRegisterClicked()
        }
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Format email tidak valid"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password minimal 6 karakter"
            isValid = false
        }

        return isValid
    }

    // ─── Auth actions (UI-only placeholders) ──────────────────────────────────

    private fun performLogin() {
        setLoginLoading(true)
        binding.root.postDelayed({
            setLoginLoading(false)
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }, 1_500)
    }

    private fun onForgotPasswordClicked() {
        // TODO: Navigate to ForgotPasswordActivity / show bottom sheet
        Toast.makeText(this, "Lupa password diklik", Toast.LENGTH_SHORT).show()
    }

    private fun onRegisterClicked() {
        // TODO: Navigate to RegisterActivity
        Toast.makeText(this, "Daftar Sekarang diklik", Toast.LENGTH_SHORT).show()
    }

    private fun onGoogleSignInClicked() {
        // TODO: Trigger Google Sign-In flow
        Toast.makeText(this, "Google Sign-In diklik", Toast.LENGTH_SHORT).show()
    }

    private fun onAppleSignInClicked() {
        // TODO: Trigger Apple Sign-In flow
        Toast.makeText(this, "iOS Sign-In diklik", Toast.LENGTH_SHORT).show()
    }

    // ─── Loading state ────────────────────────────────────────────────────────

    private fun setLoginLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "" else "Login"
        // Optional: show a CircularProgressIndicator inside the button
        // binding.progressLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}