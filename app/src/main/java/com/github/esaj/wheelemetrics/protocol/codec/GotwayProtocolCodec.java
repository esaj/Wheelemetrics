package com.github.esaj.wheelemetrics.protocol.codec;

import com.github.esaj.wheelemetrics.data.GotwayKingSongLoggableData;
import com.github.esaj.wheelemetrics.data.LoggableData;
import com.github.esaj.wheelemetrics.utils.BinaryUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * ProtocolCodec-implementation for Gotway
 *
 * @author esaj
 */
public class GotwayProtocolCodec implements ProtocolCodec
{
    public static final byte[] MSG_TAG = new byte[]{0x04, 0x18, 0x5A, 0x5A, 0x5A, 0x5A, 0x55, (byte)0xAA};
    public static final byte[] ODO_TAG = new byte[]{(byte)0xF8, 0x00, 0x18, 0x5A, 0x5A, 0x5A, 0x5A, 0x55, (byte)0xAA};

    private int lastKnownVoltage = 0;
    private int lastKnownSpeed = 0;
    private int lastKnownRunNow = 0;
    private int lastKnownCurrent = 0;
    private int lastKnownTemp = 0;
    private int lastKnownOdo = 0;

    @Override
    public LoggableData decode(InputStream dataStream) throws IOException
    {
        byte[] data = new byte[12];

        boolean tagFound = false;
        int tagHit = 0;
        while(!tagFound)
        {
            int read = dataStream.read();
            if((byte)read == MSG_TAG[tagHit])
            {
                tagHit++;
                if(tagHit == MSG_TAG.length)
                {
                    tagFound = true;
                }
            }
            else
            {
                tagHit = 0;
            }
        }

        //Read voltage etc.
        for(int i = 0; i < 12; i++)
        {
            data[i] = (byte)dataStream.read();
        }

        readData(data);

        tagFound = false;
        tagHit = 0;
        while(!tagFound)
        {
            int read = dataStream.read();
            if((byte)read == ODO_TAG[tagHit])
            {
                tagHit++;
                if(tagHit == ODO_TAG.length)
                {
                    tagFound = true;
                }
            }
            else
            {
                tagHit = 0;
            }
        }

        //Read odo
        for(int i = 0; i < 4; i++)
        {
            data[i] = (byte)dataStream.read();
        }

        lastKnownOdo = BinaryUtils.intFrom32bitBE(data, 0);

        return new GotwayKingSongLoggableData(lastKnownVoltage, lastKnownSpeed, lastKnownRunNow, lastKnownCurrent, lastKnownTemp, lastKnownOdo);
    }

    private void readData(byte[] data)
    {
        lastKnownVoltage = BinaryUtils.shortFrom16bitBE(data, 0);
        lastKnownSpeed = BinaryUtils.shortFrom16bitBE(data, 2);
        lastKnownRunNow = BinaryUtils.intFrom32bitBE(data, 4);
        lastKnownCurrent = BinaryUtils.shortFrom16bitBE(data, 8);
        lastKnownTemp = BinaryUtils.shortFrom16bitBE(data, 10);
    }
}
