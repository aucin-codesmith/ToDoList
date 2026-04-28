package com.app.todolist.ui.home

import android.content.Intent // ✅ TAMBAHAN IMPORT
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todolist.R
import com.app.todolist.adapter.TaskAdapter
import com.app.todolist.databinding.ActivityMainBinding
import com.app.todolist.model.Task
import com.app.todolist.ui.tasklist.TaskListActivity // ✅ IMPORT BARU

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter

    private val tasks = mutableListOf(
        Task(1, "Review Design System",  "14 Okt, 2023"),
        Task(2, "Daily Standup Meeting", "14 Okt, 2023", isCompleted = true),
        Task(3, "Beli Bahan Makanan",    "15 Okt, 2023"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        updateSummaryCard()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(tasks) { _, _ ->
            updateSummaryCard()
        }
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = taskAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun updateSummaryCard() {
        val total     = tasks.size
        val completed = tasks.count { it.isCompleted }
        val remaining = total - completed

        binding.tvTaskCount.text    = "$total Tugas Hari Ini"
        binding.tvTaskProgress.text = "$completed selesai · $remaining tersisa"
        binding.progressTasks.progress = if (total > 0) (completed * 100) / total else 0
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            Toast.makeText(this, "Tambah tugas baru", Toast.LENGTH_SHORT).show()
        }

        //  UPDATE 1: tvSeeAll
        binding.tvSeeAll.setOnClickListener {
            startActivity(Intent(this, TaskListActivity::class.java))
        }

        binding.cvAvatar.setOnClickListener {
            Toast.makeText(this, "Profil", Toast.LENGTH_SHORT).show()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                // UPDATE 2: nav_tasks
                R.id.nav_tasks -> {
                    startActivity(Intent(this, TaskListActivity::class.java))
                    true
                }

                R.id.nav_add -> {
                    binding.fabAdd.performClick()
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