package hr.sil.android.schlauebox.view.ui.home.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
//import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.remote.WSUser
import hr.sil.android.schlauebox.core.remote.model.RGroupInfo
import hr.sil.android.schlauebox.core.remote.model.RUserAccess
import hr.sil.android.schlauebox.core.remote.model.RUserAccessRole
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.data.PhoneContact
import hr.sil.android.schlauebox.data.UserGroup
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.dialog.ShareAppDialog
import hr.sil.android.schlauebox.view.ui.home.adapters.GroupAdapter
import hr.sil.android.schlauebox.view.ui.home.adapters.GroupsAdapter
import hr.sil.android.schlauebox.view.ui.util.ActivityForResultWrapper
import hr.sil.android.view_util.permission.DroidPermission
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class AccessSharingAddUserActivity : BaseActivity(R.id.no_ble_layout, R.id.no_internet_layout, R.id.no_location_gps_layout) {

    private val log = logger()
    private val groupSelection by lazy { findViewById<Spinner>(R.id.access_sharing_spinner) }
    private val groupsMemberShipSelection by lazy { findViewById<Spinner>(R.id.key_sharing_groups) }

    private val PROJECTION = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email.DATA)
    lateinit var selectedItem: UserGroup
    lateinit var selectedItemRGroupInfo: RGroupInfo
    private val droidPermission by lazy { DroidPermission.init(this) }
    //override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = droidPermission.link(requestCode, permissions, grantResults)
    private val activityForResultWrapper = ActivityForResultWrapper()
    private val createKey by lazy { findViewById<Button>(R.id.key_sharing_button) }
    private val emailEditText by lazy { findViewById<EditText>(R.id.key_sharing_new_user_email) }

    private val masterMacAddress by lazy { intent.getStringExtra("rMacAddress") ?: "" }

    private val nameOfDevice by lazy { intent.getStringExtra("nameOfDevice") ?: "" }
    private lateinit var finalGroupMemberShip: MutableList<RGroupInfo>

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        activityForResultWrapper.onActivityResult(requestCode, resultCode, data)
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                //TODO we should handle this one in different way

                val startIntent = Intent(this@AccessSharingAddUserActivity, AccessSharingActivity::class.java)
                startIntent.putExtra("rMacAddress", masterMacAddress)
                startIntent.putExtra("nameOfDevice", nameOfDevice)
                startActivity(startIntent)
                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
    //TODO we should handle this one in different way, custom implementation of back button is antipattern
    override fun onBackPressed() {
        val startIntent = Intent(this@AccessSharingAddUserActivity, AccessSharingActivity::class.java)
        startIntent.putExtra("rMacAddress", masterMacAddress)
        startIntent.putExtra("nameOfDevice", nameOfDevice)
        startActivity(startIntent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
    }

    override fun onPause() {
        super.onPause()
        App.ref.eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: UnauthorizedUserEvent) {
        log.info("Received unauthorized event, user will now be log outed")
        val intent = Intent( this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_sharing_add)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //val emailTextView = findViewById<TextView>(R.id.key_sharing_new_user_email)

        val contactLink = findViewById<TextView>(R.id.key_sharing_new_user_email_contacts)
        contactLink.setOnClickListener {

            GlobalScope.launch(Dispatchers.Main) {
                val phoneContact = askForEmailFromContacts()
                //emailTextView.text = phoneContact?.email ?: ""
            }
        }

        groupsMemberShipSelection.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(
                            adapterView: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                    ) {

                        selectedItemRGroupInfo = adapterView?.getItemAtPosition(position) as RGroupInfo
                    }
                }

        groupSelection.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(
                            adapterView: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                    ) {
                        selectedItem = adapterView?.getItemAtPosition(position) as UserGroup
                    }
                }

        val log = logger()
        createKey.setOnClickListener {


            val correctMasterId = MPLDeviceStore.devices[masterMacAddress]?.masterUnitId as Int
            var rGroupInfoId: RGroupInfo? = RGroupInfo()

            val userAccess = RUserAccess().apply {

                val selectedGroupInfo: RGroupInfo = groupsMemberShipSelection.getSelectedItem() as RGroupInfo

                rGroupInfoId = finalGroupMemberShip.find{ rGroupInfo -> rGroupInfo.groupName == selectedGroupInfo.groupName }

                this.groupId = rGroupInfoId?.groupId
                //this.groupUserEmail = emailTextView.text.toString()
                this.role = selectedItem.value.name
                this.masterId = correctMasterId
            }
            if (userAccess.groupUserEmail.isNotEmpty()) {

                GlobalScope.launch {
                    val result = WSUser.addUserAccess(userAccess)

                    if (result) {

                        // add new user to admin data cache
                        if (UserUtil.userGroup?.name != rGroupInfoId?.groupOwnerName) {

                            userAccess.groupId?.toLong()?.let { it ->
                                WSUser.getGroupMembershipsById(it) ?: mutableListOf()
                                //DataCache.groupMemberships(it, true)
                            }
                        }
                        // add new user to owner data cache
                        else {
                            WSUser.getGroupMembers() ?: mutableListOf()
                            //DataCache.getGroupMembers(true)
                        }

                        withContext(Dispatchers.Main) {
                            log.info("Successfully add user ${userAccess.groupUserEmail} to group")
                            //App.ref.toast(ctx.getString(R.string.app_generic_success))
                            val startIntent = Intent(this@AccessSharingAddUserActivity, AccessSharingActivity::class.java)
                            startIntent.putExtra("rMacAddress", masterMacAddress)
                            startIntent.putExtra("nameOfDevice", nameOfDevice)
                            startActivity(startIntent)
                            finish()
                        }
                    } else {
                        withContext(Dispatchers.Main) {

                            val shareAppDialog = ShareAppDialog(userAccess.groupUserEmail)
                            shareAppDialog.show( supportFragmentManager, "" )
                            //activity?.supportFragmentManager?.let { it -> generatedPinDialog.show(it, "") }
                            /*DialogUtil.messageDialogBuilder(ctx, ctx.getString(R.string.grant_access_error)) {
                                val appLink = BuildConfig.APP_ANDR_DOWNLOAD_URL
                                val iOSLink = BuildConfig.APP_IOS_DOWNLOAD_URL

                                val shareBodyText = ctx.getString(hr.sil.android.schlauebox.R.string.access_sharing_share_app_text, appLink, iOSLink)
                                val emailIntent = Intent(Intent.ACTION_SEND)
                                emailIntent.setType("message/rfc822")
                                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(userAccess.groupUserEmail))
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject/Title")
                                emailIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText)

                                startActivity(Intent.createChooser(emailIntent, ctx.getString(hr.sil.android.schlauebox.R.string.access_sharing_share_choose_sharing)))
                            }.show()*/
                        }
                    }
                }
            } else {
                validateNewEmail(emailEditText)
            }
        }

    }


    private fun validateNewEmail(emailTextView: EditText): Boolean {
        return validateEditText(null,emailTextView) { newEmail ->
            when {
                newEmail.isBlank() -> ValidationResult.INVALID_PASSWORD_BLANK
                else -> ValidationResult.VALID
            }
        }
    }

    override fun onNetworkStateUpdated(available: Boolean) {
        super.onNetworkStateUpdated(available)
        networkAvailable = available
        updateUI()
    }

    override fun onBluetoothStateUpdated(available: Boolean) {
        super.onBluetoothStateUpdated(available)
        bluetoothAvalilable = available
        updateUI()
    }

    override fun onLocationGPSStateUpdated(available: Boolean) {
        super.onLocationGPSStateUpdated(available)
        locationGPSAvalilable = available
        updateUI()
    }

    override fun onStart() {
        super.onStart()
        val userGroup = UserUtil.userGroup ?: return
        //val groups = listOf<UserGroup>(UserGroup(1, ctx.getString(R.string.access_sharing_admin_role), RUserAccessRole.ADMIN),
        //        UserGroup(1, ctx.getString(R.string.access_sharing_user_role), RUserAccessRole.USER))
        val deviceId = MPLDeviceStore.devices[masterMacAddress]?.masterUnitId
        val groupMemberShip = UserUtil.userMemberships.filter { it.master_id == deviceId && it.role == RUserAccessRole.ADMIN.name }

        val userData = RGroupInfo()
        userData.groupId = userGroup.id
        userData.groupOwnerName = userGroup.name

        finalGroupMemberShip = mutableListOf<RGroupInfo>()

        finalGroupMemberShip.add(userData)
        finalGroupMemberShip.addAll(groupMemberShip)

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                //groupSelection.adapter = GroupAdapter(groups)
                groupsMemberShipSelection.adapter = GroupsAdapter(finalGroupMemberShip)
            }
        }
    }


    private fun requestReadContactsPermission(): Deferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        droidPermission
                .request(Manifest.permission.READ_CONTACTS)
                .done { _, deniedPermissions ->
                    if (deniedPermissions.isNotEmpty()) {
                        log.info("Permissions were denied!")
                        deferred.complete(false)
                    } else {
                        log.info("Permissions granted!")
                        deferred.complete(true)
                    }
                }
                .execute()
        return deferred
    }

    private suspend fun askForEmailFromContacts(): PhoneContact? {
        var emailResult: String?
        var nameResult: String?
        var contact: PhoneContact? = null

        if (!requestReadContactsPermission().await()) {
            //this@AccessSharingAddUserActivity.alert("No address")
        } else {
            val uri = selectFromContacts().await()?.data
            if (uri != null) {
                try {
                    this.contentResolver.query(uri, PROJECTION, null, null, null).use { cursor ->
                        if (cursor != null && cursor.moveToFirst()) {
                            // get the contact's information
                            //nameResult = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                            //emailResult = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))

                            // if the user user has an roleText or phone then add it to contacts
//                            if ((!TextUtils.isEmpty(emailResult) && android.util.Patterns.EMAIL_ADDRESS.matcher(emailResult!!).matches() /* && !emailResult.equals(nameResult)) */ )) {
//                                contact = PhoneContact(nameResult, emailResult)
//                            }
                        }
                    }
                } catch (ex: Exception) {
                    //ignore
                }
            }
        }
        return contact
    }

    private fun selectFromContacts(): Deferred<Intent?> {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = ContactsContract.CommonDataKinds.Email.CONTENT_TYPE
        return activityForResultWrapper.call(this, intent)
    }

}
