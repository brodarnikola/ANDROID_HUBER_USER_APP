package hr.sil.android.schlauebox.compose.view.ui.access_sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RGroupInfo
import hr.sil.android.schlauebox.core.remote.model.RUserAccess
import hr.sil.android.schlauebox.core.remote.model.RUserAccessRole
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.util.backend.UserUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

data class AccessSharingAddUserUiState(
    val email: String = "",
    val selectedGroup: RGroupInfo? = null,
    val selectedRole: RUserAccessRole = RUserAccessRole.USER,
    val availableGroups: List<RGroupInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isUnauthorized: Boolean = false,
    val emailError: String? = null,
    val showShareAppDialog: Boolean = false,
    val shareAppEmail: String = ""
)

class AccessSharingAddUserViewModel : ViewModel() {

    private val log = logger()

    private val _uiState = MutableStateFlow(AccessSharingAddUserUiState())
    val uiState: StateFlow<AccessSharingAddUserUiState> = _uiState.asStateFlow()

    init {
        App.ref.eventBus.register(this)
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

    fun loadGroupMemberships(macAddress: String) {
        viewModelScope.launch {
            val userGroup = UserUtil.userGroup ?: return@launch
            val deviceId = MPLDeviceStore.devices[macAddress]?.masterUnitId
            val groupMemberships = UserUtil.userMemberships.filter {
                it.master_id == deviceId && it.role == RUserAccessRole.ADMIN.name
            }

            val userData = RGroupInfo().apply {
                groupId = userGroup.id
                groupOwnerName = userGroup.name
            }

            val finalGroupMembership = mutableListOf<RGroupInfo>()
            finalGroupMembership.add(userData)
            finalGroupMembership.addAll(groupMemberships)

            _uiState.update {
                it.copy(
                    availableGroups = finalGroupMembership,
                    selectedGroup = finalGroupMembership.firstOrNull()
                )
            }
        }
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                emailError = null
            )
        }
    }

    fun onGroupSelected(group: RGroupInfo) {
        _uiState.update { it.copy(selectedGroup = group) }
    }

    fun onRoleSelected(role: RUserAccessRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun onEmailFromContact(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun dismissShareAppDialog() {
        _uiState.update {
            it.copy(
                showShareAppDialog = false,
                shareAppEmail = ""
            )
        }
    }

    fun addUserAccess(macAddress: String, onSuccess: () -> Unit) {
        val currentState = _uiState.value

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be blank") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val correctMasterId = MPLDeviceStore.devices[macAddress]?.masterUnitId ?: 0 //as? Int ?: return@launch
                val selectedGroup = currentState.selectedGroup ?: return@launch

                val userAccess = RUserAccess().apply {
                    this.groupId = selectedGroup.groupId
                    this.groupUserEmail = currentState.email
                    this.role = currentState.selectedRole.name
                    this.masterId = correctMasterId
                }

                val result = WSUser.addUserAccess(userAccess)

                if (result) {
                    val userGroup = UserUtil.userGroup
                    if (userGroup?.name != selectedGroup.groupOwnerName) {
                        selectedGroup.groupId.toLong().let {
                            WSUser.getGroupMembershipsById(it)
                        }
                    } else {
                        WSUser.getGroupMembers()
                    }

                    log.info("Successfully added user ${userAccess.groupUserEmail} to group")
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showShareAppDialog = true,
                            shareAppEmail = currentState.email
                        )
                    }
                }
            } catch (e: Exception) {
                log.error("Error adding user access", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        emailError = "Failed to add user"
                    )
                }
            }
        }
    }
}