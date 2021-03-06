package com.github.esaj.wheelemetrics.protocol.codec;

import android.util.Log;

import com.github.esaj.wheelemetrics.data.LoggableData;
import com.github.esaj.wheelemetrics.protocol.LoggableDataPublisherCallback;
import com.github.esaj.wheelemetrics.utils.ThreadUtils;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * ProtocolDataRelay is used to transmit incoming data to an internal thread
 * that then passes it on to the codec for handling and to publish results via
 * LoggableDataPublisherCallback.
 *
 * The data is fed to the codec from an outside thread, so that the codec & incoming
 * data can be separated and do not have to wait for each other.
 *
 * DataFeeder will publish the resulting LoggableData via LoggableDataPublisherCallback
 * every time the codec produces one.
 *
 * @author esaj
 */
public class ProtocolDataRelay
{
    private static final String TAG = "ProtocolDataRelay";

    private DataHandler dataHandler;
    private LoggableDataPublisherCallback callback;
    private volatile Thread thread;

    public ProtocolDataRelay(ProtocolCodec codec, LoggableDataPublisherCallback callback) throws IOException
    {
        dataHandler = new DataHandler(codec);
        this.callback = callback;
        thread = new Thread(dataHandler, "DataFeeder.DataHandler");
        thread.start();
    }

    /**
     * Internal class that handles passing the data to decoding as it arrives
     */
    class DataHandler implements Runnable
    {
        private final int PIPESIZE = 128;

        private volatile boolean running = true;

        private final ProtocolCodec codec;
        private final PipedInputStream inStream;
        private final PipedOutputStream outStream;

        /**
         * @param  codec    Codec to feed the data to
         * @throws IOException if PipedInputStream construction fails
         */
        public DataHandler(ProtocolCodec codec) throws IOException
        {
            this.codec = codec;
            outStream = new PipedOutputStream();
            inStream = new PipedInputStream(outStream, PIPESIZE);
        }

        /**
         * Feeds data for handling outside this thread
         * (Note: this should never be called internally by this DataHandler, as it may cause a deadlock)
         * @param data  Data to feed for handling
         */
        public void feedData(byte[] data)
        {
            if(!running)
            {
                Log.w(TAG, "Thread not running, restarting");
                thread = new Thread(ProtocolDataRelay.this.dataHandler, "DataFeeder.DataHandler");
                thread.start();
                ThreadUtils.sleepIgnoringInterrupt(50);
            }

            if(running)
            {
                try
                {
                    outStream.write(data);
                }
                catch(IOException e)
                {
                    Log.e(TAG, "Exception occurred writing data to PipedOutputStream", e);
                }
            }
        }

        public void stop()
        {
            running = false;
        }

        @Override
        public void run()
        {
            running = true;
            while(running)
            {
                try
                {
                    LoggableData data = codec.decode(inStream);
                    if(data != null)
                    {
                        callback.publishLoggableData(data);
                    }
                }
                catch(IOException e)
                {
                    //Pipe is probably gone?
                    Log.e(TAG, "Exception caught in ProtocolDataRelay.DataHandler", e);
                    running = false;
                }
                catch(Exception e)
                {
                    Log.e(TAG, "Exception caught in ProtocolDataRelay.DataHandler", e);
                }
            }
        }
    }


    public void dataReceived(byte[] data)
    {
        dataHandler.feedData(data);
    }
}
