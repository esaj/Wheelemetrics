package com.github.esaj.wheelemetrics.data;

/**
 * @author esaj
 */
public abstract class AbstractCSVLoggableData implements LoggableData
{
    private static final String SEPARATOR = ",";

    protected String logEntry;

    protected long timestamp;
    protected int voltage;  // 100.0D
    protected int speed;  // /100.0D * 3.6D
    protected int trip; // 1000.0D
    protected int current;  // 100.0D
    protected int temperature; // / 340.0D + 36.53D
    protected int odo; // / 1000.0D

    protected AbstractCSVLoggableData()
    {
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getLogEntry()
    {
        StringBuffer buffer = new StringBuffer(128);
        buffer.append(timestamp);
        buffer.append(",");
        buffer.append(getSpeedString());
        buffer.append(",");
        buffer.append((int)(getVoltage() * getCurrent()));
        buffer.append(",");
        buffer.append(getVoltageString());
        buffer.append(",");
        buffer.append(getCurrentString());
        buffer.append(",");
        buffer.append(getTemperatureString());
        buffer.append(",");
        buffer.append(getTripString());
        buffer.append(",");
        buffer.append(getOdoString());
        buffer.append("\r\n");

        return buffer.toString();
    }

    @Override
    public String getLogEntryLegend()
    {
        return "Timestamp,Speed,Power,Voltage,Current,Temperature,Trip,Odo";
    }
}
