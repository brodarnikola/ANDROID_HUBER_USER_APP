package hr.sil.android.schlauebox.view.ui.home.fragment


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.model.MPLDeviceType
import hr.sil.android.schlauebox.core.remote.model.RAvailableLockerSize
import hr.sil.android.schlauebox.core.remote.model.RMasterUnitType
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.data.ItemHomeScreen
import hr.sil.android.schlauebox.databinding.FragmentHomeScreenBinding
import hr.sil.android.schlauebox.events.MPLDevicesUpdatedEvent
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.MainActivity
import hr.sil.android.schlauebox.view.ui.base.BaseFragment
import hr.sil.android.schlauebox.view.ui.home.adapters.MplSplAdapter
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class NavHomeFragment : BaseFragment() {

    private val fragmentLoaderHandler = Handler()
    private val log = logger()
    private lateinit var mplSplAdapter: MplSplAdapter

    var availableLockers: List<RAvailableLockerSize> = listOf()
 
    private lateinit var binding: FragmentHomeScreenBinding

    private fun deviceItemClicked(parcelLocker: ItemHomeScreen.Child) {
        val fragment = MPLItemDetailsFragment()
        val args = Bundle()
        args.putString("rMacAddress", parcelLocker.mplOrSplDevice?.macAddress)
        args.putString("nameOfDevice", parcelLocker.mplOrSplDevice?.name)
        fragment.setArguments(args)
        setNavFragment(fragment)
    }

    private fun setDeviceItemClickListener(partItem: ItemHomeScreen.Child) {
        deviceItemClicked(partItem)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding.mainAddressBoxUserName.setText(UserUtil.user?.name ?: "")
        binding.mainAddressBoxAddress.setText(UserUtil.user?.address ?: "")
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: MPLDevicesUpdatedEvent) {
        renderDeviceItems()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: UnauthorizedUserEvent) {
        log.info("Received unauthorized event, user will now be log outed")
        val startIntent = Intent(this@NavHomeFragment.requireContext(), LoginActivity::class.java)
        startActivity(startIntent)
        this@NavHomeFragment.activity?.finish()
    }

    private fun renderDeviceItems() {

        val mplSplList = getItemsForRecyclerView()

        mplSplAdapter.updateDevices(mplSplList)
        handleParcelVisibility(mplSplList)
    }


    private fun handleParcelVisibility(mplSplList: MutableList<ItemHomeScreen>) {
        binding.mainNoParcelItemsText.visibility = if (mplSplList.isEmpty()) View.VISIBLE else View.GONE
        binding.mainMplRecycleView.visibility = if (mplSplList.isEmpty()) View.GONE else View.VISIBLE
    }


    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)

        val mplSplList = getItemsForRecyclerView()

        mplSplAdapter = MplSplAdapter(mplSplList, { partItem: ItemHomeScreen.Child ->
            setDeviceItemClickListener(partItem)
        })
        binding.mainMplRecycleView.adapter = mplSplAdapter

        binding.mainMplRecycleView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        (binding.mainMplRecycleView.getItemAnimator() as SimpleItemAnimator).supportsChangeAnimations = false
    }


    private fun getItemsForRecyclerView(): MutableList<ItemHomeScreen> {
        val items = mutableListOf<ItemHomeScreen>()

        val (splList, mplList) = MPLDeviceStore.devices.values
                .filter {
                    // testUser and ProductionDevice flag
                    val isThisDeviceAvailable = when {
                        UserUtil.user?.testUser ?: false == true -> true
                        else -> {
                            if( it.isProductionReady == true )
                                true
                            else
                                false
                        }
                    }
                    it.masterUnitType != RMasterUnitType.UNKNOWN && isThisDeviceAvailable
                }
                .partition { it.masterUnitType == RMasterUnitType.SPL || it.type == MPLDeviceType.SPL || it.masterUnitType == RMasterUnitType.SPL_PLUS || it.type == MPLDeviceType.SPL_PLUS }

        if (splList.isNotEmpty()) {
            val headerHomeScreen = ItemHomeScreen.Header()
            headerHomeScreen.headerTitle = getString(R.string.nav_home_spl_title)
            items.add(headerHomeScreen)
            items.addAll(splList.map { ItemHomeScreen.Child(it) })
        }

        if (mplList.isNotEmpty()) {
            val headerHomeScreen = ItemHomeScreen.Header()
            headerHomeScreen.headerTitle = getString(R.string.nav_home_mpl_title)
            items.add(headerHomeScreen)
            items.addAll(mplList.map { ItemHomeScreen.Child(it) })
        }

        return items
    }

    override fun onPause() {
        super.onPause()
        App.ref.eventBus.unregister(this)
    }

    private fun setNavFragment(navFragment: Fragment) {
        val ctx = context ?: return
        val pendingRunnable = Runnable {
            val fragmentTransaction = (ctx as MainActivity).supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            fragmentTransaction.replace(R.id.main_frame_layout, navFragment, navFragment.tag).addToBackStack(null)
            fragmentTransaction.commit()
        }
        log.info("fragment size is: " + (ctx as MainActivity).supportFragmentManager.backStackEntryCount)
        fragmentLoaderHandler.post(pendingRunnable)
    }

}