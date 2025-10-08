package hr.sil.android.schlauebox.view.ui.home.adapters


import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.core.util.logger

import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.schlauebox.util.backend.UserUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PinManagementAdapter(var keys: MutableList<RPinManagement>,
                           var masterUnitId: Int?  ) : RecyclerView.Adapter<PinManagementAdapter.PinViewHolder>() {

    var previousSelectedRow: Int = -1

    override fun onBindViewHolder(holder: PinViewHolder, position: Int) {
        holder.bindItem(position, keys[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_pin_data, parent, false)
        return PinViewHolder(view)
    }

    override fun getItemCount() = keys.size

    inner class PinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val log = logger()
        val number: TextView = itemView.findViewById(R.id.pin_management_number)
        val name: TextView = itemView.findViewById(R.id.pin_management_name)
        val openToDelete: ImageButton = itemView.findViewById(R.id.key_sharing_item_key_delete)
        val insertNewPinLayout: RelativeLayout = itemView.findViewById(R.id.pin_management_new_layout)
        val etPinNewName: EditText = itemView.findViewById(R.id.pin_management_new_name)
        val savePinButton: ImageButton = itemView.findViewById(R.id.pin_management_save_pin)
        val mainLayout: RelativeLayout = itemView.findViewById(R.id.main_parcel_locker_item_wrapper)
        val deleteLayoutConfirm: RelativeLayout = itemView.findViewById(R.id.pin_management_delete_layout)
        val buttonDelete: Button = itemView.findViewById(R.id.pin_management_delete_button)
        val buttonCancelDeletePin: Button = itemView.findViewById(R.id.pin_management_cancel_button)

        @SuppressLint("ClickableViewAccessibility")
        fun bindItem(position: Int, pinObject: RPinManagement ) {

            number.text = pinObject.pin

//            if (pinObject.isSelected && pinObject.position % 2 == 0)
//                mainLayout.backgroundDrawable = ContextCompat.getDrawable(itemView.context, R.drawable.rounded_pin_selected_even_layout)
//            else if (pinObject.isSelected && pinObject.position % 2 != 0)
//                mainLayout.backgroundDrawable = ContextCompat.getDrawable(itemView.context, R.drawable.rounded_pin_selected_odd_layout)
//            else if (pinObject.isSelected == false && pinObject.position % 2 == 0)
//                mainLayout.backgroundColor = ContextCompat.getColor(itemView.context, R.color.pin_managment_20_percent)
//            else
//                mainLayout.backgroundColor = ContextCompat.getColor(itemView.context, R.color.pin_managment_40_percent)

            if (pinObject.pinGenerated) {

                name.text = itemView.context.getString(R.string.pin_managment_generate)
                openToDelete.setOnClickListener {
                    showAdddPinButton(pinObject, position)
                }
                openToDelete.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.group_2))
                savePinButton.setOnClickListener {
                    savePinFromGroup(pinObject, it, position)
                }

                etPinNewName.setOnTouchListener { v, event ->
                     var findSelectedRow = keys.filter { it.isSelected == true }.firstOrNull()

                     if( findSelectedRow != null && pinObject.isSelected != findSelectedRow.isSelected ) {

                         findSelectedRow.isExtendedToDelete = false
                         findSelectedRow.isSelected = false
                         notifyItemChanged(keys.indexOf(findSelectedRow), findSelectedRow);

                         pinObject.isSelected = true
                         notifyItemChanged(keys.indexOf(pinObject), pinObject);
                     }

                     previousSelectedRow = position
                     App.ref.pinManagementSelectedItem = pinObject
                     false
                 }

                etPinNewName.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        App.ref.pinManagementName = p0.toString()
                        var findSelectedRow = keys.filter { it.isSelected == true }.firstOrNull()

                        if( findSelectedRow != null && pinObject.isSelected != findSelectedRow.isSelected ) {

                            findSelectedRow.isExtendedToDelete = false
                            findSelectedRow.isSelected = false
                            notifyItemChanged(keys.indexOf(findSelectedRow), findSelectedRow);

                            pinObject.isSelected = true
                            notifyItemChanged(keys.indexOf(pinObject), pinObject);
                        }

                        previousSelectedRow = position
                        App.ref.pinManagementSelectedItem = pinObject
                    }
                })
            }
            else {
                name.text = pinObject.pinName
                openToDelete.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_cancel_access))

                if( pinObject.isExtendedToDelete )
                    deleteLayoutConfirm.visibility = View.VISIBLE
                else
                    deleteLayoutConfirm.visibility = View.GONE

                openToDelete.setOnClickListener {
                    openToDeletePin(pinObject, position)
                }

                buttonCancelDeletePin.setOnClickListener {
                    deleteLayoutConfirm.visibility = View.GONE
                    pinObject.isExtendedToDelete = false
                    notifyItemChanged(keys.indexOf(pinObject), pinObject);
                }

                buttonDelete.setOnClickListener {
                    GlobalScope.launch {

                        val pinSaved = WSUser.deletePinForSendParcel(pinObject.pinId)

                        withContext(Dispatchers.Main) {

                            if (pinSaved) {
                                log.info("Succesfully deleted pin")

                                keys.remove(pinObject)
                                val isPinSelected = keys.filter { it.isSelected == true }.firstOrNull()

                                for (items in keys) {

                                    if (isPinSelected == null && (items.position - 1) == pinObject.position)
                                        items.isSelected = true

                                    if (items.position > pinObject.position)
                                        items.position = items.position - 1
                                }
                                //previousSelectedRow = adapterPosition - 1

                                notifyDataSetChanged()
                            } else {
                            }
                        }
                    }
                }
            }

            mainLayout.setOnClickListener { view ->

                if (previousSelectedRow == -1 && position != 0) {

                    var findSelectedRow = keys.filter { it.isSelected == true }.firstOrNull()

                    if (findSelectedRow != null) {
                        findSelectedRow.isExtendedToDelete = false
                        findSelectedRow.isSelected = false
                        notifyItemChanged(keys.indexOf(findSelectedRow), findSelectedRow);
                    }

                    pinObject.isSelected = true

                    previousSelectedRow = position
                    notifyItemChanged(keys.indexOf(pinObject), pinObject);
                    //notifyDataSetChanged()
                } else {

                    if (previousSelectedRow != position && previousSelectedRow != -1) {

                        var findSelectedRow = keys.filter { it.isSelected == true }.firstOrNull()

                        if (findSelectedRow != null) {
                            findSelectedRow.isExtendedToDelete = false
                            findSelectedRow.isSelected = false
                            notifyItemChanged(keys.indexOf(findSelectedRow), findSelectedRow);
                        }

                        pinObject.isSelected = true
                        notifyItemChanged(keys.indexOf(pinObject), pinObject);

                        previousSelectedRow = position
                        //notifyDataSetChanged()
                    }
                }
                App.ref.pinManagementSelectedItem = pinObject
            }
        }

        private fun savePinFromGroup(pinObject: RPinManagement, itemView: View, position: Int) {


            if (etPinNewName.text.toString() != "") {
                GlobalScope.launch {

                    val savePin = RPinManagementSavePin()
                    savePin.groupId = UserUtil.userGroup?.id
                    savePin.masterId = masterUnitId
                    savePin.pin = pinObject.pin
                    savePin.name = etPinNewName.text.toString()

                    val pinSaved = WSUser.savePinManagementForSendParcel(savePin)

                    if( pinSaved != null ) {

                        val changePin = keys.filter { it.pinGenerated == true }.firstOrNull()
                        if( changePin != null ) {
                            changePin.pinGenerated = false
                            changePin.pinId = pinSaved.id
                            changePin.pinName = pinSaved.name
                        }
                    }

                    withContext(Dispatchers.Main) {

                        if (pinSaved != null) {

                            App.ref.pinManagementSelectedItem = pinObject

                            log.info("Pin successfully saved")
                            insertNewPinLayout.visibility = View.GONE
                            name.visibility = View.VISIBLE
                            name.text = pinObject.pinName
                            openToDelete.visibility = View.VISIBLE
                            openToDelete.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_cancel_access))
                            openToDelete.setOnClickListener { openToDeletePin(pinObject, position) }
                            notifyDataSetChanged()
                        } else {

                            log.info("Pin is not successfully saved")
                        }
                    }
                }
            } else {
                //App.ref.toast(itemView.context.getString(R.string.pin_managment_name_not_empty))
            }
        }

        private fun showAdddPinButton(pinObject: RPinManagement, position: Int) {

            insertNewPinLayout.visibility = View.VISIBLE
            val params = etPinNewName.getLayoutParams()
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT
            etPinNewName.setLayoutParams(params)

            etPinNewName.isFocusable = true
            etPinNewName.requestFocusFromTouch()
            etPinNewName.requestFocus()
            etPinNewName.setImeOptions(EditorInfo.IME_ACTION_DONE);

            openToDelete.visibility = View.GONE
            name.visibility = View.GONE

            var findSelectedRow = keys.filter { it.isSelected == true }.firstOrNull()

            if( findSelectedRow != null && pinObject.isSelected != findSelectedRow.isSelected ) {

                findSelectedRow.isExtendedToDelete = false
                findSelectedRow.isSelected = false
                notifyItemChanged(keys.indexOf(findSelectedRow), findSelectedRow);
            }
            pinObject.isSelected = true
            notifyItemChanged(keys.indexOf(pinObject), pinObject);

            previousSelectedRow = position
            App.ref.pinManagementSelectedItem = pinObject
        }

        private fun openToDeletePin(pinObject: RPinManagement, position: Int) {

            deleteLayoutConfirm.visibility = View.VISIBLE

            var findSelectedRow = keys.filter { it.isSelected == true }.firstOrNull()

            if( findSelectedRow != null && pinObject.isSelected != findSelectedRow.isSelected ) {

                findSelectedRow.isExtendedToDelete = false
                findSelectedRow.isSelected = false
                notifyItemChanged(keys.indexOf(findSelectedRow), findSelectedRow);

                pinObject.isExtendedToDelete = true
                pinObject.isSelected = true
                notifyItemChanged(keys.indexOf(pinObject), pinObject);
            }

            previousSelectedRow = position
            App.ref.pinManagementSelectedItem = pinObject
        }
    }

}