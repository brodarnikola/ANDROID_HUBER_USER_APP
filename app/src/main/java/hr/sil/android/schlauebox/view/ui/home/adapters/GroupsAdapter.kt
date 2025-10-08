package hr.sil.android.schlauebox.view.ui.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.model.RGroupInfo

class GroupsAdapter(var groups: List<RGroupInfo>) : BaseAdapter() {
    override fun getItemId(p0: Int): Long {
        return groups[p0].groupOwnerId
    }

    override fun getItem(p0: Int): Any {
        return groups[p0]
    }

    override fun getCount(): Int {
        return groups.size
    }

    override fun getView(p0: Int, convertView: View?, viewGroup: ViewGroup): View {
        val view: View
        val itemRowHolder: GroupViewHolder
        val itemView = LayoutInflater.from(viewGroup?.context).inflate(R.layout.list_group_access_sharing, viewGroup, false)

        if (convertView == null) {
            view = itemView
            itemRowHolder = GroupViewHolder(view)
            view.tag = itemRowHolder
        } else {
            view = convertView
            itemRowHolder = view.tag as GroupViewHolder
        }

        itemRowHolder.textView.text = groups[p0].groupOwnerName
        return view
    }

    inner class GroupViewHolder(row: View) {
        val textView: TextView
        init {
            this.textView = row.findViewById(R.id.groupNameTitle)
        }
    }
}