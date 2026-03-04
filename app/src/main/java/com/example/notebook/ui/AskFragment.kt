package com.example.notebook.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.notebook.R

class AskFragment : Fragment(R.layout.fragment_ask) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = activity as? com.example.notebook.MainActivity ?: return
        view.findViewById<View>(R.id.btn_ask_to_notes).setOnClickListener {
            host.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
                .selectedItemId = R.id.nav_notes
        }
        view.findViewById<View>(R.id.btn_ask_to_template).setOnClickListener { host.openTemplatePage() }
    }
}
