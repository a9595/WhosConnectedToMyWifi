/*
 * Copyright (C) 2009-2010 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package info.lamatricexiste.network

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.*
import com.evernote.android.job.JobManager
import info.lamatricexiste.network.Network.HostBean
import info.lamatricexiste.network.Network.NetInfo
import info.lamatricexiste.network.Utils.Export
import info.lamatricexiste.network.Utils.Help
import info.lamatricexiste.network.Utils.Prefs
import java.util.*

class ActivityDiscovery : ActivityNet() {

    private val TAG = "ActivityDiscovery"
    private var currentNetwork = 0
    private var network_ip: Long = 0
    private var network_start: Long = 0
    private var network_end: Long = 0
    var hosts: MutableList<HostBean>? = null
    private var adapter: HostsAdapter? = null
    private var btn_discover: Button? = null
    private var mDiscoveryTask: AbstractDiscovery? = null

    // private SlidingDrawer mDrawer;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_PROGRESS)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.discovery)
        initViews()

        //        initJobs()
    }

    private fun initJobs() {
        initDiscovering()
        JobManager.create(this@ActivityDiscovery).addJobCreator(ScanningJobCreator(network_ip,
                                                                                   network_start,
                                                                                   network_end,
                                                                                   net.gatewayIp))
//        ScanningJob.scheduleJob()
    }

    private fun initViews() {
        mInflater = LayoutInflater.from(ctxt)

        // Discover
        btn_discover = findViewById(R.id.btn_discover) as Button
        btn_discover!!.setOnClickListener { startDiscovering() }

        // Options
        val btn_options = findViewById(R.id.btn_options) as Button
        btn_options.setOnClickListener { startActivity(Intent(ctxt, Prefs::class.java)) }

        // Hosts list
        adapter = HostsAdapter(this, this)
        val list = findViewById(R.id.output) as ListView
        list.adapter = adapter
        list.itemsCanFocus = false
        list.emptyView = findViewById(R.id.list_empty)
        initList()
    }


    /**
     * Discover hosts
     */
    private fun startDiscovering() {
        initDiscovering()
        mDiscoveryTask!!.execute()
        btn_discover!!.setText(R.string.btn_discover_cancel)
        setButton(btn_discover, R.drawable.cancel, false)
        btn_discover!!.setOnClickListener { cancelTasks() }
        makeToast(R.string.discover_start)
        setProgressBarVisibility(true)
        setProgressBarIndeterminateVisibility(true)
    }

    private fun initDiscovering() {
        var method = 0
        try {
            method = Integer.parseInt(prefs.getString(Prefs.KEY_METHOD_DISCOVER,
                                                      Prefs.DEFAULT_METHOD_DISCOVER))
        } catch (e: NumberFormatException) {
            Log.e(TAG, e.message)
        }

        mDiscoveryTask = DefaultDiscovery(net.gatewayIp)
        mDiscoveryTask!!.setNetwork(network_ip, network_start, network_end)
    }

    fun stopDiscovering() {
        Log.e(TAG, "stopDiscovering()")
        mDiscoveryTask = null
        setButtonOn(btn_discover, R.drawable.discover)
        btn_discover!!.setOnClickListener { startDiscovering() }
        setProgressBarVisibility(false)
        setProgressBarIndeterminateVisibility(false)
        btn_discover!!.setText(R.string.btn_discover)
    }

    private fun initList() {
        // setSelectedHosts(false);
        adapter!!.clear()
        hosts = ArrayList<HostBean>()
    }

    fun addHost(host: HostBean) {
        host.position = hosts!!.size
        hosts?.add(host)
        adapter?.add(null)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, ActivityDiscovery.MENU_SCAN_SINGLE, 0, R.string.scan_single_title).setIcon(
                android.R.drawable.ic_menu_mylocation)
        menu.add(0, ActivityDiscovery.MENU_EXPORT, 0, R.string.preferences_export).setIcon(
                android.R.drawable.ic_menu_save)
        menu.add(0, ActivityDiscovery.MENU_OPTIONS, 0, R.string.btn_options).setIcon(
                android.R.drawable.ic_menu_preferences)
        menu.add(0, ActivityDiscovery.MENU_HELP, 0, R.string.preferences_help).setIcon(
                android.R.drawable.ic_menu_help)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            ActivityDiscovery.MENU_SCAN_SINGLE -> {
                //                scanSingle(this, null)
                return true
            }
            ActivityDiscovery.MENU_OPTIONS -> {
                startActivity(Intent(ctxt, Prefs::class.java))
                return true
            }
            ActivityDiscovery.MENU_HELP -> {
                startActivity(Intent(ctxt, Help::class.java))
                return true
            }
            ActivityDiscovery.MENU_EXPORT -> {
                export()
                return true
            }
        }
        return false
    }

    override fun setInfo() {
        // Info
        (findViewById(R.id.info_ip) as TextView).text = info_ip_str
        (findViewById(R.id.info_in) as TextView).text = info_in_str
        (findViewById(R.id.info_mo) as TextView).text = info_mo_str

        // Scan button state
        if (mDiscoveryTask != null) {
            setButton(btn_discover!!, R.drawable.cancel, false)
            btn_discover!!.setText(R.string.btn_discover_cancel)
            btn_discover!!.setOnClickListener { cancelTasks() }
        }

        if (currentNetwork != net.hashCode()) {
            Log.i(TAG, "Network info has changed")
            currentNetwork = net.hashCode()

            // Cancel running tasks
            cancelTasks()
        } else {
            return
        }

        // Get ip information
        network_ip = NetInfo.getUnsignedLongFromIp(net.ip)
        if (prefs.getBoolean(Prefs.KEY_IP_CUSTOM, Prefs.DEFAULT_IP_CUSTOM)) {
            // Custom IP
            network_start = NetInfo.getUnsignedLongFromIp(prefs.getString(Prefs.KEY_IP_START,
                                                                          Prefs.DEFAULT_IP_START))
            network_end = NetInfo.getUnsignedLongFromIp(prefs.getString(Prefs.KEY_IP_END,
                                                                        Prefs.DEFAULT_IP_END))
        } else {
            // Custom CIDR
            if (prefs.getBoolean(Prefs.KEY_CIDR_CUSTOM, Prefs.DEFAULT_CIDR_CUSTOM)) {
                net.cidr = Integer.parseInt(prefs.getString(Prefs.KEY_CIDR, Prefs.DEFAULT_CIDR))
            }
            // Detected IP
            val shift = 32 - net.cidr
            if (net.cidr < 31) {
                network_start = (network_ip shr shift shl shift) + 1
                network_end = (network_start or ((1 shl shift) - 1).toLong()) - 1
            } else {
                network_start = network_ip shr shift shl shift
                network_end = network_start or ((1 shl shift) - 1).toLong()
            }
            // Reset ip start-end (is it really convenient ?)
            val edit = prefs.edit()
            edit.putString(Prefs.KEY_IP_START, NetInfo.getIpFromLongUnsigned(network_start))
            edit.putString(Prefs.KEY_IP_END, NetInfo.getIpFromLongUnsigned(network_end))
            edit.commit()
        }

        initJobs()
    }

    override fun setButtons(disable: Boolean) {
        if (disable) {
            setButtonOff(btn_discover, R.drawable.disabled)
        } else {
            setButtonOn(btn_discover, R.drawable.discover)
        }
    }

    override fun cancelTasks() {
        if (mDiscoveryTask != null) {
            mDiscoveryTask!!.cancel(true)
            mDiscoveryTask = null
        }
    }

    // Listen for Activity results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SCAN_PORT_RESULT -> if (resultCode == Activity.RESULT_OK) {
                // Get scanned ports
                if (data != null && data.hasExtra(HostBean.EXTRA)) {
                    val host = data.getParcelableExtra<HostBean>(HostBean.EXTRA)
                    if (host != null) {
                        hosts!![host.position] = host
                    }
                }
            }
            else -> {
            }
        }
    }

    internal class ViewHolder {
        var host: TextView? = null
        var mac: TextView? = null
        var vendor: TextView? = null
        var logo: ImageView? = null
    }

    private fun export() {
        val e = Export(ctxt, hosts)
        val file = e.fileName

        val v = mInflater!!.inflate(R.layout.dialog_edittext, null)
        val txt = v.findViewById(R.id.edittext) as EditText
        txt.setText(file)

        val getFileName = AlertDialog.Builder(this)
        getFileName.setTitle(R.string.export_choose)
        getFileName.setView(v)
        getFileName.setPositiveButton(R.string.export_save) { dlg, sumthin ->
            val fileEdit = txt.text.toString()
            if (e.fileExists(fileEdit)) {
                val fileExists = AlertDialog.Builder(this@ActivityDiscovery)
                fileExists.setTitle(R.string.export_exists_title)
                fileExists.setMessage(R.string.export_exists_msg)
                fileExists.setPositiveButton(R.string.btn_yes
                ) { dialog, which ->
                    if (e.writeToSd(fileEdit)) {
                        makeToast(R.string.export_finished)
                    } else {
                        export()
                    }
                }
                fileExists.setNegativeButton(R.string.btn_no, null)
                fileExists.show()
            } else {
                if (e.writeToSd(fileEdit)) {
                    makeToast(R.string.export_finished)
                } else {
                    export()
                }
            }
        }
        getFileName.setNegativeButton(R.string.btn_discover_cancel, null)
        getFileName.show()
    }

    fun makeToast(msg: Int) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun setButton(btn: Button?, res: Int, disable: Boolean) {
        if (disable) {
            setButtonOff(btn, res)
        } else {
            setButtonOn(btn, res)
        }
    }

    private fun setButtonOff(b: Button?, drawable: Int) {
        b?.isClickable = false
        b?.isEnabled = false
        b?.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
    }

    private fun setButtonOn(b: Button?, drawable: Int) {
        b?.isClickable = true
        b?.isEnabled = true
        b?.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
    }

    companion object {
        val VIBRATE = 250.toLong()
        val SCAN_PORT_RESULT = 1
        val MENU_SCAN_SINGLE = 0
        val MENU_OPTIONS = 1
        val MENU_HELP = 2
        private val MENU_EXPORT = 3
        var mInflater: LayoutInflater? = null
    }
}

