package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.cache.status.ActionStatusKey
import hr.sil.android.schlauebox.cache.status.ActionStatusType
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RCreatedLockerKey
import hr.sil.android.schlauebox.core.util.formatFromStringToDate
import hr.sil.android.schlauebox.core.util.formatToViewDateTimeDefaults
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.core.util.macCleanToReal
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.model.MPLDevice
import hr.sil.android.schlauebox.util.backend.UserUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.util.Date
data class SendParcelsOverviewUiState(
    val isLoading: Boolean = true,
    val parcels: List<RCreatedLockerKey> = emptyList(),
    val device: MPLDevice? = null,
    val deviceType: MPLDeviceType = MPLDeviceType.UNKNOWN,
    val isInBleProximity: Boolean? = null,
    val showNextButton: Boolean = true,
    val cancellingParcelId: Int? = null,
    val errorMessage: String? = null
)
class SendParcelsOverviewViewModel : ViewModel() {
    private val log = logger()
    private val _uiState = MutableStateFlow(SendParcelsOverviewUiState())
    val uiState: StateFlow<SendParcelsOverviewUiState> = _uiState.asStateFlow()
    fun loadData(macAddress: String) {
        viewModelScope.launch {
            val device = MPLDeviceStore.devices[macAddress]

            val showNextButton = !(device?.type == MPLDeviceType.SPL ||
                    (device?.type == MPLDeviceType.SPL_PLUS &&
                            device.keypadType == ParcelLockerKeyboardType.SPL))
            _uiState.update {
                it.copy(
                    device = device,
                    deviceType = device?.type ?: MPLDeviceType.UNKNOWN,
                    isInBleProximity = device?.isInBleProximity,
                    showNextButton = showNextButton
                )
            }
            loadParcels(macAddress)
        }
    }
    fun loadParcels(macAddress: String) {
        viewModelScope.launch {
            try {
                val listOfKeys = withContext(Dispatchers.IO) {
                    WSUser.getActivePaHCreatedKeys()?.filter {
                        it.getMasterBLEMacAddress() == macAddress
                    }?.toMutableList() ?: mutableListOf()
                }
                val formattedKeys = listOfKeys.map { key ->
                    key.apply {
                        timeCreated = formatCorrectDate(timeCreated)
                    }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        parcels = formattedKeys
                    )
                }
            } catch (e: Exception) {
                log.error("Error loading parcels: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
    fun cancelParcel(macAddress: String, parcel: RCreatedLockerKey) {
        _uiState.update { it.copy(cancellingParcelId = parcel.id) }
        viewModelScope.launch {
            try {
                val communicator = MPLDeviceStore.devices[parcel.lockerMasterMac.macCleanToReal()]
                    ?.createBLECommunicator(App.ref)
                val userId = UserUtil.user?.id ?: 0
                if (communicator != null && communicator.connect() && userId != 0) {
                    log.info("Connected to ${parcel.lockerMasterMac} - deleting ${parcel.lockerMac}")

                    val response = communicator.requestParcelSendCancel(parcel.lockerMac, userId)

                    if (response.isSuccessful) {
                        val action = ActionStatusKey().apply {
                            keyId = parcel.lockerId.toString() + ActionStatusType.PAH_ACCESS_CANCEL
                        }
                        log.info("Successfully deleted parcel ${parcel.lockerId}")
                        val updatedParcels = _uiState.value.parcels.toMutableList()
                        updatedParcels.remove(parcel)
                        _uiState.update {
                            it.copy(
                                parcels = updatedParcels,
                                cancellingParcelId = null
                            )
                        }
                    } else {
                        log.error("Error deleting parcel: ${response.bleDeviceErrorCode} - ${response.bleSlaveErrorCode}")
                        _uiState.update { it.copy(cancellingParcelId = null) }
                    }

                    communicator.disconnect()
                } else {
                    log.error("Error connecting to device ${parcel.lockerMac}")
                    _uiState.update { it.copy(cancellingParcelId = null) }
                }
            } catch (e: Exception) {
                log.error("Error cancelling parcel: ${e.message}")
                _uiState.update { it.copy(cancellingParcelId = null) }
            }
        }
    }
    private fun formatCorrectDate(timeCreated: String): String {
        return try {
            val fromStringToDate: Date = timeCreated.formatFromStringToDate()
            fromStringToDate.formatToViewDateTimeDefaults()
        } catch (e: ParseException) {
            log.error("Date parsing error: ${e.message}")
            timeCreated
        }
    }
}