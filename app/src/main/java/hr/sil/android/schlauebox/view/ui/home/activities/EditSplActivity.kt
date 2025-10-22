package hr.sil.android.schlauebox.view.ui.home.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
//import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RCreatedLockerKey
import hr.sil.android.schlauebox.core.remote.model.RMasterUnit
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.core.util.macRealToClean
import hr.sil.android.schlauebox.data.DeactivateSPLInterface
import hr.sil.android.schlauebox.store.DeviceStoreRemoteUpdater
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.model.MPLDevice
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.MainActivity1
import hr.sil.android.schlauebox.view.ui.dialog.CanNotDeactivateSPLDialog
import hr.sil.android.schlauebox.view.ui.dialog.DeactivateSplDialog
import hr.sil.android.schlauebox.view.ui.dialog.ModemQueueNotZeroDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditSplActivity : BaseActivity(R.id.no_ble_layout, R.id.no_internet_layout, R.id.no_location_gps_layout), DeactivateSPLInterface {

    private val splName by lazy { findViewById<EditText>(R.id.navSettingsNameEdit) }
    private val splAddress by lazy { findViewById<EditText>(R.id.locker_details_address) }
    private val save by lazy { findViewById<Button>(R.id.key_sharing_button) }
    private val deactivate by lazy { findViewById<Button>(R.id.locker_details_deactivate_locker) }
    private val macAddress by lazy { intent.getStringExtra("rMacAddress") ?: "" }
    private lateinit var pahKeyy: List<RCreatedLockerKey>
    private val log = logger()
    val deActivationProgress by lazy { findViewById<ProgressBar>(R.id.activate_progress) }
    var device: MPLDevice? = null

    override fun onResume() {
        super.onResume()
        device = MPLDeviceStore.devices[macAddress]

        splName.setText(device?.name ?: "")
        splAddress.setText(device?.address ?: "")

        App.ref.eventBus.register(this@EditSplActivity)
    }

    private fun emptyUserActiveKeys(): Boolean {
        return device != null && device?.activeKeys?.isEmpty() ?: false && pahKeyy.isEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_spl)
        //val mFragment = EditSplActivityUI()
        //mFragment.setContentView(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        this.setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        device = MPLDeviceStore.devices[macAddress]

        save.setOnClickListener {
            if (splName.text.isNotEmpty() && splAddress.text.isNotEmpty() && device != null) {
                val splUnit = RMasterUnit().apply {
                    this.address = splAddress.text.toString()
                    this.name = splName.text.toString()
                    this.id = device?.masterUnitId ?: 0
                    this.mac = device?.macAddress?.macRealToClean() ?: ""
                    this.type = device?.masterUnitType ?: RMasterUnitType.SPL
                }
                log.info("Trying to save SPL unit data ${splUnit.address} ${splUnit.name} ${splUnit.id} ${splUnit.mac}")

                GlobalScope.launch {
                    val result = WSUser.modifyMasterUnit(splUnit)
                    withContext(Dispatchers.Main) {
                        if (result) {
                            //App.ref.toast(R.string.app_generic_success)
                            log.info("Successfully saved SPL data unit")
                        } else {
                            log.error("Error while saving SPL data")
                        }
                    }
                }
            } else {
                if (splName.text.isNullOrEmpty()) splName.error = "Please enter the text"
                if (splAddress.text.isNullOrEmpty()) splName.error = "Please enter the text"
            }
        }

        deactivate.setOnClickListener {
            if (device?.mplMasterModemQueueSize == 0) {
                val deactivateSplDialog = DeactivateSplDialog(this@EditSplActivity)
                deactivateSplDialog.show(this.supportFragmentManager, "")
            }
            else {
                val modemQueueNotZeroDialog = ModemQueueNotZeroDialog()
                modemQueueNotZeroDialog.show(supportFragmentManager, "")
            }
        }
    }

    override fun deactivateSPL() {

        deactivate.visibility = View.GONE
        deActivationProgress.visibility = View.VISIBLE
        lifecycleScope.launch {
            if( device?.isInBleProximity ?: false ) {
                if( device?.activeKeys?.isNotEmpty() ?: false ) {
                    val communicator = device?.createBLECommunicator(this@EditSplActivity)
                    if (communicator?.connect() == true) {
                        val bleResponse = communicator.invalidateKeysOnMasterUnit()
                        communicator.disconnect()
                        if (!bleResponse) {
                            withContext(Dispatchers.Main) {
                                //App.ref.toast(getString(R.string.app_generic_error))
                                log.error(bleResponse.toString())
                            }
                        } else {
                            deactivateSPLOnlyOnBackend()
                        }
                    }
                    else {
                        communicator?.disconnect()
                    }
                }
                else {
                    deactivateSPLOnlyOnBackend()
                }
            }
            else {
                if( device?.activeKeys?.isNotEmpty() ?: false ) {
                    withContext(Dispatchers.Main) {
                        val canNotDeactivateSPLDialog = CanNotDeactivateSPLDialog()
                        canNotDeactivateSPLDialog.show(supportFragmentManager, "")
                    }
                }
                else {
                    deactivateSPLOnlyOnBackend()
                }
            }
        }
    }

    private suspend fun deactivateSPLOnlyOnBackend() {
        val deactivateSplPlusBackend = WSUser.deactivateSPL(macAddress.macRealToClean())
        if( deactivateSplPlusBackend ) {
            //DataCache.clearMasterUnitCache()
            //DataCache.clearLockerInfoCache()
            //DataCache.getDevicesInfo(true)

            log.info("Ble entires ${MPLDeviceStore.devices.values.joinToString { it.macAddress }}")
            val entries = MPLDeviceStore.devices.values.map { it.macAddress.macRealToClean() }
            if (entries.isNotEmpty()) {
                log.info("All Ble entires ${MPLDeviceStore.remoteInfoKeys}")
                WSUser.getDevicesInfo(MPLDeviceStore.remoteInfoKeys)
            } else
                listOf()

            DeviceStoreRemoteUpdater.forceUpdate()
        }
        withContext(Dispatchers.Main) {
            if( deactivateSplPlusBackend ) {
                //App.ref.toast(getString(R.string.app_generic_success))
                val startIntent = Intent(this@EditSplActivity, MainActivity1::class.java)
                startActivity(startIntent)
                finish()
                log.info("Success deactivation device on ${device?.macAddress}")
            }
            else {
                //App.ref.toast(getString(R.string.app_generic_error))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (App.ref.eventBus.isRegistered(this@EditSplActivity))
            App.ref.eventBus.unregister(this)
    }

    override fun onBluetoothStateUpdated(available: Boolean) {
        super.onBluetoothStateUpdated(available)
        bluetoothAvalilable = available
        updateUI()
    }

    override fun onNetworkStateUpdated(available: Boolean) {
        super.onNetworkStateUpdated(available)
        networkAvailable = available
        updateUI()
    }

    override fun onLocationGPSStateUpdated(available: Boolean) {
        super.onLocationGPSStateUpdated(available)
        locationGPSAvalilable = available
        updateUI()
    }
}