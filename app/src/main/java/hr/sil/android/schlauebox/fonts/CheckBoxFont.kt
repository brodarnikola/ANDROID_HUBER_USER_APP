package hr.sil.android.smartlockers.enduser.fonts.view.fonts

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

/**
 * @author mfatiga
 */
internal class CheckBoxFont : androidx.appcompat.widget.AppCompatCheckBox {
    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val tf = Typeface.createFromAsset(context.assets, "fonts/Montserrat-Regular.ttf")
        setTypeface(tf)
    }
}