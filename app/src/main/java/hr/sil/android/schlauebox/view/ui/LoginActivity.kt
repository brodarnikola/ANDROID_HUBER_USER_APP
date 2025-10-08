package hr.sil.android.schlauebox.view.ui

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.status.InstallationKeyHandler
import hr.sil.android.schlauebox.core.remote.model.UserStatus
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.ActivityLoginBinding
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.util.connectivity.NetworkChecker
import hr.sil.android.schlauebox.view.ui.intro.IntroductionSlidePagerActivity
import hr.sil.android.schlauebox.view.ui.intro.RegistrationActivity
import hr.sil.android.schlauebox.view.ui.intro.TCInvitedUserActivity
import kotlinx.coroutines.*
//import org.jetbrains.anko.below
//import org.jetbrains.anko.centerHorizontally
//import org.jetbrains.anko.dip
//import org.jetbrains.anko.toast


class LoginActivity : BaseActivity(noWifiViewId = R.id.no_internet_layout) {
    val log = logger()

    //var parentJob = Job()
    var correctPassword: Boolean = true

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.tvShowPasswords.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvForgotPassword.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvRegister.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        binding.btnLogin.setOnClickListener {

            if (validate()) {

                correctPassword = false
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.visibility = View.GONE
                val params = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                )
//                params.setMargins(0, dip(-15), 0, 0)
//                params.below(binding.clPassword)
//                params.centerHorizontally()
                binding.tvShowPasswords.layoutParams = params

                GlobalScope.launch(/*parentJob*/) {

//                    if (NetworkChecker.isInternetConnectionAvailable()) {
//
//
//                        val userStatus = UserUtil.loginCheckUserStatus(
//                            binding.etEmail.text.toString(),
//                            binding.etPassword.text.toString()
//                        )
//                        withContext(Dispatchers.Main) {
//
//                            if (userStatus == UserStatus.ACTIVE) {
//                                InstallationKeyHandler.key.clear()
//                                if (UserUtil.user?.hasAcceptedTerms == false) {
//                                    SettingsHelper.userPasswordWithoutEncryption = binding.etPassword.text.toString()
//                                    val startIntent = Intent(this@LoginActivity, TCInvitedUserActivity::class.java)
//                                    startIntent.putExtra("email", binding.etEmail.text.toString())
//                                    startIntent.putExtra("password", binding.etPassword.text.toString())
//                                    startIntent.putExtra("goToMainActivity", true)
//                                    startActivity(startIntent)
//                                    finish()
//                                } else {
//                                    SettingsHelper.userPasswordWithoutEncryption = binding.etPassword.text.toString()
//                                    SettingsHelper.userRegisterOrLogin = true
//                                    val startIntent = Intent(this@LoginActivity, MainActivity::class.java)
//                                    startActivity(startIntent)
//                                    finish()
//                                }
//                            } else if (userStatus == UserStatus.INVITED) {
//                                val startIntent =
//                                        Intent(this@LoginActivity, TCInvitedUserActivity::class.java)
//                                startIntent.putExtra("email", binding.etEmail.text.toString())
//                                startIntent.putExtra("password", binding.etPassword.text.toString())
//                                startActivity(startIntent)
//                                finish()
//                            } else {
//                                log.info("Error while login device")
//                                App.ref.toast(R.string.login_error)
//                                binding.progressBar.visibility = View.GONE
//                                binding.btnLogin.visibility = View.VISIBLE
//                            }
//                        }
//                    }
//                    else {
//                        withContext(Dispatchers.Main) {
//                            App.ref.toast(R.string.app_generic_no_network)
//                            binding.progressBar.visibility = View.GONE
//                            binding.btnLogin.visibility = View.VISIBLE
//                        }
//                    }
                }
            }
            else {
//                if (correctPassword) {
//                    val params = RelativeLayout.LayoutParams(
//                            RelativeLayout.LayoutParams.WRAP_CONTENT,
//                            RelativeLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    params.setMargins(0, dip(-15), 0, 0)
//                    params.below(binding.clPassword)
//                    params.centerHorizontally()
//                    binding.tvShowPasswords.layoutParams = params
//                } else {
//                    val params = RelativeLayout.LayoutParams(
//                            RelativeLayout.LayoutParams.WRAP_CONTENT,
//                            RelativeLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    params.setMargins(0, 0, 0, 0)
//                    params.below(binding.clPassword)
//                    params.centerHorizontally()
//                    binding.tvShowPasswords.layoutParams = params
//                }
            }
        }

        /*binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        binding.tvShowPasswords.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT)

                MotionEvent.ACTION_UP -> binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            }
            true
        }

        if( SettingsHelper.usernameLogin != "" ) {
            binding.etEmail.text = Editable.Factory.getInstance().newEditable( SettingsHelper.usernameLogin )
        }*/

        binding.tvForgotPassword.setOnClickListener {
            val startIntent = Intent(this@LoginActivity, PasswordRecoveryActivity::class.java)
            startActivity(startIntent)
        }

        binding.tvRegister.setOnClickListener {
            val startIntent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            startActivity(startIntent)
            finish()
        }
    }

    private fun validate(): Boolean {
        //if (!validateEmail(binding.tilEmail, binding.etEmail)) return false

        if (!validateNewPassword())
            return false

        return true
    }

    private fun validateNewPassword(): Boolean {
        return true
//        return validateEditText(binding.tilPassword, binding.etPassword) { newPassword ->
//            when {
//                newPassword.isBlank() -> {
//                    correctPassword = false
//                    ValidationResult.INVALID_PASSWORD_BLANK
//                }
//                newPassword.length < 6 -> {
//                    correctPassword = false
//                    ValidationResult.INVALID_PASSWORD_MIN_6_CHARACTERS
//                }
//                else -> {
//                    correctPassword = true
//                    ValidationResult.VALID
//                }
//            }
//        }
    }

    /*override fun onPause() {
        super.onPause()
        parentJob.cancel()
    }*/

    override fun onBackPressed() {
        val intent = Intent(this@LoginActivity, IntroductionSlidePagerActivity::class.java)
        intent.putExtra("Back", true)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}