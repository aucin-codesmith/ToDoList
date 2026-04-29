package com.app.todolist.ui.notification

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.todolist.R
import com.app.todolist.databinding.ActivityDetailNotificationBinding
import com.app.todolist.ui.task.DetailTaskActivity

class DetailNotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailNotificationBinding

    // ── Extras ────────────────────────────────────────────────────────────────

    companion object {
        const val EXTRA_NOTIF_ID          = "extra_notif_id"
        const val EXTRA_NOTIF_TITLE       = "extra_notif_title"
        const val EXTRA_NOTIF_BODY        = "extra_notif_body"
        const val EXTRA_NOTIF_BODY_HL     = "extra_notif_body_highlight"
        const val EXTRA_NOTIF_TIME        = "extra_notif_time"
        const val EXTRA_NOTIF_CATEGORY    = "extra_notif_category"
        const val EXTRA_TASK_CATEGORY     = "extra_task_category"
        const val EXTRA_TASK_DEADLINE     = "extra_task_deadline"
        const val EXTRA_TASK_PRIORITY     = "extra_task_priority"
        const val EXTRA_HAS_TASK          = "extra_has_task"
        const val EXTRA_TASK_ID           = "extra_task_id"
        // Extras tambahan untuk pass ke DetailTaskActivity
        const val EXTRA_TASK_TITLE        = "extra_task_title"
        const val EXTRA_TASK_DESKRIPSI    = "extra_task_deskripsi"
        const val EXTRA_TASK_STATUS       = "extra_task_status"
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadIntentData()
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }


    // ── Load data ─────────────────────────────────────────────────────────────

    private fun loadIntentData() {
        val title       = intent.getStringExtra(EXTRA_NOTIF_TITLE)    ?: ""
        val body        = intent.getStringExtra(EXTRA_NOTIF_BODY)     ?: ""
        val bodyHl      = intent.getStringExtra(EXTRA_NOTIF_BODY_HL)  ?: ""
        val time        = intent.getStringExtra(EXTRA_NOTIF_TIME)     ?: ""
        val category    = intent.getStringExtra(EXTRA_NOTIF_CATEGORY) ?: "-"
        val taskCat     = intent.getStringExtra(EXTRA_TASK_CATEGORY)  ?: "-"
        val taskDl      = intent.getStringExtra(EXTRA_TASK_DEADLINE)  ?: "-"
        val taskPrio    = intent.getStringExtra(EXTRA_TASK_PRIORITY)  ?: "-"
        val hasTask     = intent.getBooleanExtra(EXTRA_HAS_TASK, false)
        val taskId      = intent.getIntExtra(EXTRA_TASK_ID, -1)
        val taskTitle   = intent.getStringExtra(EXTRA_TASK_TITLE)     ?: title
        val taskDesk    = intent.getStringExtra(EXTRA_TASK_DESKRIPSI) ?: "-"
        val taskStatus  = intent.getStringExtra(EXTRA_TASK_STATUS)    ?: "belum_selesai"

        binding.tvNotifTitle.text       = title
        binding.tvTime.text             = time
        binding.tvCategory.text         = category
        binding.tvKategoriValue.text    = taskCat
        binding.tvDeadlineValue.text    = taskDl
        binding.tvPrioritasValue.text   = taskPrio.replaceFirstChar { it.uppercase() }

        // Body dengan highlight warna primary
        setBodyWithHighlight(body, bodyHl)

        // Sembunyikan info task & tombol jika notif tidak punya task terkait
        if (!hasTask) {
            binding.cardKategori.visibility  = View.GONE
            binding.cardDeadline.visibility  = View.GONE
            binding.cardPrioritas.visibility = View.GONE
            binding.btnLihatTugas.visibility = View.GONE
        } else {
            binding.btnLihatTugas.setOnClickListener {
                navigateToTask(
                    taskId       = taskId,
                    taskTitle    = taskTitle,
                    taskKategori = taskCat,
                    taskDeadline = taskDl,
                    taskPrioritas = taskPrio,
                    taskDeskripsi = taskDesk,
                    taskStatus   = taskStatus
                )
            }
        }
    }

    // ── Highlight teks tertentu dalam body ────────────────────────────────────

    private fun setBodyWithHighlight(body: String, highlight: String) {
        if (highlight.isEmpty() || !body.contains(highlight)) {
            binding.tvNotifBody.text = body
            return
        }

        val primaryColor = ContextCompat.getColor(this, R.color.primary)
        val spannable = SpannableString(body)
        val start = body.indexOf(highlight)
        val end   = start + highlight.length

        spannable.setSpan(
            ForegroundColorSpan(primaryColor),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvNotifBody.text = spannable
    }

    // ── Navigate ke DetailTaskActivity ────────────────────────────────────────

    private fun navigateToTask(
        taskId: Int,
        taskTitle: String,
        taskKategori: String,
        taskDeadline: String,
        taskPrioritas: String,
        taskDeskripsi: String,
        taskStatus: String
    ) {
        val intent = Intent(this, DetailTaskActivity::class.java).apply {
            putExtra(DetailTaskActivity.EXTRA_TASK_ID,        taskId)
            putExtra(DetailTaskActivity.EXTRA_TASK_TITLE,     taskTitle)
            putExtra(DetailTaskActivity.EXTRA_TASK_STATUS,    taskStatus)
            putExtra(DetailTaskActivity.EXTRA_TASK_KATEGORI,  taskKategori)
            putExtra(DetailTaskActivity.EXTRA_TASK_DEADLINE,  taskDeadline)
            putExtra(DetailTaskActivity.EXTRA_TASK_PRIORITAS, taskPrioritas)
            putExtra(DetailTaskActivity.EXTRA_TASK_DESKRIPSI, taskDeskripsi)
        }
        startActivity(intent)
    }

    // ── Handle hardware/gesture back button ───────────────────────────────────

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, NotificationListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }
}