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
import com.app.todolist.R
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.databinding.ActivityAddTaskBinding
import com.app.todolist.model.TaskItem
import com.app.todolist.ui.home.HomeActivity
import com.app.todolist.ui.profile.ProfileActivity
import com.app.todolist.ui.task.TaskListActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding

    private val monthAbbrev = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Ags", "Sep", "Okt", "Nov", "Des"
    )

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

    // ── Kategori chip ("+" belum didukung, hanya placeholder) ───────────────────

    private fun setupCategoryChips() {
        binding.chipTambah.setOnClickListener {
            Toast.makeText(this, "Kategori kustom belum tersedia", Toast.LENGTH_SHORT).show()
            binding.chipPribadi.isChecked = true
        }
    }

    private fun getSelectedCategory(): String {
        return when (binding.chipGroupCategory.checkedChipId) {
            binding.chipKerja.id -> "Kerja"
            binding.chipBelajar.id -> "Belajar"
            else -> "Pribadi"
        }
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
                binding.etDeadline.setText(formatDeadlineDisplay(picked))
                binding.tilDeadline.error = null
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun formatDeadlineDisplay(cal: Calendar): String {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = monthAbbrev[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)
        val time = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        return "%02d %s %d, %s".format(day, month, year, time)
    }

    /** Format "date" pendek buat HomeActivity, contoh: "29 Apr, 2026" */
    private fun formatDateShort(cal: Calendar): String {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = monthAbbrev[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)
        return "%02d %s, %d".format(day, month, year)
    }

    /** Format "dateTime" relatif buat TaskListActivity, contoh: "Hari ini, 14:00" */
    private fun formatDateTimeRelative(cal: Calendar): String {
        val today = Calendar.getInstance()
        val diffDays = daysBetween(today, cal)
        val time = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

        val label = when (diffDays) {
            0 -> "Hari ini"
            1 -> "Besok"
            2 -> "Lusa"
            else -> {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val month = monthAbbrev[cal.get(Calendar.MONTH)]
                "%02d %s".format(day, month)
            }
        }
        return "$label, $time"
    }

    private fun daysBetween(from: Calendar, to: Calendar): Int {
        val start = Calendar.getInstance().apply {
            set(from.get(Calendar.YEAR), from.get(Calendar.MONTH), from.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        }
        val end = Calendar.getInstance().apply {
            set(to.get(Calendar.YEAR), to.get(Calendar.MONTH), to.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        }
        val diffMillis = end.timeInMillis - start.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
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

        val newTask = TaskItem(
            id = 0, // diabaikan, Room yang generate id
            title = title,
            category = getSelectedCategory(),
            description = description,
            dateTime = formatDateTimeRelative(deadline),
            date = formatDateShort(deadline),
            priority = selectedPriority,
            assigneeTag = null,
            isCompleted = false
        )

        binding.btnSave.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            TaskRepository.addTaskItem(applicationContext, newTask)
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
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); finish(); true }
                R.id.nav_tasks -> { startActivity(Intent(this, TaskListActivity::class.java)); finish(); true }
                R.id.nav_add -> true
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); finish(); true }
                else -> false
            }
        }
    }
}