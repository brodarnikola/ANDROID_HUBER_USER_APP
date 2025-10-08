package hr.sil.android.schlauebox.view.ui.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.status.ActionStatusHandler
import hr.sil.android.schlauebox.cache.status.ActionStatusKey
import hr.sil.android.schlauebox.cache.status.ActionStatusType
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.model.RCreatedLockerKey
import hr.sil.android.schlauebox.core.util.formatFromStringToDate
import hr.sil.android.schlauebox.core.util.formatToViewDateTimeDefaults
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.core.util.macCleanToReal
import hr.sil.android.schlauebox.data.CancelPickAtHomeInterface
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.view.ui.dialog.CancelDeliveryDialog
import hr.sil.android.schlauebox.view.ui.dialog.CancelDeliveryNotInProximityDialog
import hr.sil.android.schlauebox.view.ui.home.activities.SendParcelsOverviewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.util.*

class SendParcelsSharingAdapter(private var keys: MutableList<RCreatedLockerKey>, val type: MPLDeviceType, val isInBleProximity: Boolean?, val sendParcelsOverviewActivity: SendParcelsOverviewActivity)
    : RecyclerView.Adapter<SendParcelsSharingAdapter.NotesViewHolder>() {

    val log = logger()
    
    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bindItem(keys[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_send_parcel_overview, parent, false)
        return NotesViewHolder(view)
    }

    override fun getItemCount() = keys.size

    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), CancelPickAtHomeInterface {

        val log = logger()
        val name: TextView = itemView.findViewById(R.id.key_sharing_item_key_header)
        val content: TextView = itemView.findViewById(R.id.key_sharing_item_key_content)
        val delete: ImageButton = itemView.findViewById(R.id.key_sharing_item_key_delete)
        val timeCreated: TextView = itemView.findViewById(R.id.key_sharing_time_created)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_circular)

        fun bindItem(keyObject: RCreatedLockerKey) {
            name.text = itemView.context.getString(R.string.app_generic_parcel_pin, keyObject.pin)

            if (type == MPLDeviceType.SPL) {
                keyObject.timeCreated = formatCorrectDate(keyObject.timeCreated)
                content.text = itemView.context.getString(R.string.app_generic_time_created, keyObject.timeCreated)
                timeCreated.visibility = View.GONE
            } else {
                content.text = itemView.context.getString(R.string.app_generic_size, keyObject.lockerSize)
                keyObject.timeCreated = formatCorrectDate(keyObject.timeCreated)
                timeCreated.text = itemView.context.getString(R.string.app_generic_time_created, keyObject.timeCreated)
                timeCreated.visibility = View.VISIBLE
            }

            if( isInBleProximity != null && isInBleProximity  ) {
                // red icon
                //delete.isEnabled = true
                delete.setOnClickListener { splItemClicked( keyObject ) }
                delete.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_cancel_access))
            }
            else if( isInBleProximity != null && !isInBleProximity) {
                // has rights, but not in proximity, grey icon
                //delete.isEnabled = false
                delete.setOnClickListener { splItemClickedDisabled(itemView.context, itemView) }
                delete.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_cancel_access_disabled))
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

        private fun splItemClickedDisabled(context: Context?, itemView: View) {

            val cancelDeliveryNotInProximityDialog = CancelDeliveryNotInProximityDialog()
            cancelDeliveryNotInProximityDialog.show( sendParcelsOverviewActivity.supportFragmentManager, "" )
        }


        override fun cancelPickAtHomeInterface(lockerMac: String, lockerMasterMac: String, lockerId: Int, lockerKeyId: Int, userId: Int) {
            progressBar.visibility = View.VISIBLE
            delete.visibility = View.GONE
            GlobalScope.launch {
                log.info("SPl unit mac = ${lockerMasterMac.macCleanToReal()}")
                val communicator = MPLDeviceStore.devices[lockerMasterMac.macCleanToReal()]?.createBLECommunicator(App.ref)
                val userId = UserUtil.user?.id ?: 0
                if (communicator != null && communicator.connect() && userId != 0) {
                    log.info("Connected to ${lockerMasterMac} - deleting ${lockerMac}")
                    val response = communicator.requestParcelSendCancel(lockerMac, userId)
                    if (response.isSuccessful) {
                        val action = ActionStatusKey().apply {
                            keyId = lockerId.toString() + ActionStatusType.PAH_ACCESS_CANCEL
                        }
                        ActionStatusHandler.actionStatusDb.put(action)

                        withContext(Dispatchers.Main) {
                            log.error("Success delete ${lockerId}")
                            delete.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            val removePickAtHomeKey = keys.filter { it.id == lockerKeyId }.firstOrNull()
                            if( removePickAtHomeKey != null ) {
                                keys.remove(removePickAtHomeKey)
                                notifyDataSetChanged()
                            }
                        }
                    } else {
                        log.error("Error while deleting the key ${response.bleDeviceErrorCode} - ${response.bleSlaveErrorCode}")
                        withContext(Dispatchers.Main) {
                            //App.ref.toast( sendParcelsOverviewActivity.resources.getString(R.string.sent_parcel_error_delete, lockerId.toString()))
                            delete.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                        }

                    }
                    communicator.disconnect()
                } else {
                    log.error("Error while connecting the main unit ${lockerMac}")
                    withContext(Dispatchers.Main) {
                        //App.ref.toast(sendParcelsOverviewActivity.resources.getString(R.string.sent_parcel_error_delete, lockerId.toString()))
                        delete.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE

                    }
                }
            }
        }

        private fun splItemClicked(parcelLocker: RCreatedLockerKey) {
            val cancelPickAtHomeKeyDialog = CancelDeliveryDialog( parcelLocker.lockerMac, parcelLocker.lockerMasterMac, parcelLocker.lockerId, parcelLocker.id,UserUtil.user?.id ?: 0, this@NotesViewHolder )
            cancelPickAtHomeKeyDialog.show( sendParcelsOverviewActivity.supportFragmentManager, "" )
        }

    }

}