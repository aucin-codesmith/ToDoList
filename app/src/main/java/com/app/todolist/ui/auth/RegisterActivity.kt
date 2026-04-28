package com.app.todolist.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.todolist.R
import com.app.todolist.databinding.ActivityRegisterBinding
import com.google.android.material.textfield.TextInputLayout
import android.content.Intent
import com.app.todolist.ui.home.HomeActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                performRegister()
            }
        }

        binding.btnLogin.setOnClickListener {
            onLoginClicked()
        }
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    private fun validateInputs(): Boolean {
        // Gunakan trim() untuk username dan email, tapi jangan untuk password
        // karena spasi bisa jadi bagian dari password yang valid.
        val username = binding.etUsername.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()

        var isValid = true

        // Reset semua error terlebih dahulu agar tidak menumpuk
        binding.tilUsername.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        // Validasi Username
        when {
            username.isEmpty() -> {
                binding.tilUsername.error = "Username tidak boleh kosong"
                isValid = false
            }
            // Mengecek apakah username mengandung spasi
            username.contains(" ") -> {
                binding.tilUsername.error = "Username tidak boleh mengandung spasi"
                isValid = false
            }
        }

        // Validasi Email
        when {
            email.isEmpty() -> {
                binding.tilEmail.error = "Email tidak boleh kosong"
                isValid = false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Format email tidak valid"
                isValid = false
            }
        }

        // Validasi Password
        when {
            password.isEmpty() -> {
                binding.tilPassword.error = "Password tidak boleh kosong"
                isValid = false
            }
            password.length < 6 -> {
                binding.tilPassword.error = "Password minimal 6 karakter"
                isValid = false
            }
        }

        // Validasi Confirm Password
        when {
            confirmPassword.isEmpty() -> {
                binding.tilConfirmPassword.error = "Konfirmasi password tidak boleh kosong"
                isValid = false
            }
            confirmPassword != password -> {
                binding.tilConfirmPassword.error = "Password tidak cocok"
                isValid = false
            }
        }

        return isValid
    }

    // ─── Auth actions (UI-only placeholders) ──────────────────────────────────

    private fun performRegister() {
        setRegisterLoading(true)
        Toast.makeText(this, "Berhasil mendaftar", Toast.LENGTH_SHORT).show()
        binding.root.postDelayed({
            setRegisterLoading(false)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1_500)
    }

    private fun onLoginClicked() {
        // TODO: Navigate to LoginActivity
        binding.root.postDelayed({
            setRegisterLoading(false)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1_500)
    }

    // ─── Loading state ────────────────────────────────────────────────────────

    private fun setRegisterLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "" else "Register"
        // Optional: show a CircularProgressIndicator inside the button
        // binding.progressLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}