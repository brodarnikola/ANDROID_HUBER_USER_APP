package hr.sil.android.schlauebox.compose.view.ui.help

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class HelpViewModel @Inject constructor() : ViewModel() {

    val log = logger()

    private val _uiState = MutableStateFlow(HelpUiState())
    val uiState: StateFlow<HelpUiState> = _uiState.asStateFlow()

    init {
        App.ref.eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnauthorizedUser(event: UnauthorizedUserEvent) {
        log.info("Received unauthorized event, user will now be logged out")
        _uiState.value = _uiState.value.copy(isUnauthorized = true)
    }

    override fun onCleared() {
        super.onCleared()
        App.ref.eventBus.unregister(this)
    }
}

data class HelpUiState(
    val isUnauthorized: Boolean = false
)