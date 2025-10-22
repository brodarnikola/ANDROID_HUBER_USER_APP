package hr.sil.android.schlauebox.compose.view.ui.main_activity

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import hr.sil.android.schlauebox.util.NotificationHelper
import hr.sil.android.schlauebox.util.connectivity.BluetoothChecker
import hr.sil.android.schlauebox.util.connectivity.LocationGPSChecker
import hr.sil.android.schlauebox.util.connectivity.NetworkChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SystemState(
    val bluetoothAvailable: Boolean = true,
    val networkAvailable: Boolean = true,
    val locationGPSAvailable: Boolean = true
)

class SystemStateViewModel : ViewModel() {
    private val _systemState = MutableStateFlow(SystemState())
    val systemState: StateFlow<SystemState> = _systemState.asStateFlow()

    private var btCheckerListenerKey: String? = null
    private var networkCheckerListenerKey: String? = null
    private var locationGPSListenerKey: String? = null
    private val uiHandler = Handler(Looper.getMainLooper())
    private var locationGPSChecker: LocationGPSChecker? = null

    fun startMonitoring(context: Context) {
        NotificationHelper.clearNotification()

        if (btCheckerListenerKey == null) {
            btCheckerListenerKey = BluetoothChecker.addListener { available ->
                uiHandler.post {
                    _systemState.value = _systemState.value.copy(bluetoothAvailable = available)
                }
            }
        }

        if (networkCheckerListenerKey == null) {
            networkCheckerListenerKey = NetworkChecker.addListener { available ->
                uiHandler.post {
                    _systemState.value = _systemState.value.copy(networkAvailable = available)
                }
            }
        }

        if (locationGPSListenerKey == null) {
            locationGPSChecker = LocationGPSChecker(context)
            locationGPSListenerKey = locationGPSChecker?.addListener { available ->
                uiHandler.post {
                    _systemState.value = _systemState.value.copy(locationGPSAvailable = available)
                }
            }
        }
    }

    fun stopMonitoring() {
        btCheckerListenerKey?.let { BluetoothChecker.removeListener(it) }
        btCheckerListenerKey = null

        networkCheckerListenerKey?.let { NetworkChecker.removeListener(it) }
        networkCheckerListenerKey = null

        locationGPSListenerKey?.let { locationGPSChecker?.removeListener(it) }
        locationGPSListenerKey = null
        locationGPSChecker = null
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}