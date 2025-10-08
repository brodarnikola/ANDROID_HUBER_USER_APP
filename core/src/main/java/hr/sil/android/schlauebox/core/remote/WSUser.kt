package hr.sil.android.schlauebox.core.remote

import hr.sil.android.schlauebox.core.remote.base.WSBase
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.core.remote.service.UserAppService
import hr.sil.android.rest.core.configuration.ServiceConfig
import hr.sil.android.schlauebox.core.remote.service.UserPublicService
import retrofit2.Call
import retrofit2.Response
import ru.gildor.coroutines.retrofit.awaitResponse
import javax.crypto.Mac

/**
 * @author mfatiga
 */
object WSUser : WSBase() {

    suspend fun registerDevice(pushToken: String?, metaData: String = ""): Boolean {
        return if (pushToken != null) {
            val request = RUserDeviceInfo().apply {
                this.appKey = ServiceConfig.cfg.appKey
                this.token = pushToken
                this.type = RUserDeviceType.ANDROID
                this.metadata = metaData
            }
            wrapAwaitIsSuccessful(
                    call = UserPublicService.service.registerDevice(request),
                    methodName = "registerDevice()"
            )
        } else {
            false
        }
    }

    suspend fun getLanguages(): List<RLanguage>? {
        return wrapAwaitData(
                call = UserPublicService.service.getLanguages(),
                methodName = "getLanguages()",
                defaultNullValue = listOf()
        )
    }


    suspend fun getDevicesInfo(macAddress: List<String>): List<RLockerInfo>? {
        log.info("Addresses for info: " + macAddress.joinToString(",") { it })
        return wrapAwaitData(
                call = UserAppService.service.getLockersInfo(macAddress),
                methodName = "getDevicesInfo()"
        )
    }


    suspend fun registerEndUser(
            name: String,
            address: String,
            telephone: String,
            email: String,
            password: String,
            language: RLanguage,
            groupName: String,
            reducedMobility: Boolean,
            installationKey: String): REndUserInfo? {

        val request = REndUserRegisterRequest().apply {
            this.name = name
            this.address = address
            this.telephone = telephone
            this.email = email
            this.password = password
            this.languageId = language.id
            this.isNotifyPush = true
            this.isNotifyEmail = true
            this.hasAcceptedTerms = true
            this.groupName = groupName
            this.reducedMobility = reducedMobility
            this.inviteCode= installationKey
        }
        return wrapAwaitData(
                call = UserPublicService.service.registerEndUser(request),
                methodName = "registerEndUser()"
        )
    }

    suspend fun requestPasswordRecovery(email: String): Response<Boolean> {
        return UserPublicService.service.requestPasswordRecovery(email).awaitResponse()
    }

    suspend fun resetPassword(email: String, passwordCode: String, password: String): Boolean {
        val request = RResetPasswordRequest().apply {
            this.email = email
            this.passwordCode = passwordCode
            this.password = password
        }
        return wrapAwaitIsSuccessful(
                call = UserPublicService.service.resetPassword(request),
                methodName = "resetPassword()"
        )
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        val request = RUpdatePasswordRequest().apply {
            this.oldPassword = oldPassword
            this.newPassword = newPassword
        }
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.updatePassword(request),
                methodName = "updatePassword()"
        )
    }

    suspend fun acceptedTerms(): Boolean {

        return wrapAwaitIsSuccessful(
                call = UserAppService.service.acceptedTerms(),
                methodName = "acceptedTerms()"
        )
    }

    suspend fun updateUserProfile(
            name: String,
            telephone: String,
            address: String,
            language: RLanguage,
            isPushNotified: Boolean,
            isEmailNotified: Boolean,
            groupName: String,
            reducedMobility: Boolean

    ): REndUserInfo? {
        val request = RUpdateUserProfileRequest().apply {
            this.name = name
            this.telephone = telephone
            this.address = address
            this.languageId = language.id
            this.isNotifyPush = isPushNotified
            this.isNotifyEmail = isEmailNotified
            this.groupName = groupName
            this.reducedMobility = reducedMobility
        }
        return wrapAwaitData(
                call = UserAppService.service.updateUserProfile(request),
                methodName = "updateUserProfile()"
        )
    }

    suspend fun updateUserProfileInvited(
            name: String,
            address: String,
            telephone: String,
            languageId: Int,
            isPushNotified: Boolean,
            isEmailNotified: Boolean,
            groupName: String,
            password: String

    ): REndUserInfo? {
        val request = RUpdateUserProfileRequestInvited().apply {
            this.name = name
            this.address = address
            this.telephone = telephone
            this.languageId = languageId
            this.isNotifyPush = isPushNotified
            this.isNotifyEmail = isEmailNotified
            this.groupName = groupName
            this.password = password
        }
        return wrapAwaitData(
                call = UserAppService.service.updateUserProfileInvited(request),
                methodName = "updateUserProfileInvited()"
        )
    }

    suspend fun getUserInfo(): REndUserInfo? {
        return wrapAwaitData(
                call = UserAppService.service.getUserInfo(),
                methodName = "getUserInfo()"
        )
    }

    suspend fun getUserGroupInfo(): REndUserGroupInfo? {
        return wrapAwaitData(
                call = UserAppService.service.getGroupInfo(),
                methodName = "getUserGroupInfo()"
        )
    }

    suspend fun getMasterUnits(): List<RMasterUnit>? {
        return wrapAwaitData(
                call = UserAppService.service.getMasterUnits(),
                methodName = "getMasterUnits()",
                defaultNullValue = listOf()
        )
    }

    suspend fun getLockerFromMasterUnit(masterMac: String): List<RLockerUnit>? {
        return wrapAwaitData(
                call = UserAppService.service.getLockerFromMasterUnit(masterMac),
                methodName = "getLockerUnitsFromMaster()",
                defaultNullValue = listOf()
        )
    }

    suspend fun getGroupMembers(): List<REndUserGroupMember>? {
        return wrapAwaitData(
                call = UserAppService.service.getGroupMembers(),
                methodName = "getGroupMembers()",
                defaultNullValue = listOf()
        )
    }

    suspend fun getGroupMemberships(): List<RGroupInfo>? {
        return wrapAwaitData(
                call = UserAppService.service.getGroupMemberships(),
                methodName = "getGroupMemberships()",
                defaultNullValue = listOf()
        )
    }


    suspend fun getGroupMembershipsById(groupId: Long): MutableList<RGroupInfo>? {
        return wrapAwaitData(
                call = UserAppService.service.getGroupMembershipsById(groupId),
                methodName = "getGroupMembershipsById()",
                defaultNullValue = mutableListOf()
        )
    }

    suspend fun getActiveKeys(): List<RLockerKey>? {
        return wrapAwaitData(
                call = UserAppService.service.getActiveKeys(),
                methodName = "getActiveKeys()"
        )
    }

    suspend fun getActiveKeysForLocker(macAddress: String): List<RLockerKey>? {
        return wrapAwaitData(
                call = UserAppService.service.getActiveKeysForLocker(macAddress),
                methodName = "getActiveKeysForLocker()"
        )
    }

    suspend fun getActivePaFCreatedKeys(): List<RCreatedLockerKey>? {
        return wrapAwaitData(
                call = UserAppService.service.getActivePaFCreatedKeys(),
                methodName = "getActivePaFCreatedKeys()"
        )
    }

    suspend fun getActivePaHCreatedKeys(): List<RCreatedLockerKey>? {
        return wrapAwaitData(
                call = UserAppService.service.getActivePaHCreatedKeys(),
                methodName = "getActivePaHCreatedKeys()"
        )
    }

    suspend fun getAvailableLockerSizes(masterUnitId: Int): List<RAvailableLockerSize>? {
        return wrapAwaitData(
                call = UserAppService.service.getAvailableLockerSizes(masterUnitId),
                methodName = "getAvailableLockerSizes()",
                defaultNullValue = listOf()
        )
    }

    suspend fun requestMPlAccess(macAddress: String): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.requestAccess(macAddress),
                methodName = "requestAccess()"

        )
    }

    suspend fun getActiveRequests(): List<RAccessRequest>? {
        return wrapAwaitData(
                call = UserAppService.service.getActiveAccessRequests(),
                methodName = "requestAccess()"

        )
    }

    suspend fun getGeneratedPinForSendParcel(masterId: Int?): String? {
        return wrapAwaitData(
                call = UserAppService.service.getGeneratedPinFromBackendForSendParcel(masterId),
                methodName = "getGeneratedPinForSendParcel()"

        )
    }


    suspend fun getPinManagementForSendParcel(groupId: Int, masterId: Int?): List<RPinManagementResponse>? {
        return wrapAwaitData(
                call = UserAppService.service.getPinManagementForSendParcel(groupId, masterId),
                methodName = "getPinManagementForSendParcel()"

        )
    }

    suspend fun savePinManagementForSendParcel(savePin: RPinManagementSavePin): RPinManagementResponse? {
        return wrapAwaitData(
                call = UserAppService.service.savePinManagementForSendParcel(savePin),
                methodName = "savePinManagementForSendParcel()"
        )
    }

    suspend fun deletePinForSendParcel(pinId: Int): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.deletePinForSendParcel(pinId),
                methodName = "savePinManagementForSendParcel()"
        )
    }

    suspend fun activateSPL(macAddress: String): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.activateSpl(macAddress),
                methodName = "activateSPL()"
        )
    }

    suspend fun deactivateSPL(macAddress: String): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.deactivateSpl(macAddress),
                methodName = "deactivateSPL()"
        )
    }


    suspend fun addUserAccess(userAccess: RUserAccess): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.addUserAccess(userAccess),
                methodName = "addUserAccess()"
        )
    }

    suspend fun removeUserAccess(userAccess: RUserRemoveAccess): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.removeUserAccess(userAccess),
                methodName = "addUserAccess()"
        )
    }

    suspend fun createPaF(keyId: Int, email: String): RInstallationKey? {
        val pafKey = RCreatePaf().apply {
            this.keyId = keyId
            this.email = email
        }
        return wrapAwaitData(
                call = UserAppService.service.createPaF(pafKey),
                methodName = "createPaF()"
        )

    }

    suspend fun deletePaF(keyId: Int): Boolean {
        val pafKey = RDeletePaf().apply {
            this.keyId = keyId
        }
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.deletePaF(pafKey),
                methodName = "deletePaF()"
        )

    }

    suspend fun modifyMasterUnit(unit: RMasterUnit): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.modifySpl(masterUnit = unit),
                methodName = "modifyMasterUnit()"
        )

    }

    suspend fun ping(): Boolean {
        return wrapAwaitIsSuccessful(
                call = UserAppService.service.ping(),
                methodName = "serverPing()"
        )
    }


    override fun callEncryptService(mac: String, request: REncryptRequest): Call<REncryptResponse> {
        return UserAppService.service.encrypt(mac, request)
    }
}