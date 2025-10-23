
package hr.sil.android.schlauebox.utils

sealed class Resource<T> {
    data class Success<T>(val data: T? = null) : Resource<T>() {
        override fun toString() = "[Success: $data]"
    }

    data class Confirmation<T>(val data: T? = null) : Resource<T>() {
        override fun toString() = "[Success: $data]"
    }
    // Optional data allows to expose data stub just for loading state.
    data class Loading<T>(val data: T? = null) : Resource<T>() {
        override fun toString() = "[Loading: $data]"
    }
    data class Initial<T>(val data: T? = null) : Resource<T>() {
        override fun toString() = "[Loading: $data]"
    }

    data class Failure<T>(val error: Error) : Resource<T>() {
        override fun toString() = "[Failure: $error]"
    }
    data class FirebaseFailureException<T>(val error: T?) : Resource<T>() {
        override fun toString() = "[Exception failure: $error]"
    }

    fun unwrap(): T? =
        when (this) {
            is Loading -> data
            is Initial -> data
            is Success -> data
            is Confirmation -> data
            is Failure -> null
            is FirebaseFailureException -> error
        }

    inline fun onFailure(handle: (Error) -> Unit): Resource<T> {
        if (this is Failure) {
            handle(error)
        }
        return this
    }
}
