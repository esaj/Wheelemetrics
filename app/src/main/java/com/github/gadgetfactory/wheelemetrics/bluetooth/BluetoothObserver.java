package com.github.gadgetfactory.wheelemetrics.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * Simple listener/observer-interface for Bluetooth-traffic, register with BluetoothService to listen for events
 *
 * @author esaj
 */
public interface BluetoothObserver
{
    /**
     * Called when this observer has been registered to a BluetoothService-instance
     * @param bluetoothService  Instance this observer was registered to
     */
    public void onRegistered(BluetoothService bluetoothService);

    /**
     * Called when this observer has been unregistered from a BluetoothService-instance
     * @param bluetoothService  Instance this observer was unregistered from
     */
    public void onUnregistered(BluetoothService bluetoothService);

    /**
     * Called when a connection has been opened
     * @param socket    Socket connection to device
     * @param device    Device
     * @param secure    Is the connection secure or insecure
     */
    public void connectionOpened(BluetoothSocket socket, BluetoothDevice device, boolean secure);

    /**
     * Called when a connection has been closed
     * @param device    Device
     */
    public void connectionClosed(BluetoothDevice device);

    /**
     * Called when opening a connection fails
     * @param device    Device to which service was trying to connect to
     */
    public void connectionFailed(BluetoothDevice device);

    /**
     * Called when a connection is lost
     * @param device    Device to which service lost connection to
     */
    public void connectionLost(BluetoothDevice device);


    /**
     * Called when data has been sent out, note that this will run in the Thread-context of the
     * ConnectedThread of BluetoothService
     * @param data  Data sent out, handle as read-only
     */
    public void dataSent(byte[] data);

    /**
     * Called when data is received, note that this will run in the Thread-context of the
     * ConnectedThread of BluetoothService. Also the data may change after this method returns, so
     * if you need to store it, make a copy
     * @param data  Received data, handle as read-only, or make a copy if contents need to be modified/stored beyond this method call
     */
    public void dataReceived(byte[] data);
}
