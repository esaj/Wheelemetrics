package com.github.esaj.wheelemetrics.data.record;

import android.util.Log;

import com.github.esaj.wheelemetrics.data.LoggableData;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread handling asynchronous writing to file via ConcurrentLinkedQueue
 * @author esaj
 */
public class WriteThread extends Thread
{
    private static final String TAG = "WriteThread";

    private final OutputStream outStream;

    public void setSyncEveryNSamples(int syncEveryNSamples)
    {
        this.syncEveryNSamples = syncEveryNSamples;
    }

    private Queue<LoggableData> writeQueue = new ConcurrentLinkedQueue<>();

    private volatile boolean running;

    private int syncEveryNSamples = 5000;

    private int sampleCountSinceSync = 0;

    public WriteThread(OutputStream outputStream) throws IOException
    {
        this.outStream = outputStream;
    }

    public void write(LoggableData data)
    {
        writeQueue.add(data);
    }

    public void cancel()
    {
        this.running = false;
    }

    @Override
    public void run()
    {
        running = true;
        while(running)
        {
            LoggableData data = writeQueue.poll();
            if(data == null)
            {
                try
                {
                    Thread.sleep(100);
                } catch(InterruptedException e)
                {
                    running = false;
                }
            }
            else
            {
                try
                {
                    writeToFile(data);
                    sampleCountSinceSync++;
                    if(sampleCountSinceSync >= syncEveryNSamples)
                    {
                        sampleCountSinceSync = 0;
                        outStream.flush();
                    }
                } catch(Exception e)
                {
                    Log.e(TAG, "Exception writing to file", e);
                }
            }
        }

        try
        {
            //Write the rest of the entries still possibly in the queue
            LoggableData data = writeQueue.poll();
            while(data != null)
            {
                writeToFile(data);
                data = writeQueue.poll();
            }

            outStream.flush();
            outStream.close();
        } catch(IOException e)
        {
            Log.e(TAG, "Exception while flushing & closing file", e);
        }
    }

    private void writeToFile(LoggableData data) throws IOException
    {
        outStream.write(data.getLogEntry().getBytes());
    }
}
