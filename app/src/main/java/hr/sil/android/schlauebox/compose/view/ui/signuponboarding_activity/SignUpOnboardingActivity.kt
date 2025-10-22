package hr.sil.android.schlauebox.compose.view.ui.signuponboarding_activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.compose.view.ui.main_activity.MainActivity
import hr.sil.android.schlauebox.compose.view.ui.theme.AppTheme
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.preferences.PreferenceStore
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.util.backend.UserUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpOnboardingActivity : ComponentActivity() {

    private val log = logger()
    val SPLASH_START = "SPLASH_START"
    private val SPLASH_DISPLAY_LENGTH = 3000L
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var startupBeginTimestamp: Long = 0L
    private var permissionRequestGranted = false
    private val viewModel: SignUpOnboardingViewModel by viewModels()

    private var showSplashScreenState = true
    private val dismissSplashScreenDelay = 200L

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        //val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

//        var phoneNumberFromContacts: String? = null
//        val intent = intent
//        if (intent.data != null) {
//            phoneNumberFromContacts = intent.data.toString().substringAfter(":")
//        }

        // This app draws behind the system bars, so we want to handle fitting system windows
//         WindowCompat.setDecorFitsSystemWindows(window, true)

        //splashScreen.setKeepOnScreenCondition { showSplashScreenState }
        lifecycleScope.launch {
            viewModel.updateFcmToken()
            delay(dismissSplashScreenDelay)
            showSplashScreenState = false
        }


        startApp(true)

//            Timber.d("Device-NotRooted")
//            if (viewModel.isUserLoggedIn()) {
//                val intent = Intent(this, MainActivity1::class.java).apply {
//                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) }
//                startActivity(intent)
//                finish()
//            } else {
//                setContent {
//                    AppTheme {
//                        SignUpOnboardingApp(viewModel)
//                    }
//                }
//            }

        //viewModel.onInit()
    }

    private fun startApp(isInitial: Boolean = false) {

//        if (!checkPermissions()) {
//            if (isInitial) {
//                requestPermission()
//            } else {
//                showError(R.string.allow_permission)
//            }
//            return
//        }

        GlobalScope.launch(Dispatchers.Main) {
            checkSplashDelay()

            delay(SPLASH_DISPLAY_LENGTH)

            val startupClass: Class<*>
            if (SettingsHelper.firstRun) {
                Log.i("SplashActivity", "This is first start")
                if (!SettingsHelper.userRegisterOrLogin) {
                    setupSystemLanguage()
                }
                setContent {
                    AppTheme {
                        SignUpOnboardingApp(viewModel)
                    }
                }
                //startupClass = IntroductionSlidePagerActivity::class.java
            } else {

                if (!PreferenceStore.userHash.isNullOrBlank() && SettingsHelper.userRegisterOrLogin) {
                    if (UserUtil.login(SettingsHelper.usernameLogin.toString())) {

                        val startIntent =
                            Intent(this@SignUpOnboardingActivity, MainActivity::class.java)
                        startIntent.putExtra(SPLASH_START, App.Companion.ref.isFirstStart)
                        startActivity(startIntent)
                        finish()
                    } else {
                        if (!SettingsHelper.userRegisterOrLogin) {
                            setupSystemLanguage()
                        }

                        setContent {
                            AppTheme {
                                SignUpOnboardingApp(viewModel)
                            }
                        }
                    }

                } else {
                    if (!SettingsHelper.userRegisterOrLogin) {
                        setupSystemLanguage()
                    }
                    setContent {
                        AppTheme {
                            SignUpOnboardingApp(viewModel)
                        }
                    }
                    //LoginActivity::class.java
                }
                Log.i("SplashActivity", "This is second start")
                App.Companion.ref.isFirstStart = false
            }

//            val startIntent = Intent(this@SignUpOnboardingActivity, startupClass)
//            startIntent.putExtra(SPLASH_START, App.ref.isFirstStart)
//            startActivity(startIntent)
//            finish()
        }
    }

    private suspend fun checkSplashDelay() {
        val duration = System.currentTimeMillis() - startupBeginTimestamp
        if (duration < SPLASH_DISPLAY_LENGTH) {
            delay(SPLASH_DISPLAY_LENGTH - duration)
        }
    }

    private fun setupSystemLanguage() {
        val systemLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getResources().getConfiguration().getLocales().get(0).language.toString()
        } else {
            getResources().getConfiguration().locale.language.toString()
        }

        log.info("System language is: ${systemLanguage}")
        log.info("Shared preference language is: ${SettingsHelper.languageName}")

        if (systemLanguage == "de") {
            SettingsHelper.languageName = "DE"
        } else if (systemLanguage == "fr") {
            SettingsHelper.languageName = "FR"
        } else if (systemLanguage == "it") {
            SettingsHelper.languageName = "IT"
        } else {
            SettingsHelper.languageName = "EN"
        }
    }

}