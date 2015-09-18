package com.github.gadgetfactory.wheelemetrics.data.record;

import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.gadgetfactory.wheelemetrics.bluetooth.Constants;
import com.github.gadgetfactory.wheelemetrics.data.LoggableData;
import com.github.gadgetfactory.wheelemetrics.data.PlainStringLoggableData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class TelemetryFileServiceImpl extends Service implements TelemetryFileService
{
    private static final String TAG = "TelemetryFileService";

    private File currentFile;

    private WriteThread writeThread;

    public class TelemetryFileServiceBinder extends Binder
    {
        public TelemetryFileService getService()
        {
            return TelemetryFileServiceImpl.this;
        }
    }

    private TelemetryFileServiceBinder binder = new TelemetryFileServiceBinder();

    public TelemetryFileServiceImpl()
    {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent == null)
        {
            //Weird stuff happening on vee's Huawei
            return START_STICKY;
        }

        if(intent.hasExtra(Constants.MESSAGE_STRING_LOGGABLEDATA))
        {
            addLogEntry(new PlainStringLoggableData(intent.getStringExtra(Constants.MESSAGE_STRING_LOGGABLEDATA)));
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        finalizeFile(getCurrentFile());
        stopSelf();
    }

    @Override
    public void openNewFile(String fileName)
    {
        if(currentFile != null)
        {
            //Existing file open, finalize
            finalizeFile(currentFile);
        }

        if(!isExternalStorageWritable())
        {
            Log.e(TAG, "Cannot write to external storage");
            return;
        }

        try
        {
            currentFile = getStorageFile(fileName);
            if(currentFile != null)
            {
                BufferedOutputStream bos = new BufferedOutputStream((new FileOutputStream(currentFile)));
                writeThread = new WriteThread(bos);
                writeThread.start();
            }
        } catch(Exception e)
        {
            Log.e(TAG, "Failed to start thread for file writing", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public File getCurrentFile()
    {
        return currentFile;
    }

    @Override
    public void finalizeFile(File finalFile)
    {
        try
        {
            if(writeThread != null)
            {
                writeThread.cancel();
                writeThread.join(1000);
            }

            if(currentFile == null)
            {
                return;
            }

            if(!currentFile.getAbsolutePath().equals(finalFile.getAbsolutePath()))
            {
                if(!currentFile.renameTo(finalFile))
                {
                    if(!finalFile.exists())
                    {
                        finalFile.createNewFile();
                    }

                    InputStream in = new FileInputStream(currentFile);
                    OutputStream out = new FileOutputStream(finalFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }

            //This is needed so the file updates as visible on some PCs...
            MediaScannerConnection.scanFile(this, new String[]{finalFile.getAbsolutePath()}, null, null);
        } catch(Exception e)
        {
            Log.e(TAG, "Failure while finalizing file", e);
        }
    }

    @Override
    public void addLogEntry(LoggableData logEntry, boolean binary)
    {
        if(writeThread != null && writeThread.isAlive())
        {
            try
            {
                if(!binary)
                {
                    writeThread.write(logEntry);
                }
                else
                {
                    //TODO: Binary data
                }
            } catch(Exception e)
            {
                Log.w(TAG, "Exception handling log entry, invalid data?", e);
            }
        }
    }

    @Override
    public void addLogEntry(LoggableData data)
    {
        if(writeThread != null && writeThread.isAlive())
        {
            try
            {
               writeThread.write(data);
            }
            catch(Exception e)
            {
                Log.w(TAG, "Exception handling log entry, invalid data?", e);
            }
        }
        else
        {
            Log.e(TAG, "Tried to add log entry " + data.getLogEntry() + ", but writeThread is not alive");
        }
    }

    /* Checks if external storage is available for read and write */
    @Override
    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state))
        {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    @Override
    public boolean isExternalStorageReadable()
    {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            return true;
        }
        return false;
    }

    @Override
    public File getStorageFile(String fileName) throws IOException
    {
        // Get the directory for the user's public pictures directory.
        File dir = Environment.getExternalStoragePublicDirectory(Environment.getExternalStorageDirectory() + "/documents");
        if(!dir.exists())
        {
            if(!dir.mkdirs())
            {
                Log.w(TAG, "documents -directory does not exist and could not be created, falling back to downloads");
            }
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if(!dir.exists())
            {
                Log.e(TAG, "Cannot find downloads directory, falling back to external storage public directory");
            }

            dir = Environment.getExternalStorageDirectory();
        }

        File file = new File(dir, fileName);

        if(file.exists())
        {
            file.delete();
            file.createNewFile();
        }
        else
        {
            file.createNewFile();
        }
        return file;
    }
}
