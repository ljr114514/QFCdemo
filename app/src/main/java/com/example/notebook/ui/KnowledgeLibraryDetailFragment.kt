package com.example.notebook.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.notebook.MainActivity
import com.example.notebook.R

class KnowledgeLibraryDetailFragment : Fragment(R.layout.fragment_knowledge_library_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = activity as? MainActivity ?: return
        val libraryName = requireArguments().getString(ARG_LIBRARY_NAME).orEmpty()
        val fileCount = requireArguments().getString(ARG_FILE_COUNT).orEmpty()

        view.findViewById<View>(R.id.btn_file_create).setOnClickListener {
            Toast.makeText(requireContext(), "新增文件到 $libraryName（原型）", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btn_file_view_pdf).setOnClickListener {
            host.openKnowledgeFilePreviewPage(libraryName, "线性代数教材（第3章）.pdf", "PDF 文档")
        }
        view.findViewById<View>(R.id.btn_file_edit_pdf).setOnClickListener {
            Toast.makeText(requireContext(), "编辑文件：线性代数教材（第3章）.pdf（原型）", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_file_delete_pdf).setOnClickListener {
            Toast.makeText(requireContext(), "删除文件：线性代数教材（第3章）.pdf（原型）", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btn_file_view_ppt).setOnClickListener {
            host.openKnowledgeFilePreviewPage(libraryName, "课堂讲义-特征值.pptx", "PPT 讲义")
        }
        view.findViewById<View>(R.id.btn_file_edit_ppt).setOnClickListener {
            Toast.makeText(requireContext(), "编辑文件：课堂讲义-特征值.pptx（原型）", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_file_delete_ppt).setOnClickListener {
            Toast.makeText(requireContext(), "删除文件：课堂讲义-特征值.pptx（原型）", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btn_file_view_note).setOnClickListener {
            host.openKnowledgeFilePreviewPage(libraryName, "课程补充笔记.md", "Markdown 笔记")
        }
        view.findViewById<View>(R.id.btn_file_edit_note).setOnClickListener {
            Toast.makeText(requireContext(), "编辑文件：课程补充笔记.md（原型）", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_file_delete_note).setOnClickListener {
            Toast.makeText(requireContext(), "删除文件：课程补充笔记.md（原型）", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<android.widget.TextView>(R.id.tv_library_title).text = libraryName
        view.findViewById<android.widget.TextView>(R.id.tv_library_meta).text = "$fileCount · 可在应用内查看"
    }

    fun pageTitle(): String = requireArguments().getString(ARG_LIBRARY_NAME).orEmpty()

    companion object {
        private const val ARG_LIBRARY_NAME = "arg_library_name"
        private const val ARG_FILE_COUNT = "arg_file_count"

        fun newInstance(libraryName: String, fileCount: String): KnowledgeLibraryDetailFragment {
            val fragment = KnowledgeLibraryDetailFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_LIBRARY_NAME, libraryName)
                putString(ARG_FILE_COUNT, fileCount)
            }
            return fragment
        }
    }
}
