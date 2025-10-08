package hr.sil.android.schlauebox.view.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.databinding.ActivityLoginBinding
import hr.sil.android.schlauebox.databinding.ActivityPasswordRecoveryBinding
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.util.connectivity.NetworkChecker
import hr.sil.android.schlauebox.view.ui.intro.PasswordUpdateActivity
import kotlinx.coroutines.*

class PasswordRecoveryActivity : BaseActivity(noWifiViewId = R.id.no_internet_layout) {

    private val log = logger()

    private lateinit var binding: ActivityPasswordRecoveryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPasswordRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnPasswordRecovery.setOnClickListener {

            if (validate()) {

                binding.progressBar.visibility = View.VISIBLE

//                GlobalScope.launch {
//                    if (NetworkChecker.isInternetConnectionAvailable()) {
//
//                        val result = UserUtil.passwordRecovery(binding.etEmail.text.toString())
//                        log.info("Response code: ${result.code()}, is successfully: ${result.isSuccessful}, body is: ${result.body()}")
//
//                        withContext(Dispatchers.Main) {
//                            if (result.isSuccessful) {
//                                val intent = intentFor<PasswordUpdateActivity>("EMAIL" to binding.etEmail.text.toString())
//                                startActivity(intent)
//
//                            } else {
//                                binding.tilEmail.error = getString(R.string.forgot_password_email_error)
//                            }
//                            binding.progressBar.visibility = View.GONE
//                        }
//
//                    } else {
//                        App.ref.toast(R.string.app_generic_no_network)
//                    }
//                }
            }
        }
    }

    private fun validate(): Boolean {
        //if (!validateEmail(binding.tilEmail, binding.etEmail)) return false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


}