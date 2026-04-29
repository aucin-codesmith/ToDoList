package com.app.todolist.ui.task

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todolist.R
import com.app.todolist.adapter.TaskListAdapter
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.databinding.ActivityTaskListBinding
import com.app.todolist.ui.home.HomeActivity
import com.app.todolist.ui.profile.ProfileActivity
import com.app.todolist.ui.task.form.AddTaskActivity

class TaskListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskListBinding
    private lateinit var adapter: TaskListAdapter

    private var currentFilter = FilterType.ACTIVE
    private var searchQuery   = ""

    enum class FilterType { ACTIVE, COMPLETED, ALL }

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

    override fun onResume() {
        super.onResume()
        applyFilter()
    }

    private fun setupRecyclerView() {
        adapter = TaskListAdapter(
            tasks = mutableListOf(),
            onCheckedChange = { task, isChecked ->
                TaskRepository.updateTaskItemCompleted(task.id, isChecked)
                applyFilter()
            },
            onItemClick = { task -> openDetailTask(task.id) }
        )
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
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { startActivity(Intent(this, HomeActivity::class.java)); finish(); true }
                R.id.nav_tasks   -> true
                R.id.nav_add     -> { startActivity(Intent(this, AddTaskActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                else -> false
            }
        }
        binding.bottomNav.selectedItemId = R.id.nav_tasks
    }

    private fun openDetailTask(taskId: Int) {
        val task = TaskRepository.getTaskItemById(taskId) ?: return
        val intent = Intent(this, DetailTaskActivity::class.java).apply {
            putExtra(DetailTaskActivity.EXTRA_TASK_ID,        task.id)
            putExtra(DetailTaskActivity.EXTRA_TASK_TITLE,     task.title)
            putExtra(DetailTaskActivity.EXTRA_TASK_STATUS,    if (task.isCompleted) "selesai" else "belum_selesai")
            putExtra(DetailTaskActivity.EXTRA_TASK_KATEGORI,  task.category)
            putExtra(DetailTaskActivity.EXTRA_TASK_DEADLINE,  task.dateTime)
            putExtra(DetailTaskActivity.EXTRA_TASK_PRIORITAS, task.priority)
            putExtra(DetailTaskActivity.EXTRA_TASK_DESKRIPSI, task.description)
        }
        startActivity(intent)
    }

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
        val allTasks = TaskRepository.getTaskItems()
        val filtered = allTasks
            .filter { task ->
                when (currentFilter) {
                    FilterType.ACTIVE    -> !task.isCompleted
                    FilterType.COMPLETED ->  task.isCompleted
                    FilterType.ALL       -> true
                }
            }
            .filter { task ->
                searchQuery.isEmpty() || task.title.contains(searchQuery, ignoreCase = true)
            }

        adapter.updateTasks(filtered)
        val isEmpty = filtered.isEmpty()
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTasks.visibility     = if (isEmpty) View.GONE    else View.VISIBLE
    }
}