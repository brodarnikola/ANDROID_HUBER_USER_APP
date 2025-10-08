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

import android.provider.ContactsContract
import hr.sil.android.ble.scanner.model.device.BLEDevice
import hr.sil.android.ble.scanner.scan_multi.model.BLEDeviceData
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RLockerInfo
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.events.MPLDevicesUpdatedEvent
import hr.sil.android.schlauebox.store.model.MPLDevice
import hr.sil.android.schlauebox.store.model.MasterUnitWithKeys
import hr.sil.android.schlauebox.core.util.macCleanToReal
import hr.sil.android.schlauebox.core.util.macRealToClean
import hr.sil.android.util.general.delegates.synchronizedDelegate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author mfatiga
 */
object MPLDeviceStore {
    private var mDevices by synchronizedDelegate(mapOf<String, MPLDevice>())
    var remoteInfoDevices = mutableMapOf<String, RLockerInfo>()
    var remoteInfoKeys = mutableListOf<String>()
    val devices: Map<String, MPLDevice>
        get() = mDevices.toMap()

    private var bleData by synchronizedDelegate(mapOf<String, BLEDevice<BLEDeviceData>>())
    fun updateFromBLE(bleDevices: List<BLEDevice<BLEDeviceData>>) {

        bleData = bleDevices.associateBy { it.deviceAddress.uppercase() }
        mergeData()
        notifyEvents(bleDevices.map { it.deviceAddress.uppercase() })
    }

    val log = logger()
    private var remoteData by synchronizedDelegate(mapOf<String, MasterUnitWithKeys>())
    suspend fun updateFromRemote(remoteDevices: Collection<MasterUnitWithKeys>) {
        remoteInfoKeys = bleData.map { it.key.macRealToClean()  }.toMutableList()

        if (remoteInfoKeys.isNotEmpty()){
            val list= WSUser.getDevicesInfo(remoteInfoKeys)
            log.info("Fetched list size${list?.size}.. ${list?.joinToString(",") { it.mac.plus(it.productionReady) } }")
            remoteInfoDevices = list?.associateBy { it.mac.macCleanToReal() }?.toMutableMap()
                    ?: mutableMapOf()
        }

        log.info("size of remoteInfo keys is: " + remoteInfoKeys.size)
        log.info("size of bleData is: " + bleData.size)
        remoteData = remoteDevices.associateBy { it.masterUnit.mac.macCleanToReal() }
        mergeData()
        notifyEvents(remoteDevices.map { it.masterUnit.mac.uppercase() })
    }

    private fun mergeData() {
        val allKeys = (remoteData.keys + bleData.keys).distinct()

        GlobalScope.launch {
            val devices = allKeys
                    .associate {
                        it to MPLDevice.create(it, remoteData[it], bleData[it])
                    }
                    .filter {
                        it.value.isDeviceAccessible(it.value)
                    }
                    .toList()

                    .sortedBy { it.second.isInBleProximity && it.second.masterUnitId != -1  }
                    .sortedBy { it.second.isInBleProximity && it.second.masterUnitId == -1 }
                    .sortedBy { !it.second.isInBleProximity && it.second.masterUnitId != -1  }
                    .toMap().toMutableMap()

            mDevices = devices
        }
    }
 
    private fun notifyEvents(macList: List<String>) {
        App.ref.eventBus.post(MPLDevicesUpdatedEvent(macList))
    }

    fun clear() {
        remoteData = mapOf()
        bleData = mapOf()
        mDevices = mapOf()
    }
}