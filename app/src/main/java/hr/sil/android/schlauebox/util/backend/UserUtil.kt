/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2017] Swiss Innovation Lab AG
* All Rights Reserved.
*
* @author mfatiga
*
* NOTICE:  All information contained herein is, and remains
* the property of Swiss Innovation Lab AG and its suppliers,
* if any.  The intellectual and technical concepts contained
* herein are proprietary to Swiss Innovation Lab AG
* and its suppliers and may be covered by E.U. and Foreign Patents,
* patents in process, and are protected by trade secret or copyright law.
* Dissemination of this information or reproduction of this material
* is strictly forbidden unless prior written permission is obtained
* from Swiss Innovation Lab AG.
*/

package hr.sil.android.schlauebox.util.backend

import com.google.firebase.messaging.FirebaseMessaging
import hr.sil.android.rest.core.util.UserHashUtil
import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.cache.status.InstallationKeyHandler
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.core.util.DeviceInfo
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.preferences.PreferenceStore
import hr.sil.android.schlauebox.remote.WSConfig
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.util.AppUtil
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.util.awaitForResult
import hr.sil.android.schlauebox.util.connectivity.NetworkChecker
import retrofit2.Response

/**
 * @author mfatiga
 */
object UserUtil {
    private val log = logger()
    fun isUserLoggedIn() = (user != null)

    var userGroup: REndUserGroupInfo? = null
        private set

    var userMemberships: List<RGroupInfo> = listOf()

    var user: REndUserInfo? = null
        private set

    var userInvitedTempdata: REndUserInfo? = null
        private set

    fun getUserString(default: String = "--"): String {
        val loggedInUserName = user?.name ?: ""
        val loggedInUserEmail = user?.email ?: ""
        val result = when {
            loggedInUserName.isNotBlank() -> loggedInUserName
            loggedInUserEmail.isNotBlank() -> loggedInUserEmail
            else -> null
        }
        return result ?: default
    }

    private fun updateUserHash(username: String?, password: String?) {
        if (username != null && password != null && username.isNotEmpty() && password.isNotEmpty()) {
            PreferenceStore.userHash = UserHashUtil.createUserHash(username, password)
        } else {
            PreferenceStore.userHash = ""
        }
        log.info("da li ce doci sim za basic auth unutar headera")
        WSConfig.updateAuthorizationKeys()
    }

    suspend fun login(username: String, password: String): Boolean {
        updateUserHash(username, password)
        return login(username)
    }

    suspend fun register(name: String, address: String, phoneNumber: String, email: String, password: String, groupName: String, reducedMobility: Boolean): Boolean {

        WSUser.registerDevice(fcmTokenRequest(), DeviceInfo.getJsonInstance())

        val langList = DataCache.getLanguages(true)
        val language = langList.find { it.code == "EN" }
        var user: REndUserInfo? = null
        try {
            var key = ""
            val installationKeys = InstallationKeyHandler.key.getAll()
            if (!installationKeys.isNullOrEmpty()) key = installationKeys.first().key
            log.info("Getting ref key from registration $key")
            log.info("Size from installation keys ${installationKeys.size}")
            if (language != null) {
                log.info("Language id is: ${language.id}")
                user = WSUser.registerEndUser(name, address, phoneNumber, email, password, language, groupName, reducedMobility, key )
                updateUserHash(user?.email, password)
                InstallationKeyHandler.key.clear()

                return user != null && login(user.email)
            } else {
                log.error("No language found -> Language list size = ${langList.size}")
                return false
            }
        } catch (e: Exception) {
            log.error("Exception while registering user is = ${e}")
            return false
        }
    }


    suspend fun fcmTokenRequest(): String? {

        val task = FirebaseMessaging.getInstance().token.awaitForResult()
        if (!task.isSuccessful) {
            log.info("getInstanceId failed", task.exception)
        }
        // Get new Instance ID token
        val token = task.result
        val msg = "Token: $token"
        // Log and toast
        log.info(msg)

        val subscribeTask = FirebaseMessaging.getInstance().subscribeToTopic("news").awaitForResult()
        var subscribeMessage = "Subscribed"
        if (!subscribeTask.isSuccessful) {
            subscribeMessage = "Subscription failed"
        }
        log.info(subscribeMessage)
        return token
    }


    suspend fun loginCheckUserStatus(username: String, password: String): UserStatus {
        WSUser.registerDevice(fcmTokenRequest(), DeviceInfo.getJsonInstance())
        updateUserHash(username, password)
        return if (!PreferenceStore.userHash.isNullOrBlank()) {
            val responseUser = WSUser.getUserInfo()
            val group = WSUser.getUserGroupInfo()
            userMemberships = WSUser.getGroupMemberships() ?: listOf()

            if (responseUser != null && responseUser.status == UserStatus.ACTIVE.toString()) {

                user = responseUser

                if (group != null) {
                    userGroup = group
                }
                //invalidate caches on login
                AppUtil.refreshCache()

                log.info("User is logged in updating device and token...")

                val languagesList = DataCache.getLanguages()
                val languageData = languagesList.find { it.id == responseUser.languageId }

                if (languageData != null) {
                    SettingsHelper.languageName = languageData.code
                }
                SettingsHelper.pushEnabled = responseUser.isNotifyPush
                SettingsHelper.emailEnabled = responseUser.isNotifyEmail
                SettingsHelper.usernameLogin = username

                return UserStatus.ACTIVE
            } else if (responseUser != null && responseUser.status == UserStatus.INVITED.toString()) {
                userInvitedTempdata = responseUser
                if (group != null) {
                    userGroup = group
                }
                return UserStatus.INVITED
            } else {
                updateUserHash(null, null)
                user = null
                return UserStatus.NOT_LOGGED_IN
            }
        } else {
            updateUserHash(null, null)
            user = null
            return UserStatus.NOT_LOGGED_IN
        }
    }


    suspend fun login(username: String): Boolean {
        WSUser.registerDevice(fcmTokenRequest(), DeviceInfo.getJsonInstance())
        return if (!PreferenceStore.userHash.isNullOrBlank()) {
            val responseUser = WSUser.getUserInfo()
            val group = WSUser.getUserGroupInfo()
            userMemberships = WSUser.getGroupMemberships() ?: listOf()

            if (responseUser != null && responseUser.status == UserStatus.ACTIVE.toString()) {

                user = responseUser

                if (group != null) {
                    userGroup = group
                }
                //invalidate caches on login
                AppUtil.refreshCache()

                val languagesList = DataCache.getLanguages()

                val languageData = languagesList.find { it.id == responseUser.languageId }

                if (languageData != null) {
                    SettingsHelper.languageName = languageData.code
                }

                SettingsHelper.pushEnabled = responseUser.isNotifyPush
                SettingsHelper.emailEnabled = responseUser.isNotifyEmail
                SettingsHelper.usernameLogin = username

                WSConfig.updateAuthorizationKeys()
                log.info("User is logged in updating device and token...")

                return true

            } else {
                updateUserHash(null, null)
                user = null
                false
            }
        } else {
            updateUserHash(null, null)
            user = null
            false
        }
    }

    fun logout() {
        log.info("Logging out, clearing data cache...")
        updateUserHash(null, null)
        user = null
        //UserAppService.config.setAuthorization(Authorization.Custom(""))
        DataCache.clearCaches()
        MPLDeviceStore.clear()
    }

    //ping
    suspend fun ping(notifyNetworkChecker: Boolean = true): Boolean {
        val result = try {
            return WSUser.ping()
        } catch (e: Exception) {
            false
        }
        if (notifyNetworkChecker) {
            NetworkChecker.notifyInternetConnection(result)
        }
        return result
    }

    suspend fun passwordRecovery(email: String): Response<Boolean> {
        return WSUser.requestPasswordRecovery(email)
    }

    suspend fun passwordReset(email: String, passwordCode: String, password: String): Boolean {
        return WSUser.resetPassword(email, passwordCode, password)
    }

    suspend fun passwordUpdate(email: String, newPassword: String, oldPassword: String): Boolean {
        val isPasswordUpdated = WSUser.updatePassword(oldPassword, newPassword)

        if (!isPasswordUpdated) {
            log.error("Error while updating the user password")
            return false
        } else {
            updateUserHash(email, newPassword)
            return true
        }
    }

    suspend fun acceptedTerms() {
        WSUser.acceptedTerms()
    }

    suspend fun userUpdate(name: String, address: String, phone: String, language: RLanguage, pushNotification: Boolean, emailNotification: Boolean, groupName: String, reducedMobility: Boolean): Boolean {
        log.info("User update data is -> $name $phone $address   $pushNotification $emailNotification ${language.code} ${language.id} ${language.name} ${reducedMobility}")
        val userInfo = WSUser.updateUserProfile(name = name, telephone = phone, address = address, language = language, isPushNotified = pushNotification, isEmailNotified = emailNotification, groupName = groupName, reducedMobility = reducedMobility)

        if (userInfo == null) {
            log.error("Error while updating the user")
            return false
        } else {
            user = userInfo
            return true
        }
    }

    suspend fun userUpdateInvited(name: String, address: String, phone: String, languageId: Int, pushNotification: Boolean, emailNotification: Boolean, groupName: String, emailValue: String, passwordValue: String ): Boolean {

        log.info("$name $phone $address   $pushNotification $emailNotification ${languageId} ")
        val userInfo = WSUser.updateUserProfileInvited(name = name, address = address, telephone = phone, languageId = languageId,
                isPushNotified = pushNotification, isEmailNotified = emailNotification, groupName = groupName, password = passwordValue)

        if (userInfo == null) {
            log.error("Error while updating the user")
            return false
        } else {
            //invalidate caches on login
            updateUserHash(emailValue, passwordValue)
            SettingsHelper.pushEnabled = pushNotification
            SettingsHelper.emailEnabled = emailNotification
            SettingsHelper.usernameLogin = emailValue
            AppUtil.refreshCache()
            user = userInfo
            userGroup?.name = groupName
            acceptedTerms()

            val result = WSUser.registerDevice(fcmTokenRequest(), DeviceInfo.getJsonInstance())
            return true
        }
    }
}