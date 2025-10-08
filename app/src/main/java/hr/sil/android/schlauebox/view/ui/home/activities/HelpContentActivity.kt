package hr.sil.android.schlauebox.view.ui.home.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.BuildConfig
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.ActivityHelpContentBinding
import hr.sil.android.schlauebox.databinding.ActivityLoginBinding
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.LoginActivity
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class HelpContentActivity : BaseActivity() {

    private val log = logger()
    val title by lazy { intent.getIntExtra("title", 0) }
    val content by lazy { intent.getIntExtra("content", 0) }
    val positionOfPicture by lazy { intent.getIntExtra("positionOfPicture", 0) }
    val headerImage by lazy { findViewById<ImageView>(R.id.main_title_image_id) }


    val emailLink by lazy { findViewById<TextView>(R.id.help_link) }

    private lateinit var binding: ActivityHelpContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHelpContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        this.setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.sendParcelContent.setText(content)
        binding.sendParcelHeader.setText(title)

        if(positionOfPicture == 0){
            headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_pickup_parcel_small))
        }
        else if(positionOfPicture == 1){
            headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_send_parcel_small))
        }
        else {
            headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_share_access_small))
        }

        initializeLinkableTextView(BuildConfig.APP_BASE_EMAIL, emailLink) {
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${BuildConfig.APP_BASE_EMAIL}"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, this@HelpContentActivity.getString(R.string.app_generic_help))
            startActivity(Intent.createChooser(emailIntent, ""))
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