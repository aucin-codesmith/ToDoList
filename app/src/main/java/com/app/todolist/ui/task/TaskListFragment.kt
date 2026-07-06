package com.app.todolist.ui.task

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todolist.R
import com.app.todolist.adapter.TaskListAdapter
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.databinding.FragmentTaskListBinding
import com.app.todolist.model.TaskItem
import com.app.todolist.ui.task.form.AddTaskActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.app.todolist.util.SessionManager

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskListAdapter: TaskListAdapter

    private var allTasks: List<TaskItem> = emptyList()
    private var currentFilter: String = "aktif" // "aktif" | "selesai" | "semua"
    private var currentQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterTabs()
        setupSearch()
        setupClickListeners()
        // setupBottomNav() dihapus karena sudah diatur di MainActivity

        selectFilter("aktif")
        loadTasks()
    }

    override fun onResume() {
        super.onResume()
        if (::taskListAdapter.isInitialized) loadTasks()
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        taskListAdapter = TaskListAdapter(
            tasks = mutableListOf(),
            onCheckedChange = { task, isChecked ->
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    TaskRepository.updateTaskItemCompleted(requireContext(), task.id, isChecked)
                    withContext(Dispatchers.Main) { loadTasks() }
                }
            },
            onItemClick = { task ->
                openDetailTask(task)
            }
        )
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskListAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun openDetailTask(task: TaskItem) {
        val intent = Intent(requireContext(), DetailTaskActivity::class.java).apply {
            putExtra(DetailTaskActivity.EXTRA_TASK_ID, task.id)
            putExtra(DetailTaskActivity.EXTRA_TASK_TITLE, task.title)
            putExtra(
                DetailTaskActivity.EXTRA_TASK_STATUS,
                if (task.isCompleted) "selesai" else "belum_selesai"
            )
            putExtra(DetailTaskActivity.EXTRA_TASK_KATEGORI, task.category)
            putExtra(DetailTaskActivity.EXTRA_TASK_DEADLINE, task.dateTime)
            putExtra(DetailTaskActivity.EXTRA_TASK_PRIORITAS, task.priority)
            putExtra(DetailTaskActivity.EXTRA_TASK_DESKRIPSI, task.description)
        }
        startActivity(intent)
    }

    // ── Load & filter data ───────────────────────────────────────────────────

    private fun loadTasks() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val currentUserId = SessionManager.getUserId(requireContext())?.toString() ?: "0"
            val tasks = TaskRepository.getTaskItems(requireContext(), currentUserId)

            withContext(Dispatchers.Main) {
                allTasks = tasks
                applyFilterAndSearch()
            }
        }
    }

    private fun applyFilterAndSearch() {
        var result = when (currentFilter) {
            "aktif" -> allTasks.filter { !it.isCompleted }
            "selesai" -> allTasks.filter { it.isCompleted }
            else -> allTasks
        }
        if (currentQuery.isNotBlank()) {
            result = result.filter { it.title.contains(currentQuery, ignoreCase = true) }
        }

        taskListAdapter.updateTasks(result)

        val isEmpty = result.isEmpty()
        binding.rvTasks.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentQuery = s?.toString().orEmpty()
                applyFilterAndSearch()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ── Filter tabs (3 TextView manual) ──────────────────────────────────────

    private fun setupFilterTabs() {
        binding.btnFilterActive.setOnClickListener { selectFilter("aktif") }
        binding.btnFilterCompleted.setOnClickListener { selectFilter("selesai") }
        binding.btnFilterAll.setOnClickListener { selectFilter("semua") }
    }

    private fun selectFilter(filter: String) {
        currentFilter = filter
        val tabs = listOf(
            binding.btnFilterActive to "aktif",
            binding.btnFilterCompleted to "selesai",
            binding.btnFilterAll to "semua"
        )
        tabs.forEach { (tab, key) -> highlightFilterTab(tab, isSelected = key == filter) }
        applyFilterAndSearch()
    }

    private fun highlightFilterTab(tab: TextView, isSelected: Boolean) {
        // Menggunakan requireContext() agar tidak error di Fragment
        if (isSelected) {
            tab.setBackgroundResource(R.drawable.bg_chip_medium)
            tab.setTextColor(resources.getColor(R.color.on_surface, requireContext().theme))
        } else {
            tab.background = null
            tab.setTextColor(resources.getColor(R.color.on_surface_variant, requireContext().theme))
        }
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddTaskActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}