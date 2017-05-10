package info.lamatricexiste.network

import android.content.Context
import android.os.AsyncTask
import android.os.Vibrator
import info.lamatricexiste.network.Network.HostBean
import info.lamatricexiste.network.Utils.Prefs
import java.lang.ref.WeakReference

abstract class AbstractDiscovery(discover: ActivityDiscovery) : AsyncTask<Void, HostBean, Void>() {

    //private final String TAG = "AbstractDiscovery";

    protected var hosts_done = 0
    protected val mDiscover: WeakReference<ActivityDiscovery>?

    protected var ip: Long = 0
    protected var start: Long = 0
    protected var end: Long = 0
    protected var size: Long = 0

    public var onDeviceAdded: (HostBean) -> Unit = {}
    public var onFinished = {}

    init {
        mDiscover = WeakReference(discover)
    }

    fun setNetwork(ip: Long, start: Long, end: Long) {
        this.ip = ip
        this.start = start
        this.end = end
    }

    abstract override fun doInBackground(vararg params: Void): Void

    override fun onPreExecute() {
        size = (end - start + 1).toInt().toLong()
        if (mDiscover != null) {
            val discover = mDiscover.get()
            discover?.setProgress(0)
        }
    }

    override fun onProgressUpdate(vararg host: HostBean) {
        if (mDiscover != null) {
            val discover = mDiscover.get()
            if (discover != null) {
                if (!isCancelled) {
                    if (host[0] != null) {
                        discover.addHost(host[0])
                        onDeviceAdded(host[0])
                    }
                    if (size > 0) {
                        discover.setProgress((hosts_done * 10000 / size).toInt())
                    }
                }

            }
        }
    }

    override fun onPostExecute(unused: Void) {
        if (mDiscover != null) {
            val discover = mDiscover.get()
            if (discover != null) {
                if (discover.prefs.getBoolean(Prefs.KEY_VIBRATE_FINISH,
                                              Prefs.DEFAULT_VIBRATE_FINISH)) {
                    val v = discover.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    v.vibrate(ActivityDiscovery.VIBRATE)
                }
                discover.makeToast(R.string.discover_finished)
                discover.stopDiscovering()
                onFinished.invoke()
            }
        }
    }

    override fun onCancelled() {
        if (mDiscover != null) {
            val discover = mDiscover.get()
            if (discover != null) {
                discover.makeToast(R.string.discover_canceled)
                discover.stopDiscovering()
            }
        }
        super.onCancelled()
    }
}
