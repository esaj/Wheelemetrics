package com.github.esaj.wheelemetrics.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.github.esaj.wheelemetrics.utils.ThreadUtils;

/**
 * @author esaj
 */
public class RetryOnConnectionLossBluetoothObserver extends BluetoothObserverAdapter
{
    private static final String TAG = "RetryBTObserver";
    private static final int MAX_RETRIES = 10;

    private BluetoothService service;
    private BluetoothDevice lastConnectedDevice = null;
    private boolean secure = false;
    private int retryCount = 0;

    @Override
    public void onRegistered(BluetoothService bluetoothService)
    {
        this.service = bluetoothService;
    }

    @Override
    public void connectionOpened(BluetoothSocket socket, BluetoothDevice device, boolean secure)
    {
        lastConnectedDevice = device;
        retryCount = 0;
        this.secure = secure;
    }

    @Override
    public void connectionLost(BluetoothDevice device)
    {
        if(retryCount > MAX_RETRIES)
        {
            Log.w(TAG, "Failed to reconnect to device after " + MAX_RETRIES + " retries");
        }
        else if(lastConnectedDevice == device)
        {
            Log.i(TAG, "Connection lost, retry " + (retryCount + 1));
            retryCount++;
            service.connect(lastConnectedDevice, secure);
        }
    }

    @Override
    public void connectionFailed(BluetoothDevice device)
    {
        if(retryCount > 0)
        {
            //Retry on failed reconnection
            ThreadUtils.sleepIgnoringInterrupt(500);
            connectionLost(lastConnectedDevice);
        }
    }
}
