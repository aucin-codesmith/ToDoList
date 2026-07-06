package com.app.todolist.ui.task.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.app.todolist.MainActivity
import com.app.todolist.R
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.databinding.ActivityAddTaskBinding
import com.app.todolist.model.TaskItem
import com.app.todolist.util.CategoryChipHelper
import com.app.todolist.util.ReminderScheduler
import com.app.todolist.util.TaskDateFormatter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import com.app.todolist.util.SessionManager

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding

    private var selectedDeadline: Calendar? = null
    private var selectedPriority: String = "Sedang" // default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPriorityToggle()
        setupCategoryChips()
        setupDeadlinePicker()
        setupBottomNav()

        binding.btnSave.setOnClickListener {
            onSaveClicked()
        }
    }

    // ── Prioritas toggle (3 TextView manual, bukan RadioGroup) ──────────────────

    private fun setupPriorityToggle() {
        binding.btnPriorityLow.setOnClickListener { selectPriority("Rendah") }
        binding.btnPriorityMed.setOnClickListener { selectPriority("Sedang") }
        binding.btnPriorityHigh.setOnClickListener { selectPriority("Tinggi") }
        selectPriority(selectedPriority) // set default visual state
    }

    private fun selectPriority(priority: String) {
        selectedPriority = priority
        val buttons = listOf(
            binding.btnPriorityLow to "Rendah",
            binding.btnPriorityMed to "Sedang",
            binding.btnPriorityHigh to "Tinggi"
        )
        buttons.forEach { (button, label) ->
            highlightPriorityButton(button, isSelected = label == priority)
        }
    }

    private fun highlightPriorityButton(button: TextView, isSelected: Boolean) {
        if (isSelected) {
            val pill = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = resources.displayMetrics.density * 16f
                setColor(ContextCompat.getColor(this@AddTaskActivity, R.color.primary))
            }
            button.background = pill
            button.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            button.background = null
            button.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant))
        }
    }

    // ── Kategori chip (termasuk kategori kustom lewat chip "+") ─────────────────

    private fun setupCategoryChips() {
        CategoryChipHelper.setupAddCategoryChip(this, binding.chipGroupCategory, binding.chipTambah)
    }

    private fun getSelectedCategory(): String {
        val checkedId = binding.chipGroupCategory.checkedChipId
        val chip = binding.chipGroupCategory.findViewById<Chip>(checkedId)
        return chip?.text?.toString() ?: "Pribadi"
    }

    // ── Deadline picker (Date lalu Time) ────────────────────────────────────────

    private fun setupDeadlinePicker() {
        binding.etDeadline.setOnClickListener { showDatePicker() }
        binding.tilDeadline.setEndIconOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                showTimePicker(picked)
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(picked: Calendar) {
        val now = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                picked.set(Calendar.HOUR_OF_DAY, hour)
                picked.set(Calendar.MINUTE, minute)
                selectedDeadline = picked
                binding.etDeadline.setText(TaskDateFormatter.formatDeadlineDisplay(picked))
                binding.tilDeadline.error = null
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    // ── Simpan ───────────────────────────────────────────────────────────────

    private fun onSaveClicked() {
        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val description = binding.etDesc.text?.toString()?.trim().orEmpty()
        val deadline = selectedDeadline

        binding.tilTitle.error = null
        binding.tilDeadline.error = null

        var isValid = true
        if (title.isEmpty()) {
            binding.tilTitle.error = "Judul tugas tidak boleh kosong"
            isValid = false
        }
        if (deadline == null) {
            binding.tilDeadline.error = "Pilih deadline terlebih dahulu"
            isValid = false
        }
        if (!isValid || deadline == null) return

        // --- TAMBAHKAN BARIS INI UNTUK AMBIL ID USER ---
        val currentUserId = SessionManager.getUserId(this)?.toString() ?: "0"

        val newTask = TaskItem(
            id = 0,
            userId = currentUserId, // <--- SISIPKAN DI SINI
            title = title,
            category = getSelectedCategory(),
            description = description,
            dateTime = TaskDateFormatter.formatDateTimeRelative(deadline),
            date = TaskDateFormatter.formatDateShort(deadline),
            priority = selectedPriority,
            assigneeTag = null,
            isCompleted = false,
            deadlineMillis = deadline.timeInMillis
        )

        binding.btnSave.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            val newId = TaskRepository.addTaskItem(applicationContext, newTask)
            ReminderScheduler.scheduleReminder(applicationContext, newId, deadline.timeInMillis)
            runOnUiThread {
                Toast.makeText(this@AddTaskActivity, "Tugas berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // ── Bottom Nav ────────────────────────────────────────────────────────────

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_add

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { goToMainTab(R.id.nav_home); true }
                R.id.nav_tasks -> { goToMainTab(R.id.nav_tasks); true }
                R.id.nav_add -> true
                R.id.nav_profile -> { goToMainTab(R.id.nav_profile); true }
                else -> false
            }
        }
    }

    /**
     * Balik ke MainActivity (yang sudah ada di back stack di bawah AddTaskActivity ini)
     * dan minta dia menampilkan tab tertentu. FLAG_ACTIVITY_CLEAR_TOP + SINGLE_TOP
     * memastikan instance MainActivity yang sudah ada dipakai ulang (lewat onNewIntent),
     * bukan bikin instance baru.
     */
    private fun goToMainTab(tabId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_SELECTED_TAB, tabId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }
}