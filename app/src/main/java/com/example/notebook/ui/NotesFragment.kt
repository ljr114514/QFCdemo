package com.example.notebook.ui

import android.os.Bundle
import android.view.View
import android.graphics.BitmapFactory
import java.io.File
import androidx.fragment.app.Fragment
import com.example.notebook.MainActivity
import com.example.notebook.R

class NotesFragment : Fragment(R.layout.fragment_notes) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = activity as? MainActivity ?: return

        view.findViewById<View>(R.id.btn_notes_add).setOnClickListener {
            host.openAddNotePage()
        }
        view.findViewById<View>(R.id.btn_notes_handwrite).setOnClickListener {
            host.openHandwriteNotePage()
        }
        view.findViewById<View>(R.id.card_note_handwrite).setOnClickListener {
            host.openHandwriteNotePage()
        }

        val cover = File(requireContext().getExternalFilesDir(null), "handwritten_notebooks/default/cover.png")
        if (cover.exists()) {
            view.findViewById<android.widget.ImageView>(R.id.img_handwrite_cover)
                .setImageBitmap(BitmapFactory.decodeFile(cover.absolutePath))
        }

        view.findViewById<View>(R.id.card_note_raw_algebra).setOnClickListener {
            host.openHandwriteNotePage("raw_algebra_pdf", "高等代数课堂板书（可手写修改）")
        }
        view.findViewById<View>(R.id.card_note_final_algebra).setOnClickListener {
            host.openNoteDetailPage("高等代数第3章讲义版", "最终笔记 · 模板 简约课堂")
        }
        view.findViewById<View>(R.id.card_note_raw_probability).setOnClickListener {
            host.openHandwriteNotePage("raw_probability_pdf", "概率统计课堂速记（可手写修改）")
        }
        view.findViewById<View>(R.id.card_note_final_probability).setOnClickListener {
            host.openNoteDetailPage("概率统计第4章复习版", "最终笔记 · 模板 冲刺复习")
        }
    }
}
