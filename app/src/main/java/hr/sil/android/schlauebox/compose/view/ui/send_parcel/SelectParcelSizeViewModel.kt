package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.events.MPLDevicesUpdatedEvent
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.model.MPLDevice
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

data class SelectParcelSizeUiState(
    val isLoading: Boolean = false,
    val availableSizes: Map<RLockerSize, Int> = emptyMap(),
    val selectedSize: RLockerSize = RLockerSize.UNKNOWN,
    val showNextButton: Boolean = false,
    val showSizeSelection: Boolean = false,
    val deviceType: String = "",
    val device: MPLDevice? = null,
    val isUnauthorized: Boolean = false,
    val errorMessage: String? = null
)

class SelectParcelSizeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SelectParcelSizeUiState())
    val uiState: StateFlow<SelectParcelSizeUiState> = _uiState.asStateFlow()

    private var currentMacAddress: String = ""
    private val eventBus = App.ref.eventBus

    init {
        eventBus.register(this)
        App.ref.pinManagementName = ""
    }

    override fun onCleared() {
        super.onCleared()
        eventBus.unregister(this)
    }

    fun loadAvailableLockers(macAddress: String) {
        currentMacAddress = macAddress
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val device = MPLDeviceStore.devices[macAddress]
                val masterUnitId = device?.masterUnitId ?: 0

                val availableLockers = withContext(Dispatchers.IO) {
                    WSUser.getAvailableLockerSizes(masterUnitId) ?: emptyList()
                }

                val sizesMap = mutableMapOf<RLockerSize, Int>()
                RLockerSize.values().forEach { size ->
                    val count = availableLockers
                        .filter { it.size == size && it.count > 0 }
                        .firstOrNull()?.count ?: 0
                    sizesMap[size] = count
                }

                val totalAvailable = sizesMap.values.sum()
                val isInProximity = device?.isInBleProximity ?: false
                val showNext = totalAvailable > 0 && isInProximity

                val deviceTypeStr = when {
                    device?.type == MPLDeviceType.SPL_PLUS -> "SPL_PLUS"
                    device?.masterUnitType == RMasterUnitType.SPL -> "SPL"
                    else -> ""
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        availableSizes = sizesMap,
                        showNextButton = showNext,
                        showSizeSelection = true,
                        deviceType = deviceTypeStr,
                        device = device
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun selectSize(size: RLockerSize) {
        _uiState.update { it.copy(selectedSize = size) }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnauthorizedEvent(event: UnauthorizedUserEvent) {
        _uiState.update { it.copy(isUnauthorized = true) }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMPLDevicesUpdated(event: MPLDevicesUpdatedEvent) {
        if (currentMacAddress.isNotEmpty()) {
            loadAvailableLockers(currentMacAddress)
        }
    }
}