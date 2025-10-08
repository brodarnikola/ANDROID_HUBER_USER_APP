package hr.sil.android.schlauebox.data

interface CancelPickAtHomeInterface {

    fun cancelPickAtHomeInterface( lockerMac: String, lockerMasterMac: String, lockerId: Int, lockerKeyId: Int, userId: Int)
}