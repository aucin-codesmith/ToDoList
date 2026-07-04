package com.app.todolist.ui.task.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.todolist.R
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.databinding.ActivityEditTaskBinding
import com.app.todolist.model.TaskItem
import com.app.todolist.util.CategoryChipHelper
import com.app.todolist.util.TaskDateFormatter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTaskBinding

    // ─── Extras dari DetailTaskActivity (dipakai sebagai fallback / id lookup) ──

    companion object {
        const val EXTRA_TASK_ID        = "extra_task_id"
        const val EXTRA_TASK_TITLE     = "extra_task_title"
        const val EXTRA_TASK_DESKRIPSI = "extra_task_deskripsi"
        const val EXTRA_TASK_KATEGORI  = "extra_task_kategori"
        const val EXTRA_TASK_DEADLINE  = "extra_task_deadline"
        const val EXTRA_TASK_PRIORITAS = "extra_task_prioritas"
    }

    private var taskId: Int = -1
    private var selectedPriority = "Sedang"

    // Data asli dari Room — jadi basis saat menyimpan perubahan (assigneeTag,
    // isCompleted, dll ikut dipertahankan walau tidak ada field-nya di form ini)
    private var originalTask: TaskItem? = null

    // Diisi hanya kalau user benar-benar ganti deadline lewat date/time picker.
    // Kalau null saat simpan, deadline lama (originalTask.date/dateTime) dipakai apa adanya.
    private var selectedDeadlineCalendar: Calendar? = null

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)

        setupToolbar()
        setupPriorityToggle()
        setupDeadlinePicker()
        setupSaveButton()
        CategoryChipHelper.setupAddCategoryChip(this, binding.chipGroupCategory, binding.chipTambah)
        loadTaskData()
    }

    // ─── Toolbar ──────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish() // kembali ke DetailTaskActivity
        }
    }

    // ─── Load data asli dari Room ─────────────────────────────────────────────

    private fun loadTaskData() {
        if (taskId == -1) {
            Toast.makeText(this, "Tugas tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnSave.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            val task = TaskRepository.getTaskItemById(applicationContext, taskId)

            withContext(Dispatchers.Main) {
                binding.btnSave.isEnabled = true

                if (task == null) {
                    Toast.makeText(
                        this@EditTaskActivity,
                        "Tugas sudah tidak tersedia (mungkin sudah dihapus)",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@withContext
                }

                originalTask = task

                // Isi form dengan data asli
                binding.etTitle.setText(task.title)
                binding.etDesc.setText(task.description)
                binding.etDeadline.setText(task.dateTime)

                val builtInChipId = when (task.category.lowercase()) {
                    "kerja"   -> R.id.chipKerja
                    "belajar" -> R.id.chipBelajar
                    "pribadi" -> R.id.chipPribadi
                    else      -> null
                }
                if (builtInChipId != null) {
                    binding.chipGroupCategory.check(builtInChipId)
                } else {
                    // Kategori task ini kustom (bukan Pribadi/Kerja/Belajar bawaan) —
                    // tambahkan sebagai chip baru supaya kelihatan & tetap terpilih
                    CategoryChipHelper.addOrSelectChip(
                        binding.chipGroupCategory, binding.chipTambah, task.category
                    )
                }

                selectPriority(task.priority)
            }
        }
    }

    // ─── Priority Toggle ──────────────────────────────────────────────────────

    private fun setupPriorityToggle() {
        binding.btnPriorityLow.setOnClickListener  { selectPriority("Rendah") }
        binding.btnPriorityMed.setOnClickListener  { selectPriority("Sedang") }
        binding.btnPriorityHigh.setOnClickListener { selectPriority("Tinggi") }
    }

    private fun selectPriority(priority: String) {
        selectedPriority = priority

        val buttons = listOf(
            binding.btnPriorityLow  to "Rendah",
            binding.btnPriorityMed  to "Sedang",
            binding.btnPriorityHigh to "Tinggi"
        )

        buttons.forEach { (btn, label) ->
            val textView = btn as TextView
            if (label == priority) {
                textView.setBackgroundResource(R.drawable.bg_filter_selected)
                textView.setTextColor(getColor(R.color.white))
            } else {
                textView.setBackgroundResource(0)
                textView.setTextColor(getColor(R.color.on_surface_variant))
            }
        }
    }

    // ─── Deadline Picker ──────────────────────────────────────────────────────

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
                selectedDeadlineCalendar = picked
                binding.etDeadline.setText(TaskDateFormatter.formatDeadlineDisplay(picked))
                binding.tilDeadline.error = null
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    // ─── Validasi & Simpan ────────────────────────────────────────────────────

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) saveTask()
        }
    }

    private fun validateInputs(): Boolean {
        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        if (title.isEmpty()) {
            binding.tilTitle.error = "Judul tugas tidak boleh kosong"
            return false
        }
        binding.tilTitle.error = null
        return true
    }

    private fun saveTask() {
        val original = originalTask
        if (original == null) {
            Toast.makeText(this, "Data tugas belum siap, coba lagi sebentar", Toast.LENGTH_SHORT).show()
            return
        }

        val title    = binding.etTitle.text?.toString()?.trim().orEmpty()
        val desc     = binding.etDesc.text?.toString()?.trim().orEmpty()
        val category = getSelectedCategory()

        // Kalau user tidak ganti deadline (selectedDeadlineCalendar null),
        // pertahankan date/dateTime yang lama apa adanya.
        val newDate = selectedDeadlineCalendar?.let { TaskDateFormatter.formatDateShort(it) }
            ?: original.date
        val newDateTime = selectedDeadlineCalendar?.let { TaskDateFormatter.formatDateTimeRelative(it) }
            ?: original.dateTime

        val updated = original.copy(
            title = title,
            description = desc,
            category = category,
            priority = selectedPriority,
            date = newDate,
            dateTime = newDateTime
        )

        binding.btnSave.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            TaskRepository.updateTaskItem(applicationContext, updated)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditTaskActivity, "Tugas berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish() // kembali ke DetailTaskActivity
            }
        }
    }

    private fun getSelectedCategory(): String {
        val checkedId = binding.chipGroupCategory.checkedChipId
        val chip = binding.chipGroupCategory.findViewById<Chip>(checkedId)
        return chip?.text?.toString() ?: "Pribadi"
    }
}