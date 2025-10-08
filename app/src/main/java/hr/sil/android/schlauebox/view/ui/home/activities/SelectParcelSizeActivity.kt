package hr.sil.android.schlauebox.view.ui.home.activities


import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RAvailableLockerSize
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.events.MPLDevicesUpdatedEvent
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.dialog.GeneratedPinDialog
import hr.sil.android.schlauebox.view.ui.dialog.PinManagmentDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class SelectParcelSizeActivity : BaseActivity(R.id.no_ble_layout, R.id.no_internet_layout, R.id.no_location_gps_layout) {

    var availableLockers: List<RAvailableLockerSize> = listOf()
    var selectedLockerSize = RLockerSize.UNKNOWN
    var currentSelected: ImageButton? = null
    var currentUnselectedDrawable: Int = 0
    val send_parcel_next by lazy { findViewById<Button>(R.id.send_parcel_next) }
    val xsButton by lazy { findViewById<ImageButton>(R.id.send_parcel_item_xs) }
    val sButton by lazy { findViewById<ImageButton>(R.id.send_parcel_item_s) }
    val lButton by lazy { findViewById<ImageButton>(R.id.send_parcel_item_l) }
    val mButton by lazy { findViewById<ImageButton>(R.id.send_parcel_item_m) }
    val xlButton by lazy { findViewById<ImageButton>(R.id.send_parcel_item_xl) }

    val xsButtonText by lazy { findViewById<TextView>(R.id.send_parcel_item_xs_text) }
    val sButtonText by lazy { findViewById<TextView>(R.id.send_parcel_item_s_text) }
    val lButtonText by lazy { findViewById<TextView>(R.id.send_parcel_item_l_text) }
    val mButtonText by lazy { findViewById<TextView>(R.id.send_parcel_item_m_text) }
    val xlButtonText by lazy { findViewById<TextView>(R.id.send_parcel_item_xl_text) }

    val xsLayout by lazy { findViewById<LinearLayout>(R.id.llXs) }
    val sLayout by lazy { findViewById<LinearLayout>(R.id.llS) }
    val mLayout by lazy { findViewById<LinearLayout>(R.id.llM) }
    val lLayout by lazy { findViewById<LinearLayout>(R.id.llL) }
    val xlLayout by lazy { findViewById<LinearLayout>(R.id.llXL) }

    val log = logger()

    val macAddress: String by lazy { intent.getStringExtra("rMacAddress") ?: "" }
    private val device by lazy { MPLDeviceStore.devices[macAddress] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_parcel_size)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        this.setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false);

        App.ref.pinManagementName = ""

        initButtons()
    }

    private fun displayPinManagementDialog() {
        val pinManagementDialog = PinManagmentDialog(macAddress, selectedLockerSize)
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

    private fun initButtons() {
        log.info("Initialisation of the buttons")
        lifecycleScope.launch {

            availableLockers = WSUser.getAvailableLockerSizes(
                    MPLDeviceStore.devices[macAddress]?.masterUnitId ?: 0
            ) ?: listOf()
            withContext(Dispatchers.Main) {

                if (device?.type == MPLDeviceType.SPL_PLUS || device?.masterUnitType == RMasterUnitType.SPL) {
                    xsLayout.visibility = View.GONE
                    mLayout.visibility = View.GONE
                    xlLayout.visibility = View.GONE
                }

                lockerSizeSetOnClickListeners()
                //val availableLockers = MPLDeviceStore.devices[macAddress]?.availableLockers ?: listOf()
                var counter = 0
                counter += handleAccessability(xsButton, xsButtonText, availableLockers.filter { it.size == RLockerSize.XS && it.count > 0 }, R.drawable.btn_size_xs_unavailable, R.drawable.btn_size_xs_unselected, RLockerSize.XS)
                counter += handleAccessability(sButton, sButtonText, availableLockers.filter { it.size == RLockerSize.S && it.count > 0 }, R.drawable.btn_size_s_unavailable, R.drawable.btn_size_s_unselected, RLockerSize.S)
                counter += handleAccessability(mButton, mButtonText, availableLockers.filter { it.size == RLockerSize.M && it.count > 0 }, R.drawable.btn_size_m_unavailable, R.drawable.btn_size_m_unselected, RLockerSize.M)
                counter += handleAccessability(lButton, lButtonText, availableLockers.filter { it.size == RLockerSize.L && it.count > 0 }, R.drawable.btn_size_l_unavailable, R.drawable.btn_size_l_unselected, RLockerSize.L)
                counter += handleAccessability(xlButton, xlButtonText, availableLockers.filter { it.size == RLockerSize.XL && it.count > 0 }, R.drawable.btn_size_xl_unavailable, R.drawable.btn_size_xl_unselected, RLockerSize.XL)
                if (counter == 0 || MPLDeviceStore.devices[macAddress]?.isInBleProximity == false) {
                    send_parcel_next.visibility = View.GONE
                    send_parcel_next.isEnabled = false
                } else {
                    send_parcel_next.visibility = View.VISIBLE
                    send_parcel_next.isEnabled = true
                }
            }
        }
    }

    private fun lockerSizeSetOnClickListeners() {

        xsButton.setOnClickListener { v ->
            handleSizeSelectionButton(v as ImageButton, R.drawable.btn_size_xs_selected, R.drawable.btn_size_xs_unselected)
            selectedLockerSize = RLockerSize.XS
        }

        sButton.setOnClickListener { v ->
            handleSizeSelectionButton(v as ImageButton, R.drawable.btn_size_s_selected, R.drawable.btn_size_s_unselected)
            selectedLockerSize = RLockerSize.S
        }

        lButton.setOnClickListener { v ->
            handleSizeSelectionButton(v as ImageButton, R.drawable.btn_size_l_selected, R.drawable.btn_size_l_unselected)
            selectedLockerSize = RLockerSize.L
        }

        mButton.setOnClickListener { v ->
            handleSizeSelectionButton(v as ImageButton, R.drawable.btn_size_m_selected, R.drawable.btn_size_m_unselected)
            selectedLockerSize = RLockerSize.M
        }

        xlButton.setOnClickListener { v ->
            handleSizeSelectionButton(v as ImageButton, R.drawable.btn_size_xl_selected, R.drawable.btn_size_xl_unselected)
            selectedLockerSize = RLockerSize.XL
        }

        send_parcel_next.setOnClickListener {
            if (selectedLockerSize != RLockerSize.UNKNOWN) {
                if (device?.pinManagementAllowed == false)
                    displayNormalParcelSizeDialog()
                else
                    displayPinManagementDialog()
            } else {
                //App.ref.toast(R.string.nav_send_parcel_size_warning)
            }
        }

    }

    private fun handleAccessability(pickedButton: ImageButton, pickedButtonText: TextView, availableLockers: List<RAvailableLockerSize>,
                                    unselectedDrawable: Int, selectedDrawable: Int, lockerSize: RLockerSize): Int {

        val count = availableLockers.firstOrNull()?.count ?: 0
//        if (selectedLockerSize == lockerSize) {
//            handleSelectedButton(lockerSize, pickedButton, count)
//        } else if (availableLockers.isEmpty()) {
//            pickedButton.backgroundDrawable = ContextCompat.getDrawable(this, unselectedDrawable)
//            pickedButton.isEnabled = false
//        } else {
//            pickedButton.backgroundDrawable = ContextCompat.getDrawable(this, selectedDrawable)
//            pickedButton.isEnabled = true
//        }
        pickedButtonText.text = this.getString(R.string.send_parcel_available, count.toString())
        return count
    }

    private fun handleSelectedButton(lockerSize: RLockerSize, pickedButton: ImageButton, count: Int) {
//        when (lockerSize) {
//            RLockerSize.XS -> pickedButton.backgroundDrawable =
//                    if (count != 0) ContextCompat.getDrawable(this, R.drawable.btn_size_xs_selected)
//                    else ContextCompat.getDrawable(this, R.drawable.btn_size_xs_unavailable)
//            RLockerSize.S -> pickedButton.backgroundDrawable =
//                    if( count != 0 ) ContextCompat.getDrawable(this, R.drawable.btn_size_s_selected)
//                    else  ContextCompat.getDrawable(this, R.drawable.btn_size_s_unavailable)
//            RLockerSize.M -> pickedButton.backgroundDrawable =
//                    if( count != 0 ) ContextCompat.getDrawable(this, R.drawable.btn_size_m_selected)
//                    else  ContextCompat.getDrawable(this, R.drawable.btn_size_m_unavailable)
//            RLockerSize.L -> pickedButton.backgroundDrawable =
//                    if( count != 0 ) ContextCompat.getDrawable(this, R.drawable.btn_size_l_selected)
//                    else  ContextCompat.getDrawable(this, R.drawable.btn_size_l_unavailable)
//            else -> pickedButton.backgroundDrawable =
//                    if( count != 0 ) ContextCompat.getDrawable(this, R.drawable.btn_size_xl_selected)
//                    else  ContextCompat.getDrawable(this, R.drawable.btn_size_xl_unavailable)
//        }
    }

    private fun handleSizeSelectionButton(pickedButton: ImageButton, selectedDrawable: Int, unselectedDrawable: Int) {
        if (currentSelected != pickedButton) {
            //pickedButton.backgroundDrawable = ContextCompat.getDrawable(this, selectedDrawable)
            val drawable = if (currentUnselectedDrawable == 0) unselectedDrawable else currentUnselectedDrawable
            //currentSelected?.backgroundDrawable = ContextCompat.getDrawable(this, drawable)
            currentSelected = pickedButton
            currentUnselectedDrawable = unselectedDrawable
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: MPLDevicesUpdatedEvent) {
        log.info("Received MPL event $macAddress")
        initButtons()
    }


    private fun displayNormalParcelSizeDialog() {
        val generatedPinDialog = GeneratedPinDialog(macAddress, selectedLockerSize)
        generatedPinDialog.show(supportFragmentManager, "")

        // TODO: ONCE DUNJA, QA TESTER THAT SENDING PARCEL IS GOOD, THEN WE CAN DELETE THIS COMMENT
        // BECAUSE I DONT HAVE RIGHT NOW SPL PLUS, TABLET DEVICES
        //this@SelectParcelSizeActivity.supportFragmentManager.let { generatedPinDialog.show(it, "") }
        /*val displaySizesUi =
                contentView?.let {
                    GeneratedPinDialog(AnkoContext.create(this, it))
                }

        GlobalScope.launch {

            if (MPLDeviceStore.devices[macAddress] != null) {

                val generatedPin = MPLDeviceStore.devices[macAddress]?.masterUnitId?.let { WSUser.getGeneratedPinForSendParcel(it) }
                        ?: ""

                withContext(Dispatchers.Main) {

                    displaySizesUi?.progressBar?.visibility = View.GONE
                    displaySizesUi?.okButton?.visibility = View.VISIBLE

                    displaySizesUi?.generatedPinFromBackend?.visibility = View.VISIBLE
                    displaySizesUi?.generatedPinFromBackend?.text = "" + generatedPin


                    displaySizesUi?.okButton?.setOnClickListener {

                        startActivity(intentFor<SendParcelDeliveryActivity>("rMacAddress" to macAddress, "pin" to generatedPin.toInt(), "size" to selectedLockerSize.name))
                        displaySizesUi.dialog.dismiss()
                    }
                }
            }
        }*/
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