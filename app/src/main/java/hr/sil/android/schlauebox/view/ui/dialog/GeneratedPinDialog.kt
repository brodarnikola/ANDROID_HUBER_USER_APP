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
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RLockerSize
import hr.sil.android.schlauebox.databinding.DialogGeneratedPinBinding
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.view.ui.home.activities.SendParcelDeliveryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GeneratedPinDialog constructor(val masterMacAddress: String, val lockerSize: RLockerSize) : DialogFragment() {

    private lateinit var binding: DialogGeneratedPinBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogGeneratedPinBinding.inflate(LayoutInflater.from(context))

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

                if (MPLDeviceStore.devices[masterMacAddress] != null) {

                    val generatedPin = MPLDeviceStore.devices[masterMacAddress]?.masterUnitId?.let { WSUser.getGeneratedPinForSendParcel(it) }
                        ?: ""

                    withContext(Dispatchers.Main) {

                        binding.progressCircular?.visibility = View.GONE
                        binding.btnConfirm?.visibility = View.VISIBLE

                        setupButtonProperties(true, 1.0f)

                        binding.tvGeneratedPinFromBackend.visibility = View.VISIBLE
                        binding.tvGeneratedPinFromBackend.text = "" + generatedPin

                        binding.btnConfirm.setOnClickListener {

                            val startIntent = Intent(requireContext(), SendParcelDeliveryActivity::class.java)
                            startIntent.putExtra("rMacAddress", masterMacAddress)
                            startIntent.putExtra("pin", generatedPin.toInt())
                            startIntent.putExtra("size", lockerSize.name)
                            startActivity(startIntent)
                            dismiss()
                        }

                        binding.btnCancel.setOnClickListener {
                            dismiss()
                        }
                    }
                }
            }
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