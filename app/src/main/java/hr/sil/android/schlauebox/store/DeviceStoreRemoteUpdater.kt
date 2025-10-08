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

import hr.sil.android.schlauebox.cache.DataCache
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

    private const val UPDATE_PERIOD = 10000L

    private val running = AtomicBoolean(false)

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

    private val inHandleUpdate = AtomicBoolean(false)
    private suspend fun handleUpdate() {
        if (inHandleUpdate.compareAndSet(false, true)) {
            if (UserUtil.isUserLoggedIn()) {
                doUpdate()
            }
            inHandleUpdate.set(false)
        }
    }

    private suspend fun doUpdate() {
        val allActiveKeys = DataCache.getActiveKeys()
        val masterUnitsInfo = mapOf<String, RLockerInfo>()

        val units = DataCache.getMasterUnits(true)
        if( units.size == 0 ) {
            log.info("Master unit size from backend is: ${units.size}")
        }
        log.info("Master unit size = ${units.size} , ${masterUnitsInfo.values.joinToString { it.mac }}")

        MPLDeviceStore.updateFromRemote(
                units.map { masterUnit ->
                    MasterUnitWithKeys(
                            masterUnit = masterUnit,
                            activeKeys = allActiveKeys.filter { lockerKey ->
                                lockerKey.lockerMasterId == masterUnit.id
                            },
                            availableLockerSizes = listOf()
                    )
                }
        )
    }

}