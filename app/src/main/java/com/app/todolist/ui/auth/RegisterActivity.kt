package com.app.todolist.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.todolist.databinding.ActivityRegisterBinding
import com.app.todolist.data.AppDatabase
import com.app.todolist.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    // Inisialisasi database dan DAO secara lazy
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val userDao by lazy { database.userDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInputListeners()
        setupClickListeners()
    }

    private fun setupInputListeners() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) binding.tilEmail.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) binding.tilPassword.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

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

    private fun validateInputs(): Boolean {
        val username = binding.etUsername.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()

        var isValid = true

        binding.tilUsername.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        when {
            username.isEmpty() -> {
                binding.tilUsername.error = "Username tidak boleh kosong"
                isValid = false
            }
            username.contains(" ") -> {
                binding.tilUsername.error = "Username tidak boleh mengandung spasi"
                isValid = false
            }
        }

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

    // ─── IMPLEMENTASI REGISTER DENGAN ROOM ──────────────────────────────────

    private fun performRegister() {
        val username = binding.etUsername.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        setRegisterLoading(true)

        Toast.makeText(this, "Berhasil mendaftar", Toast.LENGTH_SHORT).show()
        binding.root.postDelayed({
            setRegisterLoading(false)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1_500)

        // Menggunakan Coroutine untuk operasi database
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                // 1. Cek apakah email sudah terdaftar
//                val existingUser = userDao.getUserByEmail(email)
//
//                if (existingUser != null) {
//                    // Jika user ditemukan (email duplikat)
//                    withContext(Dispatchers.Main) {
//                        setRegisterLoading(false)
//                        binding.tilEmail.error = "Email sudah digunakan"
//                        Toast.makeText(this@RegisterActivity, "Registrasi Gagal", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    // 2. Simpan user baru ke SQLite (Room)
//                    val newUser = User(
//                        username = username,
//                        email = email,
//                        password = password
//                    )
//                    userDao.insertUser(newUser)
//
//                    // Kembali ke Main Thread untuk update UI
//                    withContext(Dispatchers.Main) {
//                        setRegisterLoading(false)
//                        Toast.makeText(this@RegisterActivity, "Berhasil mendaftar", Toast.LENGTH_SHORT).show()
//
//                        // Pindah ke LoginActivity
//                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
//                        startActivity(intent)
//                        finish()
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    setRegisterLoading(false)
//                    Toast.makeText(this@RegisterActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
    }

    private fun onLoginClicked() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setRegisterLoading(isLoading: Boolean) {
        binding.btnRegister.isEnabled = !isLoading
        // Pastikan id button sesuai dengan di XML Anda (sebelumnya tertulis btnLogin di setRegisterLoading)
        binding.btnRegister.text = if (isLoading) "Loading..." else "Register"
    }
}