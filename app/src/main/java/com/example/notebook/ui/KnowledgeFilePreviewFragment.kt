package com.example.notebook.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.notebook.R

class KnowledgeFilePreviewFragment : Fragment(R.layout.fragment_knowledge_file_preview) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val libraryName = requireArguments().getString(ARG_LIBRARY_NAME).orEmpty()
        val fileName = requireArguments().getString(ARG_FILE_NAME).orEmpty()
        val fileType = requireArguments().getString(ARG_FILE_TYPE).orEmpty()

        view.findViewById<TextView>(R.id.tv_preview_library).text = libraryName
        view.findViewById<TextView>(R.id.tv_preview_file_name).text = fileName
        view.findViewById<TextView>(R.id.tv_preview_file_type).text = fileType
    }

    fun pageTitle(): String = requireArguments().getString(ARG_FILE_NAME).orEmpty()

    companion object {
        private const val ARG_LIBRARY_NAME = "arg_library_name"
        private const val ARG_FILE_NAME = "arg_file_name"
        private const val ARG_FILE_TYPE = "arg_file_type"

        fun newInstance(libraryName: String, fileName: String, fileType: String): KnowledgeFilePreviewFragment {
            val fragment = KnowledgeFilePreviewFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_LIBRARY_NAME, libraryName)
                putString(ARG_FILE_NAME, fileName)
                putString(ARG_FILE_TYPE, fileType)
            }
            return fragment
        }
    }
}
