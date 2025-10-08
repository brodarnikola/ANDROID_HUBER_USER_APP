package hr.sil.android.schlauebox.listeners

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.util.logger
//import org.jetbrains.anko.toast
import android.os.Bundle
import hr.sil.android.schlauebox.cache.status.InstallationKeyHandler
import hr.sil.android.schlauebox.data.InstallationKey


class InstallListener : BroadcastReceiver() {
    val log = logger()
    override fun onReceive(ctx: Context?, intent: Intent?) {


        val rawReferrerString = intent?.getStringExtra("referrer")

        log.info("Getting ref key from intent $rawReferrerString")
        if (!rawReferrerString.isNullOrBlank()) {
            InstallationKeyHandler.key.put(InstallationKey(rawReferrerString))
        }
    }
}