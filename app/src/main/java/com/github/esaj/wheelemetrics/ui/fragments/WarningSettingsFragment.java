package com.github.esaj.wheelemetrics.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;

import com.github.esaj.wheelemetrics.Preferences;
import com.github.esaj.wheelemetrics.ui.misc.WarningSpeedNumberPicker;

import java.util.Arrays;

import im.delight.android.baselib.Screen;
import test.ej.wheelemetricsproto.R;

/**
 * @author esaj
 */
public class WarningSettingsFragment extends Fragment
{
    private WarningSpeedNumberPicker[] warningPickers = new WarningSpeedNumberPicker[2];
    private CheckBox warningsEnabledCheckBox;
    private CheckBox warningsAudioEnabledCheckBox;
    private CheckBox warningsAudioPitchCheckBox;

    private CheckBox lockOrientationCheckBox;
    private CheckBox imperialUnitsCheckBox;

    private NumberPicker speedCorrectionFactorPicker;
    private NumberPicker currentCorrectionFactorPicker;


    private class TwoDigitNumberPickerFormatter implements NumberPicker.Formatter
    {
        public String format(int i)
        {
            return String.format("%02d", i);
        }
    }

    private class OneDecimalFixedPointNumberPickerFormatter implements NumberPicker.Formatter
    {
        public String format(int i)
        {
            return String.format("%.1f", i/10.0);
        }
    }

    private class ValueStoringWarningValueChangeListener implements NumberPicker.OnValueChangeListener
    {

        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal)
        {
            int index = ((WarningSpeedNumberPicker)picker).getWarningIndex();
            Double[] levels = Preferences.getVibrationWarningLevels();
            if(index > levels.length-1)
            {
                Double[] newLevels = new Double[index+1];
                System.arraycopy(levels, 0, newLevels, 0, levels.length);
                levels = newLevels;
            }
            levels[index] = (double)newVal;
            Arrays.sort(levels);

            //Bubblegum... :D
            if(oldVal < newVal && (levels[0] >= levels[1]))
            {
                levels[1] = levels[0] + 1;
            }
            else if(oldVal > newVal && (levels[0] >= levels[1]))
            {
                levels[0] = levels[1] - 1;
            }

            for(int i = 0; i < 2; i++)
            {
                warningPickers[i].setValue((int)Math.round(levels[i]));
            }

            Preferences.storeVibrationWarningLevels(levels);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.vibration_setting_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        warningsEnabledCheckBox = (CheckBox)view.findViewById(R.id.checkbox_warnings_enabled);
        warningsEnabledCheckBox.setChecked(Preferences.isVibrationWarningEnabled());
        warningsEnabledCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                Preferences.storeVibrationEnabled(isChecked);
            }
        });

        warningsAudioEnabledCheckBox = (CheckBox)view.findViewById(R.id.checkbox_audio_warnings_enabled);

        warningsAudioEnabledCheckBox.setChecked(Preferences.isAudioWarningEnabled());
        warningsAudioEnabledCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                Preferences.storeAudioWarningEnabled(isChecked);
            }
        });

        warningsAudioPitchCheckBox = (CheckBox)view.findViewById(R.id.checkbox_audio_pitch_enabled);
        warningsAudioPitchCheckBox.setChecked(Preferences.isAudioPitchEnabled());
        warningsAudioPitchCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                Preferences.storeAudioPitchEnabled(isChecked);
            }
        });

        lockOrientationCheckBox = (CheckBox)view.findViewById(R.id.lock_orientation);
        lockOrientationCheckBox.setChecked(Preferences.isLockOrientation());
        lockOrientationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    Screen.lockOrientation(getActivity());
                }
                else
                {
                    Screen.unlockOrientation(getActivity());
                }
                Preferences.storeLockOrientation(isChecked);
            }
        });

        imperialUnitsCheckBox = (CheckBox)view.findViewById(R.id.imperial_units);
        imperialUnitsCheckBox.setChecked(Preferences.isImperialUnits());
        imperialUnitsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                Preferences.storeImperialUnits(isChecked);
            }
        });

        warningPickers[0] = (WarningSpeedNumberPicker)view.findViewById(R.id.warning1_picker);
        warningPickers[1] = (WarningSpeedNumberPicker)view.findViewById(R.id.warning2_picker);

        warningPickers[0].setWarningIndex(0);
        warningPickers[0].setMinValue(4);
        warningPickers[0].setMaxValue(59);
        warningPickers[0].setOnLongPressUpdateInterval(75);
        warningPickers[0].setFormatter(new TwoDigitNumberPickerFormatter());
        warningPickers[0].setOnValueChangedListener(new ValueStoringWarningValueChangeListener());

        warningPickers[1].setWarningIndex(1);
        warningPickers[1].setMinValue(5);
        warningPickers[1].setMaxValue(60);
        warningPickers[1].setOnLongPressUpdateInterval(75);
        warningPickers[1].setFormatter(new TwoDigitNumberPickerFormatter());
        warningPickers[1].setOnValueChangedListener(new ValueStoringWarningValueChangeListener());

        Double[] warningLevels = Preferences.getVibrationWarningLevels();
        warningPickers[0].setValue((int)Math.round(warningLevels[0]));
        warningPickers[1].setValue((int)Math.round(warningLevels[1]));

        speedCorrectionFactorPicker = (NumberPicker)view.findViewById(R.id.speed_correction_factor);

        speedCorrectionFactorPicker.setMinValue(1);
        speedCorrectionFactorPicker.setMaxValue(20);
        speedCorrectionFactorPicker.setOnLongPressUpdateInterval(500);
        speedCorrectionFactorPicker.setFormatter(new OneDecimalFixedPointNumberPickerFormatter());
        speedCorrectionFactorPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                Preferences.storeSpeedCorrectionFactor(newVal / 10.0);
            }
        });
        speedCorrectionFactorPicker.setValue((int)(Preferences.getSpeedCorrectionFactor() * 10.0));

        currentCorrectionFactorPicker = (NumberPicker)view.findViewById(R.id.current_correction_factor);
        currentCorrectionFactorPicker.setMinValue(1);
        currentCorrectionFactorPicker.setMaxValue(20);
        currentCorrectionFactorPicker.setOnLongPressUpdateInterval(500);
        currentCorrectionFactorPicker.setFormatter(new OneDecimalFixedPointNumberPickerFormatter());
        currentCorrectionFactorPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                Preferences.storeCurrentCorrectionFactor(newVal / 10.0);
            }
        });
        currentCorrectionFactorPicker.setValue((int)(Preferences.getCurrentCorrectionFactor() * 10.0));

    }
}
