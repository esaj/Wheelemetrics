package com.github.esaj.wheelemetrics.warning;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.esaj.wheelemetrics.Preferences;
import com.github.esaj.wheelemetrics.bluetooth.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<Integer, Integer> warningSounds = new HashMap<Integer, Integer>(2);
    private SoundPool soundPool;
    private Integer streamId = null;
    private long lastSoundStartTime = 0;

    private int currentWarningLevel = Integer.MIN_VALUE;

    private double[] warningLevels = null;

    private long[][] warningPatterns =
    {
        {0, 100, 500, 0},
        {50, 100}
    };

    private boolean vibrationEnabled = true;
    private boolean audio = true;
    private boolean pitchControl = false;

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

        //soundPool = new SoundPool.Builder().setMaxStreams(1).build(); //Not working on older devices (Builder does not exist)
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        warningSounds.put(0, soundPool.load(getApplicationContext(), R.raw.annoy1, 1));
        warningSounds.put(1, soundPool.load(getApplicationContext(), R.raw.annoy2, 1));
    }

    @Override
    public void onDestroy()
    {
        Log.i(TAG, "Stopping vibration warning service");
        stopAudio();
        stopVibration();
        soundPool.release();
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
                stopVibration();
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
            else if(Preferences.AUDIO_PITCH_ENABLED.equals(key))
            {
                pitchControl = Preferences.isAudioPitchEnabled();
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
        stopVibration();
        Preferences.addPreferenceChangeListener(getApplicationContext(), preferenceChangeListener);

        Double[] values = Preferences.getVibrationWarningLevels();
        setWarningLevels(Arrays.asList(values));
        vibrationEnabled = Preferences.isVibrationWarningEnabled();
    }

    private void checkSpeedWarning(double speed)
    {
        int warningLevel = -1;
        boolean warningLevelChanged = false;

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

        if(warningLevel != currentWarningLevel)
        {
            //Log.d(TAG, "SPEED " + speed + ", LEVEL CHANGE TO : " + warningLevel + ", current: " + currentWarningLevel + ", level 0: " + warningLevels[0] + ", level 1: " + warningLevels[1]);
            currentWarningLevel = warningLevel;
            warningLevelChanged = true;

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

        if(warningLevel > -1)
        {
            if(audio)
            {
                try
                {
                    if(warningLevelChanged || (System.currentTimeMillis() - lastSoundStartTime > 100))
                    {
                        Integer soundId = warningSounds.get(warningLevel);
                        if(soundId != null)
                        {
                            float pitch = 1.0f;

                            if(pitchControl)
                            {
                                //Pitch-control
                                pitch = (float)(speed / (warningLevels[warningLevel]));

                                if(warningLevels.length > warningLevel + 1)
                                {
                                    double difference = warningLevels[warningLevel + 1] - warningLevels[warningLevel];
                                    pitch = 1.0f + (float)(1.0 - ((warningLevels[warningLevel + 1] - speed) / difference));
                                }

                                //Sanity checks
                                if(pitch > 2.0f)
                                {
                                    pitch = 2.0f;
                                }
                                else if(pitch < 1.0f)
                                {
                                    pitch = 1.0f;
                                }
                            }

                            streamId = soundPool.play(soundId, 1.0f, 1.0f, 0, 0, pitch);
                            lastSoundStartTime = System.currentTimeMillis();
                        }
                    }
                }
                catch(Exception e)
                {
                    Log.w(TAG, "SoundPool threw an exception", e);
                }
            }
        }

    }

    private void stopAudio()
    {
        try
        {
            if(streamId != null)
            {
                soundPool.stop(streamId);
            }
        }
        catch(Exception e)
        {
            Log.w(TAG, "SoundPool threw an exception", e);
        }
    }
}
