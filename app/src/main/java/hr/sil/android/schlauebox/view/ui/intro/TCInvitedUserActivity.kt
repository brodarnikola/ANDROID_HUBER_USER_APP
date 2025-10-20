package hr.sil.android.schlauebox.view.ui.intro


import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.databinding.ActivityTcinvitedUserBinding
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.MainActivity1
import kotlinx.coroutines.*


class TCInvitedUserActivity : AppCompatActivity() {

    val email: String by lazy { intent.getStringExtra("email") ?: "" }
    val password: String by lazy { intent.getStringExtra("password") ?: "" }
    val goToMainActivity: Boolean by lazy { intent.getBooleanExtra("goToMainActivity", false) }
    var didUserScrollToEnd:Boolean = false

    private lateinit var binding: ActivityTcinvitedUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTcinvitedUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onResume() {
        super.onResume()

        binding.btnAcceptTermsConditions.setOnClickListener {

//            if( didUserScrollToEnd ) {
                if( goToMainActivity ) {

                    GlobalScope.launch {
                        UserUtil.acceptedTerms()
                        SettingsHelper.userRegisterOrLogin = true
                        withContext(Dispatchers.Main) {
                            val startIntent = Intent(this@TCInvitedUserActivity, MainActivity1::class.java)
                            startActivity(startIntent)
                            finish()
                        }
                    }
                }
                else {
                    val intent = Intent(this@TCInvitedUserActivity, InviteUserActivity::class.java)
                    intent.putExtra("acceptTermsAndCondition", true)
                    intent.putExtra("email", email)
                    intent.putExtra("password", password)
                    startActivity(intent)
                    finish()
                }
//            }
        }

        val viewTreeObserver: ViewTreeObserver = binding.scrollViewTerms.getViewTreeObserver()

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.scrollViewTerms.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                val childHeight = (findViewById(R.id.llTermsAndConditions) as LinearLayout).height
                val isScrollable: Boolean = binding.scrollViewTerms.getHeight() < childHeight + binding.scrollViewTerms.getPaddingTop() + binding.scrollViewTerms.getPaddingBottom()
                if (!isScrollable) {
                    binding.btnAcceptTermsConditions.isClickable = true
                    //binding.btnAcceptTermsConditions.backgroundDrawable = ContextCompat.getDrawable(this@TCInvitedUserActivity.baseContext, R.drawable.rounded_button)
                    didUserScrollToEnd = true
                }
            }
        })

        viewTreeObserver.addOnScrollChangedListener(ViewTreeObserver.OnScrollChangedListener {
            if (binding.scrollViewTerms.getChildAt(0).getBottom() <= binding.scrollViewTerms.getHeight() + binding.scrollViewTerms.getScrollY()) {

                binding.btnAcceptTermsConditions.isClickable = true
                //binding.btnAcceptTermsConditions.backgroundDrawable = ContextCompat.getDrawable(this@TCInvitedUserActivity.baseContext, R.drawable.rounded_button)
                didUserScrollToEnd = true
                //scroll view is at bottom
            } else {
                //scroll view is not at bottom
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this@TCInvitedUserActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val startIntent = Intent(this@TCInvitedUserActivity, LoginActivity::class.java)
                startActivity(startIntent)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
