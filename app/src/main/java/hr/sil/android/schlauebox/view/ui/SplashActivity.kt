package hr.sil.android.schlauebox.view.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.preferences.PreferenceStore
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.view.ui.intro.IntroductionSlidePagerActivity
import kotlinx.coroutines.*
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.databinding.ActivityLoginBinding
import hr.sil.android.schlauebox.databinding.ActivitySplashBinding
import hr.sil.android.schlauebox.fcm.MPLFireBaseMessagingService
import hr.sil.android.schlauebox.store.DeviceStoreRemoteUpdater
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.util.awaitForResult


class SplashActivity : AppCompatActivity() {
    private val log = logger()
    val SPLASH_START = "SPLASH_START"
    private val SPLASH_DISPLAY_LENGTH = 3000L
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var startupBeginTimestamp: Long = 0L
    private var permissionRequestGranted = false

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding.btnRetry.setOnClickListener {
            binding.btnRetry.isEnabled = false
            hideError()
            startApp(true)
        }
 
        hideError()
        startApp(true) 
    }

    private fun hideError() {
        binding.tvErrorMessage.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
        setProgressVisible(true)
    }

    private fun setProgressVisible(visible: Boolean) {
        binding.progresbar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun showError( @StringRes errorText: Int) {
        setProgressVisible(false)
        binding.tvErrorMessage.text = resources.getString(errorText)
        binding.tvErrorMessage.visibility = View.VISIBLE
        binding.btnRetry.visibility = View.VISIBLE
        binding.btnRetry.isEnabled = true
    }

    override fun onResume() {
        super.onResume()

        GlobalScope.launch {
//            log.info("Started to fetch token")
//            val task = FirebaseMessaging.getInstance().token.awaitForResult()
//
//            log.info("Started to fetch token 22 ${task}")
//            if (!task.isSuccessful) {
//                log.info("getInstanceId failed", task.exception)
//            }
//            // Get new Instance ID token
//            val token = task.result
//            if (token != null) {
//                log.info("FCM token: $token")
//                MPLFireBaseMessagingService.sendRegistrationToServer(token)
//            } else {
//                log.error("Error while fetching the FCM token!")
//            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: kotlin.IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if( grantResults.isNotEmpty() ) {
            permissionRequestGranted =
                    requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        GlobalScope.launch(Dispatchers.Main) {
            MPLDeviceStore.clear()
            DataCache.getDevicesInfo(true)

            DeviceStoreRemoteUpdater.forceUpdate()
            hideError()
            startApp()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun checkPermissions(): Boolean {
        return if (permissionRequestGranted) true
        else ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun checkSplashDelay() {
        val duration = System.currentTimeMillis() - startupBeginTimestamp
        if (duration < SPLASH_DISPLAY_LENGTH) {
            delay(SPLASH_DISPLAY_LENGTH - duration)
        }
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
                if( !SettingsHelper.userRegisterOrLogin ) {
                    setupSystemLanguage()
                }
                startupClass = IntroductionSlidePagerActivity::class.java
            } else {
                startupClass = if (!PreferenceStore.userHash.isNullOrBlank() && SettingsHelper.userRegisterOrLogin) {
                    if (UserUtil.login(SettingsHelper.usernameLogin.toString())) {
                        MainActivity::class.java

                    } else {
                        if( !SettingsHelper.userRegisterOrLogin ) {
                            setupSystemLanguage()
                        }
                        LoginActivity::class.java
                    }

                } else {
                    if( !SettingsHelper.userRegisterOrLogin ) {
                        setupSystemLanguage()
                    }
                    LoginActivity::class.java
                }
                Log.i("SplashActivity", "This is second start")
                App.ref.isFirstStart = false
            }

            val startIntent = Intent(this@SplashActivity, startupClass)
            startIntent.putExtra(SPLASH_START, App.ref.isFirstStart)
            startActivity(startIntent)
            finish()
        }
    }

    private fun setupSystemLanguage() {
        val systemLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            getResources().getConfiguration().getLocales().get(0).language.toString()
        } else{
            getResources().getConfiguration().locale.language.toString()
        }

        log.info("System language is: ${systemLanguage}")
        log.info("Shared preference language is: ${SettingsHelper.languageName}")

        if( systemLanguage == "de" ) {
            SettingsHelper.languageName = "DE"
        }
        else if( systemLanguage == "fr" ) {
            SettingsHelper.languageName = "FR"
        }
        else if( systemLanguage == "it" ) {
            SettingsHelper.languageName = "IT"
        }
        else {
            SettingsHelper.languageName = "EN"
        }
    }

}
