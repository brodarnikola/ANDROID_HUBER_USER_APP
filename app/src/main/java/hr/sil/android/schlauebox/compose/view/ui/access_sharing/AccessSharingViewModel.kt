package hr.sil.android.schlauebox.compose.view.ui.access_sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.core.util.macRealToClean
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.model.MPLDevice
import hr.sil.android.schlauebox.util.backend.UserUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class AccessSharingViewModel @Inject constructor() : ViewModel() {

    val log = logger()

    private val _uiState = MutableStateFlow(AccessSharingUiState())
    val uiState: StateFlow<AccessSharingUiState> = _uiState.asStateFlow()

    private val ROLE_USER = "USER"

    init {
        App.ref.eventBus.register(this)
    }

    fun loadAccessSharing(macAddress: String, nameOfDevice: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                macAddress = macAddress,
                nameOfDevice = nameOfDevice,
                isLoading = true
            )

            val macClean = macAddress.macRealToClean()
            val device = MPLDeviceStore.devices[macAddress]

            val members = if (device?.masterUnitType == RMasterUnitType.MPL) {
                loadMplMembers(macClean, device)
            } else {
                loadSplMembers(macClean, macAddress, device)
            }

            val hasRightsToShareAccess = device?.hasRightsToShareAccess() ?: false
            val groupMembership = UserUtil.userMemberships.filter {
                it.master_id == device?.masterUnitId && it.role == RUserAccessRole.ADMIN.name
            }
            val canAddUsers = groupMembership.isNotEmpty() || hasRightsToShareAccess

            _uiState.value = _uiState.value.copy(
                members = members,
                isLoading = false,
                canAddUsers = canAddUsers,
                showEmptyState = members.isEmpty()
            )
        }
    }

    private suspend fun loadMplMembers(macClean: String, device: MPLDevice?): List<ItemRGroupInfo> {
        val finalMembersArray = mutableListOf<ItemRGroupInfo>()

        val ownerResult = WSUser.getGroupMembers()?.toMutableList() ?: mutableListOf()
        var oneOwnerUserFound = false

        if (ownerResult.isNotEmpty()) {
            val convertOwnerData = mutableListOf<RGroupDisplayMembersChild>()

            for (items in ownerResult) {
                if (items.master_mac == macClean) {
                    oneOwnerUserFound = true

                    val ownerDataObject = RGroupDisplayMembersChild().apply {
                        groupId = items.groupId
                        endUserEmail = items.email
                        endUserName = items.name
                        role = items.role
                        endUserId = items.endUserId
                        master_id = items.master_id
                    }
                    convertOwnerData.add(ownerDataObject)
                }
            }

            if (oneOwnerUserFound) {
                val rGroupInfo = RGroupDisplayMembersHeader().apply {
                    groupOwnerName = "My Group"
                }
                finalMembersArray.add(rGroupInfo)
                finalMembersArray.addAll(convertOwnerData)
            }
        }

        val dataGroupMembership = WSUser.getGroupMemberships() ?: mutableListOf()

        if (dataGroupMembership.isNotEmpty()) {
            var addOnlyOneTimeHeader = 0
            val rGroupInfo = RGroupDisplayMembersHeader().apply {
                groupOwnerName = "Other Groups"
            }

            for (items in dataGroupMembership) {
                if (items.role == ROLE_USER && items.master_mac == macClean) {
                    if (addOnlyOneTimeHeader == 0) {
                        finalMembersArray.add(rGroupInfo)
                        addOnlyOneTimeHeader = 1
                    }
                    val nameOfAdminGroup = RGroupDisplayMembersAdmin().apply {
                        groupOwnerName = items.groupName
                        role = items.role
                    }
                    finalMembersArray.add(nameOfAdminGroup)
                } else if (items.master_mac == macClean) {
                    val groupDataList = WSUser.getGroupMembershipsById(items.groupId.toLong()) ?: mutableListOf()
                    if (groupDataList.isNotEmpty()) {
                        val groupMembersData = mutableListOf<RGroupDisplayMembersChild>()
                        val nameOfAdminGroup = RGroupDisplayMembersAdmin().apply {
                            groupOwnerName = items.groupName
                        }

                        var oneAdminUserFound = false

                        for (subItems in groupDataList) {
                            if (ownerResult.isNotEmpty()) {
                                val firstMember = ownerResult[0]
                                if (subItems.master_mac == macClean && subItems.endUserId != firstMember.groupOwnerId) {
                                    oneAdminUserFound = true
                                    groupMembersData.add(createGroupMemberChild(subItems))
                                } else if (subItems.master_mac == macClean) {
                                    oneAdminUserFound = true
                                }
                            } else {
                                if (subItems.master_mac == macClean && UserUtil.user?.id != subItems.endUserId) {
                                    oneAdminUserFound = true
                                    groupMembersData.add(createGroupMemberChild(subItems))
                                }
                            }
                        }

                        if (addOnlyOneTimeHeader == 0 && oneAdminUserFound) {
                            finalMembersArray.add(rGroupInfo)
                            addOnlyOneTimeHeader = 1
                        }

                        if (oneAdminUserFound) {
                            finalMembersArray.add(nameOfAdminGroup)
                            if (groupMembersData.isNotEmpty()) {
                                finalMembersArray.addAll(groupMembersData)
                            }
                        }
                    }
                }
            }
        }

        return finalMembersArray
    }

    private suspend fun loadSplMembers(macClean: String, macAddress: String, device: MPLDevice?): List<ItemRGroupInfo> {
        val finalMembersArray = mutableListOf<ItemRGroupInfo>()

        val ownerResult = WSUser.getGroupMembers()?.toMutableList() ?: mutableListOf()
        var oneOwnerUserFound = false

        if (ownerResult.isNotEmpty()) {
            val convertOwnerData = mutableListOf<RGroupDisplayMembersChild>()
            val userData = UserUtil.user

            for (items in ownerResult) {
                if (items.master_mac == macClean && items.groupOwnerId == userData?.id) {
                    oneOwnerUserFound = true

                    val ownerDataObject = RGroupDisplayMembersChild().apply {
                        groupId = items.groupId
                        endUserEmail = items.email
                        endUserName = items.name
                        role = items.role
                        endUserId = items.endUserId
                        master_id = items.master_id
                    }
                    convertOwnerData.add(ownerDataObject)
                }
            }

            if (oneOwnerUserFound) {
                val rGroupInfo = RGroupDisplayMembersHeader().apply {
                    groupOwnerName = "My Group"
                }
                finalMembersArray.add(rGroupInfo)
                finalMembersArray.addAll(convertOwnerData)
            }
        }

        val dataGroupMembership = WSUser.getGroupMemberships() ?: mutableListOf()

        if (dataGroupMembership.isNotEmpty()) {
            var addOnlyOneTimeHeader = 0
            val rGroupInfo = RGroupDisplayMembersHeader().apply {
                groupOwnerName = "Other Groups"
            }

            for (items in dataGroupMembership) {
                if (items.role == ROLE_USER && items.master_mac == macClean) {
                    if (addOnlyOneTimeHeader == 0) {
                        finalMembersArray.add(rGroupInfo)
                        addOnlyOneTimeHeader = 1
                    }
                    val nameOfAdminGroup = RGroupDisplayMembersAdmin().apply {
                        groupOwnerName = items.groupName
                        role = items.role
                    }
                    finalMembersArray.add(nameOfAdminGroup)
                } else if (items.master_mac == macClean) {
                    val groupDataList = WSUser.getGroupMembershipsById(items.groupId.toLong()) ?: mutableListOf()
                    if (groupDataList.isNotEmpty()) {
                        val groupMembersData = mutableListOf<RGroupDisplayMembersChild>()
                        val nameOfAdminGroup = RGroupDisplayMembersAdmin().apply {
                            groupOwnerName = items.groupName
                        }

                        var oneAdminUserFound = false

                        for (subItems in groupDataList) {
                            if (ownerResult.isNotEmpty()) {
                                val firstMember = ownerResult[0]
                                if (subItems.master_mac == macClean && subItems.endUserId != firstMember.groupOwnerId) {
                                    oneAdminUserFound = true
                                    groupMembersData.add(createGroupMemberChild(subItems))
                                } else if (subItems.master_mac == macAddress) {
                                    oneAdminUserFound = true
                                }
                            } else {
                                if (subItems.master_mac == macClean && UserUtil.user?.id != subItems.endUserId) {
                                    oneAdminUserFound = true
                                    groupMembersData.add(createGroupMemberChild(subItems))
                                }
                            }
                        }

                        if (addOnlyOneTimeHeader == 0 && oneAdminUserFound) {
                            finalMembersArray.add(rGroupInfo)
                            addOnlyOneTimeHeader = 1
                        }

                        if (oneAdminUserFound) {
                            finalMembersArray.add(nameOfAdminGroup)
                            if (groupMembersData.isNotEmpty()) {
                                finalMembersArray.addAll(groupMembersData)
                            }
                        }
                    }
                }
            }
        }

        return finalMembersArray
    }

    private fun createGroupMemberChild(subItems: RGroupInfo): RGroupDisplayMembersChild {
        return RGroupDisplayMembersChild().apply {
            groupId = subItems.groupId
            groupOwnerEmail = subItems.groupOwnerEmail
            endUserName = subItems.endUserName
            endUserEmail = subItems.endUserEmail
            role = subItems.role
            endUserId = subItems.endUserId
            master_id = subItems.master_id
        }
    }

    fun deleteUserAccess(member: RGroupDisplayMembersChild) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)

            val userAccess = RUserRemoveAccess().apply {
                groupId = member.groupId
                endUserId = member.endUserId
                masterId = member.master_id
            }

            val success = WSUser.removeUserAccess(userAccess)

            withContext(Dispatchers.Main) {
                if (success) {
                    val updatedMembers = _uiState.value.members.toMutableList()
                    updatedMembers.remove(member)

                    _uiState.value = _uiState.value.copy(
                        members = updatedMembers,
                        isDeleting = false,
                        deleteSuccess = true,
                        showEmptyState = updatedMembers.isEmpty()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deleteSuccess = false
                    )
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnauthorizedUser(event: UnauthorizedUserEvent) {
        _uiState.value = _uiState.value.copy(isUnauthorized = true)
    }

    override fun onCleared() {
        super.onCleared()
        App.ref.eventBus.unregister(this)
    }
}

data class AccessSharingUiState(
    val macAddress: String = "",
    val nameOfDevice: String = "",
    val members: List<ItemRGroupInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val canAddUsers: Boolean = false,
    val showEmptyState: Boolean = false,
    val isUnauthorized: Boolean = false
)
