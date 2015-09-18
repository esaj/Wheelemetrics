package com.github.gadgetfactory.wheelemetrics.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.view.View;

public class UIBackgroundColorWarningBluetoothObserver extends BluetoothObserverAdapter
{
    private Activity activity;
    private View view;
    private volatile boolean colorChanged = false;

    public UIBackgroundColorWarningBluetoothObserver(Activity activity, View view)
    {
        this.activity = activity;
        this.view = view;
        //TODO: Can the original background color be read?
        //originalColor = ((ColorDrawable)view.getBackground()).getColor();
    }

    @Override
    public void connectionLost(BluetoothDevice device)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                view.setBackgroundColor(Color.RED);
                colorChanged = true;
            }
        });
    }

    @Override
    public void connectionOpened(BluetoothSocket socket, BluetoothDevice device, boolean secure)
    {
        if(colorChanged)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    view.setBackgroundColor(Color.BLACK);   //Hard-coded for now
                    colorChanged = false;
                }
            });
        }
    }
}
