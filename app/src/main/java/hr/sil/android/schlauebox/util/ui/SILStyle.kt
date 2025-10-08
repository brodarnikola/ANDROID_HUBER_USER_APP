package hr.sil.android.schlauebox.util.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import androidx.core.content.ContextCompat
import hr.sil.android.schlauebox.R

/**
 * @author mfatiga
 */
object SILStyle {
    object Drawables {
        fun roundedButtonRes(context: Context, normalColor: Int, pressedColor: Int): Drawable {
            return roundedButton(
                    ContextCompat.getColor(context, normalColor),
                    ContextCompat.getColor(context, pressedColor)
            )
        }

        fun roundedButton(normalColor: Int, pressedColor: Int): Drawable {
            return StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_pressed),
                        RoundCornerRectFillDrawable(pressedColor))

                addState(intArrayOf(),
                        RoundCornerRectFillDrawable(normalColor))
            }
        }
        fun roundedButtonBorderRes(context: Context, normalColor: Int, pressedColor: Int): Drawable {
            return roundedButton(
                    ContextCompat.getColor(context, normalColor),
                    ContextCompat.getColor(context, pressedColor)
            )
        }

        fun roundedButtonBorder(normalColor: Int, pressedColor: Int): Drawable {
            return StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_pressed),
                        RoundCornerRectStrokeDrawable(pressedColor, 2F, 5F))

                addState(intArrayOf(),
                        RoundCornerRectStrokeDrawable(normalColor, 2f, 5F))
            }
        }

        fun roundedCheckBoxRes(context: Context, normalColor: Int, pressedColor: Int): Drawable {
            return roundedCheckbox(
                    ContextCompat.getColor(context, normalColor),
                    ContextCompat.getColor(context, pressedColor)
            )
        }

        fun roundedCheckbox(normalColor: Int, pressedColor: Int): Drawable {
            return StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_checked),
                        RoundCornerRectFillDrawable(pressedColor, 15F))

                addState(intArrayOf(),
                        RoundCornerRectFillDrawable(normalColor, 15F))
            }
        }




    }


    fun getRoundedBackground(context: Context) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 6f
        setStroke(2, ContextCompat.getColor(context, R.color.colorPrimary))
    }


    fun getRoundedNatiBackground(context: Context) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 6f
        setStroke(2, ContextCompat.getColor(context, R.color.colorPrimary))
    }


     fun getRipple(): RippleDrawable {
        val mask = android.graphics.drawable.GradientDrawable()
        mask.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        // mask.setSize(2, 4)
        mask.setColor(-0x1000000) // the color is irrelevant here, only the alpha
        val rippleColorLst = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.argb(255, 192, 192, 192))
        return android.graphics.drawable.RippleDrawable(rippleColorLst, null, mask)
    }

}