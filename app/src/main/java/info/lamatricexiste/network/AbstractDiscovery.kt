package info.lamatricexiste.network

import android.os.AsyncTask
import info.lamatricexiste.network.Network.HostBean

abstract class AbstractDiscovery() : AsyncTask<Void, HostBean, Void>() {

    //private final String TAG = "AbstractDiscovery";

    protected var hosts_done = 0

    protected var ip: Long = 0
    protected var start: Long = 0
    protected var end: Long = 0
    protected var size: Long = 0

    public var onDeviceAdded: (HostBean) -> Unit = {}
    public var onFinished = {}

    fun setNetwork(ip: Long, start: Long, end: Long) {
        this.ip = ip
        this.start = start
        this.end = end
    }

    abstract override fun doInBackground(vararg params: Void): Void

    override fun onPreExecute() {
        size = (end - start + 1).toInt().toLong()
    }

    override fun onProgressUpdate(vararg host: HostBean?) {
        if (!isCancelled) {
            host[0]?.let {
                onDeviceAdded(it)
            }
            if (size > 0) {
                //                discover.setProgress((hosts_done * 10000 / size).toInt())
            }
        }
    }

    override fun onPostExecute(unused: Void) {
        onFinished.invoke()
    }

    override fun onCancelled() {
        super.onCancelled()
    }
}
