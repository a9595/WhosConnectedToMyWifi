/*
 * Copyright (C) 2009-2010 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package info.lamatricexiste.network.Scanning;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import info.lamatricexiste.network.AbstractDiscovery;
import info.lamatricexiste.network.Network.HardwareAddress;
import info.lamatricexiste.network.Network.HostBean;
import info.lamatricexiste.network.Network.NetInfo;
import info.lamatricexiste.network.Network.RateControl;
import info.lamatricexiste.network.Utils.Save;

public class DefaultDiscovery extends AbstractDiscovery {

    private final String TAG = "DefaultDiscovery";
    private final static int[] DPORTS = {139, 445, 22, 80};
    private final static int TIMEOUT_SCAN = 3600; // seconds
    private final static int TIMEOUT_SHUTDOWN = 10; // seconds
    private final static int THREADS = 10; //FIXME: Test, plz set in options again ?
    private final int mRateMult = 5; // Number of alive hosts between Rate
    private int pt_move = 2; // 1=backward 2=forward
    private ExecutorService mPool;
    private boolean doRateControl;
    private RateControl mRateControl;
    private Save mSave;
    private String gatewayIp;

    public DefaultDiscovery(String gatewayIp) {
        super();
        this.gatewayIp = gatewayIp;
        mRateControl = new RateControl();
        mSave = new Save();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        doRateControl = true;

    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.v(TAG, "start=" + NetInfo.getIpFromLongUnsigned(getStart()) + " (" + getStart()
                + "), end=" + NetInfo.getIpFromLongUnsigned(getEnd()) + " (" + getEnd()
                + "), length=" + getSize());
        mPool = Executors.newFixedThreadPool(THREADS);
        if (getIp() <= getEnd() && getIp() >= getStart()) {
            Log.i(TAG, "Back and forth scanning");
            // gateway
            launch(getStart());

            // hosts
            long pt_backward = getIp();
            long pt_forward = getIp() + 1;
            long size_hosts = getSize() - 1;

            for (int i = 0; i < size_hosts; i++) {
                // Set pointer if of limits
                if (pt_backward <= getStart()) {
                    pt_move = 2;
                } else if (pt_forward > getEnd()) {
                    pt_move = 1;
                }
                // Move back and forth
                if (pt_move == 1) {
                    launch(pt_backward);
                    pt_backward--;
                    pt_move = 2;
                } else if (pt_move == 2) {
                    launch(pt_forward);
                    pt_forward++;
                    pt_move = 1;
                }
            }
        } else {
            Log.i(TAG, "Sequencial scanning");
            for (long i = getStart(); i <= getEnd(); i++) {
                launch(i);
            }
        }
        mPool.shutdown();
        try {
            if (!mPool.awaitTermination(TIMEOUT_SCAN, TimeUnit.SECONDS)) {
                mPool.shutdownNow();
                Log.e(TAG, "Shutting down pool");
                if (!mPool.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.SECONDS)) {
                    Log.e(TAG, "Pool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
            mPool.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            mSave.closeDb();
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        if (mPool != null) {
            synchronized (mPool) {
                mPool.shutdownNow();
                // FIXME: Prevents some task to end (and close the Save DB)
            }
        }
        super.onCancelled();
    }

    private void launch(long i) {
        if (!mPool.isShutdown()) {
            mPool.execute(new CheckRunnable(NetInfo.getIpFromLongUnsigned(i)));
        }
    }

    private int getRate() {
        return 500;
    }

    private class CheckRunnable implements Runnable {
        private String addr;

        CheckRunnable(String addr) {
            this.addr = addr;
        }

        public void run() {
            if (isCancelled()) {
                publish(null);
            }
            Log.e(TAG, "run=" + addr);
            // Create host object
            final HostBean host = new HostBean();
            host.responseTime = getRate();
            host.ipAddress = addr;
            try {
                InetAddress h = InetAddress.getByName(addr);
                // Rate control check
                if (doRateControl && mRateControl.indicator != null && getHosts_done() % mRateMult == 0) {
                    mRateControl.adaptRate();
                }
                // Arp Check #1
                host.hardwareAddress = HardwareAddress.getHardwareAddress(addr);
                if (!NetInfo.NOMAC.equals(host.hardwareAddress)) {
                    Log.e(TAG, "found using arp #1 " + addr);
                    publish(host);
                    return;
                }
                // Native InetAddress check
                if (h.isReachable(getRate())) {
                    Log.e(TAG, "found using InetAddress ping " + addr);
                    publish(host);
                    // Set indicator and get a rate
                    if (doRateControl && mRateControl.indicator == null) {
                        mRateControl.indicator = addr;
                        mRateControl.adaptRate();
                    }
                    return;
                }
                // Arp Check #2
                host.hardwareAddress = HardwareAddress.getHardwareAddress(addr);
                if (!NetInfo.NOMAC.equals(host.hardwareAddress)) {
                    Log.e(TAG, "found using arp #2 " + addr);
                    publish(host);
                    return;
                }
                // Custom check
                int port;
                // TODO: Get ports from options
                Socket s = new Socket();
                for (int i = 0; i < DPORTS.length; i++) {
                    try {
                        s.bind(null);
                        s.connect(new InetSocketAddress(addr, DPORTS[i]), getRate());
                        Log.v(TAG, "found using TCP connect " + addr + " on port=" + DPORTS[i]);
                    } catch (IOException e) {
                    } catch (IllegalArgumentException e) {
                    } finally {
                        try {
                            s.close();
                        } catch (Exception e) {
                        }
                    }
                }

                /*
                if ((port = Reachable.isReachable(h, getRate())) > -1) {
                    Log.v(TAG, "used Network.Reachable object, "+addr+" port=" + port);
                    publish(host);
                    return;
                }
                */
                // Arp Check #3
                host.hardwareAddress = HardwareAddress.getHardwareAddress(addr);
                if (!NetInfo.NOMAC.equals(host.hardwareAddress)) {
                    Log.e(TAG, "found using arp #3 " + addr);
                    publish(host);
                    return;
                }
                publish(null);

            } catch (IOException e) {
                publish(null);
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void publish(final HostBean host) {
        setHosts_done(getHosts_done() + 1);
        if (host == null) {
            publishProgress((HostBean) null);
            return;
        }


        // Mac Addr not already detected
        if (NetInfo.NOMAC.equals(host.hardwareAddress)) {
            host.hardwareAddress = HardwareAddress.getHardwareAddress(host.ipAddress);
        }

        // NIC vendor
        host.nicVendor = HardwareAddress.getNicVendor(host.hardwareAddress);

        // Is gateway ?
        if (gatewayIp.equals(host.ipAddress)) {
            host.deviceType = HostBean.TYPE_GATEWAY;
        }

        // FQDN
        // Static
        if ((host.hostname = mSave.getCustomName(host)) == null) {
            // DNS
            try {
                host.hostname = (InetAddress.getByName(host.ipAddress)).getCanonicalHostName();
            } catch (UnknownHostException e) {
                Log.e(TAG, e.getMessage());
            }
            // TODO: NETBIOS
            //try {
            //    host.hostname = NbtAddress.getByName(addr).getHostName();
            //} catch (UnknownHostException e) {
            //    Log.i(TAG, e.getMessage());
            //}
        }

        publishProgress(host);
    }
}
