import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.store.MPLDeviceStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GeneratedPinDialogUiState(
    val isLoading: Boolean = true,
    val generatedPin: String = "",
    val errorMessage: String? = null
)

class GeneratedPinDialogViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GeneratedPinDialogUiState())
    val uiState: StateFlow<GeneratedPinDialogUiState> = _uiState.asStateFlow()

    fun loadGeneratedPin(macAddress: String) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val device = MPLDeviceStore.devices[macAddress]
                val masterUnitId = device?.masterUnitId ?: 0

                val generatedPin = withContext(Dispatchers.IO) {
                    WSUser.getGeneratedPinForSendParcel(masterUnitId) ?: ""
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generatedPin = generatedPin
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
}
