//package com.example.mvi_compose.general.repositories
//
//import android.util.Log
//import hr.sil.android.schlauebox.BuildConfig
//import hr.sil.android.schlauebox.core.remote.model.UserStatus
//import hr.sil.android.schlauebox.utils.ApiError
//import hr.sil.android.schlauebox.utils.NetworkResult
//import kotlinx.coroutines.CoroutineDispatcher
//import kotlinx.coroutines.withContext
//import retrofit2.HttpException
//import retrofit2.Response
//import java.io.IOException
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class AppRepoImpl @Inject constructor(
//    @MovieNetwork private val movieApi: MovieApi,
//    @MovieNetwork private val moshi: Moshi,
//    @IODispatcher private val ioDispatcher: CoroutineDispatcher
//) : AppRepo {
//
////    override suspend fun getMovieById(movieId: Long): Movie = withContext(ioDispatcher) {
////        return@withContext movieDao.getMovieById(movieId)
////    }
//
//    override suspend fun loginCheckUserStatus(): NetworkResult<UserStatus> = withContext(ioDispatcher) {
////
//
//        val networkResult = handleNetworkRequest {
//            Log.d( "TAG","start popular moview")
//            movieApi.getMostPopular(BuildConfig.API_KEY)
//        }
//
//        if( networkResult is NetworkResult.Success ) {
//            Log.d( "TAG","get general succes")
//        }
//
//        return@withContext networkResult
//    }
//
//    private suspend fun <T : Any> handleNetworkRequest(apiCall: suspend () -> Response<T>): NetworkResult<T> {
//        return try {
//            val response: Response<T> = apiCall.invoke()
//
//            if (response.isSuccessful && response.code() == 200 && response.body() != null) {
//                NetworkResult.Success(response.body()!!)
//            } else {
//                val errorBody = response.errorBody()?.string()
//                var apiError: ApiError? = null
//                if (errorBody != null) {
//                    try {
//                        val adapter = moshi.adapter(ApiError::class.java)
//                        apiError = adapter.fromJson(errorBody)
//                    } catch (e: Exception) {
//                        Log.e("Error","handleNetworkRequest error: ${e.localizedMessage}")
//                    }
//                }
//                NetworkResult.Error(
//                    code = response.code(),
//                    message = response.message(),
//                    apiError = apiError
//                )
//            }
//        } catch (e: HttpException) {
//            Log.e("NETWORK_HTTP_ERROR","Network request error - HttpException: ${e.localizedMessage}")
//            NetworkResult.Error(
//                code = e.code(),
//                message = e.message(),
//                apiError = null
//            )
//        } catch (e: IOException) {
//            Log.e("NETWORK_IOEXCEPTION_ERROR","Network request error - IOException: ${e.localizedMessage}")
//            NetworkResult.Exception(e)
//        }
//    }
//
//}