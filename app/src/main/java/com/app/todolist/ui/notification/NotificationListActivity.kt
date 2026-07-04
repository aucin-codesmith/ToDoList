package com.app.todolist.ui.notification

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todolist.R
import com.app.todolist.adapter.NotificationAdapter
import com.app.todolist.data.repository.NotificationRepository
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.model.NotifType
import com.app.todolist.databinding.ActivityNotificationListBinding
import com.app.todolist.ui.home.HomeFragment
import com.app.todolist.ui.profile.ProfileFragment
import com.app.todolist.ui.task.TaskListFragment
import com.app.todolist.ui.task.form.AddTaskActivity
import kotlinx.coroutines.launch

class NotificationListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationListBinding
    private lateinit var adapter: NotificationAdapter

    private var currentFilter = FilterType.ALL
    enum class FilterType { ALL, UNREAD, SYSTEM }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFilters()
        setupPromoCard()
        setupBottomNav()
        applyFilter()
    }

    override fun onResume() {
        super.onResume()
        applyFilter()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(mutableListOf()) { notif ->
            // Tandai sudah dibaca lalu buka detail
            NotificationRepository.markAsRead(notif.id)
            applyFilter()
            lifecycleScope.launch { openDetailNotification(notif.id) }
        }
        binding.rvNotifications.apply {
            layoutManager            = LinearLayoutManager(this@NotificationListActivity)
            adapter                  = this@NotificationListActivity.adapter
            isNestedScrollingEnabled = false
        }
    }

    private suspend fun openDetailNotification(notifId: Int) {
        val notif   = NotificationRepository.getNotificationById(notifId) ?: return
        val hasTask = notif.type == NotifType.DEADLINE ||
                notif.type == NotifType.REMINDER ||
                notif.type == NotifType.DONE

        // Resolve data task dari TaskRepository menggunakan taskId yang tersimpan di notif
        val linkedTask = notif.taskId?.let { TaskRepository.getTaskItemById(this, it) }

        val intent = Intent(this, DetailNotificationActivity::class.java).apply {
            putExtra(DetailNotificationActivity.EXTRA_NOTIF_ID,       notif.id)
            putExtra(DetailNotificationActivity.EXTRA_NOTIF_TITLE,    notif.title)
            putExtra(DetailNotificationActivity.EXTRA_NOTIF_BODY,     notif.body)
            putExtra(DetailNotificationActivity.EXTRA_NOTIF_BODY_HL,  notif.bodyHighlight)
            putExtra(DetailNotificationActivity.EXTRA_NOTIF_TIME,     notif.time)
            putExtra(DetailNotificationActivity.EXTRA_NOTIF_CATEGORY, notif.taskCategory)
            putExtra(DetailNotificationActivity.EXTRA_TASK_CATEGORY,  notif.taskCategory)
            putExtra(DetailNotificationActivity.EXTRA_TASK_DEADLINE,  notif.taskDeadline)
            putExtra(DetailNotificationActivity.EXTRA_TASK_PRIORITY,  notif.taskPriority)
            putExtra(DetailNotificationActivity.EXTRA_HAS_TASK,       hasTask)
            // Pass taskId task asli (bukan notifId) agar DetailNotification bisa navigasi ke task
            putExtra(DetailNotificationActivity.EXTRA_TASK_ID,        notif.taskId ?: -1)
            // Data tambahan untuk pass ke DetailTaskActivity
            putExtra(DetailNotificationActivity.EXTRA_TASK_TITLE,     linkedTask?.title     ?: notif.title)
            putExtra(DetailNotificationActivity.EXTRA_TASK_DESKRIPSI, linkedTask?.description ?: "-")
            putExtra(DetailNotificationActivity.EXTRA_TASK_STATUS,
                if (linkedTask?.isCompleted == true) "selesai" else "belum_selesai")
        }
        startActivity(intent)
    }

    private fun setupFilters() {
        binding.btnFilterAll.setOnClickListener    { selectFilter(FilterType.ALL) }
        binding.btnFilterUnread.setOnClickListener { selectFilter(FilterType.UNREAD) }
        binding.btnFilterSystem.setOnClickListener { selectFilter(FilterType.SYSTEM) }
    }

    private fun selectFilter(type: FilterType) {
        currentFilter = type
        updateFilterUI()
        applyFilter()
    }

    private fun updateFilterUI() {
        listOf(
            binding.btnFilterAll    to FilterType.ALL,
            binding.btnFilterUnread to FilterType.UNREAD,
            binding.btnFilterSystem to FilterType.SYSTEM
        ).forEach { (tv, type) ->
            val textView = tv as TextView
            if (type == currentFilter) {
                textView.setBackgroundResource(R.drawable.bg_filter_selected)
                textView.setTextColor(getColor(R.color.white))
            } else {
                textView.setBackgroundResource(R.drawable.bg_filter_unselected)
                textView.setTextColor(getColor(R.color.on_surface_variant))
            }
        }
    }

    private fun applyFilter() {
        val filtered = NotificationRepository.getNotifications().filter { notif ->
            when (currentFilter) {
                FilterType.ALL    -> true
                FilterType.UNREAD -> !notif.isRead
                FilterType.SYSTEM -> notif.type == NotifType.SYSTEM
            }
        }
        adapter.updateItems(filtered)
    }

    private fun setupPromoCard() {
        binding.btnPromoAction.setOnClickListener {
            Toast.makeText(this, "Mengaktifkan pengingat cerdas...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { startActivity(Intent(this, HomeFragment::class.java)); finish(); true }
                R.id.nav_tasks   -> { startActivity(Intent(this, TaskListFragment::class.java)); finish(); true }
                R.id.nav_add     -> { startActivity(Intent(this, AddTaskActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileFragment::class.java)); true }
                else -> false
            }
        }
    }
}