package com.example.notebook.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.notebook.R

class NoteDetailFragment : Fragment(R.layout.fragment_note_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = requireArguments().getString(ARG_TITLE).orEmpty()
        val subtitle = requireArguments().getString(ARG_SUBTITLE).orEmpty()

        view.findViewById<TextView>(R.id.tv_note_detail_title).text = title
        view.findViewById<TextView>(R.id.tv_note_detail_subtitle).text = subtitle
    }

    fun pageTitle(): String = requireArguments().getString(ARG_TITLE).orEmpty()

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_SUBTITLE = "arg_subtitle"

        fun newInstance(title: String, subtitle: String): NoteDetailFragment {
            val fragment = NoteDetailFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_SUBTITLE, subtitle)
            }
            return fragment
        }
    }
}
