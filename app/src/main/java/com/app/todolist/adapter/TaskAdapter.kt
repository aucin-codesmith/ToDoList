package com.app.todolist.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.todolist.R
import com.app.todolist.databinding.ItemTaskBinding
import com.app.todolist.model.Priority
import com.app.todolist.model.Task

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onCheckedChange: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        with(holder.binding) {

            tvTaskTitle.text = task.title
            tvTaskDate.text  = task.date

            // ── Strikethrough + dim if completed ──
            if (task.isCompleted) {
                tvTaskTitle.paintFlags = tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvTaskTitle.alpha = 0.45f
            } else {
                tvTaskTitle.paintFlags = tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvTaskTitle.alpha = 1f
            }

            // ── Priority chip ──
            val (bgRes, colorRes, label) = when (task.priority) {
                Priority.TINGGI -> Triple(R.drawable.bg_chip_high,   R.color.priority_high,   "TINGGI")
                Priority.MEDIUM -> Triple(R.drawable.bg_chip_medium, R.color.priority_medium, "MEDIUM")
                Priority.RENDAH -> Triple(R.drawable.bg_chip_low,    R.color.priority_low,    "RENDAH")
            }
            tvPriority.text       = label
            tvPriority.background = ContextCompat.getDrawable(root.context, bgRes)
            tvPriority.setTextColor(ContextCompat.getColor(root.context, colorRes))

            // ── Checkbox (remove old listener first to avoid loop) ──
            cbTask.setOnCheckedChangeListener(null)
            cbTask.isChecked = task.isCompleted
            cbTask.setOnCheckedChangeListener { _, isChecked ->
                task.isCompleted = isChecked
                onCheckedChange(task, isChecked)
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = tasks.size

    /** Call from Activity to refresh all items (e.g. after filter/sort). */
    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}