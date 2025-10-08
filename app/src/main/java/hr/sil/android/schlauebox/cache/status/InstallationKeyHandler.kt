package hr.sil.android.schlauebox.cache.status

import hr.sil.android.datacache.TwoLevelCache
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.data.InstallationKey

object InstallationKeyHandler {
    val key by lazy {
        TwoLevelCache
                .Builder(InstallationKey::class,InstallationKey::key)
                .memoryLruMaxSize(20)
                .build(App.ref)
    }
}