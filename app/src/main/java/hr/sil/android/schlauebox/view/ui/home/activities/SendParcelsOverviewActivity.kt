package hr.sil.android.schlauebox.view.ui.home.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.status.ActionStatusHandler
import hr.sil.android.schlauebox.cache.status.ActionStatusType
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.model.MPLDevice
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.home.adapters.SendParcelsSharingAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.dialog.GeneratedPinDialog
import hr.sil.android.schlauebox.view.ui.dialog.PinManagmentDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SendParcelsOverviewActivity : BaseActivity(R.id.no_ble_layout, R.id.no_internet_layout, R.id.no_location_gps_layout) {
    val macAddress: String by lazy { intent.getStringExtra("rMacAddress") ?: "" }
    val log = logger()
    private var device: MPLDevice? = null
    private lateinit var mplRecycleView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_send_parcels_overview)

        val nextButton = findViewById<Button>(R.id.send_parcel_next)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        this.setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false);
        device = MPLDeviceStore.devices[macAddress]
        mplRecycleView = findViewById<RecyclerView>(R.id.rlRecylcerView)

        if (device?.type == MPLDeviceType.SPL || (device?.type == MPLDeviceType.SPL_PLUS && device?.keypadType == ParcelLockerKeyboardType.SPL) ) {
            nextButton.isEnabled = false
            nextButton.visibility = View.GONE
        } else {
            nextButton.isEnabled = true
            nextButton.visibility = View.VISIBLE
            nextButton.setOnClickListener {
                if (device?.masterUnitType == RMasterUnitType.MPL || device?.type == MPLDeviceType.SPL_PLUS) {
                    //startActivity(intentFor<SelectParcelSizeActivity>("rMacAddress" to macAddress))
                } else {
                    if (device?.pinManagementAllowed == false)
                        displayPinDialog()
                    else
                        displayPinManagementDialog()
                }
            }
        }

    }

    private fun displayPinDialog() {

        val generatedPinDialog = GeneratedPinDialog(macAddress, RLockerSize.S)
        generatedPinDialog.show( supportFragmentManager, "" )
        // TODO: ONCE DUNJA, QA TESTER THAT SENDING PARCEL IS GOOD, THEN WE CAN DELETE THIS COMMENT
        // BECAUSE I DONT HAVE RIGHT NOW SPL PLUS, TABLET DEVICES
        /*val displayPinUi =
                contentView?.let {
                    InputPinDialog(AnkoContext.create(this, it))
                }


        displayPinUi?.okButton?.setOnClickListener {
            val pinText = displayPinUi.feedbackText.text.toString()
            val repeatPin = displayPinUi.repeatPin.text.toString()
            if (pinText == repeatPin && repeatPin.isNotBlank()) {
                startActivity(intentFor<SendParcelDeliveryActivity>("rMacAddress" to macAddress, "pin" to pinText.toInt()))
                displayPinUi.dialog.dismiss()
                finish()
            } else {
                displayPinUi.feedbackText.setHintTextColor(ContextCompat.getColor( baseContext, R.color.colorError))
                displayPinUi.repeatPin.setHintTextColor(ContextCompat.getColor(baseContext, R.color.colorError))
                App.ref.toast("  Please enter the pin in both input field!  ")
            }

        }

        displayPinUi?.cancelButton?.setOnClickListener {
            displayPinUi.dialog.dismiss()
        }*/
    }

    private fun displayPinManagementDialog() {

        val pinManagementDialog = PinManagmentDialog(macAddress, RLockerSize.S)
        pinManagementDialog.show(supportFragmentManager, "")
        // TODO: ONCE DUNJA, QA TESTER THAT SENDING PARCEL IS GOOD, THEN WE CAN DELETE THIS COMMENT
        // BECAUSE I DONT HAVE RIGHT NOW SPL PLUS, TABLET DEVICES
        /*val displaySizesUi =
                contentView?.let {
                    GeneratedPinManagementDialog(AnkoContext.create(this, it))
                }

        GlobalScope.launch {

            if (device != null && UserUtil.userGroup != null) {

                var counter = 0

                val combinedListOfPins: MutableList<RPinManagement> = mutableListOf()

                val generatedPinFromBackend = WSUser.getGeneratedPinForSendParcel(device?.masterUnitId)
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

                val pinsFromGroup = WSUser.getPinManagementForSendParcel(UserUtil.userGroup?.id!!, device?.masterUnitId)
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

                    displaySizesUi?.recyclerViewPin?.layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)
                    displaySizesUi?.recyclerViewPin?.adapter = PinManagementAdapter(combinedListOfPins, device?.masterUnitId)

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

                            val imm = this@SelectParcelSizeActivity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                            var view = this@SelectParcelSizeActivity.getCurrentFocus()
                            if (view == null) {
                                view = View(this@SelectParcelSizeActivity)
                            }
                            imm.hideSoftInputFromWindow(view!!.getWindowToken(), 0)

                            startActivity(intentFor<SendParcelDeliveryActivity>("rMacAddress" to macAddress, "pin" to App.ref.pinManagementSelectedItem.pin.toInt(), "size" to selectedLockerSize.name))
                            displaySizesUi.dialog.dismiss()
                        }
                    }
                }
            }
        }*/
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

    override fun onStart() {
        super.onStart()
        GlobalScope.launch {
            val listOfKeys = WSUser.getActivePaHCreatedKeys()?.filter {
                it.getMasterBLEMacAddress() == macAddress
            }?.toMutableList() ?: mutableListOf()
            listOfKeys.filter { ActionStatusHandler.actionStatusDb.get(it.lockerId.toString() + ActionStatusType.PAH_ACCESS_CANCEL) == null }.toMutableList()
            withContext(Dispatchers.Main) {
 
                mplRecycleView.layoutManager = LinearLayoutManager(this@SendParcelsOverviewActivity, LinearLayoutManager.VERTICAL, false)
                mplRecycleView.adapter = SendParcelsSharingAdapter(listOfKeys,device?.type ?: MPLDeviceType.UNKNOWN, device?.isInBleProximity, this@SendParcelsOverviewActivity )
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


}