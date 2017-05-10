package info.lamatricexiste.network.Scanning

import android.os.AsyncTask
import info.lamatricexiste.network.Network.HostBean

abstract class AbstractDiscovery : AsyncTask<Void?, HostBean?, Void?>() {

    //private final String TAG = "AbstractDiscovery";

    protected var hosts_done = 0

    protected var ip: Long = 0
    protected var start: Long = 0
    protected var end: Long = 0
    protected var size: Long = 0

    var onDeviceAdded: (HostBean?) -> Unit = {}
    var onFinished = {}
    var onScannedWithoutResult = {}

    fun setNetwork(ip: Long, start: Long, end: Long) {
        this.ip = ip
        this.start = start
        this.end = end
    }

    abstract override fun doInBackground(vararg params: Void?): Void

    override fun onPreExecute() {
        size = (end - start + 1).toInt().toLong()
    }

    override fun onProgressUpdate(vararg host: HostBean?) {
        if (isCancelled) return

        host[0]?.let {
            onDeviceAdded(it)
        }
        if (size > 0) {
            val progress = (hosts_done * 10000 / size).toInt()
        }
    }

    override fun onPostExecute(unused: Void?) {
        onFinished.invoke()
    }

    override fun onCancelled() {
        super.onCancelled()
    }
}
