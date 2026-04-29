package com.app.todolist.ui.task.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.todolist.R
import com.app.todolist.databinding.ActivityEditTaskBinding
import java.util.Calendar

class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTaskBinding

    // ─── Extras dari DetailTaskActivity ──────────────────────────────────────

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

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupPriorityToggle()
        setupDeadlinePicker()
        setupSaveButton()
        loadIntentData()
    }

    // ─── Toolbar ──────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish() // kembali ke DetailTaskActivity
        }
    }

    // ─── Load data dari Intent ────────────────────────────────────────────────

    private fun loadIntentData() {
        taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)

        val title     = intent.getStringExtra(EXTRA_TASK_TITLE)     ?: ""
        val deskripsi = intent.getStringExtra(EXTRA_TASK_DESKRIPSI) ?: ""
        val kategori  = intent.getStringExtra(EXTRA_TASK_KATEGORI)  ?: "Pribadi"
        val deadline  = intent.getStringExtra(EXTRA_TASK_DEADLINE)  ?: ""
        val prioritas = intent.getStringExtra(EXTRA_TASK_PRIORITAS) ?: "Sedang"

        // Isi form dengan data yang sudah ada
        binding.etTitle.setText(title)
        binding.etDesc.setText(deskripsi)
        binding.etDeadline.setText(deadline)

        // Set kategori chip yang sesuai
        val chipId = when (kategori.lowercase()) {
            "kerja"   -> R.id.chipKerja
            "belajar" -> R.id.chipBelajar
            else      -> R.id.chipPribadi
        }
        binding.chipGroupCategory.check(chipId)

        // Set prioritas yang sesuai
        selectPriority(prioritas)
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
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day -> showTimePicker(year, month, day) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(year: Int, month: Int, day: Int) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                val formatted = "%02d/%02d/%d %02d:%02d".format(day, month + 1, year, hour, minute)
                binding.etDeadline.setText(formatted)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
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
        val title    = binding.etTitle.text?.toString()?.trim().orEmpty()
        val desc     = binding.etDesc.text?.toString()?.trim().orEmpty()
        val deadline = binding.etDeadline.text?.toString()?.trim().orEmpty()
        val category = getSelectedCategory()

        // TODO: Update ke database melalui ViewModel/Repository menggunakan taskId
        Toast.makeText(
            this,
            "Tugas \"$title\" diperbarui! [$category · $selectedPriority]",
            Toast.LENGTH_SHORT
        ).show()

        finish() // kembali ke DetailTaskActivity
    }

    private fun getSelectedCategory(): String {
        return when (binding.chipGroupCategory.checkedChipId) {
            R.id.chipKerja   -> "Kerja"
            R.id.chipBelajar -> "Belajar"
            else             -> "Pribadi"
        }
    }
}