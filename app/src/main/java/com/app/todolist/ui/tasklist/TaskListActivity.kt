package com.app.todolist.ui.tasklist

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todolist.R
import com.app.todolist.adapter.TaskListAdapter
import com.app.todolist.databinding.ActivityTaskListBinding
import com.app.todolist.model.TaskItem
import com.app.todolist.ui.home.HomeActivity

class TaskListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskListBinding
    private lateinit var adapter: TaskListAdapter

    // ── Sample data (replace with ViewModel + Repository later) ──────────────
    private val allTasks = mutableListOf(
        TaskItem(1, "Desain Prototipe Mobile App", "Work",        "Hari ini, 14:00", "KMP"),
        TaskItem(2, "Review Laporan Mingguan",      "Management",  "Besok, 09:00"),
        TaskItem(3, "Update Dokumentasi API",       "Development", "Hari ini, 16:00", "DEV"),
        TaskItem(4, "Sprint Planning Q2",           "Management",  "Besok, 10:00"),
        TaskItem(5, "Desain Komponen UI",           "Design",      "Besok, 13:00",    "KMP"),
    )

    private var currentFilter = FilterType.ACTIVE
    private var searchQuery   = ""

    enum class FilterType { ACTIVE, COMPLETED, ALL }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupClickListeners()
        selectFilter(FilterType.ACTIVE)
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = TaskListAdapter(mutableListOf()) { task, isChecked ->
            allTasks.find { it.id == task.id }?.isCompleted = isChecked
            applyFilter()
        }
        binding.rvTasks.apply {
            layoutManager            = LinearLayoutManager(this@TaskListActivity)
            adapter                  = this@TaskListActivity.adapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.trim().orEmpty()
                applyFilter()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupFilters() {
        binding.btnFilterActive.setOnClickListener    { selectFilter(FilterType.ACTIVE) }
        binding.btnFilterCompleted.setOnClickListener { selectFilter(FilterType.COMPLETED) }
        binding.btnFilterAll.setOnClickListener       { selectFilter(FilterType.ALL) }
    }

    private fun setupClickListeners() {
        binding.ivMenu.setOnClickListener {
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show()
        }
        binding.ivAvatar.setOnClickListener {
            Toast.makeText(this, "Profil", Toast.LENGTH_SHORT).show()
        }
        binding.fabAdd.setOnClickListener {
            Toast.makeText(this, "Tambah tugas baru", Toast.LENGTH_SHORT).show()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tasks   -> true
                R.id.nav_add     -> { binding.fabAdd.performClick(); true }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        binding.bottomNav.selectedItemId = R.id.nav_tasks
    }

    // ── Filter logic ──────────────────────────────────────────────────────────

    private fun selectFilter(type: FilterType) {
        currentFilter = type
        updateFilterUI()
        applyFilter()
    }

    private fun updateFilterUI() {
        listOf(
            binding.btnFilterActive    to FilterType.ACTIVE,
            binding.btnFilterCompleted to FilterType.COMPLETED,
            binding.btnFilterAll       to FilterType.ALL
        ).forEach { (tv, type) ->
            // Safe cast — in XML these are now TextView, not Button
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
        val filtered = allTasks
            .filter { task ->
                when (currentFilter) {
                    FilterType.ACTIVE    -> !task.isCompleted
                    FilterType.COMPLETED ->  task.isCompleted
                    FilterType.ALL       -> true
                }
            }
            .filter { task ->
                searchQuery.isEmpty() ||
                        task.title.contains(searchQuery, ignoreCase = true)
            }

        adapter.updateTasks(filtered)

        val isEmpty = filtered.isEmpty()
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTasks.visibility     = if (isEmpty) View.GONE    else View.VISIBLE
    }
}