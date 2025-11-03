package hr.sil.android.schlauebox.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
import java.util.*

object SettingsHelper {

    private const val NAME = "HubberSettings"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences


    // list of app specific preferences
    private val IS_FIRST_RUN_PREF = Pair("is_first_run", true)

    private val TOKEN = Pair("token", "")

    val LANGUAGE_ENGLISH = "DE"
    private val SELECTED_LANGUAGE = Pair("User_settings_Language", "DE")
    private val SELECTED_PUSH_ENABLED = Pair("User_settins_PushEnabled", true)
    private val SELECTED_EMAIL_ENABLED = Pair("User_settins_EmailEnabled", true)
    private val USERNAME_LOGIN = Pair("Username_login", "")

    private val USER_PASSWORD = Pair("user_password", "")

    private val DID_USER_REGISTER_OR_LOGIN = Pair("User_register_or_login", false)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }


    fun setLocale(c: Context): Context {
        return updateResources(c, getLanguage())
    }


    fun getLanguage(): String {
        return preferences.getString(SELECTED_LANGUAGE.first, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }

    private fun updateResources(context: Context, language: String): Context {
        var context = context
        val locale = Locale(language.lowercase())
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        context = context.createConfigurationContext(config)
        return context
    }


    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var languageName: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(SELECTED_LANGUAGE.first, SELECTED_LANGUAGE.second) ?: LANGUAGE_ENGLISH

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(SELECTED_LANGUAGE.first, value)
        }

    var pushEnabled: Boolean
        get() = preferences.getBoolean(SELECTED_PUSH_ENABLED.first, SELECTED_PUSH_ENABLED.second)

        set(value) = preferences.edit {
            it.putBoolean(SELECTED_PUSH_ENABLED.first, value)
        }

    var emailEnabled: Boolean
        get() = preferences.getBoolean(SELECTED_EMAIL_ENABLED.first, SELECTED_EMAIL_ENABLED.second)

        set(value) = preferences.edit {
            it.putBoolean(SELECTED_EMAIL_ENABLED.first, value)
        }

    var firstRun: Boolean
        get() = preferences.getBoolean(IS_FIRST_RUN_PREF.first, IS_FIRST_RUN_PREF.second)

        set(value) = preferences.edit {
            it.putBoolean(IS_FIRST_RUN_PREF.first, value)
        }

    var token: String
        get() = preferences.getString(TOKEN.first, TOKEN.second) ?: ""

        set(value) = preferences.edit {
            it.putString(TOKEN.first, value)
        }

    var usernameLogin: String
        get() = preferences.getString(USERNAME_LOGIN.first, USERNAME_LOGIN.second) ?: ""

        set(value) = preferences.edit {
            it.putString(USERNAME_LOGIN.first, value)
        }

    var userPasswordWithoutEncryption: String
        get() = preferences.getString(USER_PASSWORD.first, USER_PASSWORD.second) ?: ""

        set(value) = preferences.edit {
            it.putString(USER_PASSWORD.first, value)
        }

    var userRegisterOrLogin: Boolean
        get() = preferences.getBoolean(DID_USER_REGISTER_OR_LOGIN.first, DID_USER_REGISTER_OR_LOGIN.second)

        set(value) = preferences.edit {
            it.putBoolean(DID_USER_REGISTER_OR_LOGIN.first, value)
        }

}