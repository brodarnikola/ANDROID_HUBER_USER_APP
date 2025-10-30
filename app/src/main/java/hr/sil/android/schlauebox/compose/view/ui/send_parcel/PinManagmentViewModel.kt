package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RPinManagement
import hr.sil.android.schlauebox.core.remote.model.RPinManagementSavePin
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.util.backend.UserUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PinManagementDialogUiState(
    val isLoading: Boolean = true,
    val pins: List<RPinManagement> = emptyList(),
    val selectedPin: RPinManagement? = null,
    val currentPinName: String = "",
    val isSavingPin: Boolean = false,
    val isDeletingPin: Boolean = false,
    val errorMessage: String? = null
)

class PinManagementDialogViewModel : ViewModel() {

    private val log = logger()
    private val _uiState = MutableStateFlow(PinManagementDialogUiState())
    val uiState: StateFlow<PinManagementDialogUiState> = _uiState.asStateFlow()

    fun loadPins(macAddress: String) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val device = MPLDeviceStore.devices[macAddress]
                val userGroup = UserUtil.userGroup

                if (device != null && userGroup != null) {
                    val combinedListOfPins = mutableListOf<RPinManagement>()

                    val generatedPinFromBackend = withContext(Dispatchers.IO) {
                        WSUser.getGeneratedPinForSendParcel(device.masterUnitId) ?: ""
                    }

                    val generatedPin = RPinManagement().apply {
                        pin = generatedPinFromBackend
                        pinGenerated = true
                        position = 0
                        pinId = 0
                        isSelected = true
                        isExtendedToDelete = false
                        isExtendedToName = false
                    }
                    combinedListOfPins.add(generatedPin)

                    val pinsFromGroup = withContext(Dispatchers.IO) {
                        WSUser.getPinManagementForSendParcel(userGroup.id, device.masterUnitId)
                    }

                    pinsFromGroup?.forEachIndexed { index, item ->
                        val savedPin = RPinManagement().apply {
                            pin = item.pin
                            pinName = item.name
                            pinGenerated = false
                            position = index + 1
                            pinId = item.id
                            isSelected = false
                            isExtendedToDelete = false
                            isExtendedToName = false
                        }
                        combinedListOfPins.add(savedPin)
                    }

                    App.ref.pinManagementSelectedItem = combinedListOfPins.first()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pins = combinedListOfPins,
                            selectedPin = combinedListOfPins.firstOrNull()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Device or user group not found"
                        )
                    }
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

    fun selectPin(pin: RPinManagement) {
        val updatedPins = _uiState.value.pins.map { p ->
            p.apply {
                isSelected = (p.pinId == pin.pinId && p.pin == pin.pin)
                if (!isSelected) {
                    isExtendedToDelete = false
                    isExtendedToName = false
                }
            }
        }

        _uiState.update {
            it.copy(
                selectedPin = pin,
                pins = updatedPins
            )
        }
        App.ref.pinManagementSelectedItem = pin
    }

    fun toggleNaming(pin: RPinManagement) {
        val updatedPins = _uiState.value.pins.map { p ->
            if (p.pinId == pin.pinId && p.pin == pin.pin) {
                p.apply {
                    isExtendedToName = !isExtendedToName
                    if (isExtendedToName) {
                        isSelected = true
                        isExtendedToDelete = false
                    }
                }
            } else {
                p.apply {
                    isExtendedToName = false
                    isExtendedToDelete = false
                    isSelected = false
                }
            }
            p
        }

        _uiState.update {
            it.copy(
                pins = updatedPins,
                selectedPin = if (pin.isExtendedToName) pin else _uiState.value.selectedPin,
                currentPinName = ""
            )
        }

        if (pin.isExtendedToName) {
            App.ref.pinManagementSelectedItem = pin
        }
    }

    fun updatePinName(name: String) {
        _uiState.update { it.copy(currentPinName = name) }
        App.ref.pinManagementName = name
    }

    fun saveGeneratedPin(macAddress: String) {
        val pinToSave = _uiState.value.pins.firstOrNull { it.pinGenerated == true }
        var pinName = _uiState.value.currentPinName

        if (pinToSave == null || pinName.isEmpty()) {
            return
        }

        _uiState.update { it.copy(isSavingPin = true) }

        viewModelScope.launch {
            try {
                val device = MPLDeviceStore.devices[macAddress]
                val userGroup = UserUtil.userGroup

                if (device != null && userGroup != null) {
                    val savePin = RPinManagementSavePin().apply {
                        groupId = userGroup.id
                        masterId = device.masterUnitId
                        pin = pinToSave.pin
                        name = pinName
                    }

                    val savedPin = withContext(Dispatchers.IO) {
                        WSUser.savePinManagementForSendParcel(savePin)
                    }

                    if (savedPin != null) {
                        log.info("Pin successfully saved")

                        val updatedPins = _uiState.value.pins.map { p ->
                            if (p.pinGenerated == true) {
                                p.apply {
                                    pinGenerated = false
                                    pinId = savedPin.id
                                    pinName = savedPin.name
                                    isExtendedToName = false
                                }
                            } else {
                                p
                            }
                        }

                        _uiState.update {
                            it.copy(
                                pins = updatedPins,
                                isSavingPin = false,
                                currentPinName = ""
                            )
                        }

                        App.ref.pinManagementSelectedItem = updatedPins.firstOrNull { !it.pinGenerated } ?: RPinManagement()
                        App.ref.pinManagementName = ""
                    } else {
                        log.info("Pin is not successfully saved")
                        _uiState.update { it.copy(isSavingPin = false) }
                    }
                }
            } catch (e: Exception) {
                log.error("Failed to save pin: ${e.message}")
                _uiState.update {
                    it.copy(
                        isSavingPin = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun toggleDelete(pin: RPinManagement) {
        val updatedPins = _uiState.value.pins.map { p ->
            if (p.pinId == pin.pinId && p.pin == pin.pin) {
                p.apply {
                    isExtendedToDelete = !isExtendedToDelete
                    if (isExtendedToDelete) {
                        isSelected = true
                        isExtendedToName = false
                    }
                }
            } else {
                p.apply {
                    isExtendedToDelete = false
                    isExtendedToName = false
                    isSelected = false
                }
            }
            p
        }

        _uiState.update {
            it.copy(
                pins = updatedPins,
                selectedPin = if (pin.isExtendedToDelete) pin else _uiState.value.selectedPin
            )
        }

        if (pin.isExtendedToDelete) {
            App.ref.pinManagementSelectedItem = pin
        }
    }

    fun deletePin(macAddress: String, pin: RPinManagement) {
        _uiState.update { it.copy(isDeletingPin = true) }

        viewModelScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    WSUser.deletePinForSendParcel(pin.pinId)
                }

                if (success) {
                    log.info("Successfully deleted pin")

                    val updatedPins = _uiState.value.pins.toMutableList()
                    updatedPins.remove(pin)

                    val wasPinSelected = _uiState.value.selectedPin?.pinId == pin.pinId &&
                            _uiState.value.selectedPin?.pin == pin.pin

                    if (wasPinSelected && updatedPins.isNotEmpty()) {
                        val newSelectedPin = updatedPins.firstOrNull {
                            it.position == pin.position - 1
                        } ?: updatedPins.firstOrNull()

                        newSelectedPin?.isSelected = true

                        _uiState.update {
                            it.copy(
                                pins = updatedPins,
                                selectedPin = newSelectedPin,
                                isDeletingPin = false
                            )
                        }

                        if (newSelectedPin != null) {
                            App.ref.pinManagementSelectedItem = newSelectedPin
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                pins = updatedPins,
                                isDeletingPin = false
                            )
                        }
                    }

                    updatedPins.forEachIndexed { index, p ->
                        if (p.position > pin.position) {
                            p.position = p.position - 1
                        }
                    }
                } else {
                    log.error("Failed to delete pin")
                    _uiState.update { it.copy(isDeletingPin = false) }
                }
            } catch (e: Exception) {
                log.error("Failed to delete pin: ${e.message}")
                _uiState.update {
                    it.copy(
                        isDeletingPin = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
}