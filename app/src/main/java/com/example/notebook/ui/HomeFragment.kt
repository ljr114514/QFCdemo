package com.example.notebook.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.notebook.MainActivity
import com.example.notebook.R

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = activity as? MainActivity ?: return
        view.findViewById<View>(R.id.btn_home_import).setOnClickListener { host.openAddNotePage() }
        view.findViewById<View>(R.id.btn_home_ask).setOnClickListener { host.openAskPage() }
        view.findViewById<View>(R.id.btn_home_template).setOnClickListener { host.openTemplatePage() }
    }
}
