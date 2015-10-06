package com.github.esaj.wheelemetrics.ui.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.android.bluetoothchat.DeviceListActivity;
import com.github.esaj.wheelemetrics.Preferences;
import com.github.esaj.wheelemetrics.bluetooth.BluetoothService;
import com.github.esaj.wheelemetrics.data.record.TelemetryFileService;
import com.github.esaj.wheelemetrics.data.record.TelemetryFileServiceImpl;
import com.github.esaj.wheelemetrics.ui.fragments.MainFragment;
import com.github.esaj.wheelemetrics.ui.fragments.WarningSettingsFragment;
import com.github.esaj.wheelemetrics.ui.misc.RecordButtonClickListener;
import com.github.esaj.wheelemetrics.utils.ThreadUtils;
import com.github.esaj.wheelemetrics.warning.WarningVibratorService;

import test.ej.wheelemetricsproto.R;

/**
 * @author esaj
 */
public class MainActivity extends FragmentActivity
{
    public static final String TAG = "MainActivity";

    private Button speedWarningsButton;
    private Button graphButton;
    private Button recordButton;

    private MainFragment mainFragment;
    private WarningSettingsFragment warningSettingsFragment;

    private TelemetryFileService telemetryFileService;

    private ServiceConnection telemetryFileServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder binder)
        {
            try
            {
                Log.i(TAG, "Connected to TelemetryFileService");
                TelemetryFileServiceImpl.TelemetryFileServiceBinder teleBinder = (TelemetryFileServiceImpl.TelemetryFileServiceBinder)binder;
                telemetryFileService = teleBinder.getService();

                recordButton = (Button)findViewById(R.id.record);
                recordButton.setOnClickListener(new RecordButtonClickListener(MainActivity.this, telemetryFileService));
            }
            catch(Exception e)
            {
                Log.e(TAG, "Error occurred while registering TelemetryFileService", e);
            }
        }

        public void onServiceDisconnected(ComponentName className)
        {
            telemetryFileService = null;
        }
    };

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.remove(mainFragment);
                        transaction.remove(warningSettingsFragment);
                        transaction.commit();

                        mainFragment.detachBluetooth();

                        Intent stopBtIntent = new Intent(MainActivity.this, BluetoothService.class);
                        stopService(stopBtIntent);

                        //Give the threads a moment to die off
                        ThreadUtils.sleepIgnoringInterrupt(2000);

                        unbindService(telemetryFileServiceConnection);
                        Intent stopTeleFileIntent = new Intent(MainActivity.this, TelemetryFileServiceImpl.class);
                        stopService(stopTeleFileIntent);
                        MainActivity.this.finish();

                        Intent stopVibrationIntent = new Intent(MainActivity.this, WarningVibratorService.class);
                        stopService(stopVibrationIntent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Preferences.setContext(getApplicationContext());

        setContentView(R.layout.activity_main);

        //Always keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(savedInstanceState == null)
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mainFragment = new MainFragment();
            transaction.replace(R.id.main_fragment, mainFragment);

            warningSettingsFragment = new WarningSettingsFragment();
            transaction.replace(R.id.warnings_fragment, warningSettingsFragment);
            transaction.commit();

            setupServices();
        }

        setupUi();
        //TODO: Savedinstance handling?
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }


    private void setupServices()
    {
        Intent startVibrationWarnings = new Intent(this, WarningVibratorService.class);
        startService(startVibrationWarnings);

        Intent startTeleIntent = new Intent(this, TelemetryFileServiceImpl.class);
        startService(startTeleIntent);
        bindService(startTeleIntent, telemetryFileServiceConnection, Context.BIND_AUTO_CREATE);

//        Intent receiverIntent = new Intent(this, SpeedDataReceiver.class);
//        boolean isRunning = (PendingIntent.getBroadcast(this, 0, receiverIntent, PendingIntent.FLAG_NO_CREATE) != null);
//        if(!isRunning) {
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, receiverIntent, 0);
//            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 3000, pendingIntent);
//        }
    }

    public void setupUi()
    {
        View warningSettingsFragment = findViewById(R.id.warnings_fragment);
        warningSettingsFragment.setVisibility(View.GONE);

        speedWarningsButton = (Button)findViewById(R.id.button_warnings);
        graphButton = (Button)findViewById(R.id.button_graph);

        speedWarningsButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                View warningSettingsFragment = findViewById(R.id.warnings_fragment);
                if(warningSettingsFragment.getVisibility() == View.VISIBLE)
                {
                    warningSettingsFragment.setVisibility(View.GONE);
                }
                else
                {
                    warningSettingsFragment.setVisibility(View.VISIBLE);
                }
            }
        });

        graphButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                View mainFragmentView = findViewById(R.id.graphview);
                if(mainFragmentView.getVisibility() == View.VISIBLE)
                {
                    mainFragmentView.setVisibility(View.GONE);
                }
                else
                {
                    mainFragmentView.setVisibility(View.VISIBLE);
                }
            }
        });

        Button connectButton = (Button)findViewById(R.id.button_connect);
        connectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent serverIntent = new Intent(mainFragment.getActivity(), DeviceListActivity.class);
                mainFragment.startActivityForResult(serverIntent, MainFragment.REQUEST_CONNECT_DEVICE_SECURE);
            }
        });
    }
}
