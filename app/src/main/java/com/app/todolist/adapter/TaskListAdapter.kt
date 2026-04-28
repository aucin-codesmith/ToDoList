package com.app.todolist.adapter

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.todolist.R
import com.app.todolist.databinding.ItemTaskListBinding
import com.app.todolist.model.TaskItem

class TaskListAdapter(
    private val tasks: MutableList<TaskItem>,
    private val onCheckedChange: (TaskItem, Boolean) -> Unit
) : RecyclerView.Adapter<TaskListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTaskListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        val ctx = holder.itemView.context

        with(holder.binding) {
            tvTitle.text    = task.title
            tvDateTime.text = task.dateTime
            tvCategory.text = task.category

            // ── Category chip ─────────────────────────────────────────────────
            val (bgColor, textColor) = getCategoryColors(task.category, ctx)
            val chipBg = GradientDrawable().apply {
                shape        = GradientDrawable.RECTANGLE
                cornerRadius = ctx.resources.displayMetrics.density * 20f
                setColor(bgColor)
            }
            tvCategory.background = chipBg
            tvCategory.setTextColor(textColor)

            // ── Assignee tag ──────────────────────────────────────────────────
            if (task.assigneeTag != null) {
                tvTag.visibility = View.VISIBLE
                tvTag.text       = task.assigneeTag
            } else {
                tvTag.visibility = View.GONE
            }

            // ── Completed state ───────────────────────────────────────────────
            val alpha = if (task.isCompleted) 0.45f else 1f
            if (task.isCompleted) {
                tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            tvTitle.alpha    = alpha
            tvCategory.alpha = alpha
            tvDateTime.alpha = alpha

            // ── Checkbox ──────────────────────────────────────────────────────
            cbTask.setOnCheckedChangeListener(null)
            cbTask.isChecked = task.isCompleted
            cbTask.setOnCheckedChangeListener { _, isChecked ->
                task.isCompleted = isChecked
                onCheckedChange(task, isChecked)
                notifyItemChanged(position)
            }
        }
    }

    private fun getCategoryColors(
        category: String,
        ctx: android.content.Context
    ): Pair<Int, Int> = when (category.lowercase()) {
        "work" -> Pair(
            ContextCompat.getColor(ctx, R.color.primary_fixed),
            ContextCompat.getColor(ctx, R.color.primary)
        )
        "management" -> Pair(
            ContextCompat.getColor(ctx, R.color.secondary_fixed),
            ContextCompat.getColor(ctx, R.color.secondary)
        )
        "design" -> Pair(
            ContextCompat.getColor(ctx, R.color.tertiary_fixed),
            ContextCompat.getColor(ctx, R.color.tertiary)
        )
        "development" -> Pair(
            ContextCompat.getColor(ctx, R.color.primary_fixed_dim),
            ContextCompat.getColor(ctx, R.color.on_primary_fixed_variant)
        )
        else -> Pair(
            ContextCompat.getColor(ctx, R.color.surface_container_high),
            ContextCompat.getColor(ctx, R.color.on_surface_variant)
        )
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<TaskItem>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}