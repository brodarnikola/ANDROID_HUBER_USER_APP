package hr.sil.android.schlauebox.view.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.DialogDeleteSharedPickupKeyBinding
import hr.sil.android.schlauebox.view.ui.home.activities.PickupParcelActivity
import hr.sil.android.schlauebox.view.ui.home.adapters.ParcelPickupKeysAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeleteSharedPickupKeyDialog constructor(val createdForEndUserName: String, val idOfKey: Int, val pickupParcelActivity: PickupParcelActivity, val pickupParcel: ParcelPickupKeysAdapter) : DialogFragment() {

    val log = logger()
    private lateinit var binding: DialogDeleteSharedPickupKeyBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeleteSharedPickupKeyBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)


            binding.tvDialogTitle.setText(pickupParcelActivity.resources?.getString(R.string.parcel_pickup_delete_key_title, createdForEndUserName))

            binding.btnCancel.setOnClickListener {
                dismiss()
            }

            binding.btnConfirm.setOnClickListener {

                GlobalScope.launch {
                    if (WSUser.deletePaF( idOfKey)) {
                        withContext(Dispatchers.Main) {
                            //App.ref.toast(pickupParcelActivity.resources?.getString(R.string.peripheral_settings_remove_access_success, idOfKey.toString()).toString())
                            dismiss()
                            pickupParcel.deleteSharedKey(idOfKey)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            //App.ref.toast(pickupParcelActivity.resources?.getString(R.string.peripheral_settings_remove_access_error, idOfKey.toString()).toString())
                            dismiss()
                        }
                    }
                }
            }
        }

        return dialog!!
    }


}


