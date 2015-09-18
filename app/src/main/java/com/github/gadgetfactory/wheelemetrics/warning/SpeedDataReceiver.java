package com.github.gadgetfactory.wheelemetrics.warning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.gadgetfactory.wheelemetrics.bluetooth.Constants;

/**
 * @author esaj
 */
public class SpeedDataReceiver extends BroadcastReceiver
{
    private static final String TAG = "SpeedDataReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.hasExtra(Constants.SPEED_DATA))
        {
            //Log.d(TAG, "Speed data received");

            Intent warningVibratorIntent = new Intent(context, WarningVibratorService.class);
            warningVibratorIntent.putExtras(intent);
            context.startService(warningVibratorIntent);
        }
    }
}
