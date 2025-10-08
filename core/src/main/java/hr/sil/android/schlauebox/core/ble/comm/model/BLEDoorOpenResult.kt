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

package hr.sil.android.schlauebox.core.ble.comm.model

/**
 * @author mfatiga
 */
data class BLEDoorOpenResult(
        val isSuccessful: Boolean,
        val resultStatus: ResultStatus,
        val bleDeviceErrorCode: BLEDeviceErrorCode,
        val bleSlaveErrorCode: BLESlaveErrorCode) {

    companion object {
        fun create(
                bleDeviceErrorCode: BLEDeviceErrorCode,
                bleSlaveErrorCode: BLESlaveErrorCode
        ): BLEDoorOpenResult {
            val bleDeviceSuccess = bleDeviceErrorCode == BLEDoorOpenResult.BLEDeviceErrorCode.OK
            val bleSlaveSuccess = bleSlaveErrorCode == BLEDoorOpenResult.BLESlaveErrorCode.SUCCESS

            val isSuccessful = bleDeviceSuccess && bleSlaveSuccess
            val resultStatus = when {
                !bleDeviceSuccess -> BLEDoorOpenResult.ResultStatus.DEVICE_BLE_ERROR
                !bleSlaveSuccess -> BLEDoorOpenResult.ResultStatus.SLAVE_BLE_ERROR
                else -> BLEDoorOpenResult.ResultStatus.SUCCESS
            }

            return BLEDoorOpenResult(isSuccessful, resultStatus, bleDeviceErrorCode, bleSlaveErrorCode)
        }
    }

    override fun toString(): String {
        return "{ isSuccessful=$isSuccessful; resultStatus=$resultStatus; bleDeviceErrorCode=$bleDeviceErrorCode; bleSlaveErrorCode=$bleSlaveErrorCode; }"
    }

    enum class ResultStatus {
        SUCCESS,
        DEVICE_BLE_ERROR,
        SLAVE_BLE_ERROR
    }

    enum class BLEDeviceErrorCode {
        OK,
        INVALID_PARAMETERS,
        ENCRYPTION_FAILED,
        COMMAND_WRITE_FAILED,
        READ_RESULT_FAILED
    }

    enum class BLESlaveErrorCode(val code: Byte?) {

        NONE(null),
        SUCCESS(0x00),
        CONNECT_IGNORED(0x01),
        CONNECT_START_FAILED(0x02),
        CONNECT_TIMEOUT(0x03),
        CONNECTION_DROPPED(0x04),
        DISCONNECT_IGNORED(0x05),
        DISCONNECT_START_FAILED(0x06),
        DISCOVERY_IGNORED(0x07),
        DISCOVERY_START_FAILED(0x08),
        DISCOVERY_TIMEOUT(0x09),
        DISCOVERY_CONNECTION_DROPPED(0x0A),
        CONNECTION_IN_PROGRESS(0x0B),
        WRITE_FAILED(0x0C),
        READ_FAILED(0x0D),
        NOTIFICATION_OPERATION_TIMEOUT(0x0E),
        NOTIFICATION_ARRIVAL_TIMEOUT(0x0F),
        INVALID(0xFF.toByte());


        companion object {
            fun parse(code: Byte?): BLESlaveErrorCode =
                    values().firstOrNull { it.code == code } ?: NONE
        }
    }
}