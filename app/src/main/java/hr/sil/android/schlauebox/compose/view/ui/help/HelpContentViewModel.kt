package hr.sil.android.schlauebox.compose.view.ui.help

import androidx.lifecycle.ViewModel
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HelpContentViewModel  : ViewModel() {

    val log = logger()

    private val _uiState = MutableStateFlow(HelpContentUiState())
    val uiState: StateFlow<HelpContentUiState> = _uiState.asStateFlow()

    init {
        App.ref.eventBus.register(this)
    }

    fun loadContent(titleResId: Int, contentResId: Int, picturePosition: Int) {
        _uiState.value = _uiState.value.copy(
            titleResId = titleResId,
            contentResId = contentResId,
            picturePosition = picturePosition
        )
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

data class HelpContentUiState(
    val titleResId: Int = 0,
    val contentResId: Int = 0,
    val picturePosition: Int = 0,
    val isUnauthorized: Boolean = false
)
