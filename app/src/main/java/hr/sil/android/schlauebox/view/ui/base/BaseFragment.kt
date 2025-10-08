package hr.sil.android.schlauebox.view.ui.base

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.view.ui.MainActivity


abstract class BaseFragment : Fragment() {

    private val fragmentLoaderHandler = Handler(Looper.getMainLooper())


    fun setFragment(navFragment: Fragment, forgetHistory: Boolean = false) {
        val ctx = context ?: return
        val pendingRunnable = Runnable {
            val fragmentTransaction = (ctx as MainActivity).supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            fragmentTransaction.replace(R.id.main_frame_layout, navFragment, navFragment.tag).addToBackStack(null)
            if (forgetHistory) {
                fragmentManager?.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentTransaction.commitAllowingStateLoss()
            } else fragmentTransaction.commit()
        }
        fragmentLoaderHandler.post(pendingRunnable)
    }
}