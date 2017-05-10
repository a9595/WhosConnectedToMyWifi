package info.lamatricexiste.network

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import info.lamatricexiste.network.Network.HostBean

class FirebaseManager {

    fun pushDevicesList(devicesList: ArrayList<HostBean?>) {
        DevicesObject(devicesList)

        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference;

        myRef.setValue(devicesList)
    }

}

data class DevicesObject(private val devicesList: ArrayList<HostBean?>,
                         private val timeStamp: MutableMap<String, String>? = ServerValue.TIMESTAMP) {


}
