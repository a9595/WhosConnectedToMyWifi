package info.lamatricexiste.network

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import info.lamatricexiste.network.Network.HostBean
import info.lamatricexiste.network.Network.NetInfo

// Custom ArrayAdapter
class HostsAdapter(ctxt: Context,
                           private val activityDiscovery: ActivityDiscovery) : ArrayAdapter<Void>(ctxt, R.layout.list_host, R.id.list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ActivityDiscovery.ViewHolder
        if (convertView == null) {
            convertView = ActivityDiscovery.mInflater!!.inflate(R.layout.list_host, null)
            holder = ActivityDiscovery.ViewHolder()
            holder.host = convertView!!.findViewById(R.id.list) as TextView
            holder.mac = convertView.findViewById(R.id.mac) as TextView
            holder.vendor = convertView.findViewById(R.id.vendor) as TextView
            holder.logo = convertView.findViewById(R.id.logo) as ImageView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ActivityDiscovery.ViewHolder
        }
        val host = activityDiscovery.hosts!![position]
        if (host.deviceType == HostBean.TYPE_GATEWAY) {
            holder.logo!!.setImageResource(R.drawable.router)
        } else if (host.isAlive == 1 || host.hardwareAddress != NetInfo.NOMAC) {
            holder.logo!!.setImageResource(R.drawable.computer)
        } else {
            holder.logo!!.setImageResource(R.drawable.computer_down)
        }
        if (host.hostname != null && host.hostname != host.ipAddress) {
            holder.host!!.text = host.hostname + " (" + host.ipAddress + ")"
        } else {
            holder.host!!.text = host.ipAddress
        }
        if (host.hardwareAddress != NetInfo.NOMAC) {
            holder.mac!!.text = host.hardwareAddress
            if (host.nicVendor != null) {
                holder.vendor!!.text = host.nicVendor
            } else {
                holder.vendor!!.setText(R.string.info_unknown)
            }
            holder.mac!!.visibility = View.VISIBLE
            holder.vendor!!.visibility = View.VISIBLE
        } else {
            holder.mac!!.visibility = View.GONE
            holder.vendor!!.visibility = View.GONE
        }
        return convertView
    }
}