package com.github.esaj.wheelemetrics.data;

/**
 * @author esaj
 */
public interface LoggableData
{
    /**
     * Returns voltage in volts
     * @return  Volts
     */
    double getVoltage();

    /**
     * Returns speed in kmh or rpm
     * @return kmh or rpm
     */
    double getSpeed();

    /**
     * Returns trip-meter value in kilometers
     * @return Kilometers
     */
    double getTrip();

    /**
     * Returns current in amperes
     * @return Amperes
     */
    double getCurrent();

    /**
     * Returns temperature in celsius
     * @return Celsius
     */
    double getTemperature();

    /**
     * Returns odo-meter (total mileage) value in kilometers
     * @return Kilometers
     */
    double getOdo();

    /**
     * Returns comma-separated-value -string for data
     * @return Log entry
     */
    String getLogEntry();

    /**
     * Returns comma-separated-value -string containing legend for the actual CSV-values
     * @return Legend (like "Voltage,Speed,Current,...")
     */
    String getLogEntryLegend();

    String getVoltageString();

    String getSpeedString();

    String getTripString();

    String getCurrentString();

    String getTemperatureString();

    String getOdoString();
}
