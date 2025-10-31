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
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val device = MPLDeviceStore.devices[macAddress]
                val userGroup = UserUtil.userGroup

                if (device == null || userGroup == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Device or user group not found"
                        )
                    }
                    return@launch
                }

                val combinedListOfPins = mutableListOf<RPinManagement>()

                // Get generated pin from backend
                val generatedPinFromBackend = withContext(Dispatchers.IO) {
                    WSUser.getGeneratedPinForSendParcel(device.masterUnitId) ?: ""
                }

                if (generatedPinFromBackend.isNotEmpty()) {
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
                }

                // Get saved pins from group
                val pinsFromGroup = withContext(Dispatchers.IO) {
                    WSUser.getPinManagementForSendParcel(userGroup.id, device.masterUnitId)
                }

                pinsFromGroup?.forEachIndexed { index, item ->
                    val savedPin = RPinManagement().apply {
                        pin = item.pin
                        pinName = item.name
                        pinGenerated = false
                        position = combinedListOfPins.size
                        pinId = item.id
                        isSelected = false
                        isExtendedToDelete = false
                        isExtendedToName = false
                    }
                    combinedListOfPins.add(savedPin)
                }

                val firstPin = combinedListOfPins.firstOrNull()
                if (firstPin != null) {
                    App.ref.pinManagementSelectedItem = firstPin
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pins = combinedListOfPins,
                        selectedPin = firstPin
                    )
                }
            } catch (e: Exception) {
                log.error("Failed to load pins: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load pins: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectPin(pin: RPinManagement) {
        // Create new list with updated selection state
        val updatedPins = _uiState.value.pins.map { p ->
            RPinManagement().apply {
                this.pin = p.pin
                this.pinName = p.pinName
                this.pinGenerated = p.pinGenerated
                this.position = p.position
                this.pinId = p.pinId
                this.isSelected = (p.pinId == pin.pinId && p.pin == pin.pin)
                this.isExtendedToDelete = if (isSelected) p.isExtendedToDelete else false
                this.isExtendedToName = if (isSelected) p.isExtendedToName else false
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
            RPinManagement().apply {
                this.pin = p.pin
                this.pinName = p.pinName
                this.pinGenerated = p.pinGenerated
                this.position = p.position
                this.pinId = p.pinId

                if (p.pinId == pin.pinId && p.pin == pin.pin) {
                    this.isExtendedToName = !p.isExtendedToName
                    this.isSelected = true
                    this.isExtendedToDelete = false
                } else {
                    this.isExtendedToName = false
                    this.isExtendedToDelete = false
                    this.isSelected = false
                }
            }
        }

        val toggledPin = updatedPins.find { it.pinId == pin.pinId && it.pin == pin.pin }

        _uiState.update {
            it.copy(
                pins = updatedPins,
                selectedPin = if (toggledPin?.isExtendedToName == true) toggledPin else it.selectedPin,
                currentPinName = if (toggledPin?.isExtendedToName == true) "" else it.currentPinName
            )
        }

        if (toggledPin?.isExtendedToName == true) {
            App.ref.pinManagementSelectedItem = toggledPin
            App.ref.pinManagementName = ""
        }
    }

    fun updatePinName(name: String) {
        _uiState.update { it.copy(currentPinName = name) }
        App.ref.pinManagementName = name
    }

    fun saveAndConfirm(macAddress: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val selectedPin = _uiState.value.selectedPin

            if (selectedPin == null) {
                log.error("No pin selected")
                return@launch
            }

            val pinName = App.ref.pinManagementName

            // Only save if it's a generated pin with a name
            if (pinName.isNotEmpty() && selectedPin.pinGenerated == true) {
                _uiState.update { it.copy(isSavingPin = true) }

                try {
                    val device = MPLDeviceStore.devices[macAddress]
                    val userGroup = UserUtil.userGroup

                    if (device != null && userGroup != null) {
                        val savePin = RPinManagementSavePin().apply {
                            groupId = userGroup.id
                            masterId = device.masterUnitId
                            pin = selectedPin.pin
                            name = pinName
                        }

                        withContext(Dispatchers.IO) {
                            WSUser.savePinManagementForSendParcel(savePin)
                        }

                        log.info("Pin successfully saved before confirm")
                    }
                } catch (e: Exception) {
                    log.error("Failed to save pin before confirm: ${e.message}")
                } finally {
                    _uiState.update { it.copy(isSavingPin = false) }
                }
            }

            // Always complete with the selected pin
            onComplete(selectedPin.pin)
        }
    }

    fun saveGeneratedPin(macAddress: String) {
        val pinToSave = _uiState.value.pins.firstOrNull { it.pinGenerated == true }
        var pinName = _uiState.value.currentPinName

        if (pinToSave == null) {
            log.error("No generated pin found")
            return
        }

        if (pinName.isEmpty()) {
            log.error("Pin name is empty")
            return
        }

        _uiState.update { it.copy(isSavingPin = true) }

        viewModelScope.launch {
            try {
                val device = MPLDeviceStore.devices[macAddress]
                val userGroup = UserUtil.userGroup

                if (device == null || userGroup == null) {
                    log.error("Device or user group not found")
                    _uiState.update { it.copy(isSavingPin = false) }
                    return@launch
                }

                log.info("aa Saving pin with name: $pinName")
                log.info("aa Pin to save: ${pinToSave.pin}")
                log.info("aa Device ID: ${device.masterUnitId}")
                log.info("aa User group ID: ${userGroup.id}")


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
                    log.info("Pin successfully saved with ID: ${savedPin.id}")

                    // Create updated list with the saved pin
                    val updatedPins = _uiState.value.pins.map { p ->
                        if (p.pinGenerated == true) {
                            RPinManagement().apply {
                                pin = p.pin
                                pinGenerated = false
                                pinId = savedPin.id
                                pinName = savedPin.name
                                position = p.position
                                isSelected = true
                                isExtendedToDelete = false
                                isExtendedToName = false
                            }
                        } else {
                            p
                        }
                    }

                    val newSelectedPin = updatedPins.firstOrNull { !it.pinGenerated }

                    _uiState.update {
                        it.copy(
                            pins = updatedPins,
                            selectedPin = newSelectedPin,
                            isSavingPin = false,
                            currentPinName = ""
                        )
                    }

                    if (newSelectedPin != null) {
                        App.ref.pinManagementSelectedItem = newSelectedPin
                    }
                    App.ref.pinManagementName = ""
                } else {
                    log.error("Failed to save pin - returned null")
                    _uiState.update {
                        it.copy(
                            isSavingPin = false,
                            errorMessage = "Failed to save pin"
                        )
                    }
                }
            } catch (e: Exception) {
                log.error("Exception while saving pin: ${e.message}")
                _uiState.update {
                    it.copy(
                        isSavingPin = false,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleDelete(pin: RPinManagement) {
        val updatedPins = _uiState.value.pins.map { p ->
            RPinManagement().apply {
                this.pin = p.pin
                this.pinName = p.pinName
                this.pinGenerated = p.pinGenerated
                this.position = p.position
                this.pinId = p.pinId

                if (p.pinId == pin.pinId && p.pin == pin.pin) {
                    this.isExtendedToDelete = !p.isExtendedToDelete
                    this.isSelected = true
                    this.isExtendedToName = false
                } else {
                    this.isExtendedToDelete = false
                    this.isExtendedToName = false
                    this.isSelected = false
                }
            }
        }

        val toggledPin = updatedPins.find { it.pinId == pin.pinId && it.pin == pin.pin }

        _uiState.update {
            it.copy(
                pins = updatedPins,
                selectedPin = if (toggledPin?.isExtendedToDelete == true) toggledPin else it.selectedPin
            )
        }

        if (toggledPin?.isExtendedToDelete == true) {
            App.ref.pinManagementSelectedItem = toggledPin
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
                    log.info("Successfully deleted pin with ID: ${pin.pinId}")

                    val updatedPins = _uiState.value.pins.filter {
                        !(it.pinId == pin.pinId && it.pin == pin.pin)
                    }.toMutableList()

                    // Update positions
                    updatedPins.forEachIndexed { index, p ->
                        p.position = index
                    }

                    val wasPinSelected = _uiState.value.selectedPin?.pinId == pin.pinId &&
                            _uiState.value.selectedPin?.pin == pin.pin

                    val newSelectedPin = if (wasPinSelected && updatedPins.isNotEmpty()) {
                        // Try to select the pin that was before the deleted one
                        updatedPins.find { it.position == pin.position - 1 }
                            ?: updatedPins.firstOrNull()?.apply { isSelected = true }
                    } else {
                        _uiState.value.selectedPin
                    }

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
                    log.error("Failed to delete pin - returned false")
                    _uiState.update {
                        it.copy(
                            isDeletingPin = false,
                            errorMessage = "Failed to delete pin"
                        )
                    }
                }
            } catch (e: Exception) {
                log.error("Exception while deleting pin: ${e.message}")
                _uiState.update {
                    it.copy(
                        isDeletingPin = false,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }
}