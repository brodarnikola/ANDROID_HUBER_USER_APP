package hr.sil.android.schlauebox.cache

import hr.sil.android.datacache.TwoLevelCache
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.data.DeliveryKey

/**
 * Created by Stef on 29.1.2018..
 */
object DatabaseHandler {


    val deliveryKeyDb by lazy {
        TwoLevelCache
                .Builder(DeliveryKey::class, DeliveryKey::masterMacAddress)
                .memoryLruMaxSize(20)
                .build(App.ref)
    }


}