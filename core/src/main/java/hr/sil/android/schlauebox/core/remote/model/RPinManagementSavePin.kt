package hr.sil.android.schlauebox.core.remote.model

import com.google.gson.annotations.SerializedName

class RPinManagementSavePin {

    @SerializedName("group___id")
    var groupId: Int? = null


    @SerializedName("master___id")
    var masterId: Int? = 0

    @SerializedName("pin")
    var pin: String = ""

    @SerializedName("name")
    var name: String = ""

}