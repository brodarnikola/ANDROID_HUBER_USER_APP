package hr.sil.android.schlauebox.view.ui.home.adapters


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
//import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.core.util.logger

import kotlinx.coroutines.*


class KeySharingAdapter(var keys: MutableList<ItemRGroupInfo>
                        /* val clickListener: (RGroupDisplayMembersChild) -> Unit */) : RecyclerView.Adapter<KeySharingAdapter.ViewHolder>() {


    enum class ITEM_TYPES (val typeValue: Int) {
        ITEM_HEADER (0),
        ITEM_ADMIN_NAME (1),
        ITEM_CHILD (2);

        companion object {
            fun from(findValue: Int): ITEM_TYPES = ITEM_TYPES.values().first { it.typeValue == findValue }
        }
    }


    override fun getItemViewType(position: Int): Int {

        return  ITEM_TYPES.from(keys.get(position).getListItemType()).typeValue
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val viewType = keys[position]
        when (viewType.getListItemType()) {
            ITEM_TYPES.ITEM_HEADER.typeValue -> {

                holder as HeaderHolder
                val headerItem = keys[position] as RGroupDisplayMembersHeader
                holder.bindItem(headerItem)
            }
            ITEM_TYPES.ITEM_ADMIN_NAME.typeValue -> {

                val childItem = keys[position] as RGroupDisplayMembersAdmin
                holder as AdminHolder
                holder.bindItem(childItem)
            }
            ITEM_TYPES.ITEM_CHILD.typeValue -> {

                val childItem = keys[position] as RGroupDisplayMembersChild
                holder as ChildHolder
                holder.bindItem(childItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  ViewHolder {

        if( viewType == 0 ) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_access_sharing_header, parent, false)
            return HeaderHolder(view)
        }
        else if( viewType == 1 ) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_access_sharing_admin, parent, false)
            return AdminHolder(view)
        }
        else  {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_access_sharing_child, parent, false)
            return ChildHolder(view)
        }
    }

    override fun getItemCount() = keys.size

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    inner class HeaderHolder(itemView: View) : ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.key_sharing_item_key_name)

        fun bindItem(keyObject: RGroupDisplayMembersHeader) {
            name.text = keyObject.groupOwnerName
        }
    }

    inner class AdminHolder(itemView: View) : ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.key_sharing_item_key_name)

        fun bindItem(keyObject: RGroupDisplayMembersAdmin) {
            name.text = keyObject.groupOwnerName
        }
    }


    inner class ChildHolder(itemView: View) : ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.key_sharing_item_key_name)
        val emailText: TextView = itemView.findViewById(R.id.key_sharing_item_key_email)
        val roleText: TextView = itemView.findViewById(R.id.key_sharing_item_key_role)
        val delete: ImageButton = itemView.findViewById(R.id.key_sharing_item_key_delete)

        fun bindItem(keyObject: RGroupDisplayMembersChild) {
            name.text = keyObject.endUserName
            emailText.text = keyObject.endUserEmail
            roleText.text = when (keyObject.role){
                "ADMIN" -> itemView.context.getString(R.string.access_sharing_admin_role_full)
                "USER" -> itemView.context.getString(R.string.access_sharing_user_role_full)
                else ->""
            }
            delete.setOnClickListener { deleteAccess(keyObject) }
        }

        val log = logger()
        private fun deleteAccess(key: RGroupDisplayMembersChild) {

            val userAccess = RUserRemoveAccess().apply {
                this.groupId = key.groupId
                this.endUserId = key.endUserId
                this.masterId = key.master_id
            }

//            itemView.context.alert {
//                message = ctx.getString(R.string.access_sharing_warning_message,  key.groupOwnerName)
//                positiveButton(R.string.app_generic_confirm)  {
//                    GlobalScope.launch {
//                        if (WSUser.removeUserAccess(userAccess)) {
//                            log.info("Successfully remove user ${key.groupOwnerName} from group")
//
//                            deleteItem(key /*, itemView.context */)
//                            withContext(Dispatchers.Main) {
//
//                                notifyDataSetChanged()
//                                //clickListener(key)
//                                App.ref.toast(ctx.getString(R.string.toast_access_sharing_remove_success, key.groupOwnerName))
//                            }
//                        } else {
//                            withContext(Dispatchers.Main) {
//                                App.ref.toast(ctx.getString(R.string.toast_access_sharing_remove_error))
//
//                            }
//                        }
//                    }
//                }
//                negativeButton(android.R.string.cancel) {
//
//                }
//                onCancelled {
//
//                }
//            }.show()
        }
    }

    private suspend fun deleteItem(item: RGroupDisplayMembersChild /* , context: Context */ ) {



        var deletedInOwnerList: Boolean = false

        for( ownerItems in WSUser.getGroupMembers() ?: mutableListOf() ) { //DataCache.getGroupMembers() )  {

            if( ownerItems.endUserId == item.endUserId && ownerItems.groupId == item.groupId && ownerItems.master_id == item.master_id ) {

                deletedInOwnerList = true
                //DataCache.deleteOwnerGroupElement(ownerItems.id)
                break
            }
        }



        if( deletedInOwnerList == false ) {

            val groupMemberShipId = WSUser.getGroupMemberships()?: mutableListOf() //DataCache.getGroupMemberships().toMutableList()
            var isAdminItemDeleted = false

            if( groupMemberShipId.isNotEmpty() ) {

                for (items in groupMemberShipId) {

                    var groupDataList: MutableList<RGroupInfo> = WSUser.getGroupMembershipsById(items.groupId.toLong()) ?: mutableListOf() // DataCache.groupMemberships(items.groupId.toLong()).toMutableList()
                    Log.d("KeySharingAdapter", "Admin group list size is: " + groupDataList.size)

                    if (groupDataList.size > 0) {

                        for (subItems in groupDataList) {

                            if( subItems.endUserId == item.endUserId && subItems.groupId == item.groupId && subItems.master_id == item.master_id ) {

                                isAdminItemDeleted = true
                                WSUser.getGroupMembershipsById(items.groupId.toLong()) ?: mutableListOf()
                                //DataCache.groupMemberships(items.groupId.toLong(), true)
                                break
                            }
                        }
                    }

                    if ( isAdminItemDeleted ){
                         break
                    }
                }
            }
        }

        Log.d("KeySharingAdapter", "Second example Admin group list size is: " )

        keys.remove(item)
    }

}