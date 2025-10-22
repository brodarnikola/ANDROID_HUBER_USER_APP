package hr.sil.android.schlauebox.view.ui.tcc


import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.WSUser
//import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.remote.model.RLanguage
import hr.sil.android.schlauebox.databinding.FragmentTermsConditionsBinding
import hr.sil.android.schlauebox.util.SettingsHelper
import hr.sil.android.schlauebox.view.ui.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NavTccFragment : BaseFragment() {


    private lateinit var binding: FragmentTermsConditionsBinding
    private val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    var selectedLanguage: RLanguage? = RLanguage()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentTermsConditionsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onStart() {
        super.onStart()
        GlobalScope.launch {


            val languageName = SettingsHelper.languageName
            val languagesList = WSUser.getLanguages() ?: listOf() //DataCache.getLanguages()
            selectedLanguage = languagesList.find { it.code == languageName }

            withContext(Dispatchers.Main) {

                val webSettings = binding.webViewTermsCondition.settings

                webSettings.javaScriptEnabled = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    webSettings.safeBrowsingEnabled = true  // api 26
                }

                val correctLanguage = selectedLanguage?.code?.toLowerCase()

                if (selectedLanguage != null && correctLanguage !="de") {
                    binding.webViewTermsCondition.loadUrl("https://www.schlauebox.ch/appagb_" + correctLanguage)
                } else {
                    binding.webViewTermsCondition.loadUrl("https://www.schlauebox.ch/appagb")
                }

                binding.webViewTermsCondition.webChromeClient = WebChromeClient()

                binding.webViewTermsCondition.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                        binding.progressCircular.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        binding.progressCircular.visibility = View.GONE

                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val url: String = request?.url.toString()
                        if (url.equals("mailto:info@swissinnolab.com")) {

                           // val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${ctx.getString(R.string.help_link)}"))
                            //emailIntent.putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.app_generic_help))
                            //startActivity(Intent.createChooser(emailIntent, ""))

                            return true
                        }

                        return false
                    }
                }

            }
        }

    }

    override fun onStop() {
        super.onStop()

        binding.webViewTermsCondition.removeAllViews();

        binding.webViewTermsCondition.destroy()
    }

}