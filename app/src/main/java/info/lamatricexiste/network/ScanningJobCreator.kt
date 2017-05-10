package info.lamatricexiste.network

import android.util.Log
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import com.evernote.android.job.JobRequest
import java.util.concurrent.TimeUnit

class ScanningJobCreator(private val task: AbstractDiscovery) : JobCreator {

    override fun create(tag: String?): Job {
        return ScanningJob(task)
    }

}

class ScanningJob(private val task: AbstractDiscovery) : Job() {

    override fun onRunJob(p0: Params?): Result {
        executeTask()
        return Result.RESCHEDULE
    }

    override fun onReschedule(newJobId: Int) {
        executeTask()
    }

    private fun executeTask() {
        task.onDeviceAdded = { device ->
            Log.d("job", "$device")
        }
        task.execute()
    }

    companion object {
        private val TAG = "scanningService"

        fun scheduleJob() {
            JobRequest.Builder(TAG)
                    .setPeriodic(TimeUnit.SECONDS.toMillis(15), TimeUnit.SECONDS.toMillis(5))
                    .setPersisted(true)
                    .build()
                    .schedule()
        }
    }

}
