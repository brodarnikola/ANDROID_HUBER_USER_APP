package hr.sil.android.schlauebox.compose.view.ui.home_screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.core.util.macCleanToReal
import hr.sil.android.schlauebox.core.util.macRealToClean
import hr.sil.android.schlauebox.events.MPLDevicesUpdatedEvent
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.DeviceStoreRemoteUpdater
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.model.MPLDevice
import hr.sil.android.schlauebox.util.AppUtil
import hr.sil.android.util.general.extensions.format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DeviceDetailsViewModel : ViewModel() {

    val log = logger()

    private val _uiState = MutableStateFlow(DeviceDetailsUiState())
    val uiState: StateFlow<DeviceDetailsUiState> = _uiState.asStateFlow()

    init {
        App.ref.eventBus.register(this)
    }

    fun loadDeviceDetails(macAddress: String) {
        viewModelScope.launch {
            val device = MPLDeviceStore.devices[macAddress]
            val hasUserRights = device?.hasUserRightsOnLocker() ?: false

            _uiState.value = _uiState.value.copy(
                macAddress = macAddress,
                deviceName = device?.name ?: "",
                deviceAddress = device?.address ?: "",
                hasUserRights = hasUserRights,
                isInBleProximity = device?.isInBleProximity ?: false,
                deviceType = device?.type ?: MPLDeviceType.SPL
            )

            loadAdditionalData(macAddress, device)
            updateTelemetry(device)
            updateButtonStates(device)
        }
    }

    private suspend fun loadAdditionalData(macAddress: String, device: MPLDevice?) {
        val activeRequests = WSUser.getActiveRequests()?.filter {
            it.masterMac.macCleanToReal() == macAddress
        } ?: listOf()

        val pahKeys = WSUser.getActivePaHCreatedKeys()?.filter {
            it.lockerMasterMac.macCleanToReal() == macAddress
        } ?: listOf()

        val activeKeys = WSUser.getActiveKeysForLocker(
            device?.macAddress?.macRealToClean() ?: ""
        )

        val availableLockers = WSUser.getAvailableLockerSizes(
            device?.masterUnitId ?: 0
        ) ?: listOf()

        _uiState.value = _uiState.value.copy(
            activeRequests = activeRequests,
            pahKeys = pahKeys,
            activeKeysForLocker = activeKeys ?: listOf(),
            availableLockers = availableLockers
        )
    }

    private fun updateTelemetry(device: MPLDevice?) {
        _uiState.value = _uiState.value.copy(
            rssi = device?.modemRssi?.toString() ?: "-",
            temperature = device?.temperature?.format(1) ?: "-",
            pressure = device?.pressure?.format(1) ?: "-",
            humidity = device?.humidity?.format(1) ?: "-"
        )
    }

    private fun updateButtonStates(device: MPLDevice?) {
        val hasUserRights = device?.hasUserRightsOnLocker() ?: false
        val isInProximity = device?.isInBleProximity ?: false
        val isSplOrSplPlus = device?.type == MPLDeviceType.SPL_PLUS || device?.type == MPLDeviceType.SPL

        _uiState.value = _uiState.value.copy(
            showForceOpenButton = isInProximity && isSplOrSplPlus,
            showEditButton = device?.accessTypes?.any {
                it == RMasterUnitAccessType.BY_GROUP_OWNERSHIP
            } ?: false,
            pickupButtonEnabled = hasUserRights,
            sendParcelButtonEnabled = getSendParcelAccessibility(device),
            accessSharingButtonEnabled = !(device?.accessTypes?.any {
                it == RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_USER ||
                        it == RMasterUnitAccessType.BY_ACTIVE_PAF_KEY
            } ?: false),
            showRequestAccessButton = !hasUserRights && _uiState.value.activeRequests.isEmpty(),
            showRequestPendingText = !hasUserRights && _uiState.value.activeRequests.isNotEmpty()
        )
    }

    private fun getSendParcelAccessibility(device: MPLDevice?): Boolean {
        if (device == null) return false

        val hasSharedKeys = hasUserShareKeys()
        val isAccessible = device.accessTypes.any {
            //it == RMasterUnitAccessType.BY_INSTALLATION_KEY ||
                    it == RMasterUnitAccessType.BY_GROUP_OWNERSHIP ||
                    it == RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_ADMIN
        }

        return isAccessible || hasSharedKeys || _uiState.value.availableLockers.isNotEmpty()
    }

    private fun hasUserShareKeys(): Boolean {
        val activeKeys = _uiState.value.activeKeysForLocker
        val pahKeys = _uiState.value.pahKeys

        if (activeKeys.isEmpty() || pahKeys.isEmpty()) return false

        for (activeKey in activeKeys) {
            for (pahKey in pahKeys) {
                if (activeKey.createdById == pahKey.createdById) {
                    return true
                }
            }
        }
        return false
    }

    fun requestAccess() {
        val macAddress = _uiState.value.macAddress
        val device = MPLDeviceStore.devices[macAddress]

        _uiState.value = _uiState.value.copy(
            isRequestingAccess = true
        )

        viewModelScope.launch {
            val success = if (device?.masterUnitType == RMasterUnitType.SPL ||
                device?.type == MPLDeviceType.SPL ||
                device?.type == MPLDeviceType.SPL_PLUS) {
                WSUser.activateSPL(macAddress.macRealToClean())
            } else {
                WSUser.requestMPlAccess(macAddress.macRealToClean())
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    AppUtil.refreshCache()
                    DeviceStoreRemoteUpdater.forceUpdate()
                    loadDeviceDetails(macAddress)
                }

                _uiState.value = _uiState.value.copy(
                    isRequestingAccess = false,
                    requestAccessSuccess = success
                )
            }
        }
    }

    fun forceOpenDevice(context: Context) {
        val macAddress = _uiState.value.macAddress
        val device = MPLDeviceStore.devices[macAddress]

        _uiState.value = _uiState.value.copy(
            isForceOpening = true
        )

        viewModelScope.launch {
            var lockersList = mutableListOf<String>()

            if (device?.type == MPLDeviceType.SPL) {
                lockersList.add(device.macAddress)
            } else {
                val lockerList = WSUser.getLockerFromMasterUnit(
                    device?.macAddress?.macRealToClean() ?: ""
                )
                if (lockerList?.isNotEmpty() == true) {
                    lockersList.addAll(
                        lockerList.filter { !it.isDeleted }
                            .map { it.mac.macCleanToReal() }
                    )
                }
            }

            val communicator = device?.createBLECommunicator(context)
            var bleResponse = false

            if (communicator?.connect() == true) {
                log.info("Requesting force pickup for $macAddress")

                if (device.type == MPLDeviceType.SPL ||
                    (device.type == MPLDeviceType.SPL_PLUS &&
                            device.keypadType == hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType.SPL)) {
                    if (lockersList.isNotEmpty()) {
                        bleResponse = communicator.forceOpenDoor(
                            lockersList.first().macRealToClean() ?: ""
                        )
                    }
                } else {
                    for (lockerMac in lockersList) {
                        bleResponse = communicator.forceOpenDoor(lockerMac.macRealToClean())
                    }
                }

                if (bleResponse) {
                    log.info("Success force open on $macAddress")
                } else {
                    log.error("Failed to force open: $bleResponse")
                }

                communicator.disconnect()
            } else {
                log.error("Error while connecting the device")
            }

            communicator?.disconnect()

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    isForceOpening = false,
                    forceOpenSuccess = bleResponse
                )
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: MPLDevicesUpdatedEvent) {
        if (_uiState.value.macAddress.isNotEmpty()) {
            loadDeviceDetails(_uiState.value.macAddress)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnauthorizedUser(event: UnauthorizedUserEvent) {
        _uiState.value = _uiState.value.copy(isUnauthorized = true)
    }

    override fun onCleared() {
        super.onCleared()
        App.ref.eventBus.unregister(this)
        App.ref.pinManagementName = ""
    }
}

data class DeviceDetailsUiState(
    val macAddress: String = "",
    val deviceName: String = "",
    val deviceAddress: String = "",
    val hasUserRights: Boolean = false,
    val isInBleProximity: Boolean = false,
    val deviceType: MPLDeviceType = MPLDeviceType.SPL,

    val rssi: String = "-",
    val temperature: String = "-",
    val pressure: String = "-",
    val humidity: String = "-",

    val activeRequests: List<RAccessRequest> = emptyList(),
    val pahKeys: List<RCreatedLockerKey> = emptyList(),
    val activeKeysForLocker: List<RLockerKey> = emptyList(),
    val availableLockers: List<RAvailableLockerSize> = emptyList(),

    val showForceOpenButton: Boolean = false,
    val showEditButton: Boolean = false,
    val pickupButtonEnabled: Boolean = false,
    val sendParcelButtonEnabled: Boolean = false,
    val accessSharingButtonEnabled: Boolean = false,
    val showRequestAccessButton: Boolean = false,
    val showRequestPendingText: Boolean = false,

    val isRequestingAccess: Boolean = false,
    val isForceOpening: Boolean = false,
    val requestAccessSuccess: Boolean = false,
    val forceOpenSuccess: Boolean = false,
    val isUnauthorized: Boolean = false
)
