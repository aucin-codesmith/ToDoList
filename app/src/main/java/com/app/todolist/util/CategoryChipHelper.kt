package com.app.todolist.util

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.app.todolist.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Helper untuk fitur "kategori kustom" di AddTaskActivity & EditTaskActivity.
 * Chip "+" (chipTambah) memunculkan dialog input nama, lalu chip baru
 * ditambahkan ke ChipGroup (sebelum chip "+") dan langsung dipilih.
 */
object CategoryChipHelper {

    /** Pasang listener di chip "+" untuk memunculkan dialog tambah kategori. */
    fun setupAddCategoryChip(
        context: Context,
        chipGroup: ChipGroup,
        chipTambah: Chip
    ) {
        chipTambah.setOnClickListener {
            showAddCategoryDialog(context) { newCategory ->
                addOrSelectChip(chipGroup, chipTambah, newCategory)
            }
        }
    }

    private fun showAddCategoryDialog(context: Context, onConfirmed: (String) -> Unit) {
        val input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            hint = "Contoh: Keuangan, Olahraga, dll"
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        AlertDialog.Builder(context)
            .setTitle("Kategori Baru")
            .setView(input)
            .setPositiveButton("Tambah") { dialog, _ ->
                val name = input.text?.toString()?.trim().orEmpty()
                if (name.isNotEmpty()) onConfirmed(name)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Tambah chip baru sebelum chip "+" kalau kategori dengan nama itu belum ada
     * (dicek case-insensitive supaya "kerja" dan "Kerja" tidak dobel), lalu pilih chip-nya.
     * Kalau kategori dengan nama sama sudah ada (baik chip bawaan maupun custom
     * sebelumnya), langsung pilih chip yang sudah ada itu tanpa bikin duplikat.
     */
    fun addOrSelectChip(chipGroup: ChipGroup, chipTambah: Chip, category: String) {
        for (i in 0 until chipGroup.childCount) {
            val child = chipGroup.getChildAt(i)
            if (child is Chip && child.id != chipTambah.id &&
                child.text.toString().equals(category, ignoreCase = true)
            ) {
                chipGroup.check(child.id)
                return
            }
        }

        val context = chipGroup.context
        val newChip = Chip(
            ContextThemeWrapper(context, com.google.android.material.R.style.Widget_Material3_Chip_Filter)
        ).apply {
            id = View.generateViewId()
            text = category
            isCheckable = true
            isClickable = true
            chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.selector_chip_bg)
            chipStrokeColor = ContextCompat.getColorStateList(context, R.color.outline_variant)
            chipStrokeWidth = 1f * context.resources.displayMetrics.density
        }

        val insertIndex = chipGroup.indexOfChild(chipTambah)
        chipGroup.addView(newChip, insertIndex)
        chipGroup.check(newChip.id)
    }
}