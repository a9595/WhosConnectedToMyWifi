package info.lamatricexiste.network

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import info.lamatricexiste.network.Network.HostBean
import java.text.SimpleDateFormat
import java.util.*


class FirebaseManager {

    fun pushDevicesList(devicesList: ArrayList<HostBean?>) {
        val devicesObject = DevicesObject(devicesList)

        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference;

        myRef.setValue(devicesObject)
    }

    fun scannedWithoutResult(devicesList: ArrayList<HostBean?>) {
        val devicesObject = DevicesObject(devicesList)
        devicesObject.scannedWithoutResultLastTime = devicesObject.getCurrentLocalDateTimeStamp()

        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference;

        myRef.setValue(devicesObject)
    }

}

data class DevicesObject(public val devicesList: ArrayList<HostBean?>,
                         public val timeStamp: MutableMap<String, String>? = ServerValue.TIMESTAMP) {
    var humanTimestamp: String = getCurrentLocalDateTimeStamp()
    var scannedWithoutResultLastTime: String = getCurrentLocalDateTimeStamp()

    fun getCurrentLocalDateTimeStamp(): String {
        return SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.GERMAN).format(Date())
    }

    init {
        this.humanTimestamp = getCurrentLocalDateTimeStamp()
    }

}
