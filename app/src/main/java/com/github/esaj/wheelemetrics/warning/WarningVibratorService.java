package com.github.esaj.wheelemetrics.warning;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.esaj.wheelemetrics.Preferences;
import com.github.esaj.wheelemetrics.bluetooth.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import test.ej.wheelemetricsproto.R;

/**
 * @author esaj
 */
public class WarningVibratorService extends Service
{
    private static final String TAG = "WarningVibratorService";

    public static final String VIBRATE_DATA = "wheelemetrics_vibr";
    public static final String VIBRATE_DATA_REPEAT = "wheelemetrics_vibr_repeat";

    private Vibrator vibrator;

    private MediaPlayer warning1;
    private MediaPlayer warning2;

    //Bug fix: MediaPlayer.isPlaying sometimes keep returning true even when the sound is no longer playing
    private long warning1StartedTime;
    private long warning2StartedTime;

    private int currentWarningLevel = Integer.MIN_VALUE;

    private double[] warningLevels = null;

    private long[][] warningPatterns =
    {
        {0, 100, 500, 0},
        {50, 100}
    };

    private boolean vibrationEnabled = true;
    private boolean audio = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        setUp();

        warning1 = MediaPlayer.create(getApplicationContext(), R.raw.annoy1);
        warning2 = MediaPlayer.create(getApplicationContext(), R.raw.annoy2);
    }

    @Override
    public void onDestroy()
    {
        Log.i(TAG, "Stopping vibration warning service");
        stopAudio();
        stopVibration();
        warning1.release();
        warning2.release();
        stopSelf();
    }


    private class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            if(Preferences.VIBRATION_WARNING_LEVELS.equals(key))
            {
                Double[] values = Preferences.getVibrationWarningLevels();
                setWarningLevels(Arrays.asList(values));
                currentWarningLevel = -1;
            }
            else if(Preferences.VIBRATION_ENABLED.equals(key))
            {
                vibrationEnabled = Preferences.isVibrationWarningEnabled();
                if(!vibrationEnabled)
                {
                    stopVibration();
                }
                currentWarningLevel = -1;
            }
            else if(Preferences.AUDIO_ENABLED.equals(key))
            {
                audio = Preferences.isAudioWarningEnabled();
                if(!audio)
                {
                    stopAudio();
                }
                currentWarningLevel = -1;
            }
        }
    }

    private PreferenceChangeListener preferenceChangeListener = new PreferenceChangeListener();

    private void setWarningLevels(List<Double> levels)
    {
        Collections.sort(levels);

        warningLevels = new double[levels.size()];
        int index = 0;
        for(Double val : levels)
        {
            Log.e(TAG, "WARNING LEVEL " + (index + 1) + ": " + val);
            warningLevels[index++] = val;
        }
    }

    public double[] getWarningLevels()
    {
        return warningLevels;
    }

    public void setVibratePattern(long[] pattern)
    {
        vibrator.vibrate(pattern, 0);
    }

    public void stopVibration()
    {
        vibrator.cancel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //This shouldn't even happen anymore, but just to be safe...
        if(this.vibrator == null)
        {
            setUp();
        }

        if(intent == null)
        {
            //Weird stuff happening on vee's Huawei
            return START_STICKY;
        }

        if(intent.hasExtra(VIBRATE_DATA))
        {
            vibrator.vibrate(intent.getLongArrayExtra(VIBRATE_DATA), intent.getIntExtra(VIBRATE_DATA_REPEAT, -1));
            return START_NOT_STICKY;    //Don't want to start sticky, in case this was something like BT connection loss on shutdown
        }

        if(intent.hasExtra(Constants.SPEED_DATA))
        {
            double speed = intent.getDoubleExtra(Constants.SPEED_DATA, 0);
            checkSpeedWarning(speed);
        }

        return START_STICKY;
    }

    /**
     * Separated to method as it looks like the context isn't usable yet in constructor?
     */
    private void setUp()
    {
        this.vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);

        Preferences.addPreferenceChangeListener(getApplicationContext(), preferenceChangeListener);

        Double[] values = Preferences.getVibrationWarningLevels();
        setWarningLevels(Arrays.asList(values));
        vibrationEnabled = Preferences.isVibrationWarningEnabled();
    }

    private void checkSpeedWarning(double speed)
    {
        int warningLevel = -1;

        //Log.d(TAG, "SPEED DATA: " + speed);


        //Warn on motor turning either way
        speed = Math.abs(speed);
        for(int i = 0; i < warningLevels.length; i++)
        {
            if(speed >= warningLevels[i])
            {
                warningLevel = i;
            }
            else
            {
                break;
            }
        }

        //TODO: FIXME: Testing code
        if(audio)
        {
            if(warningLevel == 0)
            {
                if(!warning1.isPlaying() || (System.currentTimeMillis() - warning1StartedTime > 100))
                {
                    warning1.start();
                    warning1StartedTime = System.currentTimeMillis();
                }
            }
            else if(warningLevel == 1)
            {
                if(!warning2.isPlaying() || (System.currentTimeMillis() - warning1StartedTime > 490))
                {
                    warning2.start();
                    warning2StartedTime = System.currentTimeMillis();
                }
            }
        }

        if(warningLevel != currentWarningLevel)
        {
            //Log.d(TAG, "SPEED " + speed + ", LEVEL CHANGE TO : " + warningLevel + ", current: " + currentWarningLevel + ", level 0: " + warningLevels[0] + ", level 1: " + warningLevels[1]);
            currentWarningLevel = warningLevel;

            if(warningLevel < 0)
            {
                stopVibration();
                stopAudio();
            }
            else
            {
                if(vibrationEnabled)
                {
                    if(warningLevel < warningPatterns.length)
                    {
                        setVibratePattern(warningPatterns[warningLevel]);
                    }
                    else
                    {
                        if(warningPatterns.length == 0)
                        {
                            Log.w(TAG, "No warning patterns defined");
                        }
                        else
                        {
                            //Use highest level
                            setVibratePattern(warningPatterns[warningPatterns.length - 1]);
                        }
                    }
                }
            }
        }
    }

    private void stopAudio()
    {
        if(warning1.isPlaying())
        {
            warning1.stop();
        }
        if(warning2.isPlaying())
        {
            warning2.stop();
        }
    }
}
