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

package hr.sil.android.schlauebox.core.remote.model

import com.google.gson.annotations.SerializedName

/**
 * @author mfatiga
 */
class RMasterUnit {
    var id: Int = 0
    var mac: String = ""
    var type: RMasterUnitType = RMasterUnitType.UNKNOWN
    var installationType: InstalationType = InstalationType.DEVICE
    var name: String = ""
    var address: String = ""
    var accessTypes: List<RMasterUnitAccessType> = listOf()
    var allowPinSave: Boolean = false
    var powerType: RPowerTypeEnum = RPowerTypeEnum.BATTERY
    var modemSleepTime: Long = 60
    @SerializedName("networkConfiguration___id")
    var networkConfigurationId: Int = 0

    var productionReady: Boolean = false

}