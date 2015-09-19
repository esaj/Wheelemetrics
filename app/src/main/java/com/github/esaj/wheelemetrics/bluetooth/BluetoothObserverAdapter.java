package com.github.esaj.wheelemetrics.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * Adapter-class for extending observers without the need to add emply implementations (just override what you need)
 *
 * @author esaj
 */
public class BluetoothObserverAdapter implements BluetoothObserver
{
    @Override
    public void onRegistered(BluetoothService bluetoothService)
    {
        //Nada
    }

    @Override
    public void onUnregistered(BluetoothService bluetoothService)
    {
        //Nada
    }

    @Override
    public void connectionOpened(BluetoothSocket socket, BluetoothDevice device, boolean secure)
    {
        //Nada
    }

    @Override
    public void connectionClosed(BluetoothDevice device)
    {
        //Nada
    }

    @Override
    public void connectionFailed(BluetoothDevice device)
    {
        //Nada
    }

    @Override
    public void connectionLost(BluetoothDevice device)
    {
        //Nada
    }

    @Override
    public void dataSent(byte[] data)
    {
        //Nada
    }

    @Override
    public void dataReceived(byte[] data)
    {
        //Nada
    }
}
