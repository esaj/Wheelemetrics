package com.github.esaj.wheelemetrics.protocol;

import com.github.esaj.wheelemetrics.data.LoggableData;

/**
 * @author esaj
 */
public interface LoggableDataPublisherCallback
{
    public void publishLoggableData(LoggableData data);
}
