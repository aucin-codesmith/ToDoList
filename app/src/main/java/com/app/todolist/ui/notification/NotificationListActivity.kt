package com.app.todolist.ui.notification

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todolist.R
import com.app.todolist.adapter.NotificationAdapter
import com.app.todolist.databinding.ActivityNotificationListBinding
import com.app.todolist.model.NotificationItem
import com.app.todolist.model.NotifType
import com.app.todolist.ui.home.HomeActivity
import com.app.todolist.ui.task.TaskListActivity

class NotificationListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationListBinding
    private lateinit var adapter: NotificationAdapter

    // ── Sample data ───────────────────────────────────────────────────────────
    private val allNotifications = mutableListOf(
        NotificationItem(
            id      = 1,
            title   = "Tugas Segera Berakhir",
            body    = "Selesaikan proyek \"Desain UI App\" sebelum jam 17:00 hari ini.",
            time    = "5 mnt yang lalu",
            type    = NotifType.DEADLINE,
            isRead  = false
        ),
        NotificationItem(
            id      = 2,
            title   = "Pengingat Harian",
            body    = "Jangan lupa untuk mengisi jurnal harian Anda untuk menjaga produktivitas.",
            time    = "2 jam yang lalu",
            type    = NotifType.REMINDER,
            isRead  = false
        ),
        NotificationItem(
            id      = 3,
            title   = "Tugas Selesai",
            body    = "\"Belanja Mingguan\" telah ditandai sebagai selesai oleh Anda.",
            time    = "Kemarin",
            type    = NotifType.DONE,
            isRead  = true
        ),
        NotificationItem(
            id      = 4,
            title   = "Pembaruan Sistem",
            body    = "Versi 2.1.0 telah tersedia dengan perbaikan bug sinkronisasi cloud.",
            time    = "2 hari yang lalu",
            type    = NotifType.SYSTEM,
            isRead  = true
        ),
    )

    private var currentFilter = FilterType.ALL

    enum class FilterType { ALL, UNREAD, SYSTEM }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

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

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnMore.setOnClickListener {
            // TODO: tampilkan popup menu (tandai semua dibaca, hapus semua, dll)
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show()
        }
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(mutableListOf()) { notif ->
            // Tandai sebagai dibaca
            val index = allNotifications.indexOfFirst { it.id == notif.id }
            if (index != -1) {
                allNotifications[index] = allNotifications[index].copy(isRead = true)
                applyFilter()
            }
            // TODO: aksi lebih lanjut sesuai tipe notif
        }

        binding.rvNotifications.apply {
            layoutManager            = LinearLayoutManager(this@NotificationListActivity)
            adapter                  = this@NotificationListActivity.adapter
            isNestedScrollingEnabled = false
        }
    }

    // ── Filter Tabs ───────────────────────────────────────────────────────────

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
        val buttons = listOf(
            binding.btnFilterAll    to FilterType.ALL,
            binding.btnFilterUnread to FilterType.UNREAD,
            binding.btnFilterSystem to FilterType.SYSTEM
        )
        buttons.forEach { (tv, type) ->
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
        val filtered = allNotifications.filter { notif ->
            when (currentFilter) {
                FilterType.ALL    -> true
                FilterType.UNREAD -> !notif.isRead
                FilterType.SYSTEM -> notif.type == NotifType.SYSTEM
            }
        }
        adapter.updateItems(filtered)
    }

    // ── Promo Card ────────────────────────────────────────────────────────────

    private fun setupPromoCard() {
        binding.btnPromoAction.setOnClickListener {
            // TODO: navigasi ke halaman pengaturan notifikasi
            Toast.makeText(this, "Mengaktifkan pengingat cerdas...", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Bottom Nav ────────────────────────────────────────────────────────────

    private fun setupBottomNav() {
        // Notifikasi tidak punya tab di bottom nav — tidak set selectedItemId
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tasks -> {
                    startActivity(Intent(this, TaskListActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, com.app.todolist.ui.task.form.AddTaskActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}