package hr.sil.android.schlauebox.view.ui.home.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.DatabaseHandler
import hr.sil.android.schlauebox.cache.status.ActionStatusType
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.core.util.macCleanToReal
import hr.sil.android.schlauebox.core.util.macRealToClean
import hr.sil.android.schlauebox.events.MPLDevicesUpdatedEvent
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.DeviceStoreRemoteUpdater
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.model.MPLDevice
import hr.sil.android.schlauebox.util.AppUtil
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.base.BaseFragment
import hr.sil.android.schlauebox.view.ui.dialog.GeneratedPinDialog
import hr.sil.android.schlauebox.view.ui.dialog.PinManagmentDialog
import hr.sil.android.schlauebox.view.ui.home.activities.*
import hr.sil.android.util.general.extensions.format
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MPLItemDetailsFragment : BaseFragment() {

    val log = logger()

    val macAddress by lazy { arguments?.getString("rMacAddress", "") ?: "" }
    val nameOfDevice by lazy { arguments?.getString("nameOfDevice", "") ?: "" }
    var activeKeysForLocker: List<RLockerKey>? = listOf()
    var pahKeys: List<RCreatedLockerKey>? = listOf()
    var activeRequests: List<RAccessRequest> = listOf()
    var availableLockers: List<RAvailableLockerSize> = listOf()
    var isDeliveryAvailable: Boolean = false
    var isPaHListAvailable: Boolean = false
    val circleValue by lazy { view?.findViewById<TextView>(R.id.mpl_details_circle_value) }
    val parcelOpenButton by lazy { view?.findViewById<ImageButton>(R.id.home_spl_details_pickup_parcel_button) }
    val accessShareButton by lazy { view?.findViewById<ImageButton>(R.id.home_spl_details_key_share) }
    val helpButton by lazy { view?.findViewById<ImageButton>(R.id.home_spl_details_help) }
    val sendParcelButton by lazy { view?.findViewById<ImageButton>(R.id.home_spl_details_send_parcel) }
    val editSPL by lazy { view?.findViewById<ImageButton>(R.id.spl_edit_button) }
    val requestAccess by lazy { view?.findViewById<Button>(R.id.request_mpl_access) }
    val forceOpen by lazy { view?.findViewById<Button>(R.id.send_parcel_force_open) }
    val progressBar by lazy { view?.findViewById<ProgressBar>(R.id.force_open_progress) }
    val activationProgress by lazy { view?.findViewById<ProgressBar>(R.id.activate_progress)}

    val requestAccessText by lazy { view?.findViewById<TextView>(R.id.request_mpl_access_text) }
    val noRightsAccessText by lazy { view?.findViewById<TextView>(R.id.no_rights_access) }
    val headerTitle by lazy { view?.findViewById<TextView>(R.id.mainAddressBoxUserName) }
    val mainLockerAddress by lazy { view?.findViewById<TextView>(R.id.mainAddressBoxAddress) }

    //Telemetry
    val humidity by lazy { view?.findViewById<TextView>(R.id.locker_details_humidity) }
    val temperatre by lazy { view?.findViewById<TextView>(R.id.locker_details_temperature) }
    val preasure by lazy { view?.findViewById<TextView>(R.id.locker_details_pressure) }
    val rssi by lazy { view?.findViewById<TextView>(R.id.locker_details_rssi) }

    var forceOpenSplPlusIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        isDeliveryAvailable = MPLDeviceStore.devices[macAddress]?.hasUserRightsOnLocker() ?: false
        val view = inflater.inflate(R.layout.fragment_device_details, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        val ctx = context ?: return
        val device = MPLDeviceStore.devices[macAddress]

        App.ref.pinManagementName = ""

        parcelOpenButton?.setOnClickListener {
            val startIntent = Intent(ctx, PickupParcelActivity::class.java)
            startIntent.putExtra("rMacAddress", macAddress)
            startActivity(startIntent)
        }

        sendParcelButton?.setOnClickListener {
            isPaHListAvailable = hasUserShareKeys()

            if (isPaHListAvailable) {
                val startIntent = Intent(requireContext(), SendParcelsOverviewActivity::class.java)
                startIntent.putExtra("rMacAddress", macAddress)
                startActivity(startIntent)
            } else {
                if ( device?.masterUnitType == RMasterUnitType.MPL || device?.installationType == InstalationType.TABLET || (device?.type == MPLDeviceType.SPL_PLUS && device?.keypadType != ParcelLockerKeyboardType.SPL) ) {

                    val startIntent = Intent(requireContext(), SelectParcelSizeActivity::class.java)
                    startIntent.putExtra("rMacAddress", macAddress)
                    startActivity(startIntent)
                } else {
                    if (device?.pinManagementAllowed == false)
                        displayNormalPinDialog()
                    else
                        displayPinManagmentDialog()
                }
            }
        }


        accessShareButton?.setOnClickListener {
            val startIntent = Intent(ctx, AccessSharingActivity::class.java)
            startIntent.putExtra("rMacAddress", macAddress)
            startIntent.putExtra("nameOfDevice", nameOfDevice)
            startActivity(startIntent)
        }

        helpButton?.setOnClickListener {
            val startIntent = Intent(ctx, HelpActivity::class.java)
            startIntent.putExtra("rMacAddress", macAddress)
            startActivity(startIntent)
        }

        editSPL?.setOnClickListener {
            val startIntent = Intent(ctx, EditSplActivity::class.java)
            startIntent.putExtra("rMacAddress", macAddress)
            startActivity(startIntent)
        }

        forceOpen?.setOnClickListener {
            progressBar?.visibility = View.VISIBLE
            forceOpen?.visibility = View.GONE

            GlobalScope.launch {
                val device = MPLDeviceStore.devices[macAddress]

                // TODO: CHECK IF HERE NEEDS TO BE .macRealToClean()
                var lockersList = mutableListOf<String>()
                if (device?.type == MPLDeviceType.SPL) lockersList.add(device.macAddress)
                else {
                    val lockerList = WSUser.getLockerFromMasterUnit(device?.macAddress?.macRealToClean()
                            ?: "")
                    if (lockerList?.isNotEmpty() ?: false) {
                        lockersList.addAll(lockerList?.filter { !it.isDeleted }?.map { it.mac.macCleanToReal() }
                                ?: listOf())
                    } else ""
                }

                val communicator = device?.createBLECommunicator(ctx)
                if (communicator?.connect() == true) {
                    log.info("Requesting force pickup for ${macAddress} ")

                    var bleResponse = false
                    if (device.type == MPLDeviceType.SPL || (device.type == MPLDeviceType.SPL_PLUS && device.keypadType == ParcelLockerKeyboardType.SPL)) {

                        if (lockersList.isNotEmpty())
                            bleResponse = communicator.forceOpenDoor(lockersList.first().macRealToClean()
                                    ?: "")
                    } else {
                        for (lockerMac in lockersList) {
                            bleResponse = communicator.forceOpenDoor(lockerMac.macRealToClean())
                        }
                    }

                    if (!bleResponse) {
                        log.error(bleResponse.toString())
                    } else {
                        log.info("Success force open on $macAddress")
                        forceOpenSplPlusIndex++
                    }
                    withContext(Dispatchers.Main) {
                        progressBar?.visibility = View.GONE
                        forceOpen?.visibility = View.VISIBLE
                    }
                    communicator.disconnect()

                } else {
                    withContext(Dispatchers.Main) {
                        //App.ref.toast(R.string.app_generic_error)
                        log.error("Error while connecting the device")
                        progressBar?.visibility = View.GONE
                        forceOpen?.visibility = View.VISIBLE
                    }
                }
                communicator?.disconnect()
            }
        }

        requestAccess?.setOnClickListener {
            log.info("Requesting access for $macAddress")
            activationProgress?.visibility = View.VISIBLE
            requestAccess?.visibility = View.INVISIBLE
            var success: Boolean

            GlobalScope.launch {
                if (device?.masterUnitType == RMasterUnitType.SPL || device?.type == MPLDeviceType.SPL
                        || device?.type == MPLDeviceType.SPL_PLUS) {
                    success = WSUser.activateSPL(macAddress.macRealToClean())
                    withContext(Dispatchers.Main) {
                        if (success) {
                            AppUtil.refreshCache()
                            DeviceStoreRemoteUpdater.forceUpdate()
                            if (device.accessTypes.any { it == RMasterUnitAccessType.BY_GROUP_OWNERSHIP }) {
                                editSPL?.visibility = View.VISIBLE
                            } else {
                                editSPL?.visibility = View.GONE
                            }
                        } else {
                            requestAccess?.visibility = View.VISIBLE
                        }
                        activationProgress?.visibility = View.GONE
                        handleUserMessages(success)
                    }
                } else {
                    log.info("Address for access ${macAddress.macRealToClean()}")
                    success = WSUser.requestMPlAccess(macAddress.macRealToClean())
                    withContext(Dispatchers.Main) {
                        if (success) {
                            requestAccess?.visibility = View.GONE
                            forceOpen?.visibility = View.GONE
                            requestAccessText?.visibility = View.VISIBLE
                        } else {
                            requestAccess?.visibility = View.VISIBLE
                        }
                        activationProgress?.visibility = View.GONE
                        handleUserMessages(success)
                    }
                }
            }
        }

    }

    private fun displayPinManagmentDialog() {

        val pinManagementDialog = PinManagmentDialog(macAddress, RLockerSize.S)
        activity?.supportFragmentManager?.let { it -> pinManagementDialog.show(it, "") }
        // TODO: ONCE DUNJA, QA TESTER THAT SENDING PARCEL IS GOOD, THEN WE CAN DELETE THIS COMMENT
        // BECAUSE I DONT HAVE RIGHT NOW SPL PLUS, TABLET DEVICES
        /*val displaySizesUi =
                activity?.contentView?.let {
                    GeneratedPinManagementDialog(AnkoContext.create(activity!!, it))
                }

        GlobalScope.launch {

            val device = MPLDeviceStore.devices[macAddress]
            if (device != null && UserUtil.userGroup != null) {

                var counter = 0

                val combinedListOfPins: MutableList<RPinManagement> = mutableListOf()

                val generatedPinFromBackend = WSUser.getGeneratedPinForSendParcel(device!!.masterUnitId)
                        ?: ""
                val generatedPin = RPinManagement()
                generatedPin.pin = generatedPinFromBackend
                generatedPin.pinGenerated = true
                generatedPin.position = counter
                generatedPin.pinId = 0
                generatedPin.isSelected = true
                generatedPin.isExtendedToDelete = false
                counter++

                combinedListOfPins.add(generatedPin)

                val pinsFromGroup = WSUser.getPinManagementForSendParcel(UserUtil.userGroup?.id!!, device!!.masterUnitId)
                if (pinsFromGroup != null) {
                    for (items in pinsFromGroup) {
                        val generatedPin = RPinManagement()
                        generatedPin.pin = items.pin
                        generatedPin.pinName = items.name
                        generatedPin.pinGenerated = false
                        generatedPin.position = counter
                        generatedPin.pinId = items.id
                        generatedPin.isSelected = false
                        generatedPin.isExtendedToDelete = false

                        combinedListOfPins.add(generatedPin)
                        counter++
                    }
                }

                withContext(Dispatchers.Main) {

                    displaySizesUi?.progressBar?.visibility = View.GONE
                    displaySizesUi?.recyclerViewPin?.visibility = View.VISIBLE

                    App.ref.pinManagementSelectedItem = combinedListOfPins.first()

                    displaySizesUi?.recyclerViewPin?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    displaySizesUi?.recyclerViewPin?.adapter = PinManagementAdapter(combinedListOfPins, device!!.masterUnitId, displaySizesUi)

                    displaySizesUi?.okButton?.setOnClickListener {

                        if (App.ref.pinManagementName != "" && App.ref.pinManagementSelectedItem.pinGenerated == true) {
                            GlobalScope.launch {
                                val savePin = RPinManagementSavePin()
                                savePin.groupId = UserUtil.userGroup?.id
                                savePin.masterId = device?.masterUnitId
                                savePin.pin = App.ref.pinManagementSelectedItem.pin
                                savePin.name = App.ref.pinManagementName

                                WSUser.savePinManagementForSendParcel(savePin)
                            }
                        }

                        if (App.ref.pinManagementSelectedItem != null) {

                            val defaultSPlParcelLockerSize = RLockerSize.S.name

                            val startIntent = Intent(requireContext(), SendParcelDeliveryActivity::class.java)
                            startIntent.putExtra("rMacAddress", macAddress)
                            startIntent.putExtra("pin", App.ref.pinManagementSelectedItem.pin.toInt())
                            startIntent.putExtra("size", defaultSPlParcelLockerSize)
                            displaySizesUi.dialog.dismiss()
                        }
                    }
                }
            }
        }*/
    }

    private fun handleUserMessages(success: Boolean) {
        if (success) {
            //App.ref.toast("Request for access send successfully")
        } else {
            //App.ref.toast(requireContext().getString(R.string.mpl_details_saving_access_settings))
        }
    }


    private fun displayNormalPinDialog() {
        val generatedPinDialog = GeneratedPinDialog(macAddress, RLockerSize.S)
        activity?.supportFragmentManager?.let { it -> generatedPinDialog.show(it, "") }
    }


    suspend private fun handleMplDetailsView(selectedMplDevice: MPLDevice?) {

        withContext(Dispatchers.Main) {
            forceOpen?.visibility = when {
                selectedMplDevice?.isInBleProximity == true && (selectedMplDevice.type == MPLDeviceType.SPL_PLUS || selectedMplDevice.type == MPLDeviceType.SPL) -> View.VISIBLE
                else -> View.GONE
            }
            headerTitle?.text = selectedMplDevice?.name
            mainLockerAddress?.text = selectedMplDevice?.address
            val rssiS = selectedMplDevice?.modemRssi ?: "-"
            val temperatureS = selectedMplDevice?.temperature?.format(1) ?: "-"
            val pressureS = selectedMplDevice?.pressure?.format(1) ?: "-"
            val humidityS = selectedMplDevice?.humidity?.format(1) ?: "-"
            rssi?.text = rssiS.toString() + " db"
            temperatre?.text = temperatureS + " C"
            preasure?.text = pressureS + " HPa"
            humidity?.text = humidityS + "%"
            if (isDeliveryAvailable && selectedMplDevice != null) {
                view?.findViewById<ConstraintLayout>(R.id.no_access_wrapper)?.visibility = View.GONE
                view?.findViewById<ConstraintLayout>(R.id.mpl_item_details_wrapper)?.visibility = View.VISIBLE
                setPickUpButtonUi(selectedMplDevice)
                setPickAtHomeButtonUi(selectedMplDevice)
                setAccessSharingButtonUi(selectedMplDevice)
            } else {
                view?.findViewById<ConstraintLayout>(R.id.no_access_wrapper)?.visibility = View.VISIBLE
                view?.findViewById<ConstraintLayout>(R.id.mpl_item_details_wrapper)?.visibility = View.GONE
            }
        }

    }

    private fun setAccessSharingButtonUi(selectedMplDevice: MPLDevice) {
        accessShareButton
        when {
            selectedMplDevice.accessTypes.any {  it == RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_USER || it == RMasterUnitAccessType.BY_ACTIVE_PAF_KEY } -> {
                accessShareButton?.isEnabled = false
                accessShareButton?.alpha = 0.4f
            }
            else -> {
                accessShareButton?.isEnabled = true
                accessShareButton?.alpha = 1.0f
            }
        }
    }

    private fun setRequestAccessButtonUI(selectedMplDevices: MPLDevice?) {
        if (activeRequests.isNotEmpty()) {
            requestAccess?.visibility = View.GONE
            forceOpen?.visibility = View.GONE
            noRightsAccessText?.visibility = View.GONE

            requestAccessText?.visibility = View.VISIBLE
        } else {
            requestAccess?.visibility = View.VISIBLE
            noRightsAccessText?.visibility = View.VISIBLE
            if( (selectedMplDevices?.masterUnitType == RMasterUnitType.SPL_PLUS || selectedMplDevices?.type == MPLDeviceType.SPL_PLUS) || selectedMplDevices?.type == MPLDeviceType.SPL )
                forceOpen?.visibility = View.VISIBLE
            else
                forceOpen?.visibility = View.GONE

            if (selectedMplDevices?.masterUnitType == RMasterUnitType.SPL
                    || selectedMplDevices?.type == MPLDeviceType.SPL || selectedMplDevices?.type == MPLDeviceType.SPL_PLUS) {
                requestAccess?.text = context?.getString(R.string.locker_details_activate_btn)
            } else {
                requestAccess?.text = context?.getString(R.string.locker_details_registration_btn)
            }
            requestAccessText?.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
        val device = MPLDeviceStore.devices[macAddress]

        GlobalScope.launch {
            val sendRequests = WSUser.getActiveRequests()?.filter { it.masterMac.macCleanToReal() == macAddress }
                    ?: listOf()
            val pahKeyy = WSUser.getActivePaHCreatedKeys()?.filter { it.lockerMasterMac.macCleanToReal() == macAddress }
                    ?: mutableListOf()

            activeKeysForLocker = WSUser.getActiveKeysForLocker(device?.macAddress?.macRealToClean()
                    ?: "")

            availableLockers = WSUser.getAvailableLockerSizes(
                    device?.masterUnitId ?: 0
            ) ?: listOf()

            pahKeys = pahKeyy
            activeRequests = sendRequests
            withContext(Dispatchers.Main) {
                handleMplDetailsView(device)
                setRequestAccessButtonUI(device)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        App.ref.eventBus.unregister(this)
        App.ref.pinManagementName = ""
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: UnauthorizedUserEvent) {
        log.info("Received unauthorized event, user will now be log outed")
        val startIntent = Intent(this@MPLItemDetailsFragment.requireContext(), LoginActivity::class.java)
        startActivity(startIntent)
        this@MPLItemDetailsFragment.activity?.finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: MPLDevicesUpdatedEvent) {
        val selectedDevice = MPLDeviceStore.devices[macAddress]
        isDeliveryAvailable = MPLDeviceStore.devices[macAddress]?.hasUserRightsOnLocker() ?: false
        selectedDevice?.let {
            GlobalScope.launch {
                handleMplDetailsView(selectedDevice)
            }
        }
    }

    private fun setPickAtHomeButtonUi(mplDevice: MPLDevice) {
        val isAccessable = getSendParcelAccessibility(mplDevice)
        val sharedKeys = hasUserShareKeys()
        log.info("isAccessable: $isAccessable $sharedKeys")

        var activeKeyFound = false
        if (activeKeysForLocker?.isNotEmpty() ?: false) {
            for (activeKeyItem in 0 until (activeKeysForLocker?.size ?: 0)) {

                for (pahItem in 0 until (pahKeys?.size ?: 0)) {

                    if (activeKeysForLocker?.get(activeKeyItem)?.createdById == pahKeys?.get(pahItem)?.createdById) {

                        activeKeyFound = true
                        break
                    } else {
                        activeKeyFound = false
                    }
                }
                if (activeKeyFound)
                    break
            }
        }

        when {
//            activeKeysForLocker?.isNotEmpty() ?: false -> {
//                //sendParcelButton?.isEnabled = isAccessable || sharedKeys || availableLockers.isNotEmpty()
//                sendParcelButton?.isEnabled = activeKeyFound
//                if (activeKeyFound)
//                    sendParcelButton?.imageResource = R.drawable.ic_send_full
//                else
//                    sendParcelButton?.imageResource = R.drawable.ic_send_parcel_disabled
//            }
//            mplDevice.accessTypes.any {  it == RMasterUnitAccessType.BY_GROUP_MEMBERSHIP_AS_USER || it == RMasterUnitAccessType.BY_ACTIVE_PAF_KEY } -> {
//                sendParcelButton?.isEnabled = false
//                sendParcelButton?.imageResource = R.drawable.ic_send_parcel_disabled
//            }
//            else -> {
//                sendParcelButton?.isEnabled = isAccessable || sharedKeys || availableLockers.isNotEmpty()
//
//                if (isAccessable || sharedKeys || availableLockers.isNotEmpty()) {
//                    sendParcelButton?.imageResource = R.drawable.ic_send_full
//                } else {
//                    sendParcelButton?.imageResource = R.drawable.ic_send_parcel_disabled
//                }
//            }
        }
    }

    private fun hasUserShareKeys(): Boolean {
        return pahKeys?.isNotEmpty() ?: false
    }

    private fun getSendParcelAccessibility(device: MPLDevice): Boolean {
        log.info("Installation type: ${device.installationType}, type: ${device.type}, masterunitype: ${device.masterUnitType}")
        if ( ( device.installationType == InstalationType.TABLET || device.type == MPLDeviceType.TABLET )
                || (device.masterUnitType == RMasterUnitType.MPL || device.type == MPLDeviceType.MASTER )
                || (device.masterUnitType == RMasterUnitType.SPL_PLUS || device.type == MPLDeviceType.SPL_PLUS)) {
            if (device.hasUserRightsOnMplSend() && device.isInBleProximity) {
                return if( availableLockers.size > 0 ) true else false
                //return device.availableLockers.any { it.count > 0 }
            }
            return false
        } else if (device.masterUnitType == RMasterUnitType.SPL) {
            val key = device.macAddress + ActionStatusType.SPL_OCCUPATION
            if (device.hasUserRightsOnMplSend() && device.isInBleProximity) {
                return if( availableLockers.size > 0 ) true else false
                //return getKeysForDelivery(device).isEmpty()
            }
        }
        return false

    }


    private fun setPickUpButtonUi(mplDevice: MPLDevice) {
        val ctx = context ?: return
        val hasKeysForDelivery = getKeysForDelivery(mplDevice)
        if (hasKeysForDelivery.isNotEmpty()) {
            circleValue?.visibility = View.VISIBLE
            circleValue?.text = hasKeysForDelivery.size.toString()
            parcelOpenButton?.isEnabled = true
            parcelOpenButton?.background = getRipple(-0x1000000)
            //parcelOpenButton?.image = ContextCompat.getDrawable(ctx, R.drawable.ic_pickup_parcel)

        } else {
            circleValue?.visibility = View.GONE
            parcelOpenButton?.isEnabled = false
            //parcelOpenButton?.image = ContextCompat.getDrawable(ctx, R.drawable.ic_pickup_parcel_disabled)
        }
    }

    private fun getKeysForDelivery(mplDevice: MPLDevice): Set<Int> {
        val lockerKeys = getLockerKeys(mplDevice)
        val usedKeys = DatabaseHandler.deliveryKeyDb.get(macAddress)?.keyIds ?: listOf()
        return lockerKeys.map { it.id }.subtract(usedKeys.asIterable())
    }

    private fun getLockerKeys(mplDevice: MPLDevice): List<RLockerKey> {
        return mplDevice.activeKeys.filter { it.purpose == RLockerKeyPurpose.DELIVERY || it.purpose == RLockerKeyPurpose.PAF }
    }


    private fun getRipple(color: Int): RippleDrawable {
        val mask = android.graphics.drawable.GradientDrawable()
        mask.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        mask.cornerRadius = 90f
        mask.setColor(color) // the color is irrelevant here, only the alpha
        val rippleColorLst = android.content.res.ColorStateList.valueOf(Color.GRAY)
        return android.graphics.drawable.RippleDrawable(rippleColorLst, null, mask)
    }


}