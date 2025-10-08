package hr.sil.android.schlauebox.view.ui.home.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.BuildConfig
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.LoginActivity
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HelpActivity : BaseActivity() {

    private val log = logger()
    val sendWrapper by lazy { findViewById<RelativeLayout>(R.id.send_parcel_wrapper) }
    val pickupParcel by lazy { findViewById<RelativeLayout>(R.id.pickup_help_wrapper) }
    val shareAccess by lazy { findViewById<RelativeLayout>(R.id.share_access_wrapper) }
    val emailLink by lazy { findViewById<TextView>(R.id.help_link) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        this.setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initializeLinkableTextView(BuildConfig.APP_BASE_EMAIL, emailLink) {
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${BuildConfig.APP_BASE_EMAIL}"))
            //emailIntent.putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.app_generic_help))
            startActivity(Intent.createChooser(emailIntent, ""))
        }


        pickupParcel.setOnClickListener {
            val startIntent = Intent( applicationContext, HelpContentActivity::class.java)
            startIntent.putExtra("title", R.string.app_generic_pickup_parcel)
            startIntent.putExtra("content", R.string.help_pickup_parcel_content)
            startIntent.putExtra("positionOfPicture", 0)
            startActivity(startIntent)
        }

        sendWrapper.setOnClickListener {
            val startIntent = Intent(applicationContext, HelpContentActivity::class.java)
            startIntent.putExtra("title", R.string.app_generic_send_parcel)
            startIntent.putExtra("content", R.string.help_send_parcel_text)
            startIntent.putExtra("positionOfPicture", 1)
            startActivity(startIntent)
        }

        shareAccess.setOnClickListener {
            val startIntent = Intent(applicationContext, HelpContentActivity::class.java)
            startIntent.putExtra("title", R.string.app_generic_key_sharing)
            startIntent.putExtra("content", R.string.help_share_access_text)
            startIntent.putExtra("positionOfPicture", 2)
            startActivity(startIntent)
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
        val intent = Intent( this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


}