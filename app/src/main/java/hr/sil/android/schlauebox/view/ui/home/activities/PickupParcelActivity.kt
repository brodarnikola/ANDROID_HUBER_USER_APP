package hr.sil.android.schlauebox.view.ui.home.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.BuildConfig
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.DatabaseHandler
//import hr.sil.android.schlauebox.cache.DataCache
//import hr.sil.android.schlauebox.cache.DatabaseHandler
import hr.sil.android.schlauebox.core.ble.comm.model.LockerFlagsUtil
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.core.util.*
import hr.sil.android.schlauebox.data.DeliveryKey
import hr.sil.android.schlauebox.databinding.ActivityParcelPickupBinding
import hr.sil.android.schlauebox.events.MPLDevicesUpdatedEvent
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.DeviceStoreRemoteUpdater
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.model.MPLDevice
import hr.sil.android.schlauebox.util.NotificationHelper
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.MainActivity1
import hr.sil.android.schlauebox.view.ui.home.adapters.ParcelPickupKeysAdapter
import hr.sil.android.util.general.extensions.hexToByteArray
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Math.abs
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class PickupParcelActivity : BaseActivity(R.id.no_ble_layout, R.id.no_internet_layout, R.id.no_location_gps_layout) {


    private lateinit var binding: ActivityParcelPickupBinding

    val log = logger()
    private lateinit var macAddress: String
    private val lockerLoaderRunning = AtomicBoolean(false)
    private val startingTime = Date()
    private var exitTime: Date? = null
    private val denyProcedureDuration = 60000L
    private val openButton by lazy { findViewById<ImageButton>(R.id.pickup_parcel_image) }
    private val statusTextDescription by lazy { findViewById<TextView>(R.id.pickup_parcel_state_text) }
    private val statusText by lazy { findViewById<TextView>(R.id.pickup_parcel_state) }
    private val progressBar by lazy { findViewById<ProgressBar>(R.id.pickup_parcel_progress_bar) }
    private val finish by lazy { findViewById<Button>(R.id.pickup_parcel_finish) }
    private val forceOpen by lazy { findViewById<Button>(R.id.send_parcel_force_open) }

    private val keysList by lazy { findViewById<RecyclerView>(R.id.pickup_parcel_keys) }
    private lateinit var keyListAdapter: ParcelPickupKeysAdapter
    private val connecting = AtomicBoolean(false)

    private val MAC_ADDRESS_7_BYTE_LENGTH = 14
    private val MAC_ADDRESS_6_BYTE_LENGTH = 12
    private val MAC_ADDRESS_LAST_BYTE_LENGTH = 2

    suspend private fun combineLockerKeys(): MutableList<RCreatedLockerKey> {
        val keysAssignedToUser = MPLDeviceStore.devices[macAddress]?.activeKeys?.filter {
            it.lockerMasterMac.macCleanToReal() == macAddress && it.purpose != RLockerKeyPurpose.PAH && isUserPartOfGroup(it.createdForGroup, it.createdForId)
        }?.map {
            RCreatedLockerKey().apply {
                this.id = it.id
                this.createdById = it.createdById
                this.lockerMac = it.lockerMac
                this.lockerId = it.lockerId
                this.lockerMasterId = it.lockerMasterId
                this.lockerMasterMac = it.lockerMasterMac
                this.createdByName = it.createdByName
                this.purpose = it.purpose
                this.masterAddress = it.masterAddress
                this.masterName = it.masterName
                this.lockerSize = it.lockerSize
                if( it.timeCreated != null ) {
                    this.timeCreated = it.timeCreated
                }
                else {
                    this.timeCreated = ""
                }
            }
        }?.toMutableList()
                ?: mutableListOf()
        log.info("Assigned keys ${keysAssignedToUser.size}")

        val remotePaFKeys = WSUser.getActivePaFCreatedKeys()
        val createdPaFKeys = remotePaFKeys?.filter { it.lockerMasterMac.macCleanToReal() == macAddress }
                ?: mutableListOf()
        log.info("PAF keys ${createdPaFKeys.size}")
        keysAssignedToUser.addAll(createdPaFKeys)
        return keysAssignedToUser.sortedByDescending { it.purpose }.sortedBy { it.timeCreated }.toMutableList()


    }

    private fun isUserPartOfGroup(createdForGroup: Int?, createdForId: Int?): Boolean {
        log.info("User memberShip data: ${UserUtil.userMemberships.find { it.groupId == createdForGroup && it.role == RUserAccessRole.ADMIN.name }}")
        return UserUtil.userMemberships.find { it.groupId == createdForGroup && it.role == RUserAccessRole.ADMIN.name } != null || UserUtil.userGroup?.id == createdForGroup || UserUtil.user?.id == createdForId
    }

    private var device: MPLDevice? = null
    private val openedParcels = mutableListOf<String>()
    private var keyPurpose = RLockerKeyPurpose.UNKNOWN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParcelPickupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        this.setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false);
        macAddress = intent.getStringExtra("rMacAddress") ?: ""
        log.info("Pickup mpl = $macAddress")

        device = MPLDeviceStore.devices[macAddress]
    }


    override fun onStart() {
        super.onStart()

        setupAdapterForKeys()

        finish.setOnClickListener {
            if (keyPurpose == RLockerKeyPurpose.DELIVERY)
                finish()
            else {

                GlobalScope.launch {

                    //DataCache.clearCacheAfterPickAtFriend()

                    MPLDeviceStore.clear()
                    DeviceStoreRemoteUpdater.forceUpdate()

                    withContext(Dispatchers.Main) {
                        val startIntent = Intent(this@PickupParcelActivity, MainActivity1::class.java)
                        startActivity(startIntent)
                        finish()
                    }
                }
            }
        }

        forceOpen.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val device = MPLDeviceStore.devices[macAddress]
            GlobalScope.launch {
                val mac = if (device?.masterUnitType == RMasterUnitType.SPL) device.macAddress else ""
                val communicator = device?.createBLECommunicator(this@PickupParcelActivity)
                if (communicator?.connect() == true) {
                    openedParcels.forEach {
                        log.info("Requesting force pickup for ${it} ")
                        // sending clean mac address
                        val bleResponse = communicator.forceOpenDoor(it)

                        if (!bleResponse) {
                            log.error(bleResponse.toString())
                        } else {
                            log.info("Success delivery on $it")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                    }
                    communicator.disconnect()

                } else {
                    withContext(Dispatchers.Main) {
                        //App.ref.toast(R.string.app_generic_error)
                        log.error("Error while connecting the device")
                    }
                }
                communicator?.disconnect()
            }
        }

        openButton.setOnClickListener {
            val ctx = this@PickupParcelActivity
            if (connecting.compareAndSet(false, true)) {
                progressBar.visibility = View.VISIBLE
                openButton.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_locked_disabled))
                openButton.isEnabled = false
                statusText.text = ctx.getString(R.string.nav_pickup_parcel_connecting).uppercase()
                statusTextDescription.text = ctx.getString(R.string.nav_pickup_parcel_connecting_please_wait).uppercase()
                val userId = UserUtil.user?.id
                log.info("Trying to open communicator")
                if (isOpenDoorPossible() && userId != null) {
                    val mac = macAddress
                    log.info("Connecting to $mac...")

                    GlobalScope.launch {
                        val comunicator = MPLDeviceStore.devices[mac]?.createBLECommunicator(this@PickupParcelActivity)
                        if (comunicator?.connect() == true) {
                            log.info("Connected!")
                            var actionSuccessfull = true
                            var lockerMacAddress = ""
                            val keys = keyListAdapter.keys
                            pickupAllDeliveries() // or pickup all keys
                            MPLDeviceStore.devices[macAddress]?.activeKeys?.filter { it.purpose == RLockerKeyPurpose.DELIVERY || it.purpose == RLockerKeyPurpose.PAF }?.forEach {
                                log.info("Requesting pickup for ${it.lockerMac} , $userId")
                                val openedMac = it.lockerMac
                                val bleResponse = comunicator.requestParcelPickup(it.lockerMac, userId)
                                keyPurpose = it.purpose
                                val id = it.id
                                if (!bleResponse.isSuccessful) {
                                    withContext(Dispatchers.Main) {
                                        log.error(bleResponse.toString())
                                        log.info("Error while picking up ${bleResponse} , $userId")
                                        actionSuccessfull = false
                                    }
                                } else {
                                    // added clean mac address
                                    withContext(Dispatchers.Main) {
                                        lockerMacAddress = it.lockerMac
                                        openedParcels.add(it.lockerMac)
                                        keyListAdapter.keys.removeAll { it.lockerMac == openedMac }
                                        keyListAdapter.notifyDataSetChanged()
                                        persistActionOpenKey(it.id)
                                        NotificationHelper.clearNotification()
                                    }
                                }
                                if (BuildConfig.DEBUG) {
                                    withContext(Dispatchers.Main) {
                                        log.info("BLUEBTOOTH LOG: ${bleResponse}")
                                        //App.ref.toast("$bleResponse")
                                    }

                                }
                            }
                            comunicator.disconnect()
                            withContext(Dispatchers.Main) {
                                if (actionSuccessfull) {
                                    finish.visibility = View.VISIBLE
                                    forceOpen.visibility = View.VISIBLE
                                    openButton.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_unlocked))
                                    openButton.isEnabled = false
                                    statusText.text = ctx.getString(R.string.nav_pickup_parcel_unlock).uppercase()
                                    statusTextDescription.text = ctx.getString(R.string.nav_pickup_parcel_content_unlock)

                                    if (MPLDeviceStore.devices[macAddress]?.activeKeys?.size ?: 0 == 1 && device?.installationType == InstalationType.TABLET) {
                                        binding.llClean.visibility = View.VISIBLE
                                        setLockerCleaningCheckBoxListener(lockerMacAddress)
                                    }
                                } else {
                                    setUnSuccessOpenView()
                                }
                                progressBar.visibility = View.GONE
                            }
                        } else {
                            comunicator?.disconnect()
                            withContext(Dispatchers.Main) {
                                setUnSuccessOpenView()
                                progressBar.visibility = View.GONE
                            }
                        }
                        denyOpenProcedure()
                    }
                } else {
                    log.error("Error while connecting the MPL device please check device proximity and user id $userId")
                    //App.ref.toast(ctx.getString(R.string.toast_pickup_parcel_error, userId))
                    openButton.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_locked))
                    progressBar.visibility = View.GONE
                    openButton.isEnabled = true
                    statusText.text = ctx.getString(R.string.nav_pickup_parcel_lock).uppercase()
                    statusTextDescription.text = ctx.getString(R.string.nav_pickup_parcel_content_lock)
                }

                connecting.set(false)
            }
        }
    }

    private fun pickupAllDeliveries() {

    }

    private fun setLockerCleaningCheckBoxListener(lockerMacAddress: String) {
        binding.lockerCleaningCheckBox.setOnClickListener {
            binding.lockerCleaningCheckBox.isEnabled = false
            binding.lockerCleaningCheckBox.alpha = 0.5f
            binding.progressBarLockerCleaning.visibility = View.VISIBLE

            GlobalScope.launch {
                val comunicator =
                        MPLDeviceStore.devices[macAddress]?.createBLECommunicator(this@PickupParcelActivity)
                if (comunicator?.connect() == true) {
                    log.info("Connected!")

                    val lockerMacAddressList: MutableList<LockerFlagsUtil.LockerInfo> =
                            mutableListOf()
                    val lockerInfo = LockerFlagsUtil.LockerInfo(byteArrayOf(), byteArrayOf())

                    when {
                        // this is for mpl with new lockers with p16
                        lockerMacAddress.length == MAC_ADDRESS_7_BYTE_LENGTH -> {
                            lockerInfo.mac =
                                    lockerMacAddress.take(MAC_ADDRESS_6_BYTE_LENGTH).macCleanToBytes()
                                            .reversedArray()
                            lockerInfo.index =
                                    lockerMacAddress.takeLast(MAC_ADDRESS_LAST_BYTE_LENGTH).hexToByteArray()
                        }
                        // this is for mpl with old lockers
                        else -> {
                            lockerInfo.mac =
                                    lockerMacAddress.macCleanToBytes().reversedArray()
                            lockerInfo.index = byteArrayOf(0x00)
                        }
                    }

                    lockerMacAddressList.add(lockerInfo)

                    // data za cleaning needed je zapravo: 0x02 ( flag da zelimo updajtati cleaning needed ) , slaveMac u obrnutom redoslijedu,
                    // locker index i plus zadnji byte ( 0x02 locker je zmazan ili 0x00 locker je cist )
                    // flag + mac + inde lockera + value flaga
                    // prvi flag ->  02 cleaning
                    //  01 - reduced
                    //  03 both

                    // flag value -> zadnji byte
                    //  00 makni sve
                    //  01 red -> true
                    //  02 cle -> true
                    //  03 both -> true
                    val byteArrayCLeaningNeeded = LockerFlagsUtil.generateCleaningRequiredData(
                            lockerMacAddressList,
                            true
                    )
                    val response = comunicator.lockerIsDirty(byteArrayCLeaningNeeded)

                    comunicator.disconnect()

                    log.info("Cleaning function is successfully: ${response}")

                    withContext(Dispatchers.Main) {
                        if (response) {
                            binding.lockerCleaningCheckBox.isEnabled = false
                            binding.lockerCleaningCheckBox.alpha = 1.0f
                            binding.progressBarLockerCleaning.visibility = View.GONE
                            //App.ref.toast(getString(R.string.app_generic_success))
                        } else {
                            binding.lockerCleaningCheckBox.isEnabled = true
                            binding.lockerCleaningCheckBox.alpha = 1.0f
                            binding.progressBarLockerCleaning.visibility = View.GONE
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.lockerCleaningCheckBox.isEnabled = true
                        binding.lockerCleaningCheckBox.alpha = 1.0f
                        binding.progressBarLockerCleaning.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun denyOpenProcedure() {
        if (lockerLoaderRunning.compareAndSet(false, true)) {
            GlobalScope.launch(Dispatchers.Default) {
                val time = exitTime?.time ?: 0L
                log.debug("Exit time $time")
                val compare = abs(time - startingTime.time)
                log.debug("compare time $compare")
                var timeForOpen = denyProcedureDuration
                if (compare in 1..denyProcedureDuration) {
                    timeForOpen = denyProcedureDuration - compare
                }
                delay(timeForOpen)
                log.debug("Starting erase procedure")
                withContext(Dispatchers.Main) {
                    forceOpen.visibility = View.GONE
                }
            }
        }
    }

    private fun setUnSuccessOpenView() {
        log.info("Connection failed!")
        openButton.setImageDrawable(ContextCompat.getDrawable(this@PickupParcelActivity, R.drawable.ic_locked))
        openButton.isEnabled = true
        statusText.text = this@PickupParcelActivity.getString(R.string.nav_pickup_parcel_lock).uppercase()
        statusTextDescription.text = this@PickupParcelActivity.getString(R.string.nav_pickup_parcel_content_lock)
    }

    private fun persistActionOpenKey(id: Int) {
        val deliveryKeys = DatabaseHandler.deliveryKeyDb.get(macAddress)
        if (deliveryKeys == null) DatabaseHandler.deliveryKeyDb.put(DeliveryKey(macAddress, listOf(id))) else {
            if (!deliveryKeys.keyIds.contains(id)) {
                val listOfIds = deliveryKeys.keyIds.plus(id)
                DatabaseHandler.deliveryKeyDb.put(DeliveryKey(macAddress, listOfIds))
            }
        }
    }

    private fun isOpenDoorPossible(): Boolean {

        var hasUnusedKeys = false
        val keys = DatabaseHandler.deliveryKeyDb.get(macAddress)
        if (keys == null) {
            return device?.activeKeys?.filter { it.purpose != RLockerKeyPurpose.PAH }?.isNotEmpty()
                    ?: false
        } else {
            device?.activeKeys?.forEach {
                if (it.purpose != RLockerKeyPurpose.PAH && !keys.keyIds.contains(it.id)) {
                    hasUnusedKeys = true
                    return@forEach
                }
            }
        }

        return device?.isInBleProximity ?: false && device?.hasUserRightsOnLocker() ?: false && hasUnusedKeys

    }

    fun setupAdapterForKeys() {
        GlobalScope.launch(Dispatchers.Default) {
            keyListAdapter = ParcelPickupKeysAdapter({
                GlobalScope.launch {
                    val keys = combineLockerKeys()
                    withContext(Dispatchers.Main) {
                        keyListAdapter.update(keys)
                    }
                }
            }, device?.masterUnitType ?: RMasterUnitType.MPL, this@PickupParcelActivity)
            withContext(Dispatchers.Main) {
                keysList.layoutManager = LinearLayoutManager(this@PickupParcelActivity, LinearLayoutManager.VERTICAL, false)
                keysList.adapter = keyListAdapter
                keysList.adapter?.notifyDataSetChanged()
                setupOpenButton()
                keyListAdapter.update(combineLockerKeys())
            }
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

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
    }

    override fun onPause() {
        super.onPause()
        App.ref.eventBus.unregister(this)
        exitTime = Date()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: UnauthorizedUserEvent) {
        log.info("Received unauthorized event, user will now be log outed")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: MPLDevicesUpdatedEvent) {
        log.info("Received MPL event")
        device = MPLDeviceStore.devices.values.find { it.macAddress == macAddress }

        if (!connecting.get()) {
            setupOpenButton()
        }

    }

    private fun setupOpenButton() {
        if (device == null) {
            statusText.text = this.getString(R.string.nav_pickup_parcel_unlock).uppercase()
            statusTextDescription.setText(R.string.nav_pickup_parcel_content_unlock)
        }
        else {
            if (device?.isInBleProximity == true && isOpenDoorPossible()) {
                openButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_locked))
                openButton.isEnabled = true
                statusText.text = this.getString(R.string.nav_pickup_parcel_lock).uppercase()
                statusTextDescription.setText(R.string.nav_pickup_parcel_content_lock)
            } else {
                if (device?.isInBleProximity == true) {
                    openButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_unlocked))
                    statusText.text = this.getString(R.string.nav_pickup_parcel_unlock).uppercase()
                    statusTextDescription.setText(R.string.nav_pickup_parcel_content_unlock)

                } else {
                    openButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_locked_disabled))
                    statusTextDescription.setText(R.string.app_generic_enter_ble)
                    statusText.text = ""
                }
                openButton.isEnabled = false
            }
        }
    }
}