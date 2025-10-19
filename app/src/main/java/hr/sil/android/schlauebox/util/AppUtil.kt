package hr.sil.android.schlauebox.util

//import hr.sil.android.schlauebox.cache.DataCache
//import hr.sil.android.schlauebox.cache.DatabaseHandler
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.DeviceStoreRemoteUpdater


object AppUtil {
    suspend fun refreshCache() {
        //DatabaseHandler.deliveryKeyDb.clear()
        //DataCache.clearCaches()
        //DataCache.preloadCaches()
        MPLDeviceStore.clear()
        //force update device store
        DeviceStoreRemoteUpdater.forceUpdate()
    }
}

