package com.app.todolist.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.data.repository.UserRepository
import com.app.todolist.databinding.FragmentProfileBinding
import com.app.todolist.ui.auth.LoginActivity
import com.app.todolist.util.SessionManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val hasSession = UserRepository.restoreSessionIfNeeded(requireContext())
            if (!hasSession) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
                return@launch
            }

            setupUserInfo()
            setupClickListeners()
        }
    }

    override fun onResume() {
        super.onResume()
        if (_binding == null) return
        viewLifecycleOwner.lifecycleScope.launch { updateStats() }
    }

    // ── User info dari UserRepository ─────────────────────────────────────────

    private suspend fun setupUserInfo() {
        val user = UserRepository.getCurrentUser() ?: return

        binding.tvAvatarInitials.text = user.avatarInitials
        binding.tvProfileName.text = user.name
        binding.tvProfileRole.text = "${user.role} ✓"
        binding.tvProfileEmail.text = user.email

        updateStats()
    }

    private suspend fun updateStats() {
        val total     = TaskRepository.getTotalCount(requireContext())
        val completed = TaskRepository.getCompletedCount(requireContext())
        val active    = TaskRepository.getRemainingCount(requireContext())

        binding.tvStatTotal.text     = total.toString()
        binding.tvStatCompleted.text = completed.toString()
        binding.tvStatActive.text    = active.toString()
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private fun setupClickListeners() {
        // Karena ini sekarang fragment utama, tombol back biasanya tidak dibutuhkan.
        // Jika UI-mu masih menampilkan tombol back, biarkan kosong atau arahkan ke tab Home.
        // binding.btnBack.setOnClickListener { ... }

        binding.btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Pengaturan", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "Notifikasi", Toast.LENGTH_SHORT).show()
        }

        binding.btnChangeUsername.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Ubah Username: @${UserRepository.getUserUsername()}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnChangePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Ubah Password", Toast.LENGTH_SHORT).show()
        }

        binding.btnAboutApp.setOnClickListener {
            Toast.makeText(requireContext(), "Tentang Aplikasi", Toast.LENGTH_SHORT).show()
        }

        binding.btnHelpSupport.setOnClickListener {
            Toast.makeText(requireContext(), "Bantuan & Dukungan", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah kamu yakin ingin keluar dari akun ini?")
            .setPositiveButton("Logout") { _, _ -> performLogout() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        UserRepository.clearCurrentUser()
        SessionManager.clear(requireContext())
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}