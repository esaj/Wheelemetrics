package com.github.gadgetfactory.wheelemetrics.data;

import com.github.gadgetfactory.wheelemetrics.utils.StringUtils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author esaj
 */
public class GotwayKingSongLoggableData extends  AbstractCSVLoggableData
{
    private static final NumberFormat twodigitFormat = NumberFormat.getInstance(Locale.US);
    static
    {
        twodigitFormat.setMaximumFractionDigits(2);
        twodigitFormat.setMinimumFractionDigits(2);
        twodigitFormat.setGroupingUsed(false);
    }


    public GotwayKingSongLoggableData(int voltage, int speed, int trip, int current, int temperature, int odo)
    {
        super();
        this.voltage = voltage;
        this.speed = speed;
        this.trip = trip;
        this.current = current;
        this.temperature = temperature;
        this.odo = odo;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public void setOdo(int odo)
    {
        this.odo = odo;
    }

    public void setVoltage(int voltage)
    {
        this.voltage = voltage;
    }

    public void setSpeed(int speed)
    {
        this.speed = speed;
    }

    public void setTrip(int trip)
    {
        this.trip = trip;
    }

    public void setCurrent(int current)
    {
        this.current = current;
    }

    public void setTemperature(int temperature)
    {
        this.temperature = temperature;
    }

    public void setLogEntry(String logEntry)
    {
        this.logEntry = logEntry;
    }


    public double getVoltage()
    {
        return voltage / 100.0D;
    }


    public double getSpeed()
    {
        return speed / 100.0D * 3.6D;
    }


    public double getTrip()
    {
        return trip / 1000.0D;
    }


    public double getCurrent()
    {
        return current / 100.0D;
    }

    public double getTemperature()
    {
        return temperature / 340.0D + 36.53D;
    }

    public double getOdo()
    {
        return odo / 1000.0d;
    }


    public String getVoltageString()
    {
        return StringUtils.getStringFromFixedPoint(voltage, 2);
    }


    public String getSpeedString()
    {
        return StringUtils.getStringFromFixedPoint((int)(speed * 3.6D), 2);
    }


    public String getTripString()
    {
        return StringUtils.getStringFromFixedPoint(trip, 3);
    }


    public String getCurrentString()
    {
        return StringUtils.getStringFromFixedPoint(current, 2);
    }


    public String getTemperatureString()
    {
        return twodigitFormat.format(temperature / 340.0D + 36.53D);
    }


    public String getOdoString()
    {
        return StringUtils.getStringFromFixedPoint(odo, 3);
    }
}
