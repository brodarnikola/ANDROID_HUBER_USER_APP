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

package hr.sil.android.schlauebox.view.ui.util

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import java.util.*

/**
 * @author mfatiga
 */
class ActivityForResultWrapper {
    private var reqCode = -1
    private var deferred: CompletableDeferred<Intent?>? = null
    fun call(activity: Activity, intent: Intent): Deferred<Intent?> {
        if (deferred == null) {
            deferred = CompletableDeferred()
            reqCode = Random().nextInt(65535) + 1

            activity.startActivityForResult(intent, reqCode)
        }
        return deferred ?: CompletableDeferred(value = null)
    }




    fun call(fragment: Fragment, intent: Intent): Deferred<Intent?> {
        if (deferred == null) {
            deferred = CompletableDeferred()
            reqCode = Random().nextInt(65535) + 1

            fragment.startActivityForResult(intent, reqCode)
        }
        return deferred ?: CompletableDeferred(value = null)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == reqCode) {
            deferred?.complete(if (resultCode == Activity.RESULT_OK) data else null)
            deferred = null
        }
    }
}