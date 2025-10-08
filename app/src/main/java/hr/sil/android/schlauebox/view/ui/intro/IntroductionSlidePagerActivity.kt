package hr.sil.android.schlauebox.view.ui.intro

import android.os.Bundle
//import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import hr.sil.android.schlauebox.R
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import hr.sil.android.schlauebox.view.ui.BaseActivity


class IntroductionSlidePagerActivity  : BaseActivity(noWifiViewId = R.id.no_internet_layout) {

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private val NUM_PAGES = 5
    private val fragmentLoaderHandler = Handler(Looper.getMainLooper())
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private lateinit var viewPager: ViewPager

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private var mPagerAdapter: PagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_slide)

        // Instantiate a ViewPager and a PagerAdapter.
        viewPager = findViewById(R.id.viewPager) as ViewPager
        //val dotsIndicator = findViewById<WormDotsIndicator>(R.id.worm_dots_indicator)
        mPagerAdapter = IntroductionSlidePagerAdapter(this.supportFragmentManager)
        viewPager.adapter = mPagerAdapter
        //dotsIndicator.setViewPager(viewPager)
        val intent = intent
        val s1 = intent.getBooleanExtra("Back", false)

        if (s1) {
            viewPager.setCurrentItem(3, false)
        }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

   private fun setFragment(navFragment: Fragment, forgetHistory: Boolean = false) {

        val pendingRunnable = Runnable {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
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