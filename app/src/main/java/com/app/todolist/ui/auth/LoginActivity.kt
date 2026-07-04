package com.app.todolist.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.todolist.data.AppDatabase
import com.app.todolist.data.repository.UserRepository
import com.app.todolist.databinding.ActivityLoginBinding
import com.app.todolist.model.UserProfile
import com.app.todolist.ui.home.HomeActivity
import com.app.todolist.util.PasswordHasher
import com.app.todolist.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Inisialisasi database dan DAO secara lazy, sama seperti RegisterActivity
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val userDao by lazy { database.userDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            // Kalau ternyata masih ada sesi valid (baru buka app lagi setelah login
            // sebelumnya), langsung ke Home tanpa nampilin form login sama sekali
            val hasSession = UserRepository.restoreSessionIfNeeded(this@LoginActivity)
            if (hasSession) {
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
                return@launch
            }

            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupInputListeners()
            setupClickListeners()
        }
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
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        setLoginLoading(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = userDao.getUserByEmail(email)

                val loginSuccess = user != null && PasswordHasher.verify(password, user.password)

                withContext(Dispatchers.Main) {
                    setLoginLoading(false)

                    if (loginSuccess && user != null) {
                        // Simpan sesi user yang login ke UserRepository (memory)
                        UserRepository.setCurrentUser(
                            UserProfile(
                                id = user.id,
                                name = user.username,
                                username = user.username,
                                email = user.email
                            )
                        )
                        // Simpan userId ke SharedPreferences untuk auto-login
                        SessionManager.saveUserId(this@LoginActivity, user.id)

                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        // Pesan generik: tidak membedakan "email tidak ada" vs "password salah"
                        binding.tilPassword.error = "Email atau password salah"
                        Toast.makeText(this@LoginActivity, "Login gagal", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoginLoading(false)
                    Toast.makeText(this@LoginActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onForgotPasswordClicked() {
        // TODO: Navigate to ForgotPasswordActivity / show bottom sheet
        Toast.makeText(this, "Lupa password diklik", Toast.LENGTH_SHORT).show()
    }

    private fun onRegisterClicked() {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }

    // ─── Loading state ────────────────────────────────────────────────────────

    private fun setLoginLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "" else "Login"
        // Optional: show a CircularProgressIndicator inside the button
        // binding.progressLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}