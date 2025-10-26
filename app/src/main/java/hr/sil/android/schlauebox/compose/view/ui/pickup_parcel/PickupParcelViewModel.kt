package hr.sil.android.schlauebox.compose.view.ui.pickup_parcel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.BuildConfig
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.DatabaseHandler
import hr.sil.android.schlauebox.core.ble.comm.model.LockerFlagsUtil
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.core.util.*
import hr.sil.android.schlauebox.data.DeliveryKey
import hr.sil.android.schlauebox.events.MPLDevicesUpdatedEvent
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.model.MPLDevice
import hr.sil.android.schlauebox.util.NotificationHelper
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.util.general.extensions.hexToByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class PickupParcelViewModel @Inject constructor() : ViewModel() {

    val log = logger()

    private val _uiState = MutableStateFlow(PickupParcelUiState())
    val uiState: StateFlow<PickupParcelUiState> = _uiState.asStateFlow()

    private val connecting = AtomicBoolean(false)
    private val lockerLoaderRunning = AtomicBoolean(false)
    private val startingTime = Date()
    private var exitTime: Date? = null
    private val denyProcedureDuration = 60000L

    private val MAC_ADDRESS_7_BYTE_LENGTH = 14
    private val MAC_ADDRESS_6_BYTE_LENGTH = 12
    private val MAC_ADDRESS_LAST_BYTE_LENGTH = 2

    private var device: MPLDevice? = null
    private val openedParcels = mutableListOf<String>()
    private var keyPurpose = RLockerKeyPurpose.UNKNOWN

    init {
        App.ref.eventBus.register(this)
    }

    fun loadPickupParcel(context: Context, macAddress: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(macAddress = macAddress)
            device = MPLDeviceStore.devices[macAddress]

            val keys = combineLockerKeys(macAddress)
            _uiState.value = _uiState.value.copy(keys = keys)

            updateOpenButtonState(context)
        }
    }

    private suspend fun combineLockerKeys(macAddress: String): List<RCreatedLockerKey> {
        val keysAssignedToUser = MPLDeviceStore.devices[macAddress]?.activeKeys?.filter {
            it.lockerMasterMac.macCleanToReal() == macAddress && it.purpose != RLockerKeyPurpose.PAH && isUserPartOfGroup(it.createdForGroup, it.createdForId)
        }?.map {
            RCreatedLockerKey().apply {
                this.id = it.id
                this.createdById = it.createdById
                this.lockerMac = it.lockerMac
                this.lockerId = it.lockerId
                this.lockerMasterId = it.lockerMasterId
                this.lockerMasterMac = it.lockerMasterMac
                this.createdByName = it.createdByName
                this.purpose = it.purpose
                this.masterAddress = it.masterAddress
                this.masterName = it.masterName
                this.lockerSize = it.lockerSize
                this.timeCreated = it.timeCreated ?: ""
            }
        }?.toMutableList() ?: mutableListOf()

        val remotePaFKeys = WSUser.getActivePaFCreatedKeys()
        val createdPaFKeys = remotePaFKeys?.filter { it.lockerMasterMac.macCleanToReal() == macAddress } ?: mutableListOf()
        keysAssignedToUser.addAll(createdPaFKeys)

        return keysAssignedToUser.sortedByDescending { it.purpose }.sortedBy { it.timeCreated }
    }

    private fun isUserPartOfGroup(createdForGroup: Int?, createdForId: Int?): Boolean {
        return UserUtil.userMemberships.find { it.groupId == createdForGroup && it.role == RUserAccessRole.ADMIN.name } != null ||
                UserUtil.userGroup?.id == createdForGroup ||
                UserUtil.user?.id == createdForId
    }

    fun openParcel(context: Context) {
        if (!connecting.compareAndSet(false, true)) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                imageIcon = 2,
                isConnecting = true,
                statusText = context.getString(R.string.nav_pickup_parcel_connecting).uppercase(),
                statusDescription = context.getString(R.string.nav_pickup_parcel_connecting_please_wait).uppercase()
            )

            val userId = UserUtil.user?.id
            val macAddress = _uiState.value.macAddress

            if (isOpenDoorPossible(macAddress) && userId != null) {
                val communicator = MPLDeviceStore.devices[macAddress]?.createBLECommunicator(context)

                if (communicator?.connect() == true) {
                    var actionSuccessful = true
                    var lockerMacAddress = ""

                    MPLDeviceStore.devices[macAddress]?.activeKeys?.filter {
                        it.purpose == RLockerKeyPurpose.DELIVERY || it.purpose == RLockerKeyPurpose.PAF
                    }?.forEach { key ->
                        val bleResponse = communicator.requestParcelPickup(key.lockerMac, userId)
                        keyPurpose = key.purpose

                        if (!bleResponse.isSuccessful) {
                            actionSuccessful = false
                        } else {
                            withContext(Dispatchers.Main) {
                                lockerMacAddress = key.lockerMac
                                openedParcels.add(key.lockerMac)
                                persistActionOpenKey(macAddress, key.id)
                                NotificationHelper.clearNotification()

                                val updatedKeys = _uiState.value.keys.filter { it.lockerMac != key.lockerMac }
                                _uiState.value = _uiState.value.copy(keys = updatedKeys)
                            }
                        }
                    }

                    communicator.disconnect()

                    withContext(Dispatchers.Main) {
                        if (actionSuccessful) {
                            val showCleaning = MPLDeviceStore.devices[macAddress]?.activeKeys?.size == 1 &&
                                    device?.installationType == InstalationType.TABLET

                            _uiState.value = _uiState.value.copy(
                                imageIcon = 1,
                                isConnecting = false,
                                isUnlocked = true,
                                statusText = context.getString(R.string.nav_pickup_parcel_unlock).uppercase(),
                                statusDescription = context.getString(R.string.nav_pickup_parcel_content_unlock),
                                showFinishButton = true,
                                showForceOpenButton = true,
                                showCleaningCheckbox = showCleaning,
                                lastOpenedLockerMac = lockerMacAddress
                            )
                        } else {
                            setUnsuccessfulOpenView(context)
                        }
                    }
                } else {
                    communicator?.disconnect()
                    withContext(Dispatchers.Main) {
                        setUnsuccessfulOpenView(context)
                    }
                }
                denyOpenProcedure()
            } else {
                _uiState.value = _uiState.value.copy(
                    imageIcon = 0,
                    isConnecting = false,
                    statusText = context.getString(R.string.nav_pickup_parcel_lock).uppercase(),
                    statusDescription = context.getString(R.string.nav_pickup_parcel_content_lock)
                )
            }

            connecting.set(false)
        }
    }

    fun forceOpen(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isForceOpening = true)

            val macAddress = _uiState.value.macAddress
            val communicator = device?.createBLECommunicator(context)

            if (communicator?.connect() == true) {
                openedParcels.forEach { lockerMac ->
                    communicator.forceOpenDoor(lockerMac)
                }
                communicator.disconnect()
            }

            _uiState.value = _uiState.value.copy(isForceOpening = false)
        }
    }

    fun setLockerCleaning(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCleaningLocker = true)

            val macAddress = _uiState.value.macAddress
            val lockerMacAddress = _uiState.value.lastOpenedLockerMac
            val communicator = MPLDeviceStore.devices[macAddress]?.createBLECommunicator(context)

            if (communicator?.connect() == true) {
                val lockerMacAddressList = mutableListOf<LockerFlagsUtil.LockerInfo>()
                val lockerInfo = LockerFlagsUtil.LockerInfo(byteArrayOf(), byteArrayOf())

                when {
                    lockerMacAddress.length == MAC_ADDRESS_7_BYTE_LENGTH -> {
                        lockerInfo.mac = lockerMacAddress.take(MAC_ADDRESS_6_BYTE_LENGTH).macCleanToBytes().reversedArray()
                        lockerInfo.index = lockerMacAddress.takeLast(MAC_ADDRESS_LAST_BYTE_LENGTH).hexToByteArray()
                    }
                    else -> {
                        lockerInfo.mac = lockerMacAddress.macCleanToBytes().reversedArray()
                        lockerInfo.index = byteArrayOf(0x00)
                    }
                }

                lockerMacAddressList.add(lockerInfo)
                val byteArrayCleaningNeeded = LockerFlagsUtil.generateCleaningRequiredData(lockerMacAddressList, true)
                val response = communicator.lockerIsDirty(byteArrayCleaningNeeded)

                communicator.disconnect()

                _uiState.value = _uiState.value.copy(
                    isCleaningLocker = false,
                    cleaningCheckboxEnabled = !response
                )
            } else {
                _uiState.value = _uiState.value.copy(isCleaningLocker = false)
            }
        }
    }

    private fun denyOpenProcedure() {
        if (lockerLoaderRunning.compareAndSet(false, true)) {
            viewModelScope.launch(Dispatchers.Default) {
                val time = exitTime?.time ?: 0L
                val compare = abs(time - startingTime.time)
                var timeForOpen = denyProcedureDuration
                if (compare in 1..denyProcedureDuration) {
                    timeForOpen = denyProcedureDuration - compare
                }
                delay(timeForOpen)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(showForceOpenButton = false)
                }
            }
        }
    }

    private fun setUnsuccessfulOpenView(context: Context) {
        _uiState.value = _uiState.value.copy(
            imageIcon = 0,
            isConnecting = false,
            isUnlocked = false,
            statusText = context.getString(R.string.nav_pickup_parcel_lock).uppercase(),
            statusDescription = context.getString(R.string.nav_pickup_parcel_content_lock)
        )
    }

    private fun persistActionOpenKey(macAddress: String, id: Int) {
        val deliveryKeys = DatabaseHandler.deliveryKeyDb.get(macAddress)
        if (deliveryKeys == null) {
            DatabaseHandler.deliveryKeyDb.put(DeliveryKey(macAddress, listOf(id)))
        } else {
            if (!deliveryKeys.keyIds.contains(id)) {
                val listOfIds = deliveryKeys.keyIds.plus(id)
                DatabaseHandler.deliveryKeyDb.put(DeliveryKey(macAddress, listOfIds))
            }
        }
    }

    private fun isOpenDoorPossible(macAddress: String): Boolean {
        var hasUnusedKeys = false
        val keys = DatabaseHandler.deliveryKeyDb.get(macAddress)

        if (keys == null) {
            return device?.activeKeys?.filter { it.purpose != RLockerKeyPurpose.PAH }?.isNotEmpty() ?: false
        } else {
            device?.activeKeys?.forEach {
                if (it.purpose != RLockerKeyPurpose.PAH && !keys.keyIds.contains(it.id)) {
                    hasUnusedKeys = true
                    return@forEach
                }
            }
        }

        return device?.isInBleProximity ?: false && device?.hasUserRightsOnLocker() ?: false && hasUnusedKeys
    }

    fun updateOpenButtonState(context: Context) {
        val macAddress = _uiState.value.macAddress

        if (device == null) {
            _uiState.value = _uiState.value.copy(
                statusText = context.getString(R.string.nav_pickup_parcel_unlock),
                //statusDescription = context.getString(R.string.nav_pickup_parcel_content_unlock)
            )
        } else {
            if (device?.isInBleProximity == true && isOpenDoorPossible(macAddress)) {
                _uiState.value = _uiState.value.copy(
                    imageIcon = 0,
                    isUnlocked = false,
                    statusText = context.getString(R.string.nav_pickup_parcel_lock),
                    //statusDescription = context.getString(R.string.nav_pickup_parcel_content_lock)
                )
            } else {
                if (device?.isInBleProximity == true) {
                    _uiState.value = _uiState.value.copy(
                        imageIcon = 1,
                        isUnlocked = true,
                        statusText = context.getString(R.string.nav_pickup_parcel_unlock),
                        //statusDescription = context.getString(R.string.nav_pickup_parcel_content_unlock)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        imageIcon = 2,
                        isUnlocked = false,
                        statusText = context.getString(R.string.app_generic_enter_ble),
                        //statusDescription = context.getString(R.string.app_generic_enter_ble).uppercase()
                    )
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnauthorizedUser(event: UnauthorizedUserEvent) {
        _uiState.value = _uiState.value.copy(isUnauthorized = true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceUpdate(event: MPLDevicesUpdatedEvent) {
        viewModelScope.launch {
            device = MPLDeviceStore.devices.values.find { it.macAddress == _uiState.value.macAddress }
        }
    }

    fun onPause() {
        exitTime = Date()
    }

    override fun onCleared() {
        super.onCleared()
        App.ref.eventBus.unregister(this)
    }
}

data class PickupParcelUiState(
    val macAddress: String = "",
    val keys: List<RCreatedLockerKey> = emptyList(),
    val isConnecting: Boolean = false,
    val isUnlocked: Boolean = false,
    val imageIcon: Int = 0,
    val isForceOpening: Boolean = false,
    val isCleaningLocker: Boolean = false,
    val statusText: String = "",
    val statusDescription: String = "",
    val showFinishButton: Boolean = false,
    val showForceOpenButton: Boolean = false,
    val showCleaningCheckbox: Boolean = false,
    val cleaningCheckboxEnabled: Boolean = true,
    val lastOpenedLockerMac: String = "",
    val isUnauthorized: Boolean = false
)
