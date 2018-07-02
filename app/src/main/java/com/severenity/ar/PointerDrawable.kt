package com.severenity.ar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable

class PointerDrawable: Drawable() {
    private val paint = Paint()
    var enabled = false

    override fun setAlpha(alpha: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getOpacity(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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