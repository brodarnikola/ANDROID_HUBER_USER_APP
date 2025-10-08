package hr.sil.android.schlauebox.view.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.DialogCancelDeliveryBinding
import hr.sil.android.schlauebox.databinding.DialogLogoutBinding
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.home.adapters.SendParcelsSharingAdapter

class CancelDeliveryDialog constructor(val lockerMac: String, val lockerMasterMac: String, val lockerId: Int, val lockerKeyId: Int, val userId: Int, val notesViewHolder: SendParcelsSharingAdapter.NotesViewHolder) : DialogFragment() {

    val log = logger()
    private lateinit var binding: DialogCancelDeliveryBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCancelDeliveryBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.btnCancel.setOnClickListener {
                dismiss()
            }

            binding.btnConfirm.setOnClickListener {
                notesViewHolder.cancelPickAtHomeInterface(lockerMac, lockerMasterMac, lockerId, lockerKeyId, userId)
                dismiss()
            }
        }

        return dialog!!
    }

}


