package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.status.ActionStatusKey
import hr.sil.android.schlauebox.cache.status.ActionStatusType
import hr.sil.android.schlauebox.core.remote.model.InstalationType
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.util.backend.UserUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

data class SendParcelDeliveryUiState(
    val isLoading: Boolean = true,
    val isSuccess: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val isUnauthorized: Boolean = false
)

class SendParcelDeliveryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SendParcelDeliveryUiState())
    val uiState: StateFlow<SendParcelDeliveryUiState> = _uiState.asStateFlow()

    private val eventBus = App.ref.eventBus

    init {
        eventBus.register(this)
    }

    override fun onCleared() {
        super.onCleared()
        eventBus.unregister(this)
    }

    fun sendParcel(macAddress: String, pin: Int, size: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                isSuccess = false,
                hasError = false,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val locker = MPLDeviceStore.devices[macAddress]
                val comunicator = locker?.createBLECommunicator(App.ref.applicationContext)
                val user = UserUtil.user

                if (size.isNotEmpty() && pin != 0 && user != null && comunicator?.connect() == true) {
                    val reducedMobilityByte = if (user.reducedMobility ?: false) 0x01.toByte() else 0x00

                    val response = if (locker.installationType == InstalationType.TABLET) {
                        comunicator.requestParcelSendCreateForTablets(
                            RLockerSize.valueOf(size),
                            user.id,
                            pin,
                            reducedMobilityByte
                        )
                    } else {
                        comunicator.requestParcelSendCreate(
                            RLockerSize.valueOf(size),
                            user.id,
                            pin
                        )
                    }

                    comunicator.disconnect()

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            if (locker.masterUnitType == RMasterUnitType.SPL) {
                                val action = ActionStatusKey().apply {
                                    keyId = locker.macAddress + ActionStatusType.SPL_OCCUPATION
                                }
                            }

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    hasError = false
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = false,
                                    hasError = true
                                )
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = false,
                                hasError = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = false,
                            hasError = true,
                            errorMessage = e.message
                        )
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnauthorizedEvent(event: UnauthorizedUserEvent) {
        _uiState.update { it.copy(isUnauthorized = true) }
    }
}