package com.example.notebook.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.notebook.MainActivity
import com.example.notebook.R

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = activity as? MainActivity ?: return
        view.findViewById<View>(R.id.btn_profile_template).setOnClickListener { host.openTemplatePage() }
        view.findViewById<View>(R.id.btn_profile_import).setOnClickListener { host.openAddNotePage() }
    }
}
