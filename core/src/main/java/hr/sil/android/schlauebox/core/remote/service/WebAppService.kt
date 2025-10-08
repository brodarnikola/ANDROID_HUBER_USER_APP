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

package hr.sil.android.schlauebox.core.remote.service

import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.rest.core.factory.RestServiceAccessor
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author szuzul
 */
interface WebAppService {
    companion object : RestServiceAccessor<WebAppService>(WebAppService::class) {
        //auth: Basic
        private const val ENDPOINT_PREFIX = "service/rest/"
    }

    @POST(ENDPOINT_PREFIX + "language")
    fun getLanguages(@Body pagination: RPagination): Call<RWebLanguage>

}


