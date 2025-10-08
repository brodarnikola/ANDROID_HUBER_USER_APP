package hr.sil.android.schlauebox.view.ui.home.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.status.ActionStatusHandler
import hr.sil.android.schlauebox.cache.status.ActionStatusKey
import hr.sil.android.schlauebox.cache.status.ActionStatusType
import hr.sil.android.schlauebox.core.remote.model.InstalationType
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SendParcelDeliveryActivity : BaseActivity(R.id.no_ble_layout, R.id.no_internet_layout, R.id.no_location_gps_layout) {

    val log = logger()
    val finishDelivery: Button by lazy { findViewById<Button>(R.id.send_parcel_delivery_next) }
    val retryDelivery: Button by lazy { findViewById<Button>(R.id.send_parcel_retry) }
    val contentMesssage: TextView by lazy { findViewById<TextView>(R.id.send_parcel_main_text) }
    val progressBar: ProgressBar by lazy { findViewById<ProgressBar>(R.id.send_parcel_progress_bar) }
    val macAddress: String by lazy { intent.getStringExtra("rMacAddress") ?: "" }
    private val device by lazy { MPLDeviceStore.devices[macAddress] }
    var mac: String? = null
    var pin: Int = 0
    var size: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_parcel_delivery)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        this.setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        progressBar.visibility = View.VISIBLE
        contentMesssage.visibility = View.GONE
        mac = intent.getStringExtra("rMacAddress")
        pin = intent.getIntExtra("pin", 0)
        size = intent.getStringExtra("size") ?: RLockerSize.L.name
        sendParcel(size)

        finishDelivery.setOnClickListener {
            finish()
        }

        retryDelivery.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            sendParcel(size)
        }
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

    private fun sendParcel(size: String?) {
        log.info("Connecting to $mac...")
        finishDelivery.isEnabled = false
        finishDelivery.alpha = 0.5f
        retryDelivery.isEnabled = false
        retryDelivery.alpha = 0.5f
        val locker = MPLDeviceStore.devices[mac]
        GlobalScope.launch {
            val comunicator = locker?.createBLECommunicator(this@SendParcelDeliveryActivity)
            val user = UserUtil.user
            log.info(" Send parcel delivery Size $size, pin $pin, User ${user?.name}")
            if (size != null && pin != 0 && user != null && comunicator?.connect() == true) {
                log.info("Connected!")

                val reducedMobilityByte = if( UserUtil.user?.reducedMobility ?: false ) 0x01.toByte() else 0x00

                val response = if( device?.installationType == InstalationType.TABLET ) comunicator.requestParcelSendCreateForTablets(RLockerSize.valueOf(size), user.id, pin, reducedMobilityByte)
                else comunicator.requestParcelSendCreate(RLockerSize.valueOf(size), user.id, pin)
                comunicator.disconnect()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        retryDelivery.visibility = View.GONE
                        finishDelivery.isEnabled = true
                        finishDelivery.alpha = 1.0f
                        finishDelivery.visibility = View.VISIBLE
                        log.info("LockerType : ${locker.type}")
                        contentMesssage.visibility = View.VISIBLE
                        contentMesssage.setText(R.string.nav_send_parcel_content_text)
                        contentMesssage.setTextColor(Color.WHITE)
                        if (locker.masterUnitType == RMasterUnitType.SPL) {
                            val action = ActionStatusKey().apply {
                                keyId = locker.macAddress + ActionStatusType.SPL_OCCUPATION
                            }
                            ActionStatusHandler.actionStatusDb.put(action)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        retryDelivery.visibility = View.VISIBLE
                        retryDelivery.alpha = 1.0f
                        finishDelivery.visibility = View.GONE
                        contentMesssage.visibility = View.VISIBLE
                        contentMesssage.setText(R.string.nav_send_parcel_failed)
                        contentMesssage.setTextColor(ContextCompat.getColor(this@SendParcelDeliveryActivity, R.color.colorError))
                        retryDelivery.isEnabled = true
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    retryDelivery.visibility = View.VISIBLE
                    retryDelivery.alpha = 1.0f
                    finishDelivery.visibility = View.GONE
                    contentMesssage.visibility = View.VISIBLE
                    contentMesssage.setText(R.string.nav_send_parcel_failed)
                    contentMesssage.setTextColor(ContextCompat.getColor(this@SendParcelDeliveryActivity, R.color.colorError))
                    retryDelivery.isEnabled = true
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
    }

    override fun onPause() {
        super.onPause()
        App.ref.eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: UnauthorizedUserEvent) {
        log.info("Received unauthorized event, user will now be log outed")
        val intent = Intent( this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}