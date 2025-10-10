
package hr.sil.android.schlauebox.compose.view.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
//import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import hr.sil.android.schlauebox.compose.view.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpOnboardingActivity : AppCompatActivity() {

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


//            Timber.d("Device-NotRooted")
//            if (viewModel.isUserLoggedIn()) {
//                val intent = Intent(this, MainActivity::class.java).apply {
//                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) }
//                startActivity(intent)
//                finish()
//            } else {
                setContent {
                    AppTheme {
                        SignUpOnboardingApp(viewModel)
                    }
                }
//            }

        //viewModel.onInit()
    }

}
