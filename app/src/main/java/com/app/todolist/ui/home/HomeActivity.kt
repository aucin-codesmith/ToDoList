package com.app.todolist.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todolist.R
import com.app.todolist.adapter.TaskAdapter
import com.app.todolist.databinding.ActivityMainBinding
import com.app.todolist.model.Priority
import com.app.todolist.model.Task
import com.app.todolist.ui.task.add.AddTaskActivity
import com.app.todolist.ui.profile.ProfileActivity
import com.app.todolist.ui.task.TaskListActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter

    private val tasks = mutableListOf(
        Task(1, "Review Design System",   "14 Okt, 2023", Priority.TINGGI),
        Task(2, "Daily Standup Meeting",  "14 Okt, 2023", Priority.MEDIUM, isCompleted = true),
        Task(3, "Beli Bahan Makanan",     "15 Okt, 2023", Priority.RENDAH),
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
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        binding.tvSeeAll.setOnClickListener {
            startActivity(Intent(this, TaskListActivity::class.java))
        }

        binding.cvAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                R.id.nav_tasks -> {
                    startActivity(Intent(this, TaskListActivity::class.java))
                    true
                }

                R.id.nav_add -> {
                    startActivity(Intent(this, AddTaskActivity::class.java))
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }
}