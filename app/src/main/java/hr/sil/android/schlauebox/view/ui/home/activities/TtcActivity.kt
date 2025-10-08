package hr.sil.android.schlauebox.view.ui.home.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.view.ui.BaseActivity

class TtcActivity : BaseActivity( noWifiViewId = R.id.no_internet_layout) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ttc)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                //TODO we should handle this one in different way

                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


}