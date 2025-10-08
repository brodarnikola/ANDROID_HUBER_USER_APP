package hr.sil.android.schlauebox.view.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.core.remote.model.RPinManagement
import hr.sil.android.schlauebox.core.remote.model.RPinManagementSavePin
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.DialogPinManagmentBinding
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.view.ui.home.activities.SendParcelDeliveryActivity
import hr.sil.android.schlauebox.view.ui.home.adapters.PinManagementAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PinManagmentDialog constructor(val masterMacAddress: String, val lockerSize: RLockerSize) : DialogFragment() {

    val log = logger()
    private lateinit var binding: DialogPinManagmentBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPinManagmentBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)


            setupButtonProperties(false, 0.8f)

            GlobalScope.launch {

                val device = MPLDeviceStore.devices[masterMacAddress]
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

                        binding.progressCircular.visibility = View.GONE
                        binding.recyclerViewPin.visibility = View.VISIBLE

                        setupButtonProperties(true, 1.0f)

                        App.ref.pinManagementSelectedItem = combinedListOfPins.first()

                        binding.recyclerViewPin.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                        binding.recyclerViewPin.adapter = PinManagementAdapter(combinedListOfPins, device.masterUnitId)

                        binding.btnConfirm.setOnClickListener {

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

                                //val defaultSPlParcelLockerSize = RLockerSize.S.name

                                val startIntent = Intent(requireContext(), SendParcelDeliveryActivity::class.java)
                                startIntent.putExtra("rMacAddress", masterMacAddress)
                                startIntent.putExtra("pin", App.ref.pinManagementSelectedItem.pin.toInt())
                                startIntent.putExtra("size", lockerSize.name)
                                startActivity(startIntent)
                                dismiss()
                            }
                        }

                        binding.btnCancel.setOnClickListener {
                            dismiss()
                        }
                    }
                }
            }

            /*GlobalScope.launch {

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
            }
    */
        }

        return dialog!!
    }

    private fun setupButtonProperties(isEnabled: Boolean, alphaValue: Float) {
        binding.btnConfirm.isEnabled = isEnabled
        binding.btnConfirm.alpha = alphaValue
        binding.btnCancel.isEnabled = isEnabled
        binding.btnCancel.alpha = alphaValue
    }
}