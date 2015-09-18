package com.github.gadgetfactory.wheelemetrics.data;

/**
 * "Temporary" measure for moving data for logging.
 *
 * 18.9.2015  In a few years, you can add a comment here saying "temporary, my ass!"
 *
 * @author esaj
 */
public class PlainStringLoggableData implements LoggableData
{
    private final String logString;

    public PlainStringLoggableData(String logString)
    {
        this.logString = logString;
    }

    @Override
    public String getLogEntry()
    {
        return logString;
    }

    @Override
    public String getLogEntryLegend()
    {
        return "";
    }

    @Override
    public double getVoltage()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getSpeed()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getTrip()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getCurrent()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getTemperature()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getOdo()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVoltageString()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSpeedString()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTripString()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCurrentString()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTemperatureString()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getOdoString()
    {
        throw new UnsupportedOperationException();
    }
}
