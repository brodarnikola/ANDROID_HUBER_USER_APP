package hr.sil.android.schlauebox.view.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.model.RLanguage

class LanguageAdapter(val listOfLanguages: List<RLanguage>) : BaseAdapter() {
    override fun getItemId(p0: Int): Long {
        return listOfLanguages[p0].id.toLong()
    }

    override fun getItem(p0: Int): Any {
        return listOfLanguages[p0]
    }

    override fun getCount(): Int {
        return listOfLanguages.size
    }

    override fun getView(p0: Int, convertView: View?, viewGroup: ViewGroup): View {
        val view: View
        val itemRowHolder: LanguageConfigurationViewHolder

        val itemView =
                LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.list_languages, viewGroup, false)

        if (convertView == null) {
            view = itemView
            itemRowHolder = LanguageConfigurationViewHolder(view)
            view.tag = itemRowHolder
        } else {
            view = convertView
            itemRowHolder = view.tag as LanguageConfigurationViewHolder
        }


        itemRowHolder.textView.text = listOfLanguages[p0].name
        return view
    }

    inner class LanguageConfigurationViewHolder(row: View) {
        val textView: TextView

        init {
            this.textView = row.findViewById(R.id.item_language_name)
        }
    }
}