package com.example.notebook.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class HandwritingCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Stroke(val path: Path, val paint: Paint)

    private val strokes = mutableListOf<Stroke>()
    private val redoStrokes = mutableListOf<Stroke>()

    private var currentPath: Path? = null
    private val rawPoints = mutableListOf<Pair<Float, Float>>()
    private var currentX = 0f
    private var currentY = 0f

    private var baseBitmap: Bitmap? = null
    private var inkSeedBitmap: Bitmap? = null
    private var showBaseLayer = true
    private var penColor: Int = Color.parseColor("#2F7BD8")
    private var strokeWidth: Float = 8f
    private var eraserMode = false
    private var highlighterMode = false
    private var stylusOnlyMode = false
    private var shapeAssistMode = false
    private var lassoMode = false

    enum class BackgroundMode { BLANK, RULED, GRID }
    private var backgroundMode: BackgroundMode = BackgroundMode.BLANK

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DCEAFF")
        strokeWidth = 1.2f
    }

    private val lassoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3A78D4")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private var lassoRect: RectF? = null
    private var selectedRect: RectF? = null
    private var selectedOriginalRect: RectF? = null
    private var selectedBitmap: Bitmap? = null
    private var draggingSelection = false
    private var selectionOffsetX = 0f
    private var selectionOffsetY = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(if (showBaseLayer) Color.parseColor("#F7FBFF") else Color.TRANSPARENT)
        if (showBaseLayer) drawBackgroundGuides(canvas)
        if (showBaseLayer) {
            baseBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        }
        inkSeedBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        strokes.forEach { canvas.drawPath(it.path, it.paint) }
        currentPath?.let { path ->
            canvas.drawPath(path, createPaint())
        }
        lassoRect?.let { canvas.drawRect(it, lassoPaint) }
        selectedRect?.let { canvas.drawRect(it, lassoPaint) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (stylusOnlyMode) {
            val toolType = event.getToolType(0)
            val isStylus = toolType == MotionEvent.TOOL_TYPE_STYLUS || toolType == MotionEvent.TOOL_TYPE_ERASER
            if (!isStylus) return false
        }

        if (lassoMode) {
            return handleLassoTouch(event)
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path().apply { moveTo(event.x, event.y) }
                rawPoints.clear()
                rawPoints.add(event.x to event.y)
                currentX = event.x
                currentY = event.y
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val path = currentPath ?: return true
                rawPoints.add(event.x to event.y)
                val dx = abs(event.x - currentX)
                val dy = abs(event.y - currentY)
                if (dx >= 2f || dy >= 2f) {
                    path.quadTo(currentX, currentY, (event.x + currentX) / 2f, (event.y + currentY) / 2f)
                    currentX = event.x
                    currentY = event.y
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val path = currentPath ?: return true
                path.lineTo(event.x, event.y)
                val finalPath = if (shapeAssistMode && !eraserMode) {
                    buildShapeAssistedPath(path)
                } else {
                    path
                }
                strokes.add(Stroke(finalPath, createPaint()))
                currentPath = null
                rawPoints.clear()
                redoStrokes.clear()
                invalidate()
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setPenColor(color: Int) {
        penColor = color
        eraserMode = false
        lassoMode = false
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
    }

    fun setEraser(enabled: Boolean) {
        eraserMode = enabled
        if (enabled) highlighterMode = false
        if (enabled) lassoMode = false
    }

    fun setHighlighter(enabled: Boolean) {
        highlighterMode = enabled
        if (enabled) eraserMode = false
        if (enabled) lassoMode = false
    }

    fun setStylusOnly(enabled: Boolean) {
        stylusOnlyMode = enabled
    }

    fun setBackgroundMode(mode: BackgroundMode) {
        backgroundMode = mode
        invalidate()
    }

    fun setShapeAssist(enabled: Boolean) {
        shapeAssistMode = enabled
    }

    fun setLassoMode(enabled: Boolean) {
        lassoMode = enabled
        if (!enabled) {
            lassoRect = null
            selectedRect = null
            selectedBitmap = null
            draggingSelection = false
        }
        invalidate()
    }

    fun undo() {
        if (strokes.isNotEmpty()) {
            redoStrokes.add(strokes.removeAt(strokes.lastIndex))
            invalidate()
        }
    }

    fun redo() {
        if (redoStrokes.isNotEmpty()) {
            strokes.add(redoStrokes.removeAt(redoStrokes.lastIndex))
            invalidate()
        }
    }

    fun clearAll() {
        baseBitmap = null
        inkSeedBitmap = null
        strokes.clear()
        redoStrokes.clear()
        currentPath = null
        lassoRect = null
        selectedRect = null
        selectedBitmap = null
        invalidate()
    }

    fun loadBitmap(bitmap: Bitmap?) {
        baseBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true)
        inkSeedBitmap = null
        strokes.clear()
        redoStrokes.clear()
        currentPath = null
        invalidate()
    }

    fun loadPage(base: Bitmap?, ink: Bitmap?) {
        baseBitmap = base?.copy(Bitmap.Config.ARGB_8888, true)
        inkSeedBitmap = ink?.copy(Bitmap.Config.ARGB_8888, true)
        strokes.clear()
        redoStrokes.clear()
        currentPath = null
        lassoRect = null
        selectedRect = null
        selectedOriginalRect = null
        selectedBitmap = null
        invalidate()
    }

    fun setShowBaseLayer(show: Boolean) {
        showBaseLayer = show
        invalidate()
    }

    fun exportBitmap(): Bitmap? {
        return exportBitmap(includeBaseLayer = true)
    }

    fun exportBitmap(includeBaseLayer: Boolean): Bitmap? {
        if (width <= 0 || height <= 0) return null
        if (baseBitmap == null && inkSeedBitmap == null && strokes.isEmpty() && currentPath == null) return null
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (includeBaseLayer) {
            canvas.drawColor(Color.parseColor("#F7FBFF"))
            drawBackgroundGuides(canvas)
            baseBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        } else {
            canvas.drawColor(Color.TRANSPARENT)
        }
        inkSeedBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        strokes.forEach { canvas.drawPath(it.path, it.paint) }
        currentPath?.let { canvas.drawPath(it, createPaint()) }
        return bitmap
    }

    fun exportInkBitmap(): Bitmap? = exportBitmap(includeBaseLayer = false)

    private fun createPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (eraserMode) Color.parseColor("#F7FBFF") else penColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            this.strokeWidth = if (highlighterMode) this@HandwritingCanvasView.strokeWidth + 8f else this@HandwritingCanvasView.strokeWidth
            if (highlighterMode && !eraserMode) {
                alpha = 120
            }
        }
    }

    private fun buildShapeAssistedPath(defaultPath: Path): Path {
        if (rawPoints.size < 3) return defaultPath
        val first = rawPoints.first()
        val last = rawPoints.last()
        val distance = hypot(last.first - first.first, last.second - first.second)

        val minX = rawPoints.minOf { it.first }
        val maxX = rawPoints.maxOf { it.first }
        val minY = rawPoints.minOf { it.second }
        val maxY = rawPoints.maxOf { it.second }
        val w = maxX - minX
        val h = maxY - minY

        val assisted = Path()
        val closed = distance < max(24f, strokeWidth * 2f)

        if (!closed) {
            assisted.moveTo(first.first, first.second)
            assisted.lineTo(last.first, last.second)
            return assisted
        }

        val ratio = if (h == 0f) 99f else w / h
        if (ratio in 0.7f..1.3f) {
            assisted.addOval(RectF(minX, minY, maxX, maxY), Path.Direction.CW)
        } else {
            assisted.addRect(RectF(minX, minY, maxX, maxY), Path.Direction.CW)
        }
        return assisted
    }

    private fun handleLassoTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val selected = selectedRect
                if (selected != null && selected.contains(event.x, event.y) && selectedBitmap != null) {
                    draggingSelection = true
                    selectedOriginalRect = RectF(selected)
                    selectionOffsetX = event.x - selected.left
                    selectionOffsetY = event.y - selected.top
                } else {
                    draggingSelection = false
                    lassoRect = RectF(event.x, event.y, event.x, event.y)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (draggingSelection) {
                    selectedRect?.let {
                        val w = it.width()
                        val h = it.height()
                        val left = event.x - selectionOffsetX
                        val top = event.y - selectionOffsetY
                        it.set(left, top, left + w, top + h)
                    }
                } else {
                    lassoRect?.let {
                        it.right = event.x
                        it.bottom = event.y
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (draggingSelection) {
                    applySelectionMove()
                    draggingSelection = false
                } else {
                    captureSelection()
                }
                invalidate()
                return true
            }
        }
        return false
    }

    private fun captureSelection() {
        val rect = normalizeRect(lassoRect ?: return)
        lassoRect = null
        val source = exportBitmap() ?: return
        val safe = clampRectToCanvas(rect) ?: return
        if (safe.width() < 4 || safe.height() < 4) return
        selectedRect = RectF(safe)
        selectedOriginalRect = RectF(safe)
        selectedBitmap = Bitmap.createBitmap(source, safe.left, safe.top, safe.width(), safe.height())
    }

    private fun applySelectionMove() {
        val dstRectF = selectedRect ?: return
        val srcRectF = selectedOriginalRect ?: return
        val selection = selectedBitmap ?: return
        val src = exportBitmap() ?: return
        val mutable = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        // Clear original selection area with page color.
        canvas.drawRect(srcRectF, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F7FBFF") })
        canvas.drawBitmap(selection, dstRectF.left, dstRectF.top, null)

        loadBitmap(mutable)
        selectedRect = null
        selectedOriginalRect = null
        selectedBitmap = null
    }

    private fun normalizeRect(rect: RectF): RectF {
        val left = min(rect.left, rect.right)
        val right = max(rect.left, rect.right)
        val top = min(rect.top, rect.bottom)
        val bottom = max(rect.top, rect.bottom)
        return RectF(left, top, right, bottom)
    }

    private fun clampRectToCanvas(rect: RectF): Rect? {
        val left = rect.left.toInt().coerceAtLeast(0)
        val top = rect.top.toInt().coerceAtLeast(0)
        val right = rect.right.toInt().coerceAtMost(width)
        val bottom = rect.bottom.toInt().coerceAtMost(height)
        if (right <= left || bottom <= top) return null
        return Rect(left, top, right, bottom)
    }

    private fun drawBackgroundGuides(canvas: Canvas) {
        when (backgroundMode) {
            BackgroundMode.BLANK -> Unit
            BackgroundMode.RULED -> {
                var y = 56f
                while (y < height.toFloat()) {
                    canvas.drawLine(0f, y, width.toFloat(), y, bgPaint)
                    y += 56f
                }
            }
            BackgroundMode.GRID -> {
                var y = 56f
                while (y < height.toFloat()) {
                    canvas.drawLine(0f, y, width.toFloat(), y, bgPaint)
                    y += 56f
                }
                var x = 56f
                while (x < width.toFloat()) {
                    canvas.drawLine(x, 0f, x, height.toFloat(), bgPaint)
                    x += 56f
                }
            }
        }
    }
}
