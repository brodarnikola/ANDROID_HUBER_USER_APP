package hr.sil.android.schlauebox.fcm

//import com.firebase.jobdispatcher.FirebaseJobDispatcher
//import com.firebase.jobdispatcher.GooglePlayDriver
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.util.AppUtil
import hr.sil.android.schlauebox.util.NotificationHelper
import hr.sil.android.schlauebox.view.ui.MainActivity1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


open class MPLFireBaseMessagingService : FirebaseMessagingService() {
    val log = logger()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        log.info("From: " + remoteMessage.from!!)
        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            log.info("Message data payload: " + remoteMessage.data)

            if (/* Check if data needs to be processed by long running job */ false) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow(remoteMessage.data)
            }

        }
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            log.info("Message Notification Body: " + remoteMessage.notification!!.body!!)
        }
    }

    override fun onNewToken(token: String) {
        log.info("Refreshed token: $token")
        GlobalScope.launch(Dispatchers.Default){
            if (!sendRegistrationToServer(token)) {
                log.error("Error in registration to server please check your internet connection")
            }

        }
    }



    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
//        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
//        val myJob = dispatcher.newJobBuilder()
//                .setService(NotificationJobService::class.java)
//                .setTag("my-job-tag")
//                .build()
//        dispatcher.schedule(myJob)
        // [END dispatch_job]
    }

    private fun handleNow(result: Map<String, String>) {
        val type = result["type"]?:""
        log.info("Push notification type is: ${type} .. is it true: ${type=="DEFAULT"}")
        if(type=="DEFAULT"){
            NotificationHelper.createNotification(result["subject"], result["body"], MainActivity1::class.java)
        }

        GlobalScope.launch {
            AppUtil.refreshCache()
        }
        log.info("Short task when notification is opened is done")
    }

    companion object {
        suspend fun sendRegistrationToServer(token: String): Boolean {
            return WSUser.registerDevice(token)
        }
    }
}