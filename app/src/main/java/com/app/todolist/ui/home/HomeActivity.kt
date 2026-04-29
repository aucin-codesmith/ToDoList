package com.app.todolist.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todolist.R
import com.app.todolist.adapter.TaskAdapter
import com.app.todolist.data.repository.NotificationRepository
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.data.repository.UserRepository
import com.app.todolist.databinding.ActivityMainBinding
import com.app.todolist.ui.notification.NotificationListActivity
import com.app.todolist.ui.profile.ProfileActivity
import com.app.todolist.ui.task.TaskListActivity
import com.app.todolist.ui.task.form.AddTaskActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserGreeting()
        setupRecyclerView()
        updateSummaryCard()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data dari repository saat kembali dari halaman lain
        taskAdapter.updateTasks(TaskRepository.getRecentTasks())
        updateSummaryCard()
        updateNotifBadge()
    }

    // ── User greeting ─────────────────────────────────────────────────────────

    private fun setupUserGreeting() {
        val firstName = UserRepository.getCurrentUser().name.split(" ").first()
        binding.tvGreeting.text = "Halo, $firstName 👋"
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            tasks = TaskRepository.getRecentTasks().toMutableList()
        ) { task, isChecked ->
            TaskRepository.updateTaskItemCompleted(task.id, isChecked)
            updateSummaryCard()
        }
        binding.rvTasks.apply {
            layoutManager            = LinearLayoutManager(this@HomeActivity)
            adapter                  = taskAdapter
            isNestedScrollingEnabled = false
        }
    }

    // ── Summary card ──────────────────────────────────────────────────────────

    private fun updateSummaryCard() {
        val total     = TaskRepository.getTotalCount()
        val completed = TaskRepository.getCompletedCount()
        val remaining = TaskRepository.getRemainingCount()

        binding.tvTaskCount.text    = "$total Tugas Hari Ini"
        binding.tvTaskProgress.text = "$completed selesai · $remaining tersisa"
        binding.progressTasks.progress = if (total > 0) (completed * 100) / total else 0
    }

    // ── Notification badge ────────────────────────────────────────────────────

    private fun updateNotifBadge() {
        val unread = NotificationRepository.getUnreadCount()
        // Tampilkan badge jika ada notif belum dibaca
        // binding.badgeNotif?.visibility = if (unread > 0) View.VISIBLE else View.GONE
        // binding.badgeNotif?.text       = unread.toString()
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        binding.tvSeeAll.setOnClickListener {
            startActivity(Intent(this, TaskListActivity::class.java))
        }

        binding.cvNotif.setOnClickListener {
            startActivity(Intent(this, NotificationListActivity::class.java))
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> true
                R.id.nav_tasks   -> { startActivity(Intent(this, TaskListActivity::class.java)); true }
                R.id.nav_add     -> { startActivity(Intent(this, AddTaskActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                else             -> false
            }
        }
    }
}