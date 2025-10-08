package hr.sil.android.schlauebox.view.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.schlauebox.BuildConfig
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.databinding.DialogShareAppBinding

class ShareAppDialog constructor(val groupUserEmail: String) : DialogFragment() {

    private lateinit var binding: DialogShareAppBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogShareAppBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)


            binding.btnConfirm.setOnClickListener {
                val appLink = BuildConfig.APP_ANDR_DOWNLOAD_URL
                val iOSLink = BuildConfig.APP_IOS_DOWNLOAD_URL

                val shareBodyText = this@ShareAppDialog.getString(R.string.access_sharing_share_app_text, appLink, iOSLink)
                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.setType("message/rfc822")
                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(groupUserEmail))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject/Title")
                emailIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText)
                startActivity(Intent.createChooser(emailIntent, this@ShareAppDialog.getString(R.string.access_sharing_share_choose_sharing)))
                dismiss()
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

}