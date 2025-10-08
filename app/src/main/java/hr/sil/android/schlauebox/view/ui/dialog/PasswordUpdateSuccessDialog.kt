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
import hr.sil.android.schlauebox.databinding.DialogPasswordUpdateSuccessBinding
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.intro.PasswordUpdateActivity

class PasswordUpdateSuccessDialog constructor(val passwordUpdateActivity: PasswordUpdateActivity) : DialogFragment() {

    private lateinit var binding: DialogPasswordUpdateSuccessBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPasswordUpdateSuccessBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.btnUserSettings.setOnClickListener {
                val startIntent = Intent(this@PasswordUpdateSuccessDialog.requireContext(), LoginActivity::class.java)
                startActivity(startIntent)
                dismiss()
                passwordUpdateActivity.finish()
            }
        }

        return dialog!!
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.dialog_password_update_success, container, false)
        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog?.setCanceledOnTouchOutside(false)
        }
        return view
    }

}