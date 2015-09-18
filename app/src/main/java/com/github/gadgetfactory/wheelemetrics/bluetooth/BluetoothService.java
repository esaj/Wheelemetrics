package com.github.gadgetfactory.wheelemetrics.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.gadgetfactory.wheelemetrics.data.LoggableData;
import com.github.gadgetfactory.wheelemetrics.protocol.LoggableDataPublisherCallback;
import com.github.gadgetfactory.wheelemetrics.protocol.ProtocolAutodetectionLogic;
import com.github.gadgetfactory.wheelemetrics.protocol.codec.ProtocolDataRelay;
import com.github.gadgetfactory.wheelemetrics.warning.SpeedDataReceiver;
import com.github.gadgetfactory.wheelemetrics.warning.WarningVibratorService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A messy bluetooth-service, adapted from the BluetoothChatExample
 *
 * @author esaj
 */
public class BluetoothService extends Service
{
    private static final String TAG = "BluetoothService";

    //UUID_SPP-profile
    private static final UUID UUID_SPP = UUID.fromString(Constants.SPP);

    private final BluetoothAdapter adapter;

    private Handler handler;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private ConnectionAliveCheckerThread checkerThread;

    private BluetoothDevice device;
    private boolean isSecure;

    private ProtocolAutodetectionLogic autodetectionLogic = new ProtocolAutodetectionLogic();

    public class BluetoothBinder extends Binder
    {
        public BluetoothService getService()
        {
            return BluetoothService.this;
        }
    }

    private BluetoothBinder binder = new BluetoothBinder();

    // Constants that indicate the current connection state
    public enum State
    {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED;

        public static State getByOrdinal(int ordinal)
        {
            return values()[ordinal];
        }
    }

    private State state;

    private CopyOnWriteArrayList<BluetoothObserver> observers = new CopyOnWriteArrayList<>();


    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        if(intent == null)
        {
            //Weird stuff happening on vee's Huawei
            return START_STICKY;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        this.observers.clear();
        Log.i(TAG, "Shutting down BluetoothService");
        cancelThreads();
        stopSelf();
    }

    public BluetoothService()
    {
        adapter = BluetoothAdapter.getDefaultAdapter();
        registerObserver(new RetryOnConnectionLossBluetoothObserver());
        state = State.NOT_CONNECTED;
    }

    public void setHandler(Handler handler)
    {
        this.handler = handler;
    }

    /**
     * Registers the given observer to receive callbacks
     * @param observer  BluetoothObserver to register
     */
    public void registerObserver(BluetoothObserver observer)
    {
        this.observers.add(observer);
        observer.onRegistered(this);
    }

    /**
     * Unregisters the given observer from receiving callbacks
     * @param observer  BluetoothObserver to unregister
     */
    public void unregisterObserver(BluetoothObserver observer)
    {
        this.observers.remove(observer);
        observer.onUnregistered(this);
    }

    /**
     * Set the current state of connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(State state)
    {
        Log.d(TAG, "setState() " + this.state.name() + " -> " + state.name());
        this.state = state;

        // Give the new state to the Handler so the UI Activity can update
        handler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state.ordinal(), -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized State getState()
    {
        return state;
    }

    /**
     * Start the service.
     */
    public synchronized void start()
    {
        //TODO: Is this even needed anymore
        Log.d(TAG, "Service starting/resetting");

        // Cancel any thread attempting to make a connection
        cancelThreads();

        setState(State.NOT_CONNECTED);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect to
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure)
    {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if(state == State.CONNECTING)
        {
            if(connectThread != null)
            {
                connectThread.cancel();
                connectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if(connectedThread != null)
        {
            connectedThread.cancel();
            connectedThread = null;
        }

        isSecure = secure;

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device, secure);
        connectThread.start();
        setState(State.CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType, boolean secure)
    {
        Log.d(TAG, "Connected, socket type:" + socketType);
        cancelThreads();

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(device, socket, socketType);
        connectedThread.start();

        checkerThread = new ConnectionAliveCheckerThread(connectedThread, 2500);
        checkerThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = handler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);

        setState(State.CONNECTED);

        sendVibrateIntent(new long[]{0, 1000});

        for(BluetoothObserver observer : observers)
        {
            try
            {
                observer.connectionOpened(socket, device, secure);
            }
            catch(Exception e)
            {
                Log.e(TAG, "Exception caught from BluetoothObserver " + observer.getClass().getCanonicalName(), e);
            }
        }

    }

    private void sendVibrateIntent(long[] vibrateData)
    {
        Intent vibrateIntent = new Intent(this, WarningVibratorService.class);
        vibrateIntent.putExtra(WarningVibratorService.VIBRATE_DATA, vibrateData);
        startService(vibrateIntent);
    }

    private void cancelThreads()
    {
        // Cancel the thread that completed the connection
        if(connectThread != null)
        {
            try
            {
                connectThread.cancel();
                connectThread.join(5000);
            }
            catch(InterruptedException e)
            {
                Log.w(TAG, "Interrupted while waiting for connectThread to die", e);
            }
            finally
            {
                connectThread = null;
            }
        }

        if(checkerThread != null)
        {
            try
            {
                checkerThread.cancel();
                checkerThread.join(500);
            }
            catch(InterruptedException e)
            {
                //Nevermind
            }
            finally
            {
                checkerThread = null;
            }

        }

        // Cancel any thread currently running a connection
        if(connectedThread != null)
        {
            try
            {
                connectedThread.cancel();
                connectedThread.join(5000);
            }
            catch(InterruptedException e)
            {
                Log.w(TAG, "Interrupted while waiting for connectThread to die", e);
            }
            finally
            {
                connectedThread = null;
            }
        }

        setState(State.NOT_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop()
    {
        Log.d(TAG, "Service stop requested");
        cancelThreads();
        setState(State.NOT_CONNECTED);
        Log.d(TAG, "Service stop complete");
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out)
    {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized(this)
        {
            if(state != State.CONNECTED)
            {
                return;
            }
            r = connectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed()
    {
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        handler.sendMessage(msg);
        cancelThreads();

        for(BluetoothObserver observer : observers)
        {
            try
            {
                observer.connectionFailed(device);
            }
            catch(Exception e)
            {
                Log.e(TAG, "Exception caught from BluetoothObserver " + observer.getClass().getCanonicalName(), e);
            }
        }
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost(BluetoothDevice device)
    {
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        handler.sendMessage(msg);

        sendVibrateIntent(new long[]{100, 100, 100, 300, 100, 100, 100, 300, 100, 100, 100, 300, 100, 100, 100, 300, 100, 100, 100, 300, 100, 100, 100, 300});

        for(BluetoothObserver observer : observers)
        {
            try
            {
                observer.connectionLost(device);
            }
            catch(Exception e)
            {
                Log.e(TAG, "Exception caught from BluetoothObserver " + observer.getClass().getCanonicalName(), e);
            }

        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread
    {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        private final boolean secure;
        private String socketType;

        public ConnectThread(BluetoothDevice device, boolean secure)
        {
            this.device = device;
            this.secure = secure;
            BluetoothSocket tmp = null;
            socketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try
            {
                if(secure)
                {
                    tmp = device.createRfcommSocketToServiceRecord(UUID_SPP);
                }
                else
                {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(UUID_SPP);
                }
            }
            catch(IOException e)
            {
                Log.e(TAG, "Socket Type: " + socketType + "create() failed", e);
            }
            socket = tmp;
        }

        public void run()
        {
            Log.i(TAG, "BEGIN connectThread SocketType:" + socketType);
            setName("ConnectThread" + socketType);

            // Always cancel discovery because it will slow down a connection
            adapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try
            {
                //Need to sleep a bit or socket.connect seems to fail more often than not on Huawei?
                Thread.sleep(250);

                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket.connect();
            }
            catch(Exception e)
            {
                // Close the socket
                Log.e(TAG, "Exception caught trying to connect socket", e);
                try
                {
                    socket.close();
                }
                catch(IOException e2)
                {
                    Log.e(TAG, "unable to close() " + socketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized(BluetoothService.this)
            {
                connectThread = null;
            }

            // Start the connected thread
            connected(socket, device, socketType, secure);
        }

        public void cancel()
        {
            try
            {
                socket.close();
            }
            catch(IOException e)
            {
                Log.e(TAG, "close() of " + socketType + " socket failed", e);
            }
        }
    }

    private class ConnectionAliveCheckerThread extends Thread
    {
        private ConnectedThread watchedThread;
        private final long timeOutMs;
        private boolean running = true;

        public ConnectionAliveCheckerThread(ConnectedThread threadToWatch, long timeOutMs)
        {
            this.watchedThread = threadToWatch;
            this.timeOutMs = timeOutMs;
        }

        public void cancel()
        {
            running = false;
            this.interrupt();
        }

        @Override
        public void run()
        {
            while(running)
            {
                try
                {
                    Thread.sleep(100);

                    long lastReceived =  watchedThread.getLastDataReceived();
                    if(lastReceived > -1)
                    {
                        long lastDataSince = System.currentTimeMillis() - lastReceived;
                        if(lastDataSince > timeOutMs)
                        {
                            Log.i(TAG, "Watched thread appears dead, cancelling");
                            watchedThread.cancel();
                            running = false;
                        }
                    }

                    if(!watchedThread.isAlive())
                    {
                        Log.w(TAG, "ConnectionCheckerThread: Watched thread has died, bailing out");
                        running = false;
                    }
                }
                catch(InterruptedException e)
                {
                    //Nevermind
                }
                catch(Exception e)
                {
                    Log.w(TAG, "Exception caught in ConnectionAliveCheckerThread", e);
                }
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread
    {
        private final BluetoothDevice device;
        private final BluetoothSocket socket;
        private final InputStream inStream;
        private final OutputStream outStream;
        private boolean running = true;
        private volatile long lastDataReceived = -1;

        public ConnectedThread(BluetoothDevice device, BluetoothSocket socket, String socketType)
        {
            Log.d(TAG, "ConnectedThread starting: " + socket + " (" + socketType + ")");
            this.device = device;
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try
            {
                running = true;
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch(IOException e)
            {
                Log.e(TAG, "Failure creating socket-streams", e);
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public long getLastDataReceived()
        {
            return lastDataReceived;
        }

        public void run()
        {
            Log.i(TAG, "ConnectedThread entering run-state");
            byte[] buffer = new byte[1024];

            //Detect protocol

            try
            {
                ProtocolDataRelay relay = autodetectionLogic.autodetectProtocol(inStream, new LoggableDataPublisherCallback()
                {
                    @Override
                    public void publishLoggableData(LoggableData data)
                    {
                        //Full packet received, send forwards
                        Intent speedDataIntent = new Intent(Constants.SPEED_DATA);
                        speedDataIntent.setClass(getApplicationContext(), SpeedDataReceiver.class);
                        speedDataIntent.putExtra(Constants.SPEED_DATA, data.getSpeed());
                        getApplicationContext().sendBroadcast(speedDataIntent);

                        Intent dataIntent = new Intent(Constants.MESSAGE_STRING_LOGGABLEDATA);
                        dataIntent.setClass(getApplicationContext(), LoggableDataReceiver.class);
                        dataIntent.putExtra(Constants.MESSAGE_STRING_LOGGABLEDATA, data.getLogEntry());
                        getApplicationContext().sendBroadcast(dataIntent);

                        handler.obtainMessage(Constants.MESSAGE_DATA_READ, data).sendToTarget();
                    }
                });

                registerObserver(new ProtocolDataRelayBluetoothObserver(relay));
            }
            catch(Exception e)
            {
                Log.e(TAG, "Autodetection failure", e);
            }
            // Keep listening to the InputStream while connected
            while(running)
            {
                try
                {
                    int count = inStream.read(buffer);
                    byte[] data = new byte[count];
                    System.arraycopy(buffer, 0, data, 0, count);

                    lastDataReceived = System.currentTimeMillis();

                    for(BluetoothObserver observer : observers)
                    {
                        try
                        {
                            observer.dataReceived(data);
                        }
                        catch(Exception e)
                        {
                            Log.e(TAG, "Exception caught from BluetoothObserver " + observer.getClass().getCanonicalName(), e);
                        }
                    }

                    // Send the obtained bytes to the UI Activity
                    handler.obtainMessage(Constants.MESSAGE_BINARYDATA_READ, data)
                            .sendToTarget();
                }
                catch(Exception e)
                {
                    Log.e(TAG, "disconnected", e);
                    setState(BluetoothService.State.NOT_CONNECTED);
                    connectionLost(device);
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer)
        {
            try
            {
                outStream.write(buffer);

                // Share the sent message back to the UI Activity
                handler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();

                for(BluetoothObserver observer : observers)
                {
                    try
                    {
                        observer.dataSent(buffer);
                    }
                    catch(Exception e)
                    {
                        Log.e(TAG, "Exception caught from BluetoothObserver " + observer.getClass().getCanonicalName(), e);
                    }
                }

            }
            catch(IOException e)
            {
                Log.e(TAG, "Exception caught during write", e);
            }
        }

        public void cancel()
        {
            try
            {
                running = false;
                ConnectedThread.this.interrupt();
                inStream.close();
                outStream.close();
                socket.close();

                Log.i(TAG, "Bluetooth socket closed");

                for(BluetoothObserver observer : observers)
                {
                    try
                    {
                        observer.connectionClosed(device);
                    }
                    catch(Exception e)
                    {
                        Log.e(TAG, "Exception caught from BluetoothObserver " + observer.getClass().getCanonicalName(), e);
                    }
                }
            }
            catch(IOException e)
            {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

}
