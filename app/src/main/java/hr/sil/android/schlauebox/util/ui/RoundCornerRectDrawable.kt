package hr.sil.android.schlauebox.util.ui

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * @author mfatiga
 */
open class RoundCornerRectDrawable(val paint: Paint, private val cornerRadius: Float = 10F) : Drawable() {
    var color: Int
        get() = paint.color
        set(value) {
            paint.color = value
            invalidateSelf()
        }

    override fun draw(canvas: Canvas) {
        val height = bounds.height()
        val width = bounds.width()
        val rect = RectF(0F, 0F, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }
}