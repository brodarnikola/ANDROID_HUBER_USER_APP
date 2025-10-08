package hr.sil.android.schlauebox.util.ui

import android.graphics.Paint

/**
 * @author mfatiga
 */
class RoundCornerRectStrokeDrawable(color: Int, strokeWidth: Float, cornerRadius: Float = 10F) : RoundCornerRectDrawable(
        Paint().apply {
            this@apply.isAntiAlias = true
            this@apply.color = color
            this@apply.strokeWidth = strokeWidth
            this@apply.style = Paint.Style.STROKE
        }, cornerRadius)