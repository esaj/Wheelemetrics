package com.github.esaj.wheelemetrics.utils;

public class ConversionUtils
{
    public static double kilometersToMiles(double kilometers)
    {
        return kilometers * 0.621371192;
    }

    public static double celsiusToFahrenheit(double celsius)
    {
        return celsius * 1.8 + 32.0;
    }
}
