package com.github.esaj.wheelemetrics.protocol.codec;

import com.github.esaj.wheelemetrics.data.LoggableData;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author esaj
 */
public interface ProtocolCodec
{
    public LoggableData decode(InputStream dataStream) throws IOException;

    //TODO: Supported commands list
    //public Collection<CommandDefinition> getCommandList();

    //TODO: Command encoding
    //public byte[] encode(CommandDefinition, params);

}
