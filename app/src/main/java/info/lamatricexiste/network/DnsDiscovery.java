/*
 * Copyright (C) 2009-2010 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package info.lamatricexiste.network;

import info.lamatricexiste.network.Network.HardwareAddress;
import info.lamatricexiste.network.Network.HostBean;
import info.lamatricexiste.network.Network.NetInfo;
import info.lamatricexiste.network.Utils.Prefs;

import java.io.IOException;
import java.net.InetAddress;

import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.util.Log;

public class DnsDiscovery /*extends AbstractDiscovery*/ {

    private final String TAG = "DnsDiscovery";

    public DnsDiscovery() {
        super();
    }
/*

    @Override
    protected Void doInBackground(Void... params) {
        if (getMDiscover() != null) {
            final ActivityDiscovery discover = getMDiscover().get();
            if (discover != null) {
                Log.i(TAG, "start=" + NetInfo.getIpFromLongUnsigned(getStart()) + " (" + getStart()
                        + "), end=" + NetInfo.getIpFromLongUnsigned(getEnd()) + " (" + getEnd()
                        + "), length=" + getSize());

                int timeout = Integer.parseInt(discover.prefs.getString(Prefs.KEY_TIMEOUT_DISCOVER,
                        Prefs.DEFAULT_TIMEOUT_DISCOVER));
                Log.i(TAG, "timeout=" + timeout + "ms");

                for (long i = getStart(); i < getEnd() + 1; i++) {
                    setHosts_done(getHosts_done() + 1);
                    HostBean host = new HostBean();
                    host.ipAddress = NetInfo.getIpFromLongUnsigned(i);
                    try {
                        InetAddress ia = InetAddress.getByName(host.ipAddress);
                        host.hostname = ia.getCanonicalHostName();
                        host.isAlive = ia.isReachable(timeout) ? 1 : 0;
                    } catch (java.net.UnknownHostException e) {
                        Log.e(TAG, e.getMessage());
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    if (host.hostname != null && !host.hostname.equals(host.ipAddress)) {
                        // Is gateway ?
                        if (discover.net.gatewayIp.equals(host.ipAddress)) {
                            host.deviceType = 1;
                        }
                        // Mac Addr
                        host.hardwareAddress = HardwareAddress.getHardwareAddress(host.ipAddress);
                        // NIC vendor
                        try {
                            host.nicVendor = HardwareAddress.getNicVendor(host.hardwareAddress);
                        } catch (SQLiteDatabaseCorruptException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        publishProgress(host);
                    } else {
                        publishProgress((HostBean) null);
                    }
                }
            }
        }
        return null;
    }
*/

}
