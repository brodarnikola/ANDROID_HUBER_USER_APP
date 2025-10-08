package hr.sil.android.schlauebox.util.ui

import android.graphics.Paint

/**
 * @author mfatiga
 */
class RoundCornerRectFillDrawable(color: Int, cornerRadius: Float = 10F) : RoundCornerRectDrawable(
        Paint().apply {
            this@apply.isAntiAlias = true
            this@apply.color = color
        }, cornerRadius)