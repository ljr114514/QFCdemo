package com.example.notebook.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.notebook.MainActivity
import com.example.notebook.R

class AiStudioFragment : Fragment(R.layout.fragment_ai_studio) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = activity as? MainActivity ?: return
        view.findViewById<View>(R.id.btn_ai_upload_pdf).setOnClickListener {
            Toast.makeText(requireContext(), "选择 PDF（原型）", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_ai_generate).setOnClickListener {
            Toast.makeText(requireContext(), "已开始生成最终笔记（原型）", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_ai_open_notes).setOnClickListener {
            host.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
                .selectedItemId = R.id.nav_notes
        }
        view.findViewById<View>(R.id.btn_ai_open_kb).setOnClickListener {
            host.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
                .selectedItemId = R.id.nav_kb
        }
    }
}
