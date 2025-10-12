package hr.sil.android.schlauebox.compose.view.ui

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SignUpOnboardingViewModel : ViewModel() {

//    val selectedTheme = MutableStateFlow(Theme.LIGHT)
//
//    val appAuthUnlockMethods  = mutableListOf<AuthType>()
//    private val _currentAppAuthUnlockMethod: MutableState<AuthType?> = mutableStateOf(null)
//    val currentAppAuthUnlockMethod: State<AuthType?>
//        get() = _currentAppAuthUnlockMethod

    init {
        //selectedTheme.value = sp.getSelectedTheme()
    }

    fun isUserLoggedIn(): Boolean {
        return true
        //return sp.getIsUserLoggedIn()
    }

    suspend fun updateFcmToken() {
        //ss.setFcmToken(FirebaseMessaging.getInstance().token.await())
    }

    fun onInit() {
        // set local DB encryption key (if it doesn't exist)
//        if (ss.getLocalDbKey().isEmpty()) {
//            ss.setLocalDbKey(encryptionHelper.generateAesKey())
//        }
//
//        if (!PRO_VERSION_GOD_MODE) {
//            billingManagerUtil.getUserSubscriptions()
//
//            billingManagerUtil.getAllSkuDetails(object : SkuDetailsListener {
//                override fun onProductDetailsUpdated() {
//                    Timber.d("${BillingManagerUtil.TAG} - SignUpOnboardingViewModel - onProductDetailsUpdated")
//                }
//
//                override fun onSkuDetailsFail(e: Exception?) {
//                    Timber.d("${BillingManagerUtil.TAG} - SignUpOnboardingViewModel - onSkuDetailsFail: ${e?.localizedMessage}")
//                }
//            })
//        }
    }
}
