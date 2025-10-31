package hr.sil.android.schlauebox.compose.view.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.ViewGroupOverlay
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.esotericsoftware.minlog.Log
//import com.google.android.material.textfield.TextInputLayout
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.util.NotificationHelper
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.util.connectivity.BluetoothChecker
import hr.sil.android.schlauebox.util.connectivity.LocationGPSChecker
import hr.sil.android.schlauebox.util.connectivity.NetworkChecker
import kotlinx.coroutines.*
//import org.jetbrains.anko.*
//import org.jetbrains.anko.sdk15.coroutines.textChangedListener


open class BaseComponentActivity(noBleViewId: Int = 0, noWifiViewId: Int = 0, noLocationGPSViewId: Int = 0) : ComponentActivity() {

    var viewLoaded = true
    private var btCheckerListenerKey: String? = null
    private var networkCheckerListenerKey: String? = null
    private var locationGPSListenerKey: String? = null
    var networkAvailable: Boolean = true
    var bluetoothAvalilable: Boolean = true
    var locationGPSAvalilable: Boolean = true

    val frame by lazy { if (noBleViewId != 0) findViewById<FrameLayout>(noBleViewId) else null }
    val noWifiFrame by lazy { if (noWifiViewId != 0) findViewById<FrameLayout>(noWifiViewId) else null }
    val noLocationGPSFrame by lazy { if (noLocationGPSViewId != 0) findViewById<FrameLayout>(noLocationGPSViewId) else null }
    private val uiHandler by lazy { Handler(Looper.getMainLooper()) }

    override fun onResume() {
        super.onResume()
        loger.info("Base actitvity started on resume")
        NotificationHelper.clearNotification()

        if (btCheckerListenerKey == null) {
            btCheckerListenerKey = BluetoothChecker.addListener { available ->
                uiHandler.post { onBluetoothStateUpdated(available) }
            }
        }
        if (networkCheckerListenerKey == null) {
            networkCheckerListenerKey = NetworkChecker.addListener { available ->
                uiHandler.post { onNetworkStateUpdated(available) }
            }
        }
        if (locationGPSListenerKey == null) {
            locationGPSListenerKey = LocationGPSChecker(this).addListener { available ->
                uiHandler.post { onLocationGPSStateUpdated(available) }
            }
        }
    }

    fun updateUI() {
        if (frame != null && noWifiFrame != null) {
            frame?.visibility = if (bluetoothAvalilable) View.GONE else View.VISIBLE
            noWifiFrame?.visibility = if (networkAvailable) View.GONE else {
                if (!bluetoothAvalilable) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
            noLocationGPSFrame?.visibility = if (locationGPSAvalilable) View.GONE else {
                if (!bluetoothAvalilable || !networkAvailable) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }
    }

    fun initializeLinkableTextView(textResource: String, textView: TextView, clickHandler: () -> Unit) {
        val ulIndices = mutableListOf<Int>()
        var textClean = ""
        for ((index, char) in textResource.withIndex()) {
            if (char == '_') {
                ulIndices.add(index - ulIndices.size)
            } else {
                textClean += char
            }
        }
        if (ulIndices.size < 2) {
            ulIndices.clear()
            ulIndices.add(0)
            ulIndices.add(textClean.length)
        }

        val start = ulIndices.first()
        val end = ulIndices.last()

        val spannableString = SpannableString(textClean)
        spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        clickHandler()
                    }
                },
                start, end, 0)

        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    override fun onPause() {
        super.onPause()
        Log.info("onPause()")

        btCheckerListenerKey?.let { BluetoothChecker.removeListener(it) }
        btCheckerListenerKey = null
        networkCheckerListenerKey?.let { NetworkChecker.removeListener(it) }
        networkCheckerListenerKey = null
        locationGPSListenerKey?.let { LocationGPSChecker(this).removeListener(it) }
        locationGPSListenerKey = null
    }

//    fun setNoBleOverLay() {
//        val viewGroup = contentView?.overlay as ViewGroupOverlay?
//        val overlayView =
//                _FrameLayout(this).apply {
//                    background = ContextCompat.getDrawable(this@BaseActivity, R.drawable.bg_bluetooth)
//                    alpha = 0.8f
//                    textView(R.string.app_generic_no_ble) {
//                        textColor = Color.WHITE
//                        allCaps = true
//                    }.lparams {
//                        gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
//                    }
//                }
//
//        viewGroup?.add(overlayView)
//
//    }

    val loger = logger()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loger.info("Base actitvity started on create")
    }

    open fun onBluetoothStateUpdated(available: Boolean) {}

    open fun onNetworkStateUpdated(available: Boolean) {}

    open fun onLocationGPSStateUpdated(available: Boolean) {}

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SettingsHelper.setLocale(base))
    }

   // protected fun validateEmail(emailInputLayout: TextInputLayout?, emailParam: EditText): Boolean {
   protected fun validateEmail(emailInputLayout: TextView?, emailParam: EditText): Boolean {
        return validateEditText(emailInputLayout, emailParam) { email ->
            when {
                email.isBlank() -> ValidationResult.INVALID_EMAIL_BLANK
                !isEmailValid(email) -> ValidationResult.INVALID_EMAIL
                else -> ValidationResult.VALID
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    enum class ValidationResult(val messageResource: Int?) {
        VALID(null),
        INVALID_OLD_PASSWORD(R.string.edit_user_validation_current_password_invalid),
        INVALID_CURRENT_USER_DATA(R.string.edit_user_validation_current_user_invalid),
        INVALID_PASSWORDS_DO_NOT_MATCH(R.string.edit_user_validation_passwords_do_not_match),
        INVALID_PASSWORD_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_PASSWORD_NEED_TO_BE_DIFFERENT(R.string.password_need_to_be_different),
        INVALID_PASSWORD_MIN_6_CHARACTERS(R.string.edit_user_validation_password_min_6_characters),
        INVALID_USERNAME_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_USERNAME_MIN_4_CHARACTERS(R.string.edit_user_validation_username_min_4_characters),
        INVALID_FIRST_NAME_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_GROUP_NAME_FIRST_CHARACTER(R.string.group_name_correct_start),
        INVALID_LAST_NAME_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_STREET_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_STREET_NO_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_ZIP_CODE_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_CITY_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_COUNTRY_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_EMAIL_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_EMAIL(R.string.message_email_invalid),
        INVALID_PHONE_NO_BLANK(R.string.edit_user_validation_blank_fields_exist),
        INVALID_PIN(R.string.reset_password_error),
        INVALID_TERMS_AND_CONDITIONS_NOT_CHECKED(R.string.edit_user_validation_terms_and_conditions_not_checked),
        INVALID_PRIVACY_POLICY_NOT_CHECKED(R.string.edit_user_validation_terms_and_conditions_not_checked);

        fun getText(context: Context) = if (messageResource != null) {
            context.resources.getString(messageResource)
        } else {
            null
        }

        fun isValid() = this == VALID
    }


    protected fun validateSetError(emailInputLayout: TextView?, result: ValidationResult): ValidationResult {
        val errorText = if (!result.isValid()) result.getText(this) else null
        emailInputLayout?.error = errorText
        return result
    }

    fun validateEditText(emailInputLayout: TextView?, editText: EditText, validate: (value: String) -> ValidationResult): Boolean {
        val result = validate(editText.text.toString())
        validateSetError(emailInputLayout, result)
        return result.isValid()
    }

}