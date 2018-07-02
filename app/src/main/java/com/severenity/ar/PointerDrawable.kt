package com.severenity.ar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable

/**
 * Helper class to display pointer on the surface view of the camera
 */
class PointerDrawable: Drawable() {
    private val paint = Paint()
    var enabled = false

    override fun setAlpha(alpha: Int) {
        TODO("not implemented")
    }

    override fun getOpacity(): Int {
        TODO("not implemented")
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        TODO("not implemented")
    }

    override fun draw(canvas: Canvas) {
        val cx = bounds.width().div(2.0f)
        val cy = bounds.height().div(2.0f)

        if (enabled) {
            paint.color = Color.GREEN
            canvas.drawCircle(cx, cy, 10f, paint)
        } else {
            paint.color = Color.GRAY
            canvas.drawText("X", cx, cy, paint)
        }
    }
}