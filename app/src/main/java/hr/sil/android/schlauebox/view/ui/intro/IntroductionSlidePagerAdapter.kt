package hr.sil.android.schlauebox.view.ui.intro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import hr.sil.android.schlauebox.view.ui.intro.fragment.IntroStartSlideFragment
import hr.sil.android.schlauebox.view.ui.intro.fragment.KeySharingSlideFragment
import hr.sil.android.schlauebox.view.ui.intro.fragment.PickupSlideFragment
import hr.sil.android.schlauebox.view.ui.intro.fragment.WelcomeSlideFragment

class IntroductionSlidePagerAdapter(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager) {

    // 2
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return WelcomeSlideFragment()
            }
            1 -> {
                return PickupSlideFragment()
            }
            2 -> {
                return KeySharingSlideFragment()
            }
            3 -> {
                return IntroStartSlideFragment()
            }
            else -> {
                return WelcomeSlideFragment()
            }

        }
    }

    // 3
    override fun getCount(): Int {
        return 4
    }
}