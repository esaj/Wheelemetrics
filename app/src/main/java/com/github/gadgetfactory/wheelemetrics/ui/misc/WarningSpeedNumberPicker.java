package com.github.gadgetfactory.wheelemetrics.ui.misc;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

/**
 * @author esaj
 */
public class WarningSpeedNumberPicker extends NumberPicker
{
    private int warningIndex;

    public WarningSpeedNumberPicker(Context context)
    {
        super(context);
    }

    public WarningSpeedNumberPicker(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public WarningSpeedNumberPicker(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public void setWarningIndex(int warningIndex)
    {
        this.warningIndex = warningIndex;
    }

    public int getWarningIndex()
    {
        return warningIndex;
    }
}
