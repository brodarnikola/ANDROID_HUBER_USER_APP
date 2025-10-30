package hr.sil.android.schlauebox.compose.view.ui.send_parcel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RPinManagement
import hr.sil.android.schlauebox.core.remote.model.RPinManagementSavePin
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
    val errorMessage: String? = null
)

class PinManagementDialogViewModel : ViewModel() {

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
        _uiState.update { it.copy(selectedPin = pin) }
        App.ref.pinManagementSelectedItem = pin
    }

    fun savePinIfNeeded(macAddress: String) {
        viewModelScope.launch {
            try {
                val selectedPin = _uiState.value.selectedPin
                val pinName = App.ref.pinManagementName

                if (pinName.isNotEmpty() && selectedPin?.pinGenerated == true) {
                    val device = MPLDeviceStore.devices[macAddress]
                    val userGroup = UserUtil.userGroup

                    if (device != null && userGroup != null) {
                        withContext(Dispatchers.IO) {
                            val savePin = RPinManagementSavePin().apply {
                                groupId = userGroup.id
                                masterId = device.masterUnitId
                                pin = selectedPin.pin
                                name = pinName
                            }
                            WSUser.savePinManagementForSendParcel(savePin)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message)
                }
            }
        }
    }
}