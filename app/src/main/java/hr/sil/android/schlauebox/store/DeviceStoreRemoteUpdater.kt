/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2018] Swiss Innovation Lab AG
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

package hr.sil.android.schlauebox.store

//import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RLockerInfo
import hr.sil.android.schlauebox.core.remote.model.RLockerKey
import hr.sil.android.schlauebox.core.remote.model.RMasterUnit
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.store.model.MasterUnitWithKeys
import hr.sil.android.schlauebox.util.backend.UserUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author mfatiga
 */
object DeviceStoreRemoteUpdater {
    private val log = logger()

    private const val UPDATE_PERIOD = 10_000L // 10 seconds
    private const val BACKEND_FETCH_PERIOD = 60_000L // 30 seconds

    private val running = AtomicBoolean(false)
    private val inHandleUpdate = AtomicBoolean(false)

    // Keep the last fetch timestamp
    private var lastBackendFetch = 0L
    private var cachedActiveKeys: List<RLockerKey> = emptyList()
    private var cachedMasterUnits: List<RMasterUnit> = emptyList()

    fun run() {
        if (running.compareAndSet(false, true)) {
            GlobalScope.launch(Dispatchers.Default) {
                while (true) {
                    try {
                        handleUpdate()
                    } catch (ex: Exception) {
                        log.error("Periodic remote-update failed...", ex)
                    }
                    delay(UPDATE_PERIOD)
                }
            }
        }
    }

    suspend fun forceUpdate() {
        handleUpdate()
    }

    private suspend fun handleUpdate() {
        if (inHandleUpdate.compareAndSet(false, true)) {
            if (UserUtil.isUserLoggedIn()) {
                doUpdate()
            }
            inHandleUpdate.set(false)
        }
    }

    private suspend fun doUpdate() {
        val now = System.currentTimeMillis()

        // Fetch from backend only every 30 seconds
        if (now - lastBackendFetch >= BACKEND_FETCH_PERIOD) {
            log.info("Fetching master units and keys from backend...")
            cachedActiveKeys = WSUser.getActiveKeys() ?: emptyList()
            cachedMasterUnits = WSUser.getMasterUnits() ?: emptyList()
            lastBackendFetch = now
        } else {
            log.debug("Using cached master units and keys (no backend call)")
        }

        if (cachedMasterUnits.isEmpty()) {
            log.info("Master unit size from backend is: ${cachedMasterUnits.size}")
        }

        log.info(
            "Master unit size = ${cachedMasterUnits.size}, " +
                    "${cachedMasterUnits.joinToString { it.mac }}"
        )

        MPLDeviceStore.updateFromRemote(
            cachedMasterUnits.map { masterUnit ->
                MasterUnitWithKeys(
                    masterUnit = masterUnit,
                    activeKeys = cachedActiveKeys.filter { it.lockerMasterId == masterUnit.id },
                    availableLockerSizes = emptyList()
                )
            }
        )
    }
}