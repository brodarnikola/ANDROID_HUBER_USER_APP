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

package hr.sil.android.schlauebox.view.ui.util.extensions

import android.graphics.Typeface
import android.widget.TextView

/**
 * @author mfatiga
 */
val TextView.light: Typeface get() = Typeface.createFromAsset(context.assets, "fonts/Montserrat-Light.ttf")
val TextView.regular: Typeface get() = Typeface.createFromAsset(context.assets, "fonts/Montserrat-Regular.ttf")
val TextView.medium: Typeface get() = Typeface.createFromAsset(context.assets, "fonts/Montserrat-Medium.ttf")
val TextView.bold: Typeface get() = Typeface.createFromAsset(context.assets, "fonts/Montserrat-Bold.ttf")
