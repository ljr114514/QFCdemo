package com.example.notebook.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.notebook.R

class KnowledgeCreateFragment : Fragment(R.layout.fragment_knowledge_create) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btn_create_kb_submit).setOnClickListener {
            Toast.makeText(requireContext(), "已创建知识库（原型）", Toast.LENGTH_SHORT).show()
        }
    }
}
