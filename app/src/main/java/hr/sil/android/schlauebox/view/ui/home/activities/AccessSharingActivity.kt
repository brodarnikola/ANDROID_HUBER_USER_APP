package hr.sil.android.schlauebox.view.ui.home.activities


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.clans.fab.FloatingActionButton
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R
import hr.sil.android.schlauebox.core.remote.WSUser
//import hr.sil.android.schlauebox.cache.DataCache
import hr.sil.android.schlauebox.core.remote.model.*
import hr.sil.android.schlauebox.core.util.logger
import hr.sil.android.schlauebox.core.util.macRealToClean
import hr.sil.android.schlauebox.events.UnauthorizedUserEvent
import hr.sil.android.schlauebox.store.MPLDeviceStore
import hr.sil.android.schlauebox.store.model.MPLDevice
import hr.sil.android.schlauebox.util.backend.UserUtil
import hr.sil.android.schlauebox.view.ui.BaseActivity
import hr.sil.android.schlauebox.view.ui.LoginActivity
import hr.sil.android.schlauebox.view.ui.home.adapters.KeySharingAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class AccessSharingActivity : BaseActivity(R.id.no_ble_layout, R.id.no_internet_layout, R.id.no_location_gps_layout) {

    val log = logger()

    private var macCleanAddress = ""

    private val macReal by lazy {
        intent.getStringExtra("rMacAddress") ?: ""
    }
    private val nameOfDevice by lazy { intent.getStringExtra("nameOfDevice") ?: "" }

    private var userDevice: MPLDevice? = null

    var members = mutableListOf<REndUserGroupMember>()
    val noAccessKeys by lazy {  findViewById<TextView>(R.id.key_sharing_content) }
    val createUser by lazy { findViewById<FloatingActionButton>(R.id.key_sharing_create_button) }

    val recyclerView by lazy { findViewById<RecyclerView>(R.id.main_spl_recycle_view) }

    var finalMembersArray: MutableList<ItemRGroupInfo> = mutableListOf()

    var internetConnection: Boolean = true

    enum class ITEM_TYPES(val typeValue: Int) {
        ITEM_CHILD(2);
    }

    private val ROLE_USER = "USER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_sharing)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        this.setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        userDevice = MPLDeviceStore.devices[macReal]
        macCleanAddress = macReal.macRealToClean()

        createUser.setOnClickListener {
            val startIntent = Intent(this@AccessSharingActivity, AccessSharingAddUserActivity::class.java)
            startIntent.putExtra("rMacAddress", macReal)
            startIntent.putExtra("nameOfDevice", nameOfDevice)

            finish()
            startActivity(startIntent)
        }
    }


    override fun onStart() {
        super.onStart()

        GlobalScope.launch {


            if (internetConnection)
                addOwnerGroupAndAdminGroupToRecylerView()

            val adminOwnerShipGroup: Collection<RGroupInfo> = WSUser.getGroupMemberships() ?: mutableListOf() //DataCache.getGroupMemberships()
            val adminDataList: MutableList<RGroupInfo> = mutableListOf()

            for (items in adminOwnerShipGroup) {
                val usersFromGroup =   WSUser.getGroupMembershipsById(items.groupId.toLong()) ?: mutableListOf()
                //val usersFromGroup: Collection<RGroupInfo> = DataCache.groupMemberships(items.groupId.toLong())
                adminDataList.addAll(usersFromGroup)
            }

            log.info("Members: ${members.size}")
            log.info("Members: ${finalMembersArray.size}")
            withContext(Dispatchers.Main) {

                recyclerView.adapter = KeySharingAdapter(finalMembersArray)
                recyclerView.layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)

                checkVisibilityForFloatingButtonAndRecyclerView()
            }
        }

    }

    private suspend fun addOwnerGroupAndAdminGroupToRecylerView() {

        if (userDevice?.masterUnitType == RMasterUnitType.MPL)
            displayMplDevices()
        else
            dislaySplDevices()
    }

    private suspend fun dislaySplDevices() {

        val ownerResult: MutableList<REndUserGroupMember> = WSUser.getGroupMembers()?.toMutableList() ?: mutableListOf() //DataCache.getGroupMembers().toMutableList()
        var oneOwnerUserFound: Boolean = false
        // First I'm adding all data from owner list to finalMembersArray
        if (ownerResult.isNotEmpty()) {

            val convertOwnerData: MutableList<RGroupDisplayMembersChild> = mutableListOf()

            val userData: REndUserInfo? = UserUtil.user

            for (items in ownerResult) {

                // I'm owner in this group, OWNER CASE
                if (items.master_mac == macCleanAddress && items.groupOwnerId == userData?.id) {

                    oneOwnerUserFound = true

                    val ownerDataObject: RGroupDisplayMembersChild = RGroupDisplayMembersChild()
                    ownerDataObject.groupId = items.groupId
                    ownerDataObject.endUserEmail = items.email
                    ownerDataObject.endUserName = items.name
                    ownerDataObject.role = items.role
                    ownerDataObject.endUserId = items.endUserId
                    ownerDataObject.master_id = items.master_id

                    convertOwnerData.add(ownerDataObject)
                }
            }

            if (oneOwnerUserFound == true) {

                val rGroupInfo: RGroupDisplayMembersHeader = RGroupDisplayMembersHeader()
                rGroupInfo.groupOwnerName = this.getString(R.string.access_sharing_my_group)

                finalMembersArray.add(rGroupInfo)
                finalMembersArray.addAll(convertOwnerData)
            }
        }

        val dataGroupMemberShip = WSUser.getGroupMemberships()?: mutableListOf() // DataCache.getGroupMemberships().toMutableList()

        if (dataGroupMemberShip.isNotEmpty()) {

            var oneAdminUserFound = false
            var addOnlyOneTimeHeader = 0

            val rGroupInfo = RGroupDisplayMembersHeader()
            rGroupInfo.groupOwnerName = this.getString(R.string.access_sharing_other_group)

            for (items in dataGroupMemberShip) {

                if (items.role == ROLE_USER && items.master_mac == macCleanAddress) {

                    if (addOnlyOneTimeHeader == 0) {

                        finalMembersArray.add(rGroupInfo)
                        addOnlyOneTimeHeader = 1
                    }
                    val nameOfAdminGroup = RGroupDisplayMembersAdmin()
                    nameOfAdminGroup.groupOwnerName = items.groupName
                    nameOfAdminGroup.role = items.role

                    finalMembersArray.add(nameOfAdminGroup)
                }
                else if (items.master_mac == macCleanAddress) {
                    val groupDataList: Collection<RGroupInfo> =  WSUser.getGroupMembershipsById(items.groupId.toLong()) ?: mutableListOf() // DataCache.groupMemberships(items.groupId.toLong())

                    if (groupDataList.size > 0) {

                        val groupMembersData: MutableList<RGroupDisplayMembersChild> = mutableListOf()
                        val nameOfAdminGroup: RGroupDisplayMembersAdmin = RGroupDisplayMembersAdmin()
                        nameOfAdminGroup.groupOwnerName = items.groupName

                        for (subItems in groupDataList) {

                            if (ownerResult.isNotEmpty()) {
                                val firstMember = ownerResult[0]
                                firstMember.let {
                                    if (subItems.master_mac == macCleanAddress && subItems.endUserId != firstMember.groupOwnerId) {
                                        oneAdminUserFound = true
                                        val groupDataObject: RGroupDisplayMembersChild = RGroupDisplayMembersChild()
                                        groupDataObject.groupId = subItems.groupId
                                        groupDataObject.groupOwnerEmail = subItems.groupOwnerEmail
                                        groupDataObject.endUserName = subItems.endUserName
                                        groupDataObject.endUserEmail = subItems.endUserEmail
                                        groupDataObject.role = subItems.role
                                        groupDataObject.endUserId = subItems.endUserId
                                        groupDataObject.master_id = subItems.master_id
                                        groupMembersData.add(groupDataObject)
                                    } else if (subItems.master_mac == nameOfDevice) {

                                        oneAdminUserFound = true

                                    } else {

                                    }
                                }
                            } else {
                                if (subItems.master_mac == macCleanAddress && UserUtil.user?.id != subItems.endUserId) {
                                    oneAdminUserFound = true
                                    val groupDataObject: RGroupDisplayMembersChild = RGroupDisplayMembersChild()
                                    groupDataObject.groupId = subItems.groupId
                                    groupDataObject.groupOwnerEmail = subItems.groupOwnerEmail
                                    groupDataObject.endUserName = subItems.endUserName
                                    groupDataObject.endUserEmail = subItems.endUserEmail
                                    groupDataObject.role = subItems.role
                                    groupDataObject.endUserId = subItems.endUserId
                                    groupDataObject.master_id = subItems.master_id
                                    groupMembersData.add(groupDataObject)
                                }
                            }
                        }


                        if (addOnlyOneTimeHeader == 0 && oneAdminUserFound) {
                            finalMembersArray.add(rGroupInfo)
                            addOnlyOneTimeHeader = 1
                        }

                        if (oneAdminUserFound) {
                            finalMembersArray.add(nameOfAdminGroup)
                            if (groupMembersData.size > 0)
                                finalMembersArray.addAll(groupMembersData)
                            oneAdminUserFound = false
                        }
                    }
                }
            }
        }
    }

    private suspend fun displayMplDevices() {

        val ownerResult: MutableList<REndUserGroupMember> =  WSUser.getGroupMembers()?.toMutableList() ?: mutableListOf() // DataCache.getGroupMembers().toMutableList()
        var oneOwnerUserFound: Boolean = false
        // First I'm adding all data from owner list to finalMembersArray
        if (ownerResult.isNotEmpty()) {

            val convertOwnerData: MutableList<RGroupDisplayMembersChild> = mutableListOf() 

            for (items in ownerResult) {

                if (items.master_mac == macCleanAddress) {

                    oneOwnerUserFound = true

                    val ownerDataObject: RGroupDisplayMembersChild = RGroupDisplayMembersChild()
                    ownerDataObject.groupId = items.groupId
                    ownerDataObject.endUserEmail = items.email
                    ownerDataObject.endUserName = items.name
                    ownerDataObject.role = items.role
                    ownerDataObject.endUserId = items.endUserId
                    ownerDataObject.master_id = items.master_id

                    convertOwnerData.add(ownerDataObject)
                }
            }

            if (oneOwnerUserFound == true) {

                val rGroupInfo: RGroupDisplayMembersHeader = RGroupDisplayMembersHeader()
                rGroupInfo.groupOwnerName = this.getString(R.string.access_sharing_my_group)

                finalMembersArray.add(rGroupInfo)
                finalMembersArray.addAll(convertOwnerData)
            }
        }

        val dataGroupMemberShip = WSUser.getGroupMemberships()?: mutableListOf() //DataCache.getGroupMemberships().toMutableList()

        if (dataGroupMemberShip.isNotEmpty()) {

            var oneAdminUserFound: Boolean = false
            var addOnlyOneTimeHeader: Int = 0

            val rGroupInfo: RGroupDisplayMembersHeader = RGroupDisplayMembersHeader()
            rGroupInfo.groupOwnerName = this.getString(R.string.access_sharing_other_group)

            for (items in dataGroupMemberShip) {

                if (items.role == ROLE_USER && items.master_mac == macCleanAddress) {

                    if (addOnlyOneTimeHeader == 0) {

                        finalMembersArray.add(rGroupInfo)
                        addOnlyOneTimeHeader = 1
                    }
                    val nameOfAdminGroup = RGroupDisplayMembersAdmin()
                    nameOfAdminGroup.groupOwnerName = items.groupName
                    nameOfAdminGroup.role = items.role

                    finalMembersArray.add(nameOfAdminGroup)
                }
                else if( items.master_mac == macCleanAddress ) {
                    val groupDataList: Collection<RGroupInfo> = WSUser.getGroupMembershipsById(items.groupId.toLong()) ?: mutableListOf() //DataCache.groupMemberships(items.groupId.toLong())
                    if (groupDataList.size > 0) {
                        val groupMembersData: MutableList<RGroupDisplayMembersChild> = mutableListOf()
                        val nameOfAdminGroup = RGroupDisplayMembersAdmin()
                        nameOfAdminGroup.groupOwnerName = items.groupName

                        for (subItems in groupDataList) {

                            if (ownerResult.isNotEmpty()) {
                                val firstMember = ownerResult[0]
                                firstMember.let {
                                    if (subItems.master_mac == macCleanAddress && subItems.endUserId != firstMember.groupOwnerId) {
                                        oneAdminUserFound = true
                                        val groupDataObject: RGroupDisplayMembersChild = RGroupDisplayMembersChild()
                                        groupDataObject.groupId = subItems.groupId
                                        groupDataObject.groupOwnerEmail = subItems.groupOwnerEmail
                                        groupDataObject.endUserName = subItems.endUserName
                                        groupDataObject.endUserEmail = subItems.endUserEmail
                                        groupDataObject.role = subItems.role
                                        groupDataObject.endUserId = subItems.endUserId
                                        groupDataObject.master_id = subItems.master_id
                                        groupMembersData.add(groupDataObject)
                                    } else if (subItems.master_mac == macCleanAddress) {

                                        oneAdminUserFound = true

                                    } else {

                                    }
                                }
                            } else {
                                if (subItems.master_mac == macCleanAddress && UserUtil.user?.id != subItems.endUserId) {
                                    oneAdminUserFound = true
                                    val groupDataObject: RGroupDisplayMembersChild = RGroupDisplayMembersChild()
                                    groupDataObject.groupId = subItems.groupId
                                    groupDataObject.groupOwnerEmail = subItems.groupOwnerEmail
                                    groupDataObject.endUserName = subItems.endUserName
                                    groupDataObject.endUserEmail = subItems.endUserEmail
                                    groupDataObject.role = subItems.role
                                    groupDataObject.endUserId = subItems.endUserId
                                    groupDataObject.master_id = subItems.master_id
                                    groupMembersData.add(groupDataObject)
                                }
                            }
                        }

                        if (addOnlyOneTimeHeader == 0 && oneAdminUserFound) {

                            finalMembersArray.add(rGroupInfo)
                            addOnlyOneTimeHeader = 1
                        }

                        if (oneAdminUserFound) {

                            finalMembersArray.add(nameOfAdminGroup)
                            if (groupMembersData.size > 0)
                                finalMembersArray.addAll(groupMembersData)
                            oneAdminUserFound = false
                        }
                    }
                }
            }
        }
    }

    override fun onNetworkStateUpdated(available: Boolean) {
        super.onNetworkStateUpdated(available)
        networkAvailable = available
        internetConnection = available
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

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: UnauthorizedUserEvent) {
        log.info("Received unauthorized event, user will now be log outed")
        val intent = Intent( this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        finalMembersArray.clear()
        App.ref.eventBus.unregister(this)
    }

    fun checkVisibilityForFloatingButtonAndRecyclerView() {
        if (finalMembersArray.size == 0) {
            noAccessKeys.visibility = View.VISIBLE
        } else {
            noAccessKeys.visibility = View.GONE
        }

        val hasRightsToShareAccess = userDevice?.hasRightsToShareAccess()

        val groupMemberShip = UserUtil.userMemberships.filter { it.master_id == userDevice?.masterUnitId && it.role == RUserAccessRole.ADMIN.name }
        if( groupMemberShip.size > 0 || ( hasRightsToShareAccess != null && hasRightsToShareAccess ) ) {
            createUser.visibility = View.VISIBLE
        }
        else {
            createUser.visibility = View.GONE
        }
    }

}