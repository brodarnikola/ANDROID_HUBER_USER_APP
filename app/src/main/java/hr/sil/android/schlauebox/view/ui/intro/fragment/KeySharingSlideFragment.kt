package hr.sil.android.schlauebox.view.ui.intro.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.view.ui.base.BaseFragment

class KeySharingSlideFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_introduction_key_sharing, container, false)
    }
}