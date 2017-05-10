package info.lamatricexiste.network

import android.util.Log
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import com.evernote.android.job.JobRequest
import info.lamatricexiste.network.Network.HostBean
import info.lamatricexiste.network.Scanning.DefaultDiscovery
import java.util.concurrent.TimeUnit

class ScanningJobCreator(private val network_ip: Long,
                         private val network_start: Long,
                         private val network_end: Long,
                         private val gatewayIp: String) : JobCreator {

    override fun create(tag: String?): Job {
        return ScanningJob(network_ip, network_start, network_end, gatewayIp)
    }

}

class ScanningJob(private val network_ip: Long,
                  private val network_start: Long,
                  private val network_end: Long,
                  private val gatewayIp: String) : Job() {

    override fun onRunJob(p0: Params?): Result {
        executeTask()
        return Result.RESCHEDULE
    }


    override fun onReschedule(newJobId: Int) {
        executeTask()
    }

    private val devicesList = ArrayList<HostBean?>()

    private fun executeTask() {
        val task = DefaultDiscovery(gatewayIp)
        task.setNetwork(network_ip, network_start, network_end)

        task.onDeviceAdded = { device ->
            FirebaseManager().pushDevicesList(devicesList)
            devicesList.add(device)
            Log.d("DEVICEEEEEE: ", "$device")
        }
        task.onFinished = {
            FirebaseManager().pushDevicesList(devicesList)
        }
        task.onScannedWithoutResult = {
            FirebaseManager().scannedWithoutResult(devicesList)
        }
        task.execute().get()
    }

    companion object {
        private val TAG = "scanningService"

        fun scheduleJob() {
            JobRequest.Builder(TAG)
                    .setPeriodic(900000)
                    .setPersisted(true)
                    .build()
                    .schedule()
        }
    }

}

