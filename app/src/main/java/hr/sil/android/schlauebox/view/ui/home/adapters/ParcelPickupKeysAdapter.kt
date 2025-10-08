package hr.sil.android.schlauebox.view.ui.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.remote.model.RCreatedLockerKey
import hr.sil.android.schlauebox.core.remote.model.RLockerKeyPurpose
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.core.util.formatFromStringToDate
import hr.sil.android.schlauebox.core.util.formatToViewDateTimeDefaults
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.data.DeleteSharedKeyInterface
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.view.ui.dialog.DeleteSharedPickupKeyDialog
import hr.sil.android.schlauebox.view.ui.dialog.SharePickupKeyDialog
import hr.sil.android.schlauebox.view.ui.home.activities.PickupParcelActivity
import java.text.ParseException
import java.util.*

class ParcelPickupKeysAdapter(val updateKeys: () -> Unit, val type: RMasterUnitType, val pickupParcelActivity: PickupParcelActivity) : RecyclerView.Adapter<ParcelPickupKeysAdapter.NotesViewHolder>(), DeleteSharedKeyInterface {

    override fun deleteSharedKey(id: Int) {

        val removeKey = keys.filter { it.id == id }.firstOrNull()
        if( removeKey != null ) {
            keys.remove(removeKey)
            notifyDataSetChanged()
        }
    }

    val keys = mutableListOf<RCreatedLockerKey>()

    fun update(newKeys: List<RCreatedLockerKey>) {
        keys.clear()
        keys.addAll(newKeys)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): NotesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_pickup_key, parent, false)
        return NotesViewHolder(view)
    }


    override fun getItemCount(): Int {
        return keys.size
    }

    fun removeItem(index: Int) {
        keys.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, keys.size)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bindItem(keys[position])
    }


    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val log = logger()
        val name: TextView = itemView.findViewById(R.id.key_sharing_item_key_header)
        val shareButton: ImageButton = itemView.findViewById(R.id.peripheral_settings_share)
        var deleteButton: ImageButton = itemView.findViewById(R.id.peripheral_settings_delete)

        fun bindItem(parcelLockerKey: RCreatedLockerKey) {
            name.text = ""
            when (parcelLockerKey.purpose) {
                RLockerKeyPurpose.DELIVERY -> {
                    if (type == RMasterUnitType.MPL) {
                        parcelLockerKey.timeCreated = formatCorrectDate(parcelLockerKey.timeCreated)
                        name.text = itemView.context.getString(R.string.peripheral_settings_share_access, parcelLockerKey.lockerSize, parcelLockerKey.timeCreated)
                    } else {

                        parcelLockerKey.timeCreated = formatCorrectDate(parcelLockerKey.timeCreated)
                        name.text = itemView.context.getString(R.string.peripheral_settings_share_access_spl, parcelLockerKey.timeCreated)
                    }
                    shareButton.visibility = View.VISIBLE
//                    shareButton.onClick {
//                        displayFeedbackDialog(parcelLockerKey.id, parcelLockerKey.lockerMasterId)
//                    }
                    deleteButton.visibility = View.GONE
                }
                RLockerKeyPurpose.PAF -> {
                    log.info("Created P@F for ${parcelLockerKey.createdForId}")
                    val mplName = MPLDeviceStore.devices[parcelLockerKey.getMasterBLEMacAddress()]?.name

                    if (parcelLockerKey.createdForId != null) {
                        if (type == RMasterUnitType.MPL) {
                            log.info("Base time created is: ${parcelLockerKey.baseTimeCreated}")
                            if( parcelLockerKey.baseTimeCreated != null )
                                parcelLockerKey.baseTimeCreated = formatCorrectDate(parcelLockerKey.baseTimeCreated)
                            name.text = itemView.context.getString(R.string.peripheral_settings_remove_access, parcelLockerKey.createdForEndUserName, parcelLockerKey.lockerSize, parcelLockerKey.baseTimeCreated)
                        } else {
                            if( parcelLockerKey.baseTimeCreated != null )
                                parcelLockerKey.baseTimeCreated = formatCorrectDate(parcelLockerKey.baseTimeCreated)
                            name.text = itemView.context.getString(R.string.peripheral_settings_remove_access_spl, parcelLockerKey.createdForEndUserName, parcelLockerKey.baseTimeCreated)
                        }
                        shareButton.visibility = View.GONE
                        deleteButton.visibility = View.VISIBLE
//                        deleteButton.onClick {
//                            val deleteSharedPickupKeyDialog = DeleteSharedPickupKeyDialog( parcelLockerKey.createdForEndUserName ?: "", parcelLockerKey.id, pickupParcelActivity, this@ParcelPickupKeysAdapter )
//                            deleteSharedPickupKeyDialog.show( pickupParcelActivity.supportFragmentManager, "" )
//                        }
                    } else {
                        if (type == RMasterUnitType.MPL) {
                            if( parcelLockerKey.baseTimeCreated != null )
                                parcelLockerKey.baseTimeCreated = formatCorrectDate(parcelLockerKey.baseTimeCreated)
                            name.text = itemView.context.getString(R.string.peripheral_settings_grant_access, parcelLockerKey.createdByName, parcelLockerKey.lockerSize, parcelLockerKey.baseTimeCreated)
                        } else {
                            if( parcelLockerKey.baseTimeCreated != null )
                                parcelLockerKey.baseTimeCreated = formatCorrectDate(parcelLockerKey.baseTimeCreated)
                            name.text = itemView.context.getString(R.string.peripheral_settings_grant_access_spl, parcelLockerKey.createdByName, parcelLockerKey.baseTimeCreated)
                        }
                        shareButton.visibility = View.GONE
                        deleteButton.visibility = View.GONE
                    }
                }
                RLockerKeyPurpose.UNKNOWN -> {}
                RLockerKeyPurpose.PAH -> {}
            }
        }

        private fun formatCorrectDate(timeCreated: String) : String {
            val fromStringToDate: Date
            var fromDateToString = ""
            try {
                fromStringToDate = timeCreated.formatFromStringToDate()
                fromDateToString = fromStringToDate.formatToViewDateTimeDefaults()
            }
            catch (e: ParseException) {
                e.printStackTrace()
            }
            log.info("Correct date is: ${fromDateToString}")
            return fromDateToString
        }

        suspend private fun isUserMemberOfGroup(email: String, masterId: Int): Boolean {
            val groups = DataCache.getGroupMembers()
            val groupMemberships = groups.filter { it.email == email }
            return groupMemberships.isNotEmpty()
        }

        private fun displayFeedbackDialog(keyId: Int, masterId: Int) {

            val sharePickupKeyDialog = SharePickupKeyDialog(keyId, masterId, pickupParcelActivity)
            sharePickupKeyDialog.show( pickupParcelActivity.supportFragmentManager, "" )
            // TODO: ONCE DUNJA, QA TESTER THAT SENDING PARCEL IS GOOD, THEN WE CAN DELETE THIS COMMENT
            // BECAUSE I DONT HAVE RIGHT NOW SPL PLUS, TABLET DEVICES
            //activity?.supportFragmentManager?.let { it -> sharePickupKeyDialog.show(it, "") }

           /* val pafDialogUi = InputAddressDialog(AnkoContext.create(itemView.context, itemView))
            pafDialogUi.okButton.setOnClickListener {
                val mailAddress = pafDialogUi.feedbackText.text.toString()
                if( mailAddress != "" ) {
                    pafDialogUi.errorMessage.visibility = View.GONE
                    GlobalScope.launch(Dispatchers.Default) {
                        if (!isUserMemberOfGroup(mailAddress, masterId)) {
                            log.info("PaF share $keyId - $mailAddress")
                            withContext(Dispatchers.Main) {
                                pafDialogUi.dialog.dismiss()
                            }
                            log.info("P@h created $keyId mail: $mailAddress")
                            val returnedData = WSUser.createPaF(keyId, mailAddress)
                            log.info("Invitation key = ${returnedData?.invitationCode}")
                            if (returnedData?.invitationCode.isNullOrEmpty()) {
                                withContext(Dispatchers.Main) {
                                    val message = itemView.resources.getString(R.string.app_generic_success)
                                    App.ref.toast(message)
                                    updateKeys()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    DialogUtil.messageDialogBuilder(itemView.context, itemView.context.getString(R.string.grant_access_error)) {
                                        val key = returnedData?.invitationCode
                                        log.info("Invitation Key: $key")
                                        val appLink = BuildConfig.APP_ANDR_DOWNLOAD_URL + key
                                        val iOSLink = BuildConfig.APP_IOS_DOWNLOAD_URL
                                        var shareBodyText = itemView.context.getString(R.string.access_sharing_share_app_text, appLink, iOSLink)
                                        val emailIntent = Intent(Intent.ACTION_SEND)
                                        emailIntent.setType("message/rfc822")
                                        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mailAddress))
                                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, itemView.context.getString(R.string.access_sharing_share_title))
                                        emailIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText)

                                        val message = itemView.context.getString(R.string.access_sharing_share_choose_sharing)
                                        startActivity(itemView.context, Intent.createChooser(emailIntent, message), null)

                                    }.show()
                                }
                            }

                        } else {
                            withContext(Dispatchers.Main) {
                                App.ref.toast(R.string.grant_access_error_exists)
                            }
                        }
                    }
                }
                else {
                    pafDialogUi.errorMessage.visibility = View.VISIBLE
                }
            }

            pafDialogUi.cancelButton.setOnClickListener {
                pafDialogUi.dialog.dismiss()
            }*/
        }
    }

}