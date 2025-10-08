/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2018] Swiss Innovation Lab AG
* All Rights Reserved.
*
* @author szuzul
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

package hr.sil.android.schlauebox.core.remote.model

import com.google.gson.annotations.SerializedName
import hr.sil.android.ble.scanner.scan_multi.model.BLEDeviceType

/**
 * @author szuzul
 */
class RLockerUnit {
    var id: Int = 0
    var mac: String = ""
    var isAvailable: Boolean = false

    var isDeleted: Boolean = false
    @SerializedName("master___mac")
    var masterMac: String = ""

    var lockerType: RLockerType = RLockerType.NORMAL //RLockerType = RLockerType.NORMAL

    @SerializedName("master___id")
    var masterId: Int = 0

    var size: RLockerSize = RLockerSize.UNKNOWN
}