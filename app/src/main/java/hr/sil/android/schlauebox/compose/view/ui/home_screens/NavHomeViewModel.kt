
package hr.sil.android.schlauebox.compose.view.ui.home_screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.compose.view.ui.signuponboarding_activity.SignUpOnboardingSections
//import hr.sil.android.schlauebox.cache.DataCache
//import hr.sil.android.schlauebox.cache.status.InstallationKeyHandler
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.utils.BaseViewModel
import hr.sil.android.schlauebox.utils.UiEvent
import hr.sil.android.schlauebox.utils.UiEvent.*
import hr.sil.android.schlauebox.utils.isEmailValid
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.data.ItemHomeScreen
import hr.sil.android.schlauebox.events.MPLDevicesUpdatedEvent
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.MPLDeviceStore 
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@HiltViewModel
class NavHomeViewModel @Inject constructor()  : BaseViewModel<NavHomeUiState, HomeScreenEvent>() {

    val log = logger()

    private val _uiState = MutableStateFlow(NavHomeUiState())
    val uiState: StateFlow<NavHomeUiState> = _uiState.asStateFlow()

    init {
        App.ref.eventBus.register(this)
        loadUserInfo()
    }

    private fun loadUserInfo() {
        _uiState.value = _uiState.value.copy(
            userName = UserUtil.user?.name ?: "",
            address = UserUtil.user?.address ?: ""
        )
    }

    fun loadDevices() {
        viewModelScope.launch {
            val items = getItemsForRecyclerView()
            _uiState.value = _uiState.value.copy(devices = items)
        }
    }

    private fun getItemsForRecyclerView(): List<ItemHomeScreen> {
        val items = mutableListOf<ItemHomeScreen>()

        val (splList, mplList) = MPLDeviceStore.devices.values
            .filter {
                val isThisDeviceAvailable = when {
                    UserUtil.user?.testUser == true -> true
                    else -> it.isProductionReady == true
                }
                it.masterUnitType != RMasterUnitType.UNKNOWN && isThisDeviceAvailable
            }
            .partition {
                it.masterUnitType == RMasterUnitType.SPL ||
                        it.type == MPLDeviceType.SPL ||
                        it.masterUnitType == RMasterUnitType.SPL_PLUS ||
                        it.type == MPLDeviceType.SPL_PLUS
            }

        if (splList.isNotEmpty()) {
            val header = ItemHomeScreen.Header()
            header.headerTitle = "Single" // Use string resource
            items.add(header)
            items.addAll(splList.map { ItemHomeScreen.Child(it) })
        }

        if (mplList.isNotEmpty()) {
            val header = ItemHomeScreen.Header()
            header.headerTitle = "Multiple" // Use string resource
            items.add(header)
            items.addAll(mplList.map { ItemHomeScreen.Child(it) })
        }

        return items
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: MPLDevicesUpdatedEvent) {
        loadDevices()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnauthorizedUser(event: UnauthorizedUserEvent) {
        _uiState.value = _uiState.value.copy(isUnauthorized = true)
    }
    
    override fun initialState(): NavHomeUiState {
        return NavHomeUiState()
    }

    override fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.OnForgotPasswordRequest -> {

                viewModelScope.launch {
                    _state.update { it.copy(loading = true) }
                    val response = UserUtil.passwordRecovery(
                        event.email
                    )
                    _state.update { it.copy(loading = false) }

                    log.info("Response code: ${response.code()}, is successfully: ${response.isSuccessful}, body is: ${response.body()}")

                    if (response.isSuccessful) {
                        log.info("Response code 22: ${response.code()}, is successfully: ${response.isSuccessful}, body is: ${response.body()}")
                        sendUiEvent(HomeScreenUiEvent.NavigateToNextScreen(SignUpOnboardingSections.FORGOT_PASSWORD_UPDATE_SCREEN.route))
//                        val startIntent = Intent(event.context, MainActivity1::class.java)
//                        event.context.startActivity(startIntent)
//                        event.activity.finish()
                    } else {
                        sendUiEvent(
                            ShowToast(
                                "Email doesn't exist in the system",
                                Toast.LENGTH_SHORT
                            )
                        )
                    }

                }

//                viewModelScope.launch {
//                    _state.update { it.copy(loading = true) }
//                    login(email = event.email, password = event.password, context = event.context)
//                    _state.update { it.copy(loading = false) }
//                }
            }

        }
    }

    fun getEmailError(email: String, context: Context): String {
        var emailError = ""
        if (email.isBlank()) {
            emailError = context.getString(R.string.forgot_password_error)
        } else if (!email.isEmailValid()) {
            emailError = context.getString(R.string.pickup_parcel_email_error)
        }

        return emailError
    }

}

data class NavHomeUiState(
    val loading: Boolean = false,

    val userName: String = "",
    val address: String = "",
    val devices: List<ItemHomeScreen> = emptyList(),
    val isUnauthorized: Boolean = false
)

sealed class HomeScreenEvent {
    data class OnForgotPasswordRequest(
        val email: String,
        val context: Context,
        val activity: Activity
    ) : HomeScreenEvent()
}

sealed class HomeScreenUiEvent : UiEvent {
    data class NavigateToNextScreen(val route: String) : HomeScreenUiEvent()

    object NavigateBack : HomeScreenUiEvent()
}