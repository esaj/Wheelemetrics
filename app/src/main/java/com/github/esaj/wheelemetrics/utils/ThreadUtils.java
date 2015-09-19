package com.github.esaj.wheelemetrics.utils;

/**
 * @author esaj
 */
public class ThreadUtils
{
    private ThreadUtils()
    {
        //Do not instantiate
    }

    public static void sleepIgnoringInterrupt(long time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch(InterruptedException e)
        {
            //Nevermind
        }
    }
}
