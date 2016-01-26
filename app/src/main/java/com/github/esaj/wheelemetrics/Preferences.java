package com.github.esaj.wheelemetrics;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author esaj
 */
public class Preferences
{
    private static final String TAG = "Preferences";

    private static SharedPreferences preferences;

    public static final String VIBRATION_ENABLED = "vibr";
    public static final String VIBRATION_WARNING_LEVELS = "vibrlevels";
    public static final String AUDIO_ENABLED = "audio";
    public static final String AUDIO_PITCH_ENABLED = "pitch";
    public static final String LOCK_ORIENTATION = "lockOrient";
    public static final String IMPERIAL_UNITS = "imperial";

    public static final String SPEED_CORRECTION_FACTOR = "speedcorr";
    public static final String CURRENT_CORRECTION_FACTOR = "currcorr";

    private static final Set<String> DEFAULT_WARNING_LEVELS = new HashSet<String>();

    static
    {
        DEFAULT_WARNING_LEVELS.add("5.0");
        DEFAULT_WARNING_LEVELS.add("15.0");
    }

    private interface StringTransformer<T>
    {
        public <T> T transformFromString(String string);
        public String transformToString(T value);
    }

    private static final StringTransformer<Double> doubleTransformer = new StringTransformer<Double>()
    {
        @Override
        public Double transformFromString(String string)
        {
            return Double.parseDouble(string);
        }

        @Override
        public String transformToString(Double value)
        {
            return Double.toString(value);
        }
    };

    public static void setContext(Context context)
    {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences getPreferences(Context context)
    {
        if(preferences == null)
        {
            setContext(context);
        }
        return preferences;
    }

    public static void addPreferenceChangeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener)
    {
        getPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void removePreferenceChangeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener)
    {
        getPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static Double[] getVibrationWarningLevels()
    {
        Set<String> values = preferences.getStringSet(VIBRATION_WARNING_LEVELS, DEFAULT_WARNING_LEVELS);
        List<Double> warnLevels = getValuesFromStringSet(values, doubleTransformer);
        if(warnLevels.size() < 2)
        {
            warnLevels =  getValuesFromStringSet(DEFAULT_WARNING_LEVELS, doubleTransformer);
        }
        Collections.sort(warnLevels);
        return warnLevels.toArray(new Double[warnLevels.size()]);
    }

    public static void storeVibrationWarningLevels(Double[] levels)
    {
        Set<String> values = getStringSetFromValues(levels, doubleTransformer);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(VIBRATION_WARNING_LEVELS, values);
        editor.apply();
    }

    public static boolean isVibrationWarningEnabled()
    {
        return preferences.getBoolean(VIBRATION_ENABLED, true);
    }

    public static void storeVibrationEnabled(boolean enabled)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(VIBRATION_ENABLED, enabled);
        editor.apply();
    }

    public static boolean isAudioWarningEnabled()
    {
        return preferences.getBoolean(AUDIO_ENABLED, true);
    }

    public static void storeAudioWarningEnabled(boolean enabled)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AUDIO_ENABLED, enabled);
        editor.apply();
    }

    public static boolean isAudioPitchEnabled()
    {
        return preferences.getBoolean(AUDIO_PITCH_ENABLED, false);
    }

    public static void storeAudioPitchEnabled(boolean enabled)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AUDIO_PITCH_ENABLED, enabled);
        editor.apply();
    }

    public static boolean isLockOrientation()
    {
        return preferences.getBoolean(LOCK_ORIENTATION, false);
    }

    public static void storeLockOrientation(boolean enabled)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(LOCK_ORIENTATION, enabled);
        editor.apply();
    }

    public static boolean isImperialUnits()
    {
        return preferences.getBoolean(IMPERIAL_UNITS, false);
    }

    public static void storeImperialUnits(boolean enabled)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(IMPERIAL_UNITS, enabled);
        editor.apply();
    }

    public static double getSpeedCorrectionFactor()
    {
        return (double)preferences.getFloat(SPEED_CORRECTION_FACTOR, 1.0f);
    }

    public static void storeSpeedCorrectionFactor(double corrFactor)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(SPEED_CORRECTION_FACTOR, (float)corrFactor);
        editor.apply();
    }

    public static double getCurrentCorrectionFactor()
    {
        return (double)preferences.getFloat(CURRENT_CORRECTION_FACTOR, 1.0f);
    }

    public static void storeCurrentCorrectionFactor(double corrFactor)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(CURRENT_CORRECTION_FACTOR, (float)corrFactor);
        editor.apply();
    }

    private static <T> T getValueFromString(String value, StringTransformer<T> transformer)
    {
        return transformer.transformFromString(value);
    }

    private static <T> List<T> getValuesFromStringSet(Set<String> values, StringTransformer<T> transformer)
    {
        List<T> resultValues = new ArrayList<T>(values.size());
        for(String val : values)
        {
            try
            {
                resultValues.add(getValueFromString(val, transformer));
            }
            catch(NumberFormatException e)
            {
                Log.e(TAG, "Transform-failure for string: " + val + ", transformer was " + (transformer.getClass().getSimpleName()), e);
            }
        }
        return resultValues;
    }

    private static <T> String getStringFromValue(T value, StringTransformer<T> transformer)
    {
        return transformer.transformToString(value);
    }

    private static <T> Set<String> getStringSetFromValues(T[] values, StringTransformer<T> transformer)
    {
        Set<String> set = new HashSet<String>(values.length);

        for(T val : values)
        {
            try
            {
                set.add(getStringFromValue(val, transformer));
            }
            catch(Exception e)
            {
                Log.e(TAG, "Transform-failure for value: " + val + ", transformer was " + (transformer.getClass().getSimpleName()), e);
            }
        }
        return set;
    }
}
