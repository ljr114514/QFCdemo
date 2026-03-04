package com.example.notebook.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.notebook.MainActivity
import com.example.notebook.R

class KnowledgeFragment : Fragment(R.layout.fragment_knowledge) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = activity as? MainActivity ?: return
        view.findViewById<View>(R.id.btn_kb_create).setOnClickListener { host.openKnowledgeCreatePage() }
        view.findViewById<View>(R.id.card_kb_linear_algebra).setOnClickListener {
            host.openKnowledgeLibraryPage("线性代数课程库", "12 个文件")
        }
        view.findViewById<View>(R.id.card_kb_probability).setOnClickListener {
            host.openKnowledgeLibraryPage("概率统计课程库", "9 个文件")
        }
        view.findViewById<View>(R.id.card_kb_machine_learning).setOnClickListener {
            host.openKnowledgeLibraryPage("机器学习导论课程库", "15 个文件")
        }

        view.findViewById<View>(R.id.btn_kb_view_linear).setOnClickListener {
            host.openKnowledgeLibraryPage("线性代数课程库", "12 个文件")
        }
        view.findViewById<View>(R.id.btn_kb_edit_linear).setOnClickListener {
            Toast.makeText(requireContext(), "编辑知识库：线性代数课程库（原型）", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_kb_delete_linear).setOnClickListener {
            Toast.makeText(requireContext(), "删除知识库：线性代数课程库（原型）", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btn_kb_view_probability).setOnClickListener {
            host.openKnowledgeLibraryPage("概率统计课程库", "9 个文件")
        }
        view.findViewById<View>(R.id.btn_kb_edit_probability).setOnClickListener {
            Toast.makeText(requireContext(), "编辑知识库：概率统计课程库（原型）", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_kb_delete_probability).setOnClickListener {
            Toast.makeText(requireContext(), "删除知识库：概率统计课程库（原型）", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btn_kb_view_ml).setOnClickListener {
            host.openKnowledgeLibraryPage("机器学习导论课程库", "15 个文件")
        }
        view.findViewById<View>(R.id.btn_kb_edit_ml).setOnClickListener {
            Toast.makeText(requireContext(), "编辑知识库：机器学习导论课程库（原型）", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_kb_delete_ml).setOnClickListener {
            Toast.makeText(requireContext(), "删除知识库：机器学习导论课程库（原型）", Toast.LENGTH_SHORT).show()
        }
    }
}
