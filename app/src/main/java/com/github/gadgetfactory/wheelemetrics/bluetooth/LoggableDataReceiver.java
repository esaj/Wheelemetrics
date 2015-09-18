package com.github.gadgetfactory.wheelemetrics.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.gadgetfactory.wheelemetrics.data.record.TelemetryFileServiceImpl;

public class LoggableDataReceiver extends BroadcastReceiver
{
    private static final String TAG = "LogDataReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.hasExtra(Constants.MESSAGE_STRING_LOGGABLEDATA))
        {
            Log.d(TAG, "Log data received");

            Intent logDataIntent = new Intent(context, TelemetryFileServiceImpl.class);
            logDataIntent.putExtras(intent);
            context.startService(logDataIntent);
        }
    }
}