package com.example.notebook.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.notebook.R
import com.example.notebook.ui.widget.HandwritingCanvasView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HandwriteNoteFragment : Fragment(R.layout.fragment_handwrite_note) {
    private data class NotebookPage(var base: Bitmap? = null, var ink: Bitmap? = null)

    private lateinit var canvas: HandwritingCanvasView
    private lateinit var pageIndicator: TextView
    private lateinit var pageStrip: LinearLayout
    private lateinit var noteTitle: String
    private lateinit var notebookId: String

    private val pages = mutableListOf<NotebookPage>()
    private var currentPageIndex = 0
    private var stylusOnly = false
    private var shapeAssist = false
    private var lassoMode = false
    private var showBaseLayer = true

    private val openPdfLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        val renderedPages = renderPdfPages(uri)
        if (renderedPages.isEmpty()) {
            Toast.makeText(requireContext(), "PDF 导入失败", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        pages.clear()
        renderedPages.forEach { bmp -> pages.add(NotebookPage(base = bmp, ink = null)) }
        showPage(0, saveBeforeSwitch = false)
        saveNotebookAuto()
        Toast.makeText(requireContext(), "已导入 ${renderedPages.size} 页 PDF", Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notebookId = requireArguments().getString(ARG_NOTEBOOK_ID).orEmpty().ifBlank { "default" }
        noteTitle = requireArguments().getString(ARG_TITLE).orEmpty().ifBlank { "手写笔记" }

        canvas = view.findViewById(R.id.handwriting_canvas)
        pageIndicator = view.findViewById(R.id.tv_page_indicator)
        pageStrip = view.findViewById(R.id.page_strip_container)
        val widthLabel = view.findViewById<TextView>(R.id.tv_stroke_width)
        val widthSeekbar = view.findViewById<SeekBar>(R.id.seek_stroke_width)
        val stylusToggle = view.findViewById<MaterialButton>(R.id.btn_stylus_only)
        val shapeToggle = view.findViewById<MaterialButton>(R.id.btn_shape_assist)
        val lassoToggle = view.findViewById<MaterialButton>(R.id.btn_lasso)
        val layerToggle = view.findViewById<MaterialButton>(R.id.btn_toggle_base_layer)

        loadNotebook()
        if (pages.isEmpty()) pages.add(NotebookPage())
        showPage(0, saveBeforeSwitch = false)

        view.findViewById<View>(R.id.btn_pen).setOnClickListener {
            lassoMode = false
            canvas.setLassoMode(false)
            lassoToggle.text = "套索"
            canvas.setEraser(false)
            canvas.setHighlighter(false)
            Toast.makeText(requireContext(), "画笔模式", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_eraser).setOnClickListener {
            lassoMode = false
            canvas.setLassoMode(false)
            lassoToggle.text = "套索"
            canvas.setEraser(true)
            Toast.makeText(requireContext(), "橡皮模式", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_highlighter).setOnClickListener {
            lassoMode = false
            canvas.setLassoMode(false)
            lassoToggle.text = "套索"
            canvas.setHighlighter(true)
            Toast.makeText(requireContext(), "荧光笔模式", Toast.LENGTH_SHORT).show()
        }
        lassoToggle.setOnClickListener {
            lassoMode = !lassoMode
            canvas.setLassoMode(lassoMode)
            lassoToggle.text = if (lassoMode) "套索（开）" else "套索"
        }
        shapeToggle.setOnClickListener {
            shapeAssist = !shapeAssist
            canvas.setShapeAssist(shapeAssist)
            shapeToggle.text = if (shapeAssist) "图形矫正：开" else "图形矫正：关"
        }
        layerToggle.setOnClickListener {
            showBaseLayer = !showBaseLayer
            canvas.setShowBaseLayer(showBaseLayer)
            layerToggle.text = if (showBaseLayer) "底图显示：开" else "底图显示：关"
        }

        view.findViewById<View>(R.id.btn_undo).setOnClickListener { canvas.undo() }
        view.findViewById<View>(R.id.btn_redo).setOnClickListener { canvas.redo() }
        view.findViewById<View>(R.id.btn_clear).setOnClickListener {
            canvas.clearAll()
            pages[currentPageIndex] = NotebookPage()
            refreshPageStrip()
        }
        view.findViewById<View>(R.id.btn_save).setOnClickListener {
            saveNotebookAuto()
            Toast.makeText(requireContext(), "手写笔记已保存", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_export_pdf).setOnClickListener { exportPdf() }
        view.findViewById<View>(R.id.btn_export_ink).setOnClickListener { exportInkPng() }
        view.findViewById<View>(R.id.btn_import_pdf_bg).setOnClickListener {
            openPdfLauncher.launch(arrayOf("application/pdf"))
        }

        view.findViewById<View>(R.id.btn_page_prev).setOnClickListener {
            if (currentPageIndex > 0) showPage(currentPageIndex - 1)
        }
        view.findViewById<View>(R.id.btn_page_next).setOnClickListener {
            if (currentPageIndex < pages.lastIndex) showPage(currentPageIndex + 1)
        }
        view.findViewById<View>(R.id.btn_page_add).setOnClickListener {
            saveCurrentPageState()
            pages.add(NotebookPage())
            showPage(pages.lastIndex, saveBeforeSwitch = false)
        }
        view.findViewById<View>(R.id.btn_page_delete).setOnClickListener {
            if (pages.size == 1) {
                pages[0] = NotebookPage()
                showPage(0, saveBeforeSwitch = false)
                return@setOnClickListener
            }
            pages.removeAt(currentPageIndex)
            currentPageIndex = currentPageIndex.coerceAtMost(pages.lastIndex)
            showPage(currentPageIndex, saveBeforeSwitch = false)
        }

        view.findViewById<View>(R.id.btn_color_blue).setOnClickListener { canvas.setPenColor(0xFF2F7BD8.toInt()) }
        view.findViewById<View>(R.id.btn_color_black).setOnClickListener { canvas.setPenColor(0xFF1E2432.toInt()) }
        view.findViewById<View>(R.id.btn_color_green).setOnClickListener { canvas.setPenColor(0xFF2D9B73.toInt()) }
        view.findViewById<View>(R.id.btn_color_red).setOnClickListener { canvas.setPenColor(0xFFD14C4C.toInt()) }

        view.findViewById<View>(R.id.btn_bg_blank).setOnClickListener { canvas.setBackgroundMode(HandwritingCanvasView.BackgroundMode.BLANK) }
        view.findViewById<View>(R.id.btn_bg_ruled).setOnClickListener { canvas.setBackgroundMode(HandwritingCanvasView.BackgroundMode.RULED) }
        view.findViewById<View>(R.id.btn_bg_grid).setOnClickListener { canvas.setBackgroundMode(HandwritingCanvasView.BackgroundMode.GRID) }
        stylusToggle.setOnClickListener {
            stylusOnly = !stylusOnly
            canvas.setStylusOnly(stylusOnly)
            stylusToggle.text = if (stylusOnly) "仅手写笔输入：开" else "仅手写笔输入：关"
        }

        widthSeekbar.max = 28
        widthSeekbar.progress = 6
        widthLabel.text = "粗细：8"
        canvas.setStrokeWidth(8f)
        widthSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress + 2
                widthLabel.text = "粗细：$value"
                canvas.setStrokeWidth(value.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    override fun onPause() {
        super.onPause()
        saveNotebookAuto()
    }

    fun pageTitle(): String = noteTitle

    private fun showPage(index: Int, saveBeforeSwitch: Boolean = true) {
        if (saveBeforeSwitch) saveCurrentPageState()
        currentPageIndex = index
        val page = pages[currentPageIndex]
        canvas.loadPage(page.base, page.ink)
        canvas.setShowBaseLayer(showBaseLayer)
        pageIndicator.text = "第 ${currentPageIndex + 1} / ${pages.size} 页"
        refreshPageStrip()
    }

    private fun saveCurrentPageState() {
        val page = pages[currentPageIndex]
        page.base = canvas.exportBitmap(includeBaseLayer = true)?.let { merged ->
            if (showBaseLayer) page.base ?: merged else page.base
        }
        page.ink = canvas.exportInkBitmap()
    }

    private fun refreshPageStrip() {
        pageStrip.removeAllViews()
        pages.forEachIndexed { idx, page ->
            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(108, 148).apply { marginEnd = 10 }
                radius = 14f
                strokeWidth = 2
                setCardBackgroundColor(Color.parseColor("#F7FBFF"))
                strokeColor = if (idx == currentPageIndex) Color.parseColor("#3A78D4") else Color.parseColor("#CFE0FF")
                setOnClickListener { showPage(idx) }
            }
            val holder = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(8)
            }
            val img = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    108
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(Color.parseColor("#EAF4FF"))
            }
            val preview = page.ink ?: page.base
            if (preview != null) img.setImageBitmap(preview)
            val txt = TextView(requireContext()).apply {
                text = "第 ${idx + 1} 页"
                textSize = 12f
                setTextColor(Color.parseColor("#425B86"))
            }
            holder.addView(img)
            holder.addView(txt)
            card.addView(holder)
            pageStrip.addView(card)
        }
    }

    private fun notebookDir(): File {
        return File(requireContext().getExternalFilesDir(null), "handwritten_notebooks/$notebookId")
    }

    private fun saveNotebookAuto() {
        saveCurrentPageState()
        val dir = notebookDir()
        if (!dir.exists()) dir.mkdirs()

        dir.listFiles { f -> f.name.startsWith("page_") }?.forEach { it.delete() }

        pages.forEachIndexed { idx, page ->
            page.base?.let {
                FileOutputStream(File(dir, "page_${idx + 1}_base.png")).use { out ->
                    it.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            page.ink?.let {
                FileOutputStream(File(dir, "page_${idx + 1}_ink.png")).use { out ->
                    it.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
        }

        // Cover for notes list.
        val cover = pages.firstOrNull()?.let { it.ink ?: it.base }
        if (cover != null) {
            FileOutputStream(File(dir, "cover.png")).use { out ->
                cover.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
        }
    }

    private fun loadNotebook() {
        val dir = notebookDir()
        if (!dir.exists()) return
        val baseFiles = dir.listFiles { file ->
            file.isFile && file.name.startsWith("page_") && file.name.endsWith("_base.png")
        }?.sortedBy { it.name } ?: emptyList()
        val inkFiles = dir.listFiles { file ->
            file.isFile && file.name.startsWith("page_") && file.name.endsWith("_ink.png")
        }?.associateBy { it.name.replace("_ink.png", "") } ?: emptyMap()

        if (baseFiles.isNotEmpty()) {
            pages.clear()
            baseFiles.forEach { bf ->
                val key = bf.name.replace("_base.png", "")
                val base = BitmapFactory.decodeFile(bf.absolutePath)
                val ink = inkFiles[key]?.let { BitmapFactory.decodeFile(it.absolutePath) }
                pages.add(NotebookPage(base, ink))
            }
            return
        }

        // fallback compatibility: old single-layer page_x.png
        val oldFiles = dir.listFiles { file ->
            file.isFile && file.name.startsWith("page_") && file.name.endsWith(".png")
        }?.sortedBy { it.name } ?: return
        if (oldFiles.isEmpty()) return
        pages.clear()
        oldFiles.forEach { f -> pages.add(NotebookPage(base = BitmapFactory.decodeFile(f.absolutePath))) }
    }

    private fun exportPdf() {
        saveCurrentPageState()
        val valid = pages.mapNotNull { page ->
            when {
                page.base != null && page.ink != null -> mergeLayers(page.base!!, page.ink!!)
                page.base != null -> page.base
                page.ink != null -> page.ink
                else -> null
            }
        }
        if (valid.isEmpty()) {
            Toast.makeText(requireContext(), "当前没有可导出内容", Toast.LENGTH_SHORT).show()
            return
        }
        val folder = File(requireContext().getExternalFilesDir(null), "handwritten_notes")
        if (!folder.exists()) folder.mkdirs()
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(folder, "handwrite_$stamp.pdf")
        val pdf = PdfDocument()
        valid.forEachIndexed { idx, bmp ->
            val info = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, idx + 1).create()
            val page = pdf.startPage(info)
            page.canvas.drawBitmap(bmp, 0f, 0f, null)
            pdf.finishPage(page)
        }
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        Toast.makeText(requireContext(), "已导出：${file.name}", Toast.LENGTH_SHORT).show()
    }

    private fun exportInkPng() {
        saveCurrentPageState()
        val ink = pages[currentPageIndex].ink
        if (ink == null) {
            Toast.makeText(requireContext(), "当前页没有手写层内容", Toast.LENGTH_SHORT).show()
            return
        }
        val folder = File(requireContext().getExternalFilesDir(null), "handwritten_notes")
        if (!folder.exists()) folder.mkdirs()
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(folder, "ink_layer_$stamp.png")
        FileOutputStream(file).use { out -> ink.compress(Bitmap.CompressFormat.PNG, 100, out) }
        Toast.makeText(requireContext(), "已导出手写层：${file.name}", Toast.LENGTH_SHORT).show()
    }

    private fun renderPdfPages(uri: Uri): List<Bitmap> {
        return try {
            try {
                requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
            }
            requireContext().contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val list = mutableListOf<Bitmap>()
                    for (i in 0 until renderer.pageCount) {
                        renderer.openPage(i).use { page ->
                            val scale = 1.6f
                            val bmp = Bitmap.createBitmap(
                                (page.width * scale).toInt(),
                                (page.height * scale).toInt(),
                                Bitmap.Config.ARGB_8888
                            )
                            bmp.eraseColor(Color.WHITE)
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            list.add(bmp)
                        }
                    }
                    list
                }
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun mergeLayers(base: Bitmap, ink: Bitmap): Bitmap {
        val w = maxOf(base.width, ink.width)
        val h = maxOf(base.height, ink.height)
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = android.graphics.Canvas(out)
        c.drawColor(Color.WHITE)
        c.drawBitmap(base, 0f, 0f, null)
        c.drawBitmap(ink, 0f, 0f, null)
        return out
    }

    companion object {
        private const val ARG_NOTEBOOK_ID = "arg_notebook_id"
        private const val ARG_TITLE = "arg_title"

        fun newInstance(notebookId: String, title: String): HandwriteNoteFragment {
            return HandwriteNoteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NOTEBOOK_ID, notebookId)
                    putString(ARG_TITLE, title)
                }
            }
        }
    }
}
