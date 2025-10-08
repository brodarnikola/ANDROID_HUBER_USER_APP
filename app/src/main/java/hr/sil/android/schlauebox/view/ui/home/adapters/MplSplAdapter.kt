package hr.sil.android.schlauebox.view.ui.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.cache.DatabaseHandler
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.model.InstalationType
import hr.sil.android.schlauebox.core.remote.model.RLockerKeyPurpose
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.data.ItemHomeScreen
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.model.MPLDevice
import hr.sil.android.schlauebox.view.ui.util.ListDiffer


class MplSplAdapter(var mplLocker: MutableList<ItemHomeScreen>, val clickListener: (ItemHomeScreen.Child) -> Unit) : RecyclerView.Adapter<MplSplAdapter.ViewHolder>() {

    val log = logger()
    private val devices: MutableList<ItemHomeScreen> = mplLocker.toMutableList()

    enum class ITEM_TYPES(val typeValue: Int) {
        ITEM_HEADER_HOME_SCREEN(0),
        ITEM_CHILD_HOME_SCREEN(1);

        companion object {
            fun from(findValue: Int): ITEM_TYPES = values().first { it.typeValue == findValue }
        }
    }

    override fun getItemViewType(position: Int): Int {

        return ITEM_TYPES.from(devices.get(position).getRecyclerviewItemType()).typeValue
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewType = devices[position]
        when (viewType.getRecyclerviewItemType()) {
            ITEM_TYPES.ITEM_HEADER_HOME_SCREEN.typeValue -> {

                holder as HeaderHolder
                val headerItem = devices[position] as ItemHomeScreen.Header
                holder.bindItem(headerItem)
            }
            ITEM_TYPES.ITEM_CHILD_HOME_SCREEN.typeValue -> {

                holder as MplItemViewHolder
                val childItem = devices[position] as ItemHomeScreen.Child
                holder.bindItem(childItem, clickListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_home_screen_header, parent, false)
            return HeaderHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_home_screen_child, parent, false)
            return MplItemViewHolder(view)
        }
    }

    override fun getItemCount() = devices.size

    fun updateDevices(updatedDevices: MutableList<ItemHomeScreen>) {

        val listDiff = ListDiffer.getDiff(
                devices,
                updatedDevices
        ) { old, new ->
            if( old is ItemHomeScreen.Child && new is ItemHomeScreen.Child ) {
                old.mplOrSplDevice?.macAddress == new.mplOrSplDevice?.macAddress &&
                        old.mplOrSplDevice?.type == new.mplOrSplDevice?.type &&
                        old.mplOrSplDevice?.masterUnitId == new.mplOrSplDevice?.masterUnitId  &&
                        old.mplOrSplDevice?.accessTypes == new.mplOrSplDevice?.accessTypes &&
                        old.mplOrSplDevice?.isSplActivate == new.mplOrSplDevice?.isSplActivate &&
                        old.mplOrSplDevice?.masterUnitType == new.mplOrSplDevice?.masterUnitType &&
                        old.mplOrSplDevice?.mplMasterDeviceStatus == new.mplOrSplDevice?.mplMasterDeviceStatus &&
                        old.mplOrSplDevice?.name == new.mplOrSplDevice?.name &&
                        old.mplOrSplDevice?.address == new.mplOrSplDevice?.address &&
                        old.mplOrSplDevice?.activeKeys?.size == new.mplOrSplDevice?.activeKeys?.size &&
                        old.mplOrSplDevice?.availableLockers?.size == new.mplOrSplDevice?.availableLockers?.size &&
                        old.mplOrSplDevice?.isInBleProximity == new.mplOrSplDevice?.isInBleProximity &&
                        old.mplOrSplDevice?.modemRssi == new.mplOrSplDevice?.modemRssi &&
                        old.mplOrSplDevice?.humidity == new.mplOrSplDevice?.humidity &&
                        old.mplOrSplDevice?.temperature == new.mplOrSplDevice?.temperature &&
                        old.mplOrSplDevice?.pressure == new.mplOrSplDevice?.pressure &&
                        old.mplOrSplDevice?.hasUserRightsOnLocker() == new.mplOrSplDevice?.hasUserRightsOnLocker()
            } else if( old is ItemHomeScreen.Header && new is ItemHomeScreen.Header  ) {
                old.headerTitle == new.headerTitle
            } else {
                false
            }
        }

        for (diff in listDiff) {
            when (diff) {
                is ListDiffer.DiffInserted -> {
                    devices.addAll(diff.elements)
                    log.info("notifyItemRangeInserted")
                    notifyItemRangeInserted(diff.position, diff.elements.size)
                }
                is ListDiffer.DiffRemoved -> {
                    //remove devices
                    for (i in (devices.size - 1) downTo diff.position) {
                        devices.removeAt(i)
                    }
                    log.info("notifyItemRangeRemoved")
                    notifyItemRangeRemoved(diff.position, diff.count)
                }
                is ListDiffer.DiffChanged -> {
                    devices[diff.position] = diff.newElement
                    log.info("notifyItemChanged")
                    notifyItemChanged(diff.position)
                }
            }
        }
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class HeaderHolder(itemView: View) : ViewHolder(itemView) {

        val headerTitle: TextView = itemView.findViewById(R.id.main_parcel_locker_item_name)

        fun bindItem(keyObject: ItemHomeScreen.Header) {
            headerTitle.text = keyObject.headerTitle
        }
    }

    inner class MplItemViewHolder(itemView: View) : ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.main_parcel_locker_item_name)
        val address: TextView = itemView.findViewById(R.id.main_parcel_locker_item_address)
        val groupedLabel: TextView = itemView.findViewById(R.id.main_locker_availability)
        var splIconImage: ImageView = itemView.findViewById(R.id.main_single_parcel_locker_icon)
        var arrow: ImageView = itemView.findViewById(R.id.main_single_parcel_locker_arrow)
        val newMplDeliveryBadge by lazy { itemView.findViewById<View>(R.id.home_mpl_circle_value) }
        val circleValue by lazy { itemView.findViewById<TextView>(R.id.mpl_details_circle_value) }

        fun bindItem(currentItem: ItemHomeScreen.Child, clickListener: (ItemHomeScreen.Child) -> Unit) {
            val parcelLocker = MPLDeviceStore.devices[currentItem.mplOrSplDevice?.macAddress]
            if (parcelLocker != null) {

                val lockerKeys = parcelLocker.activeKeys.filter { it -> it.purpose != RLockerKeyPurpose.UNKNOWN && it.purpose != RLockerKeyPurpose.PAH }
                val usedKeys = DatabaseHandler.deliveryKeyDb.get(parcelLocker.macAddress)?.keyIds
                        ?: listOf()
                val hasKeysForDelivery = lockerKeys.map { it.id }.subtract(usedKeys.asIterable())

                if (hasKeysForDelivery.isNotEmpty()) {
                    circleValue.visibility = View.VISIBLE
                    circleValue.width = 1
                    circleValue.height = 15
                    circleValue?.text = hasKeysForDelivery.size.toString()
                } else {
                    circleValue.visibility = View.GONE
                }

                var unavailable = false
                val parcelIcon =
                        if ( (parcelLocker.installationType == InstalationType.DEVICE && parcelLocker.masterUnitType == RMasterUnitType.MPL) || parcelLocker.type == MPLDeviceType.MASTER) {
                            if (parcelLocker.isInBleProximity && parcelLocker.hasUserRightsOnLocker() && parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED) {
                                groupedLabel.visibility = View.VISIBLE
                                groupedLabel.text = parcelLocker.availableLockers.joinToString(" ") { it.size.toString() + ": " + it.count }

                                arrow.visibility = View.VISIBLE
                                displayMPLSPLNameAndAddress(parcelLocker)

                                R.drawable.ic_available_mpl
                            }
                            else if (!parcelLocker.isInBleProximity && parcelLocker.hasUserRightsOnLocker()) {
                                groupedLabel.visibility = View.VISIBLE
                                groupedLabel.text = parcelLocker.availableLockers.joinToString(" ") { it.size.toString() + ": " + it.count }

                                arrow.visibility = View.VISIBLE
                                displayMPLSPLNameAndAddress(parcelLocker)

                                R.drawable.ic_locker_yellow
                            }
                            else if (parcelLocker.isInBleProximity && !parcelLocker.hasUserRightsOnLocker()) {
                                groupedLabel.visibility = View.GONE

                                arrow.visibility = View.VISIBLE
                                displayMPLSPLNameAndAddress(parcelLocker)

                                R.drawable.ic_unregistered_mpl
                            }
                            else {
                                unavailable = true
                                groupedLabel.visibility = View.GONE
                                displayMPLSPLNameAndAddress(parcelLocker)
                                arrow.visibility = View.GONE
                                address.text = itemView.context.getString(R.string.app_generic_not_activated)
                                R.drawable.ic_unavailable_mpl
                            }
                        }
                        else if( parcelLocker.installationType == InstalationType.TABLET || parcelLocker.type == MPLDeviceType.TABLET ) {
                            if (parcelLocker.isInBleProximity && parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED && parcelLocker.masterUnitId != -1) {
                                arrow.visibility = View.VISIBLE
                                displayMPLSPLNameAndAddress(parcelLocker)
                                R.drawable.ic_t_mpl_green
                            } else if (parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED && parcelLocker.isInBleProximity && parcelLocker.masterUnitId == -1) {
                                arrow.visibility = View.VISIBLE
                                displayMPLSPLNameAndAddress(parcelLocker)
                                R.drawable.ic_t_mpl_grey
                            } else if (!parcelLocker.isInBleProximity) {
                                arrow.visibility = View.VISIBLE
                                displayMPLSPLNameAndAddress(parcelLocker)
                                R.drawable.ic_t_mpl_yellow
                            } else if (parcelLocker.isInBleProximity) {
                                displayMPLSPLNameAndAddress(parcelLocker)
                                unavailable = true
                                arrow.visibility = View.GONE
                                address.visibility = View.GONE
                                R.drawable.ic_t_mpl_red
                            } else {
                                displayMPLSPLNameAndAddress(parcelLocker)
                                unavailable = true
                                arrow.visibility = View.GONE
                                address.visibility = View.GONE
                                R.drawable.ic_t_mpl_red
                            }
                        }
                        // when will be implemented on backend, that masterUnitType return SPL PLUS for SPL PLUS
                        // then I need to uncomment this method here
                       /* else if( parcelLocker.masterUnitType == RMasterUnitType.SPL_PLUS || parcelLocker.type == MPLDeviceType.SPL_PLUS ) {
                            if (parcelLocker.isInBleProximity && parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.REGISTERED && parcelLocker.masterUnitId != -1) {
                                arrow.visibility = View.VISIBLE
                                displayMPLSPLNameAndAddress(parcelLocker)
                                R.drawable.ic_available_spl_plus
                            } else if (parcelLocker.mplMasterDeviceStatus == MPLDeviceStatus.UNREGISTERED && parcelLocker.isInBleProximity) {
                                arrow.visibility = View.VISIBLE
                                displayMPLSPLNameAndAddress(parcelLocker)
                                R.drawable.ic_unregistered_spl_plus
                            } else if (!parcelLocker.isInBleProximity) {
                                arrow.visibility = View.VISIBLE
                                displayMPLSPLNameAndAddress(parcelLocker)
                                R.drawable.ic_available_spl_plus_yellow
                            } else if (parcelLocker.isInBleProximity) {
                                displayMPLSPLNameAndAddress(parcelLocker)
                                unavailable = true
                                arrow.visibility = View.GONE
                                address.visibility = View.GONE
                                R.drawable.ic_unavailable_spl_plus
                            } else {
                                displayMPLSPLNameAndAddress(parcelLocker)
                                unavailable = true
                                arrow.visibility = View.GONE
                                address.visibility = View.GONE
                                R.drawable.ic_unavailable_spl_plus
                            }
                        }*/
                        else {

                            groupedLabel.visibility = View.GONE

                            if (parcelLocker.isInBleProximity && parcelLocker.hasUserRightsOnLocker()) {

                                arrow.visibility = View.VISIBLE
                                if (parcelLocker.name.isEmpty()) {
                                    name.text = parcelLocker.macAddress
                                }
                                displayMPLSPLNameAndAddress(parcelLocker)
                                R.drawable.ic_available_spl
                            }
                            else if (parcelLocker.isInBleProximity && !parcelLocker.isSplActivate && !parcelLocker.hasUserRightsOnLocker()) {

                                arrow.visibility = View.VISIBLE
                                if (parcelLocker.address.isEmpty()) {
                                    name.text = parcelLocker.macAddress
                                    address.text = itemView.context.getString(R.string.app_generic_not_activated)
                                }
                                displayMPLSPLNameAndAddress(parcelLocker)
                                R.drawable.ic_s_locker_grey
                            }
                            else if (!parcelLocker.isInBleProximity && parcelLocker.hasUserRightsOnLocker()) {

                                arrow.visibility = View.VISIBLE
                                displayMPLSPLNameAndAddress(parcelLocker)
                                R.drawable.ic_s_locker_yellow
                            }
                            else if (parcelLocker.isInBleProximity && !parcelLocker.hasUserRightsOnLocker()) {
                                displayMPLSPLNameAndAddress(parcelLocker)
                                arrow.visibility = View.GONE
                                unavailable = true
                                R.drawable.ic_unavailable_spl
                            } else {
                                displayMPLSPLNameAndAddress(parcelLocker)
                                unavailable = true
                                address.text = itemView.context.getString(R.string.app_generic_access_forbidden)
                                arrow.visibility = View.GONE
                                R.drawable.ic_unavailable_spl
                            }
                        }


                splIconImage.setImageDrawable(ContextCompat.getDrawable(itemView.context, parcelIcon))

                itemView.setOnClickListener {
                    if (unavailable) {
                        //App.ref.toast(R.string.app_generic_access_forbidden)
                    } else {
                        clickListener(currentItem)
                    }
                }
            }
        }

        private fun displayMPLSPLNameAndAddress(parcelLocker: MPLDevice) {
            if( parcelLocker.name.isNotEmpty() && parcelLocker.address.isNotEmpty() ) {
                name.visibility = View.VISIBLE
                address.visibility = View.VISIBLE
                name.text = parcelLocker.name
                address.text = parcelLocker.address
            }
            else if( parcelLocker.name.isEmpty() && parcelLocker.address.isNotEmpty() ) {
                name.visibility = View.GONE
                address.visibility = View.VISIBLE
                address.text = parcelLocker.address
            }
            else if( parcelLocker.name.isNotEmpty() && parcelLocker.address.isEmpty() ) {
                address.visibility = View.GONE
                name.visibility = View.VISIBLE
                name.text = parcelLocker.name
            }
            else {
                name.visibility = View.GONE
                address.visibility = View.GONE
            }
        }
    }
}