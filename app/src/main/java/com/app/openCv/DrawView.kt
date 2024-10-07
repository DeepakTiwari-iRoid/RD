package com.app.openCv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.util.AttributeSet
import android.view.View

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint().apply {
        color = 0xFF00FF00.toInt() // Green color
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val path = Path()
    private var points: List<org.opencv.core.Point> = emptyList()

    fun setPoints(points: List<org.opencv.core.Point>) {
        this.points = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.size == 4) {
            path.reset()
            path.moveTo(points[0].x.toFloat(), points[0].y.toFloat())
            for (i in 1 until points.size) {
                path.lineTo(points[i].x.toFloat(), points[i].y.toFloat())
            }
            path.close()
            canvas.drawPath(path, paint)
        }
    }

}