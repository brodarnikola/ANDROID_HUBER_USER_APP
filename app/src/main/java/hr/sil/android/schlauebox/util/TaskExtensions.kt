package hr.sil.android.schlauebox.util

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine



suspend fun <T> Task<T>.awaitForResult(): Task<T> {
    return suspendCoroutine { continuation ->
        this@awaitForResult.addOnCompleteListener { task ->
            continuation.resume(task)
        }
    }
}