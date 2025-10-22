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

package hr.sil.android.schlauebox.store.model

import android.content.Context
import hr.sil.android.ble.scanner.model.device.BLEDevice
import hr.sil.android.ble.scanner.scan_multi.model.BLEDeviceData
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvMplMaster
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvMplTablet
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvSpl
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvSplPlus
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLModemStatus
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties
import hr.sil.android.schlauebox.App
//import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.ble.comm.MPLUserBLECommunicator
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.core.util.macRealToClean
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.util.general.extensions.lerpInDomain
import kotlinx.coroutines.runBlocking

/**
 * @author mfatiga
 */
class MPLDevice private constructor(
        val macAddress: String,

        // type
        val type: MPLDeviceType,
        val installationType: InstalationType?,

        // from remote
        val masterUnitId: Int,
        var accessTypes: List<RMasterUnitAccessType>,
        val isSplActivate: Boolean = false,
        val masterUnitType: RMasterUnitType,
        val name: String,
        val address: String,
        val activeKeys: List<RLockerKey>,

        // from BLE
        val mplMasterDeviceStatus: MPLDeviceStatus,
        val mplMasterModemStatus: MPLModemStatus,
        val mplMasterModemQueueSize: Int,
        val bleRssi: Int?,
        val bleTxPower: Int?,
        val bleDistance: Double?,
        val batteryVoltage: Double?,

        // combined
        val availableLockers: List<RAvailableLockerSize>,
        val isInBleProximity: Boolean,
        val modemRssi: Int?,
        val humidity: Double?,
        val pressure: Double?,
        val temperature: Double?,
        val pinManagementAllowed: Boolean?,
        val keypadType: ParcelLockerKeyboardType,
        val isDeviceAccessible: Boolean? = false,
        val isProductionReady: Boolean? = false

) {

    fun isDeviceAccessible(mplDevice: MPLDevice): Boolean {
        if (isInBleProximity) {
            return mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED && mplDevice.isDeviceAccessible == true /* && mplDevice.installationType != InstalationType.UNKNOWN*/
        } else {
            return accessTypes.any {
                it == RMasterUnitAccessType.BY_GROUP_OWNERSHIP || it == RMasterUnitAccessType.BY_ACTIVE_PAF_KEY
                        || it == RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_ADMIN || it == RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_USER
            }
        }
    }

    fun hasRightsToShareAccess(): Boolean {
        return accessTypes.any { it == RMasterUnitAccessType.BY_GROUP_OWNERSHIP || it == RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_ADMIN }
    }


    fun hasUserRightsOnLocker(): Boolean {
        if (accessTypes.any {
                    it == RMasterUnitAccessType.BY_GROUP_OWNERSHIP || it == RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_ADMIN
                            || it == RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_USER || it == RMasterUnitAccessType.BY_ACTIVE_PAF_KEY
                }) {
            return true
        }
        return false
    }

    fun hasUserRightsOnMplSend(): Boolean {
        return accessTypes.any {
            it == RMasterUnitAccessType.BY_GROUP_OWNERSHIP /*|| it == RMasterUnitAccessType.BY_ACTIVE_PAF_KEY*/
                    || it == RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_ADMIN
        }
    }

    companion object {
        private fun getAvailableLockers(remoteData: MasterUnitWithKeys?,
                                        bleData: BLEDevice<BLEDeviceData>?
        ): List<RAvailableLockerSize> {
            var freeXS = 0
            var freeS = 0
            var freeM = 0
            var freeL = 0
            var freeXL = 0

            if (bleData != null) {
                //prioritize getting available sizes from BLE advertisement
                val bleProps = bleData.data.properties
                if (bleProps is BLEAdvMplMaster) {
                    freeXS = bleProps.slavesFreeXS.value ?: 0
                    freeS = bleProps.slavesFreeS.value ?: 0
                    freeM = bleProps.slavesFreeM.value ?: 0
                    freeL = bleProps.slavesFreeL.value ?: 0
                    freeXL = bleProps.slavesFreeXL.value ?: 0
                } else if (bleProps is BLEAdvMplTablet) {
                    freeXS = bleProps.slavesFreeXS.value ?: 0
                    freeS = bleProps.slavesFreeS.value ?: 0
                    freeM = bleProps.slavesFreeM.value ?: 0
                    freeL = bleProps.slavesFreeL.value ?: 0
                    freeXL = bleProps.slavesFreeXL.value ?: 0
                }
                else if (bleProps is BLEAdvSplPlus) {
                    freeS = bleProps.lockersFreeS.value ?: 0
                    freeL = bleProps.lockersFreeL.value ?: 0
                }

            } else {
                for (availableLockerSize in (remoteData?.availableLockerSizes ?: listOf())) {
                    when (availableLockerSize.size) {
                        RLockerSize.XS -> {
                            freeXS = availableLockerSize.count
                        }
                        RLockerSize.S -> {
                            freeS = availableLockerSize.count
                        }
                        RLockerSize.M -> {
                            freeM = availableLockerSize.count
                        }
                        RLockerSize.L -> {
                            freeL = availableLockerSize.count
                        }
                        RLockerSize.XL -> {
                            freeXL = availableLockerSize.count
                        }
                        else -> {
                        }
                    }
                }
            }

            return listOf(
                    RAvailableLockerSize(RLockerSize.XS, freeXS),
                    RAvailableLockerSize(RLockerSize.S, freeS),
                    RAvailableLockerSize(RLockerSize.M, freeM),
                    RAvailableLockerSize(RLockerSize.L, freeL),
                    RAvailableLockerSize(RLockerSize.XL, freeXL))
        }


        fun create(macAddress: String,
                   remoteData: MasterUnitWithKeys?,
                   bleData: BLEDevice<BLEDeviceData>?): MPLDevice {

            // remote
            val masterUnit = remoteData?.masterUnit
            val activeKeys = remoteData?.activeKeys
            val installationType = remoteData?.masterUnit?.installationType
            val productionReadyUserHasAccess = remoteData?.masterUnit?.productionReady

            // ble
            var mplDeviceType = MPLDeviceType.UNKNOWN
            var mplMasterDeviceStatus = MPLDeviceStatus.UNKNOWN
            var mplMasterModemStatus = MPLModemStatus.UNKNOWN
            var mplMasterModemQueueSize = 0
            var bleMasterUnitType: RMasterUnitType = RMasterUnitType.UNKNOWN
            val bleProps = bleData?.data?.properties
            var batteryVoltage: Double? = null
            val isInBleProximity = bleProps != null
            var modemRssi: Int? = null
            var humidity: Double? = null
            var pressure: Double? = null
            var temperature: Double? = null
            val pinManagementAllowed: Boolean? = remoteData?.masterUnit?.allowPinSave
            var keypadType = ParcelLockerKeyboardType.SPL_PLUS

            when (bleProps) {
                is BLEAdvMplMaster -> {
                    batteryVoltage = getBatteryVoltage(bleProps)
                    mplDeviceType = MPLDeviceType.MASTER
                    mplMasterDeviceStatus = bleProps.deviceStatus.value ?: MPLDeviceStatus.UNKNOWN
                    mplMasterModemStatus = bleProps.modemStatus.value ?: MPLModemStatus.UNKNOWN
                    mplMasterModemQueueSize = bleProps.modemQueue.value ?: 0
                    modemRssi = bleProps.modemRSSI.value
                    temperature = bleProps.temperature.value
                    pressure = bleProps.pressure.value
                    humidity = bleProps.humidity.value
                    bleMasterUnitType = RMasterUnitType.MPL
                }

                is BLEAdvMplTablet -> {
                    mplDeviceType = MPLDeviceType.TABLET
                    mplMasterDeviceStatus = bleProps.deviceStatus.value ?: MPLDeviceStatus.UNKNOWN
                    temperature = bleProps.temperature.value
                    pressure = bleProps.pressure.value
                    humidity = bleProps.humidity.value
                    bleMasterUnitType = RMasterUnitType.MPL
                }

                is BLEAdvSpl -> {
                    batteryVoltage = getBatteryVoltage(bleProps)
                    mplDeviceType = MPLDeviceType.SPL
                    mplMasterDeviceStatus = bleProps.deviceStatus.value ?: MPLDeviceStatus.UNKNOWN
                    mplMasterModemStatus = bleProps.modemStatus.value ?: MPLModemStatus.UNKNOWN
                    mplMasterModemQueueSize = bleProps.modemQueue.value ?: 0
                    modemRssi = bleProps.modemRSSI.value
                    temperature = bleProps.temperature.value
                    pressure = bleProps.pressure.value
                    humidity = bleProps.humidity.value
                    bleMasterUnitType = RMasterUnitType.SPL
                }

                is BLEAdvSplPlus -> {
                    batteryVoltage = getBatteryVoltage(bleProps)
                    mplDeviceType = MPLDeviceType.SPL_PLUS
                    mplMasterDeviceStatus = bleProps.deviceStatus.value ?: MPLDeviceStatus.UNKNOWN
                    mplMasterModemStatus = bleProps.modemStatus.value ?: MPLModemStatus.UNKNOWN
                    mplMasterModemQueueSize = bleProps.modemQueue.value ?: 0
                    modemRssi = bleProps.modemRSSI.value
                    temperature = bleProps.temperature.value
                    pressure = bleProps.pressure.value
                    humidity = bleProps.humidity.value
                    bleMasterUnitType = RMasterUnitType.SPL_PLUS
                    keypadType = bleProps.keyboardType.value ?: ParcelLockerKeyboardType.SPL_PLUS
                }
            }


            return runBlocking {
                val getDevicesInfo = MPLDeviceStore.remoteInfoDevices
                val device = getDevicesInfo.values.find { it.mac == macAddress.macRealToClean() }
                val log = logger()
                log.info("Device info is: ${ device }, device address is: ${device?.address}, device mac is ${device?.mac}  ")
                val rName = masterUnit?.name ?: device?.name ?: macAddress
                val rAddress = masterUnit?.address ?: device?.address ?: ""
                val isSplActivate = device?.splIsActivated ?: true
                val isProductionReady = device?.productionReady


                MPLDevice(
                        macAddress = macAddress,
                        type = mplDeviceType,
                        installationType = installationType,
                        // from remote
                        activeKeys = activeKeys ?: listOf(),
                        masterUnitId = masterUnit?.id ?: -1,
                        accessTypes = masterUnit?.accessTypes ?: listOf(),
                        isSplActivate = isSplActivate,
                        masterUnitType = masterUnit?.type ?: bleMasterUnitType,
                        name = rName,
                        address = rAddress,

                        // from BLE
                        mplMasterDeviceStatus = mplMasterDeviceStatus,
                        mplMasterModemStatus = mplMasterModemStatus,
                        mplMasterModemQueueSize = mplMasterModemQueueSize,
                        bleRssi = bleData?.rssi,
                        bleTxPower = bleData?.data?.txPower,
                        bleDistance = bleData?.data?.distance,
                        batteryVoltage = batteryVoltage,

                        // combined
                        availableLockers = getAvailableLockers(remoteData, bleData),
                        isInBleProximity = isInBleProximity,
                        modemRssi = modemRssi,
                        humidity = humidity,
                        pressure = pressure,
                        temperature = temperature,
                        pinManagementAllowed = pinManagementAllowed,
                        keypadType = keypadType,
                        isDeviceAccessible = device != null,
                        isProductionReady = if( productionReadyUserHasAccess != null ) productionReadyUserHasAccess else isProductionReady
                )
            }
        }

        private fun getBatteryVoltage(bleProps: BLEAdvProperties): Double {
            when (bleProps) {
                is BLEAdvMplMaster -> {
                    val raw = bleProps.batteryRaw.value?.toDouble()?.lerpInDomain(0.0, 255.0, 0.0, 65535.0)
                            ?: 0.0
                    return 0.0005895 * raw - 18.65
                }
                is BLEAdvSpl -> {
                    val raw = bleProps.batteryRaw.value?.toDouble()?.lerpInDomain(0.0, 255.0, 0.0, 65535.0)
                            ?: 0.0
                    return 0.0005895 * raw - 18.65
                }
                else -> return 0.0
            }
        }
    }

    // util
    fun createBLECommunicator(context: Context): MPLUserBLECommunicator {
        return MPLUserBLECommunicator(context, macAddress, App.ref)
    }
}