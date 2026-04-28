package com.app.todolist.ui.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.todolist.R
import com.app.todolist.databinding.ActivityAddTaskBinding
import com.app.todolist.ui.home.HomeActivity
import com.app.todolist.ui.tasklist.TaskListActivity
import java.util.Calendar

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding

    // State
    private var selectedPriority = "Sedang"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPriorityToggle()
        setupDeadlinePicker()
        setupSaveButton()
        setupBottomNav()

        // Set default priority
        selectPriority("Sedang")
    }
    // ── Priority Toggle ───────────────────────────────────────────────────────

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
            if (label == priority) {
                btn.setBackgroundResource(R.drawable.bg_filter_selected)
                btn.setTextColor(getColor(R.color.white))
            } else {
                btn.setBackgroundResource(0)
                btn.setTextColor(getColor(R.color.on_surface_variant))
            }
        }
    }


    // ── Deadline Picker ───────────────────────────────────────────────────────

    private fun setupDeadlinePicker() {
        binding.etDeadline.setOnClickListener { showDatePicker() }
        binding.tilDeadline.setEndIconOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                showTimePicker(year, month, day)
            },
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

    // ── Validasi & Simpan ─────────────────────────────────────────────────────

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveTask()
            }
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

        // TODO: Simpan ke database melalui ViewModel/Repository
        Toast.makeText(
            this,
            "Tugas \"$title\" disimpan! [$category · $selectedPriority]",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    private fun getSelectedCategory(): String {
        return when (binding.chipGroupCategory.checkedChipId) {
            R.id.chipPribadi -> "Pribadi"
            R.id.chipKerja   -> "Kerja"
            R.id.chipBelajar -> "Belajar"
            else             -> "Pribadi"
        }
    }

    // ── Bottom Nav ────────────────────────────────────────────────────────────

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_add

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
                R.id.nav_add     -> true
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}