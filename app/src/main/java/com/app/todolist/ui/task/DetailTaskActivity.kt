package com.app.todolist.ui.task

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import android.widget.TextView
import com.app.todolist.R
import com.app.todolist.data.repository.TaskRepository
import com.app.todolist.ui.task.form.EditTaskActivity
import com.app.todolist.util.ReminderScheduler
import kotlinx.coroutines.launch

class DetailTaskActivity : AppCompatActivity() {

    // ─── Views ────────────────────────────────────────────────────────────────

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvTaskTitle: TextView
    private lateinit var chipStatus: Chip
    private lateinit var tvKategori: TextView
    private lateinit var tvDeadline: TextView
    private lateinit var tvPrioritas: TextView
    private lateinit var tvDeskripsi: TextView
    private lateinit var btnTandaiSelesai: MaterialButton
    private lateinit var btnEdit: MaterialButton
    private lateinit var btnHapus: MaterialButton

    // ─── Extras ───────────────────────────────────────────────────────────────

    companion object {
        const val EXTRA_TASK_ID        = "extra_task_id"
        const val EXTRA_TASK_TITLE     = "extra_task_title"
        const val EXTRA_TASK_STATUS    = "extra_task_status"    // "selesai" | "belum_selesai"
        const val EXTRA_TASK_KATEGORI  = "extra_task_kategori"
        const val EXTRA_TASK_DEADLINE  = "extra_task_deadline"
        const val EXTRA_TASK_PRIORITAS = "extra_task_prioritas"
        const val EXTRA_TASK_DESKRIPSI = "extra_task_deskripsi"
    }

    private var taskId: Int = -1
    private var isCompleted: Boolean = false

    // Data task untuk diteruskan ke EditTaskActivity
    private var currentTitle     = ""
    private var currentKategori  = ""
    private var currentDeadline  = ""
    private var currentPrioritas = ""
    private var currentDeskripsi = ""
    private var currentDeadlineMillis: Long = 0L

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_task)

        initViews()
        setupToolbar()
        loadIntentData()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Reload dari repository agar perubahan dari EditTaskActivity langsung terlihat
        if (taskId != -1) {
            lifecycleScope.launch {
                TaskRepository.getTaskItemById(this@DetailTaskActivity, taskId)?.let { task ->
                    currentTitle     = task.title
                    currentKategori  = task.category
                    currentDeadline  = task.dateTime
                    currentPrioritas = task.priority
                    currentDeskripsi = task.description
                    isCompleted      = task.isCompleted
                    currentDeadlineMillis = task.deadlineMillis

                    tvTaskTitle.text = currentTitle
                    tvKategori.text  = currentKategori
                    tvDeadline.text  = currentDeadline
                    tvPrioritas.text = currentPrioritas.replaceFirstChar { it.uppercase() }
                    tvDeskripsi.text = currentDeskripsi
                    applyStatus(if (isCompleted) "selesai" else "belum_selesai")
                }
            }
        }
    }

    // ─── Init ─────────────────────────────────────────────────────────────────

    private fun initViews() {
        toolbar          = findViewById(R.id.toolbar)
        tvTaskTitle      = findViewById(R.id.tv_task_title)
        chipStatus       = findViewById(R.id.chip_status)
        tvKategori       = findViewById(R.id.tv_kategori)
        tvDeadline       = findViewById(R.id.tv_deadline)
        tvPrioritas      = findViewById(R.id.tv_prioritas)
        tvDeskripsi      = findViewById(R.id.tv_deskripsi)
        btnTandaiSelesai = findViewById(R.id.btn_tandai_selesai)
        btnEdit          = findViewById(R.id.btn_edit)
        btnHapus         = findViewById(R.id.btn_hapus)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
    }

    // ─── Load data ────────────────────────────────────────────────────────────

    private fun loadIntentData() {
        taskId           = intent.getIntExtra(EXTRA_TASK_ID, -1)
        currentTitle     = intent.getStringExtra(EXTRA_TASK_TITLE)     ?: "Tanpa Judul"
        val status       = intent.getStringExtra(EXTRA_TASK_STATUS)    ?: "belum_selesai"
        currentKategori  = intent.getStringExtra(EXTRA_TASK_KATEGORI)  ?: "-"
        currentDeadline  = intent.getStringExtra(EXTRA_TASK_DEADLINE)  ?: "-"
        currentPrioritas = intent.getStringExtra(EXTRA_TASK_PRIORITAS) ?: "-"
        currentDeskripsi = intent.getStringExtra(EXTRA_TASK_DESKRIPSI) ?: "-"

        tvTaskTitle.text = currentTitle
        tvKategori.text  = currentKategori
        tvDeadline.text  = currentDeadline
        tvPrioritas.text = currentPrioritas.replaceFirstChar { it.uppercase() }
        tvDeskripsi.text = currentDeskripsi

        applyStatus(status)
    }

    // ─── Status UI ────────────────────────────────────────────────────────────

    private fun applyStatus(status: String) {
        isCompleted = status == "selesai"
        if (isCompleted) {
            chipStatus.text = "Selesai"
            chipStatus.setChipBackgroundColorResource(R.color.secondary_fixed)
            chipStatus.setTextColor(getColor(R.color.secondary))
            btnTandaiSelesai.text = "Tandai Belum Selesai"
        } else {
            chipStatus.text = "Belum Selesai"
            chipStatus.setChipBackgroundColorResource(R.color.primary_fixed)
            chipStatus.setTextColor(getColor(R.color.primary))
            btnTandaiSelesai.text = "Tandai Selesai"
        }
        btnTandaiSelesai.setIconResource(R.drawable.ic_check_circle)
    }

    // ─── Click Listeners ──────────────────────────────────────────────────────

    private fun setupClickListeners() {
        btnTandaiSelesai.setOnClickListener { toggleTaskStatus() }
        btnEdit.setOnClickListener          { openEditTask() }
        btnHapus.setOnClickListener         { showDeleteConfirmationDialog() }
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    private fun toggleTaskStatus() {
        isCompleted = !isCompleted
        // Langsung update di repository agar semua Activity mendapat data terbaru
        lifecycleScope.launch {
            TaskRepository.updateTaskItemCompleted(this@DetailTaskActivity, taskId, isCompleted)
            if (isCompleted) {
                // Tugas selesai — tidak perlu diingatkan lagi
                ReminderScheduler.cancelReminder(this@DetailTaskActivity, taskId)
            } else {
                // Dibalik jadi belum selesai — jadwalkan lagi kalau deadline-nya masih di masa depan
                ReminderScheduler.scheduleReminder(this@DetailTaskActivity, taskId, currentDeadlineMillis)
            }
        }
        applyStatus(if (isCompleted) "selesai" else "belum_selesai")
        val message = if (isCompleted) "Tugas ditandai selesai" else "Tugas ditandai belum selesai"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun openEditTask() {
        val intent = Intent(this, EditTaskActivity::class.java).apply {
            putExtra(EditTaskActivity.EXTRA_TASK_ID,        taskId)
            putExtra(EditTaskActivity.EXTRA_TASK_TITLE,     currentTitle)
            putExtra(EditTaskActivity.EXTRA_TASK_DESKRIPSI, currentDeskripsi)
            putExtra(EditTaskActivity.EXTRA_TASK_KATEGORI,  currentKategori)
            putExtra(EditTaskActivity.EXTRA_TASK_DEADLINE,  currentDeadline)
            putExtra(EditTaskActivity.EXTRA_TASK_PRIORITAS, currentPrioritas)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Tugas")
            .setMessage("Apakah kamu yakin ingin menghapus tugas ini? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ -> deleteTask() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteTask() {
        // Hapus dari repository agar data konsisten di semua Activity
        lifecycleScope.launch {
            TaskRepository.deleteTaskItem(this@DetailTaskActivity, taskId)
            ReminderScheduler.cancelReminder(this@DetailTaskActivity, taskId)
            Toast.makeText(this@DetailTaskActivity, "Tugas dihapus", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}