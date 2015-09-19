package com.github.gadgetfactory.wheelemetrics.protocol;

import com.github.gadgetfactory.wheelemetrics.protocol.codec.ProtocolDataRelay;
import com.github.gadgetfactory.wheelemetrics.protocol.resolver.GotwayProtocolResolver;
import com.github.gadgetfactory.wheelemetrics.protocol.resolver.KingSongProtocolResolver;
import com.github.gadgetfactory.wheelemetrics.protocol.resolver.ProtocolResolver;
import com.github.gadgetfactory.wheelemetrics.utils.BinaryUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Autodetection logic for different wheel protocols
 *
 * @author esaj
 */
public class ProtocolAutodetectionLogic
{
    private static ProtocolResolver[] resolvers = new ProtocolResolver[]
            {
                    new GotwayProtocolResolver(),
                    new KingSongProtocolResolver()
            };

    public ProtocolDataRelay autodetectProtocol(InputStream in, LoggableDataPublisherCallback callback) throws IOException
    {
        int minBytes = 0;
        for(ProtocolResolver resolver : resolvers)
        {
            if(minBytes < resolver.getRequiredMinimumBytes())
            {
                minBytes = resolver.getRequiredMinimumBytes();
            }
        }

        byte[] buffer = new byte[minBytes];
        BinaryUtils.readNumberOfBytes(in, buffer, 0, minBytes);

        double highestMatch = 0;
        ProtocolResolver bestCandidate = null;
        for(ProtocolResolver resolver : resolvers)
        {
            double match = resolver.match(buffer);
            if(match > highestMatch)
            {
                highestMatch = match;
                bestCandidate = resolver;

                if(match >= 0.9999999D)
                {
                    //Pretty much perfect match, don't bother further
                    break;
                }
            }
        }

        if(bestCandidate == null)
        {
            return null;
        }

        return new ProtocolDataRelay(bestCandidate.getProtocolCodec(), callback);
    }


}
