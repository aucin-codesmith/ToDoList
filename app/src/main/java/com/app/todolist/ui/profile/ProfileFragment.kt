package com.app.todolist.ui.profile

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.data.repository.UserRepository
import com.app.todolist.databinding.FragmentProfileBinding
import com.app.todolist.ui.auth.LoginActivity
import com.app.todolist.util.NotificationSettingsDialog
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
        binding.tvProfileEmail.text = user.email

        updateStats()
    }

    private suspend fun updateStats() {
        val currentUserId = SessionManager.getUserId(requireContext())?.toString() ?: "0"

        val total     = TaskRepository.getTotalCount(requireContext(), currentUserId)
        val completed = TaskRepository.getCompletedCount(requireContext(), currentUserId)
        val active    = TaskRepository.getRemainingCount(requireContext(), currentUserId)

        binding.tvStatTotal.text     = total.toString()
        binding.tvStatCompleted.text = completed.toString()
        binding.tvStatActive.text    = active.toString()
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private fun setupClickListeners() {
        // Karena ini sekarang fragment utama, tombol back biasanya tidak dibutuhkan.
        // Jika UI-mu masih menampilkan tombol back, biarkan kosong atau arahkan ke tab Home.
        // binding.btnBack.setOnClickListener { ... }

        binding.btnNotifications.setOnClickListener {
            NotificationSettingsDialog.show(requireContext(), viewLifecycleOwner.lifecycleScope)
        }

        binding.btnChangeUsername.setOnClickListener {
            showChangeUsernameDialog()
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnAboutApp.setOnClickListener {
            showAboutAppDialog()
        }

        binding.btnHelpSupport.setOnClickListener {
            showHelpSupportDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showAboutAppDialog() {
        val versionName = try {
            requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0)
                .versionName
        } catch (e: Exception) {
            "-"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Tentang Aplikasi")
            .setMessage(
                "To-Do List App\nVersi $versionName\n\n" +
                        "Aplikasi ini membantu kamu mengatur dan mengingat tugas-tugas " +
                        "dengan pengingat deadline, kategori, dan prioritas."
            )
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun showHelpSupportDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Bantuan & Dukungan")
            .setMessage(
                "Butuh bantuan? Hubungi kami di:\n\n" +
                        "Email: support@todolist.app\n\n" +
                        "Atau lihat FAQ berikut:\n" +
                        "• Tugas tidak muncul reminder? Pastikan izin notifikasi sudah diaktifkan.\n" +
                        "• Lupa password? Gunakan menu \"Ubah Password\" di halaman profil."
            )
            .setPositiveButton("Tutup", null)
            .show()
    }
    private fun dialogPadding(): Int = (20 * resources.displayMetrics.density).toInt()

    private fun showChangeUsernameDialog() {
        val ctx = requireContext()
        val padding = dialogPadding()

        val usernameInput = EditText(ctx).apply {
            setText(UserRepository.getUserUsername())
            hint = "Username baru"
            setSelection(text.length)
        }

        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding / 2, padding, 0)
            addView(usernameInput)
        }

        AlertDialog.Builder(ctx)
            .setTitle("Ubah Username")
            .setView(container)
            .setPositiveButton("Simpan") { _, _ ->
                val newUsername = usernameInput.text.toString()
                viewLifecycleOwner.lifecycleScope.launch {
                    val error = UserRepository.updateUsername(ctx, newUsername)
                    if (error != null) {
                        Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show()
                    } else {
                        binding.tvProfileName.text = UserRepository.getCurrentUser()?.name.orEmpty()
                        binding.tvAvatarInitials.text = UserRepository.getUserInitials()
                        Toast.makeText(ctx, "Username berhasil diubah", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val ctx = requireContext()
        val padding = dialogPadding()

        fun passwordField(hintText: String) = EditText(ctx).apply {
            hint = hintText
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val oldPasswordInput = passwordField("Password lama")
        val newPasswordInput = passwordField("Password baru")
        val confirmPasswordInput = passwordField("Konfirmasi password baru")

        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding / 2, padding, 0)
            addView(oldPasswordInput)
            addView(newPasswordInput)
            addView(confirmPasswordInput)
        }

        AlertDialog.Builder(ctx)
            .setTitle("Ubah Password")
            .setView(container)
            .setPositiveButton("Simpan") { _, _ ->
                val oldPassword = oldPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                if (newPassword != confirmPassword) {
                    Toast.makeText(ctx, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val error = UserRepository.updatePassword(ctx, oldPassword, newPassword)
                    if (error != null) {
                        Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(ctx, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
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