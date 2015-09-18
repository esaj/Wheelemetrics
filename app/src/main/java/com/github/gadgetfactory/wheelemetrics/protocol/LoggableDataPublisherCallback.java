package com.github.gadgetfactory.wheelemetrics.protocol;

import com.github.gadgetfactory.wheelemetrics.data.LoggableData;

/**
 * @author esaj
 */
public interface LoggableDataPublisherCallback
{
    public void publishLoggableData(LoggableData data);
}
