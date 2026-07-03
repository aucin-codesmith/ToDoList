package com.app.todolist.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.todolist.R
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.data.repository.UserRepository
import com.app.todolist.databinding.ActivityProfileBinding
import com.app.todolist.ui.auth.LoginActivity
import com.app.todolist.ui.home.HomeActivity
import com.app.todolist.ui.task.TaskListActivity
import com.app.todolist.ui.task.form.AddTaskActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Session guard: pastikan ada user yang login sebelum lanjut render Profile
        if (UserRepository.getCurrentUser() == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserInfo()
        setupClickListeners()
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        // Refresh stats saat kembali dari halaman lain (misal task baru ditambah)
        updateStats()
    }

    // ── User info dari UserRepository ─────────────────────────────────────────

    private fun setupUserInfo() {
        val user = UserRepository.getCurrentUser() ?: return

        // Avatar initials (2 huruf pertama nama)
        binding.tvAvatarInitials.text = user.avatarInitials

        // Nama lengkap sebagai display name
        binding.tvProfileName.text = user.name

        // Role badge — tampilkan role dari UserProfile
        binding.tvProfileRole.text = "${user.role} ✓"

        // Email
        binding.tvProfileEmail.text = user.email

        // Stats dari TaskRepository
        updateStats()
    }

    private fun updateStats() {
        val total     = TaskRepository.getTotalCount()
        val completed = TaskRepository.getCompletedCount()
        val active    = TaskRepository.getRemainingCount()

        binding.tvStatTotal.text     = total.toString()
        binding.tvStatCompleted.text = completed.toString()
        binding.tvStatActive.text    = active.toString()
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSettings.setOnClickListener {
            Toast.makeText(this, "Pengaturan", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotifications.setOnClickListener {
            Toast.makeText(this, "Notifikasi", Toast.LENGTH_SHORT).show()
        }

        binding.btnChangeUsername.setOnClickListener {
            Toast.makeText(
                this,
                "Ubah Username: @${UserRepository.getUserUsername()}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnChangePassword.setOnClickListener {
            Toast.makeText(this, "Ubah Password", Toast.LENGTH_SHORT).show()
        }

        binding.btnAboutApp.setOnClickListener {
            Toast.makeText(this, "Tentang Aplikasi", Toast.LENGTH_SHORT).show()
        }

        binding.btnHelpSupport.setOnClickListener {
            Toast.makeText(this, "Bantuan & Dukungan", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Apakah kamu yakin ingin keluar dari akun ini?")
            .setPositiveButton("Logout") { _, _ -> performLogout() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        UserRepository.clearCurrentUser()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    // ── Bottom Nav ────────────────────────────────────────────────────────────

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_profile

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { startActivity(Intent(this, HomeActivity::class.java)); finish(); true }
                R.id.nav_tasks   -> { startActivity(Intent(this, TaskListActivity::class.java)); finish(); true }
                R.id.nav_add     -> { startActivity(Intent(this, AddTaskActivity::class.java)); true }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}