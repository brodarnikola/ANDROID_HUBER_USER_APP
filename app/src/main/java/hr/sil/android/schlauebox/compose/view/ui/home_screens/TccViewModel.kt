package hr.sil.android.schlauebox.compose.view.ui.home_screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RLanguage
import hr.sil.android.schlauebox.util.SettingsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TccViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private var selectedLanguage: RLanguage? = null

    fun loadUrl() {
        viewModelScope.launch {
//            val languageName = SettingsHelper.languageName
//            val languagesList = withContext(Dispatchers.IO) {
//                WSUser.getLanguages() ?: listOf()
//            }
//            selectedLanguage = languagesList.find { it.code == languageName }
//
//            val correctLanguage = selectedLanguage?.code?.lowercase()
//
//            val urlToLoad = if (selectedLanguage != null && correctLanguage != "de") {
//                "https://www.schlauebox.ch/appagb_$correctLanguage"
//            } else {
//                "https://www.schlauebox.ch/appagb"
//            }

            _url.value = "https://www.schlauebox.ch/appagb"
        }
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
}