package com.app.todolist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.todolist.R
import com.app.todolist.databinding.ItemNotificationBinding
import com.app.todolist.model.NotificationItem
import com.app.todolist.model.NotifType

class NotificationAdapter(
    private val items: MutableList<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = holder.binding
        val notif = items[position]
        val ctx   = holder.itemView.context

        item.tvNotifTitle.text = notif.title
        item.tvNotifBody.text  = notif.body
        item.tvNotifTime.text  = notif.time

        // ── Unread dot ────────────────────────────────────────────────────────
        item.viewUnreadDot.visibility = if (!notif.isRead) View.VISIBLE else View.GONE

        // ── Card background: lebih terang jika belum dibaca ───────────────────
        item.cardNotif.setCardBackgroundColor(
            ContextCompat.getColor(
                ctx,
                if (!notif.isRead) R.color.surface_container_low
                else R.color.surface_container_lowest
            )
        )

        // ── Icon & icon background by type ────────────────────────────────────
        when (notif.type) {
            NotifType.DEADLINE -> {
                item.ivNotifIcon.setImageResource(R.drawable.ic_notif_bell)
                item.flIconBg.background =
                    ContextCompat.getDrawable(ctx, R.drawable.bg_notif_icon_primary)
            }
            NotifType.REMINDER -> {
                item.ivNotifIcon.setImageResource(R.drawable.ic_notif_reminder)
                item.flIconBg.background =
                    ContextCompat.getDrawable(ctx, R.drawable.bg_notif_icon_primary)
            }
            NotifType.DONE -> {
                item.ivNotifIcon.setImageResource(R.drawable.ic_notif_done)
                item.flIconBg.background =
                    ContextCompat.getDrawable(ctx, R.drawable.bg_notif_icon_grey)
            }
            NotifType.SYSTEM -> {
                item.ivNotifIcon.setImageResource(R.drawable.ic_notif_update)
                item.flIconBg.background =
                    ContextCompat.getDrawable(ctx, R.drawable.bg_notif_icon_grey)
            }
        }

        // ── Click ─────────────────────────────────────────────────────────────
        holder.itemView.setOnClickListener { onItemClick(notif) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<NotificationItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}