package com.example.notebook.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.notebook.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

class TemplateFragment : Fragment(R.layout.fragment_template) {

    private val prefsName = "template_style_prefs"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = activity as? com.example.notebook.MainActivity ?: return
        val prefs = requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)

        val colorPreview = view.findViewById<MaterialCardView>(R.id.card_color_preview)
        val colorHex = view.findViewById<TextView>(R.id.tv_color_hex)
        val seekR = view.findViewById<SeekBar>(R.id.seek_color_r)
        val seekG = view.findViewById<SeekBar>(R.id.seek_color_g)
        val seekB = view.findViewById<SeekBar>(R.id.seek_color_b)
        val etR = view.findViewById<EditText>(R.id.et_color_r)
        val etG = view.findViewById<EditText>(R.id.et_color_g)
        val etB = view.findViewById<EditText>(R.id.et_color_b)

        val fontSeek = view.findViewById<SeekBar>(R.id.seek_font_size)
        val paraSeek = view.findViewById<SeekBar>(R.id.seek_paragraph_space)
        val fontValue = view.findViewById<TextView>(R.id.tv_font_size_value)
        val paraValue = view.findViewById<TextView>(R.id.tv_paragraph_space_value)

        val etPrompt = view.findViewById<TextInputEditText>(R.id.et_prompt)
        val spinnerPrompts = view.findViewById<Spinner>(R.id.spinner_saved_prompts)
        val previewDesc = view.findViewById<TextView>(R.id.tv_template_preview_desc)

        val initialR = prefs.getInt("color_r", 140)
        val initialG = prefs.getInt("color_g", 200)
        val initialB = prefs.getInt("color_b", 255)
        val initialFont = prefs.getInt("font_progress", 3)
        val initialPara = prefs.getInt("para_progress", 2)
        val lastPrompt = prefs.getString("last_prompt", "") ?: ""

        seekR.progress = initialR
        seekG.progress = initialG
        seekB.progress = initialB
        etR.setText(initialR.toString())
        etG.setText(initialG.toString())
        etB.setText(initialB.toString())
        fontSeek.progress = initialFont
        paraSeek.progress = initialPara
        etPrompt.setText(lastPrompt)

        val savedPrompts = loadPrompts(prefs)
        bindPromptSpinner(spinnerPrompts, savedPrompts)

        fun renderPreview() {
            val color = Color.rgb(seekR.progress, seekG.progress, seekB.progress)
            val hex = String.format(Locale.US, "#%02X%02X%02X", seekR.progress, seekG.progress, seekB.progress)
            colorPreview.setCardBackgroundColor(color)
            colorHex.text = hex
            etR.setText(seekR.progress.toString())
            etG.setText(seekG.progress.toString())
            etB.setText(seekB.progress.toString())

            val fontPt = 11 + fontSeek.progress
            val paraRatio = 1.0f + paraSeek.progress * 0.1f
            val paraText = String.format(Locale.US, "%.1fx", paraRatio)
            fontValue.text = "$fontPt pt"
            paraValue.text = paraText

            val promptText = etPrompt.text?.toString()?.trim().orEmpty()
            val promptSummary = if (promptText.isBlank()) "Prompt 未设置" else "Prompt 已设置"
            previewDesc.text = "当前：$hex | $fontPt pt | $paraText | $promptSummary"
        }

        var syncingColorInput = false
        val colorListener = simpleSeekListener {
            if (!syncingColorInput) {
                syncingColorInput = true
                renderPreview()
                syncingColorInput = false
            }
        }
        seekR.setOnSeekBarChangeListener(colorListener)
        seekG.setOnSeekBarChangeListener(colorListener)
        seekB.setOnSeekBarChangeListener(colorListener)
        fontSeek.setOnSeekBarChangeListener(simpleSeekListener { renderPreview() })
        paraSeek.setOnSeekBarChangeListener(simpleSeekListener { renderPreview() })

        val palettePairs = listOf(
            R.id.palette_1 to intArrayOf(140, 200, 255),
            R.id.palette_2 to intArrayOf(110, 168, 254),
            R.id.palette_3 to intArrayOf(64, 183, 168),
            R.id.palette_4 to intArrayOf(246, 183, 86),
            R.id.palette_5 to intArrayOf(240, 127, 161),
            R.id.palette_6 to intArrayOf(157, 140, 255)
        )
        palettePairs.forEach { pair ->
            view.findViewById<View>(pair.first).setOnClickListener {
                seekR.progress = pair.second[0]
                seekG.progress = pair.second[1]
                seekB.progress = pair.second[2]
                renderPreview()
            }
        }

        bindRgbInput(etR, seekR) { renderPreview() }
        bindRgbInput(etG, seekG) { renderPreview() }
        bindRgbInput(etB, seekB) { renderPreview() }

        view.findViewById<MaterialButton>(R.id.btn_prompt_save).setOnClickListener {
            val prompt = etPrompt.text?.toString()?.trim().orEmpty()
            if (prompt.isBlank()) {
                Toast.makeText(requireContext(), "请先输入 Prompt", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updated = savePrompt(prefs, prompt)
            bindPromptSpinner(spinnerPrompts, updated)
            spinnerPrompts.setSelection(updated.indexOf(prompt).coerceAtLeast(0))
            renderPreview()
            Toast.makeText(requireContext(), "Prompt 已保存", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<MaterialButton>(R.id.btn_prompt_load).setOnClickListener {
            val list = loadPrompts(prefs)
            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "暂无已保存 Prompt", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val idx = spinnerPrompts.selectedItemPosition
            if (idx in list.indices) {
                etPrompt.setText(list[idx])
                renderPreview()
            }
        }

        view.findViewById<View>(R.id.btn_template_preview).setOnClickListener {
            host.openNoteDetailPage("样式预览", "主配色与 Prompt 预览")
        }

        view.findViewById<View>(R.id.btn_template_apply).setOnClickListener {
            val prompt = etPrompt.text?.toString()?.trim().orEmpty()
            prefs.edit()
                .putInt("color_r", seekR.progress)
                .putInt("color_g", seekG.progress)
                .putInt("color_b", seekB.progress)
                .putInt("font_progress", fontSeek.progress)
                .putInt("para_progress", paraSeek.progress)
                .putString("last_prompt", prompt)
                .apply()
            host.findViewById<BottomNavigationView>(R.id.bottom_nav).selectedItemId = R.id.nav_notes
            Toast.makeText(requireContext(), "样式与 Prompt 已应用", Toast.LENGTH_SHORT).show()
        }

        renderPreview()
    }

    private fun savePrompt(prefs: android.content.SharedPreferences, prompt: String): List<String> {
        val current = loadPrompts(prefs).toMutableList()
        current.remove(prompt)
        current.add(0, prompt)
        if (current.size > 20) {
            current.subList(20, current.size).clear()
        }
        prefs.edit().putString("saved_prompts", current.joinToString("\n|||\n")).putString("last_prompt", prompt).apply()
        return current
    }

    private fun loadPrompts(prefs: android.content.SharedPreferences): List<String> {
        val raw = prefs.getString("saved_prompts", "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split("\n|||\n").map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun bindPromptSpinner(spinner: Spinner, prompts: List<String>) {
        val items = if (prompts.isEmpty()) listOf("暂无已保存 Prompt") else prompts
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun bindRgbInput(input: EditText, seekBar: SeekBar, onUpdate: () -> Unit) {
        val applyValue = {
            val raw = input.text?.toString()?.trim().orEmpty()
            val value = raw.toIntOrNull()?.coerceIn(0, 255) ?: seekBar.progress
            if (seekBar.progress != value) {
                seekBar.progress = value
            }
            if (input.text?.toString() != value.toString()) {
                input.setText(value.toString())
            }
            onUpdate()
        }
        input.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) applyValue()
        }
        input.setOnEditorActionListener { _, _, _ ->
            applyValue()
            false
        }
    }

    private fun simpleSeekListener(onChange: () -> Unit): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = onChange()
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        }
    }
}
