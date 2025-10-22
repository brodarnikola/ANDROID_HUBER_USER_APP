package hr.sil.android.schlauebox.utils

data class ApiError(
    val message: String,
    val error: ErrorInfo,
    val additionalInfo: String,
)

data class ErrorInfo(
    val message: String,
    val name: String,
    val status: Int,
    val type: String
)