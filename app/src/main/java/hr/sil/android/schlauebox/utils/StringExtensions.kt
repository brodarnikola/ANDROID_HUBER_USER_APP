package hr.sil.android.schlauebox.utils

import android.util.Patterns
import java.io.File

fun String?.isEmailValid() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String?.isPhoneValid() = !isNullOrEmpty() && Patterns.PHONE.matcher(this).matches()
