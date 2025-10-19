package hr.sil.android.schlauebox.view.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.ActivityMainBinding
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.util.backend.UserUtil.logout
import hr.sil.android.schlauebox.view.ui.home.fragment.NavHomeFragment
import hr.sil.android.schlauebox.view.ui.settings.NavSettingsFragment
import hr.sil.android.schlauebox.view.ui.tcc.NavTccFragment
import hr.sil.android.view_util.extensions.hideKeyboard
import hr.sil.android.view_util.permission.DroidPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : BaseActivity(R.id.no_ble_layout, R.id.no_internet_layout, R.id.no_location_gps_layout) {

    private val log = logger()

    private val fragmentLoaderHandler = Handler(Looper.getMainLooper())

    private val homeNavFragment = NavFragment.HOME
    private var currentNavFragment = homeNavFragment
    private val droidPermission by lazy { DroidPermission.init(this) }

    private lateinit var binding: ActivityMainBinding

    enum class NavFragment(val navItemIndex: Int, val tag: String, val loader: () -> Fragment) {
        HOME(0, "tag_nav_home", { NavHomeFragment() }),
        TCC(1, "tag_nav_tcc", { NavTccFragment() }),
        SETTINGS(2, "tag_nav_settings", { NavSettingsFragment() })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        if (!UserUtil.isUserLoggedIn()) {
////            setContentView(_RelativeLayout(this).apply {
////                progressBar {
////                    isIndeterminate = true
////                }.lparams {
////                    centerInParent()
////                }
////            })
//            GlobalScope.launch(Dispatchers.Main) {
//                if (UserUtil.login(SettingsHelper.usernameLogin)) {
//                    continueOnCreate(savedInstanceState)
//                } else {
//                    logout()
//                }
//            }
//        } else {
//            continueOnCreate(savedInstanceState)
//        }
        continueOnCreate(savedInstanceState)

    }

    private fun continueOnCreate(savedInstanceState: Bundle?) {
        //MainActivityUi().setContentView(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewLoaded = true

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setNotification()
//        binding.mainBottomHeaderId.setOnNavigationItemSelectedListener { item: MenuItem ->
//            val selectedNavFragment = NavFragment.values().firstOrNull { it.navItemIndex == item.itemId }
//            val fragmentManager = supportFragmentManager
//            // this will clear the back stack and displays no animation on the screen
//            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
//            if (selectedNavFragment != null) setNavFragment(selectedNavFragment)
//            else when (item.itemId) {
//                R.id.navigation_home -> {
//                    setNavFragment(NavFragment.HOME, true)
//                }
//
//                R.id.navigation_tc -> {
//                    setNavFragment(NavFragment.TCC, true)
//                }
//
//                R.id.navigation_settings -> {
//                    setNavFragment(NavFragment.SETTINGS, true)
//                }
//
//            }
//
//            true
//        }
        //get start fragment
        val startNavFragment = NavFragment.HOME

        //load initial fragment
        if (savedInstanceState == null) {
            log.info("Starting $startNavFragment")
            setNavFragment(startNavFragment, true)
        }

        val permissions = mutableListOf<String>().apply {
            addAll(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addAll(arrayOf(Manifest.permission.BLUETOOTH_SCAN,  Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (hr.sil.android.schlauebox.BuildConfig.DEBUG) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        droidPermission
                .request(*permissions)
                .done { _, deniedPermissions ->
                    if (deniedPermissions.isNotEmpty()) {
                        log.info("Some permissions were denied!")
                        App.ref.permissionCheckDone = true
                    } else {
                        log.info("Permissions accepted...")
                        App.ref.permissionCheckDone = true
                    }
                }
                .execute()
    }

    fun setNavFragment(navFragment: NavFragment, forceLoad: Boolean = false) {
        hideKeyboard()
        if (forceLoad || navFragment != currentNavFragment) {
            currentNavFragment = navFragment
            //binding.mainBottomHeaderId.menu.getItem(navFragment.navItemIndex).isChecked = true

            val pendingRunnable = Runnable {
                val fragment = navFragment.loader()
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                fragmentTransaction.replace(R.id.main_frame_layout, fragment, navFragment.tag)
                fragmentTransaction.commitAllowingStateLoss()
            }
            fragmentLoaderHandler.post(pendingRunnable)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                if ( supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if ( supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
    }

    override fun onPause() {
        super.onPause()
        App.ref.eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: UnauthorizedUserEvent) {
        log.info("Received unauthorized event, user will now be log outed")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun setNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW))
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (intent.extras != null) {
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!!.get(key)
                log.info("Key: $key Value: $value")
            }

        }
    }

    override fun onNetworkStateUpdated(available: Boolean) {
        super.onNetworkStateUpdated(available)
        networkAvailable = available
        if (viewLoaded) {
            updateUI()
        }
    }

    override fun onBluetoothStateUpdated(available: Boolean) {
        super.onBluetoothStateUpdated(available)
        bluetoothAvalilable = available
        if (viewLoaded) {
            updateUI()
        }
    }

    override fun onLocationGPSStateUpdated(available: Boolean) {
        super.onLocationGPSStateUpdated(available)
        locationGPSAvalilable = available
        updateUI()
    }

}
