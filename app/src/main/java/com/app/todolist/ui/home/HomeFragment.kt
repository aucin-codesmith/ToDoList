package com.app.todolist.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todolist.R // Ini import yang baru ditambahkan
import com.app.todolist.adapter.TaskAdapter
import com.app.todolist.data.repository.NotificationRepository
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.data.repository.UserRepository
import com.app.todolist.databinding.FragmentHomeBinding
import com.app.todolist.model.TaskItem
import com.app.todolist.ui.auth.LoginActivity
import com.app.todolist.ui.notification.NotificationListActivity
import com.app.todolist.ui.task.form.AddTaskActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: TaskAdapter

    // Inflate layout XML ke Fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Inisialisasi logika setelah view berhasil dibuat
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            // Coba pulihkan sesi (dari memory atau SharedPreferences+Room)
            val hasSession = UserRepository.restoreSessionIfNeeded(requireContext())
            if (!hasSession) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
                return@launch
            }

            setupUserGreeting()
            setupRecyclerView(TaskRepository.getRecentTasks(requireContext()))
            updateSummaryCard()
            setupClickListeners()
        }
    }

    override fun onResume() {
        super.onResume()
        // Guard: onViewCreated berjalan async, jadi onResume bisa terpanggil duluan
        if (!::taskAdapter.isInitialized) return

        viewLifecycleOwner.lifecycleScope.launch {
            // Refresh data dari repository saat kembali dari halaman lain
            taskAdapter.updateTasks(TaskRepository.getRecentTasks(requireContext()))
            updateSummaryCard()
            updateNotifBadge()
        }
    }

    // ── User greeting ─────────────────────────────────────────────────────────

    private fun setupUserGreeting() {
        val firstName = UserRepository.getCurrentUser()?.name?.split(" ")?.first().orEmpty()
        binding.tvGreeting.text = "Halo, $firstName 👋"
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private fun setupRecyclerView(initialTasks: List<TaskItem>) {
        taskAdapter = TaskAdapter(
            tasks = initialTasks.toMutableList()
        ) { task, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                TaskRepository.updateTaskItemCompleted(requireContext(), task.id, isChecked)
                updateSummaryCard()
            }
        }
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
            isNestedScrollingEnabled = false
        }
    }

    // ── Summary card ──────────────────────────────────────────────────────────

    private suspend fun updateSummaryCard() {
        val total = TaskRepository.getTotalCount(requireContext())
        val completed = TaskRepository.getCompletedCount(requireContext())
        val remaining = TaskRepository.getRemainingCount(requireContext())

        binding.tvTaskCount.text = "$total Tugas Hari Ini"
        binding.tvTaskProgress.text = "$completed selesai · $remaining tersisa"
        binding.progressTasks.progress = if (total > 0) (completed * 100) / total else 0
    }

    // ── Notification badge ────────────────────────────────────────────────────

    private suspend fun updateNotifBadge() {
        val unread = NotificationRepository.getUnreadCount(requireContext())
        // Tampilkan badge jika ada notif belum dibaca
        // binding.badgeNotif?.visibility = if (unread > 0) View.VISIBLE else View.GONE
        // binding.badgeNotif?.text       = unread.toString()
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddTaskActivity::class.java))
        }

        binding.tvSeeAll.setOnClickListener {
            // Kita "tembak" id bottomNav yang ada di MainActivity
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            bottomNav.selectedItemId = R.id.nav_tasks
        }

        binding.cvNotif.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationListActivity::class.java))
        }
    }

    // Hindari memory leak dengan membersihkan binding saat fragment dihancurkan
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}