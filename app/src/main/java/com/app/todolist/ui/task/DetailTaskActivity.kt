package com.app.todolist.ui.task

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.app.todolist.R

class DetailTaskActivity : AppCompatActivity() {

    // ─── Views ────────────────────────────────────────────────────────────────

    private lateinit var toolbar: Toolbar
    private lateinit var tvTaskTitle: TextView
    private lateinit var chipStatus: Chip
    private lateinit var tvKategori: TextView
    private lateinit var tvDeadline: TextView
    private lateinit var tvPrioritas: TextView
    private lateinit var tvDeskripsi: TextView
    private lateinit var btnTandaiSelesai: MaterialButton
    private lateinit var btnEdit: MaterialButton
    private lateinit var btnHapus: MaterialButton

    // ─── Data (passed via Intent extras) ─────────────────────────────────────

    companion object {
        const val EXTRA_TASK_ID       = "extra_task_id"
        const val EXTRA_TASK_TITLE    = "extra_task_title"
        const val EXTRA_TASK_STATUS   = "extra_task_status"   // "selesai" | "belum_selesai"
        const val EXTRA_TASK_KATEGORI = "extra_task_kategori"
        const val EXTRA_TASK_DEADLINE = "extra_task_deadline"
        const val EXTRA_TASK_PRIORITAS = "extra_task_prioritas" // "tinggi" | "sedang" | "rendah"
        const val EXTRA_TASK_DESKRIPSI = "extra_task_deskripsi"
    }

    private var taskId: Int = -1
    private var isCompleted: Boolean = false

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_task)

        initViews()
        setupToolbar()
        loadIntentData()
        setupClickListeners()
    }

    // ─── Init ─────────────────────────────────────────────────────────────────

    private fun initViews() {
        toolbar           = findViewById(R.id.toolbar)
        tvTaskTitle       = findViewById(R.id.tv_task_title)
        chipStatus        = findViewById(R.id.chip_status)
        tvKategori        = findViewById(R.id.tv_kategori)
        tvDeadline        = findViewById(R.id.tv_deadline)
        tvPrioritas       = findViewById(R.id.tv_prioritas)
        tvDeskripsi       = findViewById(R.id.tv_deskripsi)
        btnTandaiSelesai  = findViewById(R.id.btn_tandai_selesai)
        btnEdit           = findViewById(R.id.btn_edit)
        btnHapus          = findViewById(R.id.btn_hapus)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    // ─── Load data from Intent ────────────────────────────────────────────────

    private fun loadIntentData() {
        taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)

        val title     = intent.getStringExtra(EXTRA_TASK_TITLE)     ?: "Tanpa Judul"
        val status    = intent.getStringExtra(EXTRA_TASK_STATUS)    ?: "belum_selesai"
        val kategori  = intent.getStringExtra(EXTRA_TASK_KATEGORI)  ?: "-"
        val deadline  = intent.getStringExtra(EXTRA_TASK_DEADLINE)  ?: "-"
        val prioritas = intent.getStringExtra(EXTRA_TASK_PRIORITAS) ?: "-"
        val deskripsi = intent.getStringExtra(EXTRA_TASK_DESKRIPSI) ?: "-"

        tvTaskTitle.text   = title
        tvKategori.text    = kategori
        tvDeadline.text    = deadline
        tvPrioritas.text   = prioritas.replaceFirstChar { it.uppercase() }
        tvDeskripsi.text   = deskripsi

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
            btnTandaiSelesai.setIconResource(R.drawable.ic_check_circle)
        } else {
            chipStatus.text = "Belum Selesai"
            chipStatus.setChipBackgroundColorResource(R.color.primary_fixed)
            chipStatus.setTextColor(getColor(R.color.primary))
            btnTandaiSelesai.text = "Tandai Selesai"
            btnTandaiSelesai.setIconResource(R.drawable.ic_check_circle)
        }
    }

    // ─── Click Listeners ──────────────────────────────────────────────────────

    private fun setupClickListeners() {
        btnTandaiSelesai.setOnClickListener {
            toggleTaskStatus()
        }

        btnEdit.setOnClickListener {
            onEditClicked()
        }

        btnHapus.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    private fun toggleTaskStatus() {
        isCompleted = !isCompleted
        val newStatus = if (isCompleted) "selesai" else "belum_selesai"
        applyStatus(newStatus)

        // TODO: Update status in your repository / ViewModel
        val message = if (isCompleted) "Tugas ditandai selesai" else "Tugas ditandai belum selesai"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun onEditClicked() {
        // TODO: Navigate to EditTaskActivity and pass taskId
        // Example:
        // val intent = Intent(this, EditTaskActivity::class.java)
        // intent.putExtra(EditTaskActivity.EXTRA_TASK_ID, taskId)
        // startActivity(intent)
        Toast.makeText(this, "Edit tugas: $taskId", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Tugas")
            .setMessage("Apakah kamu yakin ingin menghapus tugas ini? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                deleteTask()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteTask() {
        // TODO: Delete task from repository / ViewModel, then finish
        Toast.makeText(this, "Tugas dihapus", Toast.LENGTH_SHORT).show()
        finish()
    }
}