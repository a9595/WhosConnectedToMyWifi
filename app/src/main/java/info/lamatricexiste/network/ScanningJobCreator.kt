package info.lamatricexiste.network

import android.util.Log
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import com.evernote.android.job.JobRequest
import java.util.concurrent.TimeUnit

class ScanningJobCreator(private val activityDiscovery: ActivityDiscovery,
                         private val network_ip: Long,
                         private val network_start: Long,
                         private val network_end: Long) : JobCreator {

    override fun create(tag: String?): Job {
        return ScanningJob(network_ip, network_start, network_end, activityDiscovery)
    }

}

class ScanningJob(private val network_ip: Long,
                  private val network_start: Long,
                  private val network_end: Long,
                  private val activityDiscovery: ActivityDiscovery) : Job() {

    override fun onRunJob(p0: Params?): Result {
        executeTask()
        return Result.SUCCESS
    }

    override fun onReschedule(newJobId: Int) {
        executeTask()
    }

    private fun executeTask() {
        val task = DefaultDiscovery(activityDiscovery)
        task.setNetwork(network_ip, network_start, network_end)

        task.onDeviceAdded = { device ->
            Log.d("job", "$device")
        }
        task.execute()
    }

    companion object {
        private val TAG = "scanningService"

        fun scheduleJob() {
            JobRequest.Builder(TAG)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                    .setPersisted(true)
                    .build()
                    .schedule()
        }
    }

}
