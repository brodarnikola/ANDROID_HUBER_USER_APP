package hr.sil.android.schlauebox.view.ui.intro

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.ActivityPasswordUpdateBinding
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.util.connectivity.NetworkChecker
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.dialog.PasswordUpdateSuccessDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PasswordUpdateActivity : BaseActivity(noWifiViewId = R.id.no_internet_layout) {

    var wrongPassword: Boolean = false
    val log = logger()
    private lateinit var binding: ActivityPasswordUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPasswordUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        //binding.etRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        val email = intent.getStringExtra("EMAIL")
        log.info("Password update activity, email is: ${email}")
        binding.btnPasswordUpdate.setOnClickListener {

            if (validate()) {
                binding.progressBar.visibility = View.VISIBLE
                GlobalScope.launch {
                    if (NetworkChecker.isInternetConnectionAvailable()) {

                        val result = email != null && submitResetPass(email)
                        withContext(Dispatchers.Main) {
                            when {
                                result -> showCustomDialog()
                                else -> showErrorWrongPin()
                            }
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                    else {
                        //App.ref.toast(R.string.app_generic_no_network)
                    }
                }
            }
        }

//        binding.tvShowPasswords.setOnTouchListener { view, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT
//                    binding.etRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT
//                }
//                MotionEvent.ACTION_UP -> {
//                    binding.etPassword.inputType =
//                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//                    binding.etRepeatPassword.inputType =
//                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//                }
//            }
//            true
//        }
    }

    private fun showCustomDialog() {
        val passwordUpdateSuccess = PasswordUpdateSuccessDialog(this@PasswordUpdateActivity)
        passwordUpdateSuccess.show(this@PasswordUpdateActivity.supportFragmentManager, "")
    }

    private fun validate(): Boolean {

        var validated = true
        if (!validateNewPassword()) {
            validated = false
        }
        if (!validateRepeatPassword()) {
            validated = false
            wrongPassword = true
        }
        if (!validateNewPin()) {
            validated = false
            wrongPassword = true
        }
        return validated
    }

    private fun validateNewPassword(): Boolean {
        return true
//        return validateEditText(binding.tilPassword, binding.etPassword) { newPassword ->
//            when {
//                newPassword.isBlank() -> ValidationResult.INVALID_PASSWORD_BLANK
//                newPassword.length < 6 -> ValidationResult.INVALID_PASSWORD_MIN_6_CHARACTERS
//                else -> ValidationResult.VALID
//            }
//        }
    }

    private fun validateRepeatPassword(): Boolean {
        return true
//        return validateEditText(binding.tilRepeatPassword, binding.etRepeatPassword) { repeatPassword ->
//            val password = binding.etPassword.text.toString().trim()
//            if (repeatPassword != password) {
//                ValidationResult.INVALID_PASSWORDS_DO_NOT_MATCH
//            } else {
//                ValidationResult.VALID
//            }
//        }
    }

    private fun validateNewPin(): Boolean {
        return true
//        return validateEditText(binding.tilPin, binding.etPin) { pickupPin ->
//            when {
//                pickupPin.isEmpty() -> ValidationResult.INVALID_PASSWORD_BLANK
//                else -> ValidationResult.VALID
//            }
//        }
    }

    private fun showErrorWrongPin(): Boolean {

        binding.tilPassword.error = null
        binding.tilRepeatPassword.error = null

        return true
//        return validateEditText(binding.tilPin, binding.etPin) { pickupPin ->
//            when {
//                pickupPin.isNotEmpty() -> ValidationResult.INVALID_PIN
//                else -> ValidationResult.VALID
//            }
//        }
    }

    suspend private fun submitResetPass(email: String): Boolean {
        return true
//        if (binding.etPassword.text.toString() == binding.etRepeatPassword.text.toString()) {
//            return UserUtil.passwordReset(
//                    email = email,
//                    passwordCode = binding.etPin.text.toString(),
//                    password = binding.etPassword.text.toString()
//            )
//        } else {
//            //etPassword.error = this.getString(R.string.app_generic_password_doesnt_match)
//            //etRepeatPassword.error = this.getString(R.string.app_generic_password_doesnt_match)
//            return false
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


}