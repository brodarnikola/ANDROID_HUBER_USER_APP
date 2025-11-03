package hr.sil.android.schlauebox.compose.view.ui.home_screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RLanguage
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.util.backend.UserUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

data class SettingsUiState(
    val name: String = "",
    val email: String = "",
    val address: String = "",
    val phone: String = "",
    val groupNameRow1: String = "",
    val groupNameRow2: String = "",
    val reducedMobility: Boolean = false,
    val pushNotifications: Boolean = false,
    val emailNotifications: Boolean = false,
    val availableLanguages: List<RLanguage> = emptyList(),
    val selectedLanguage: RLanguage? = null,
    val oldPassword: String = "",
    val newPassword: String = "",
    val retypePassword: String = "",
    val oldPasswordError: String? = null,
    val newPasswordError: String? = null,
    val retypePasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSaveEnabled: Boolean = false,
    val appVersion: String = "",
    val isUnauthorized: Boolean = false
)

open class SettingsViewModel() : ViewModel() {

    private val log = logger()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val MAX_ROW_LENGTH = 15

    init {
        App.ref.eventBus.register(this)
        loadSettings()
    }

    override fun onCleared() {
        App.ref.eventBus.unregister(this)
        super.onCleared()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnauthorizedEvent(event: UnauthorizedUserEvent) {
        log.info("Received unauthorized event, user will now be logged out")
        _uiState.update { it.copy(isUnauthorized = true) }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val languages = WSUser.getLanguages() ?: listOf()
                val languageName = SettingsHelper.languageName
                val selectedLang = languages.firstOrNull { it.code == languageName }

                val user = UserUtil.user
                val groupName = UserUtil.userGroup?.name ?: ""
                val (row1, row2) = splitGroupName(groupName)

                _uiState.update {
                    it.copy(
                        name = user?.name ?: "",
                        email = user?.email ?: "",
                        address = user?.address ?: "",
                        phone = user?.telephone ?: "",
                        groupNameRow1 = row1,
                        groupNameRow2 = row2,
                        reducedMobility = user?.reducedMobility ?: false,
                        pushNotifications = SettingsHelper.pushEnabled,
                        emailNotifications = SettingsHelper.emailEnabled,
                        availableLanguages = languages,
                        selectedLanguage = selectedLang
                    )
                }
            } catch (e: Exception) {
                log.error("Error loading settings", e)
            }
        }
    }

    private fun splitGroupName(groupName: String): Pair<String, String> {
        return when {
            groupName.length <= MAX_ROW_LENGTH -> groupName to ""
            groupName.contains(" ") -> {
                val words = groupName.split(" ")
                val row1 = StringBuilder()
                val row2 = StringBuilder()
                var currentRow = row1

                for (word in words) {
                    val testString = if (currentRow.isEmpty()) word else "$currentRow $word"
                    if (testString.length <= MAX_ROW_LENGTH) {
                        if (currentRow.isNotEmpty()) currentRow.append(" ")
                        currentRow.append(word)
                    } else {
                        currentRow = row2
                        if (row2.isNotEmpty()) row2.append(" ")
                        row2.append(word)
                    }
                }
                row1.toString() to row2.toString()
            }
            else -> {
                groupName.substring(0, MAX_ROW_LENGTH) to
                        groupName.substring(MAX_ROW_LENGTH, groupName.length.coerceAtMost(MAX_ROW_LENGTH * 2))
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, isSaveEnabled = true) }
    }

    fun onAddressChanged(address: String) {
        _uiState.update { it.copy(address = address, isSaveEnabled = true) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone, isSaveEnabled = true) }
    }

    fun onGroupNameRow1Changed(text: String) {
        if (text.length <= MAX_ROW_LENGTH) {
            _uiState.update { it.copy(groupNameRow1 = text, isSaveEnabled = true) }
        }
    }

    fun onGroupNameRow2Changed(text: String) {
        if (text.length <= MAX_ROW_LENGTH) {
            _uiState.update { it.copy(groupNameRow2 = text, isSaveEnabled = true) }
        }
    }

    fun onReducedMobilityChanged(checked: Boolean) {
        _uiState.update { it.copy(reducedMobility = checked, isSaveEnabled = true) }
    }

    fun onPushNotificationsChanged(checked: Boolean) {
        _uiState.update { it.copy(pushNotifications = checked, isSaveEnabled = true) }
    }

    fun onEmailNotificationsChanged(checked: Boolean) {
        _uiState.update { it.copy(emailNotifications = checked, isSaveEnabled = true) }
    }

    fun onLanguageSelected(language: RLanguage) {
        _uiState.update { it.copy(selectedLanguage = language, isSaveEnabled = true) }
    }

    fun onOldPasswordChanged(password: String) {
        _uiState.update { it.copy(oldPassword = password, oldPasswordError = null, isSaveEnabled = true) }
    }

    fun onNewPasswordChanged(password: String) {
        _uiState.update { it.copy(newPassword = password, newPasswordError = null, isSaveEnabled = true) }
    }

    fun onRetypePasswordChanged(password: String) {
        _uiState.update { it.copy(retypePassword = password, retypePasswordError = null, isSaveEnabled = true) }
    }

    fun setAppVersion(version: String) {
        _uiState.update { it.copy(appVersion = version) }
    }

    fun saveSettings(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentState = _uiState.value

        if (currentState.groupNameRow1.isEmpty()) {
            onError("Group name cannot be empty")
            return
        }

        if (currentState.groupNameRow1.startsWith(" ")) {
            onError("Group name cannot start with a space")
            return
        }

        val groupNameLength = currentState.groupNameRow1.length + currentState.groupNameRow2.length
        if (groupNameLength < 4) {
            onError("Group name must be at least 4 characters")
            return
        }

        if (currentState.oldPassword.isNotEmpty() && currentState.oldPassword != SettingsHelper.userPasswordWithoutEncryption) {
            _uiState.update { it.copy(oldPasswordError = "Current password is invalid") }
            onError("Current password is invalid")
            return
        }

        if (currentState.newPassword != currentState.retypePassword) {
            _uiState.update { it.copy(retypePasswordError = "Passwords do not match") }
            onError("Passwords do not match")
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val groupName = "${currentState.groupNameRow1.trim()} ${currentState.groupNameRow2.trim()}".trim()
                val selectedLang = currentState.selectedLanguage ?: return@launch

                val result = UserUtil.userUpdate(
                    name = currentState.name,
                    address = currentState.address,
                    phone = currentState.phone,
                    language = selectedLang,
                    pushNotification = currentState.pushNotifications,
                    emailNotification = currentState.emailNotifications,
                    groupName = groupName,
                    reducedMobility = currentState.reducedMobility
                )

                if (result) {
                    log.info("Settings saved successfully, applying language: ${selectedLang.code}")
                    SettingsHelper.languageName = selectedLang.code
                    SettingsHelper.pushEnabled = currentState.pushNotifications
                    SettingsHelper.emailEnabled = currentState.emailNotifications
                    UserUtil.userGroup?.name = groupName
                    App.ref.languageCode = selectedLang

                    if (currentState.oldPassword.isNotEmpty() && currentState.retypePassword.isNotEmpty()
                        && currentState.newPassword == currentState.retypePassword) {
                        val passwordResult = UserUtil.passwordUpdate(
                            email = currentState.email,
                            newPassword = currentState.newPassword,
                            oldPassword = currentState.oldPassword
                        )
                        if (passwordResult) {
                            SettingsHelper.userPasswordWithoutEncryption = currentState.retypePassword
                        } else {
                            _uiState.update { it.copy(isLoading = false) }
                            onError("Failed to update password")
                            return@launch
                        }
                    }

                    _uiState.update { it.copy(isLoading = false, isSaveEnabled = false) }
                    log.info("Restarting app to apply language change")
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    onError("Failed to save settings")
                }
            } catch (e: Exception) {
                log.error("Error saving settings", e)
                _uiState.update { it.copy(isLoading = false) }
                onError("Error: ${e.message}")
            }
        }
    }
}