package hr.sil.android.schlauebox.view.ui.intro.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.databinding.FragmentIntroductionStartBinding
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.base.BaseFragment

class IntroStartSlideFragment : BaseFragment() {

    private lateinit var binding: FragmentIntroductionStartBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentIntroductionStartBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.introStartSignIn.setOnClickListener {
            SettingsHelper.firstRun = false
            val startupClass = LoginActivity::class.java
            val startIntent = Intent(this@IntroStartSlideFragment.activity, startupClass)
            startIntent.putExtra("SPLASH_START", false)
            startActivity(startIntent)
            this@IntroStartSlideFragment.activity?.finish()
        }
    }
}