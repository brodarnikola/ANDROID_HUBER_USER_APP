package hr.sil.android.schlauebox.compose.view.ui.main_activity


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import dagger.hilt.android.AndroidEntryPoint
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.compose.view.ui.theme.AppTheme
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.ActivityMainBinding
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.MainActivity1.NavFragment
import hr.sil.android.view_util.permission.DroidPermission
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class MainActivity : BaseActivity(R.id.no_ble_layout, R.id.no_internet_layout, R.id.no_location_gps_layout) { //: ComponentActivity() { // AppCompatActivity() {

    private val log = logger()

    private val fragmentLoaderHandler = Handler(Looper.getMainLooper())

    private val homeNavFragment = NavFragment.HOME
    private var currentNavFragment = homeNavFragment
    private val droidPermission by lazy { DroidPermission.init(this) }

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setNotification()

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

        setContent {
            AppTheme {
                MainComposeApp()
                // A surface container using the 'background' color from the theme
            }
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

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    MVI_ComposeTheme {
//    }
//}