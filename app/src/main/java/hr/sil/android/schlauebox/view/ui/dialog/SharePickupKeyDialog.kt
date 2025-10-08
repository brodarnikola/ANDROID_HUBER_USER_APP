package hr.sil.android.schlauebox.view.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.DialogSharePickupKeyBinding
import hr.sil.android.schlauebox.view.ui.home.activities.PickupParcelActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharePickupKeyDialog constructor(val keyId: Int, val masterId: Int, val pickupParcelActivity: PickupParcelActivity) : DialogFragment() {

    val log = logger()

    private lateinit var binding: DialogSharePickupKeyBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSharePickupKeyBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.btnConfirm.setOnClickListener {
                val mailAddress = binding.pickupPasswordInput.text.toString()
                if( validateEmail(mailAddress) ) {
                    binding.tvErrorMessage.visibility = View.GONE
                    GlobalScope.launch(Dispatchers.Default) {
                        if (!isUserMemberOfGroup(mailAddress, masterId)) {
                            log.info("PaF share $keyId - $mailAddress")
                            withContext(Dispatchers.Main) {
                                dismiss()
                            }
                            log.info("P@h created $keyId mail: $mailAddress")
                            val returnedData = WSUser.createPaF(keyId, mailAddress)
                            log.info("Invitation key = ${returnedData?.invitationCode}")
                            if (returnedData?.invitationCode.isNullOrEmpty()) {
                                withContext(Dispatchers.Main) {
                                    val message = pickupParcelActivity.resources?.getString(R.string.app_generic_success).toString()
                                    //App.ref.toast(message)
                                    pickupParcelActivity.setupAdapterForKeys()
                                    //updateKeys()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    val shareAppDialog = ShareAppDialog(mailAddress)
                                    shareAppDialog.show( pickupParcelActivity.supportFragmentManager, "" )
                                }
                            }

                        } else {
                            withContext(Dispatchers.Main) {
                                //App.ref.toast(R.string.grant_access_error_exists)
                            }
                        }
                    }
                }
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

    private fun validateEmail(mailAddress: String): Boolean {
        if( mailAddress.isEmpty() ) {
            binding.tvErrorMessage.visibility = View.VISIBLE
            binding.tvErrorMessage.setText(pickupParcelActivity.resources?.getString(R.string.edit_user_validation_blank_fields_exist).toString())
            return false
        }
        else if( !android.util.Patterns.EMAIL_ADDRESS.matcher(mailAddress).matches() ) {
            binding.tvErrorMessage.visibility = View.VISIBLE
            binding.tvErrorMessage.setText(pickupParcelActivity.resources?.getString(R.string.message_email_invalid).toString())
            return false
        }
        else {
            binding.tvErrorMessage.visibility = View.GONE
            return true
        }
    }

    suspend private fun isUserMemberOfGroup(email: String, masterId: Int): Boolean {
        val groups = DataCache.getGroupMembers()
        val groupMemberships = groups.filter { it.email == email }
        return groupMemberships.isNotEmpty()
    }
}