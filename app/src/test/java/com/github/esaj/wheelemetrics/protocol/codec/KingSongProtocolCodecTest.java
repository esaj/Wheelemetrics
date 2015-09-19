package com.github.esaj.wheelemetrics.protocol.codec;

import com.github.esaj.wheelemetrics.data.GotwayKingSongLoggableData;
import com.github.esaj.wheelemetrics.data.LoggableData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author esaj
 */
public class KingSongProtocolCodecTest
{
    private KingSongProtocolCodec codec;

    @Before
    public void setUp()
    {
        codec = new KingSongProtocolCodec();
    }

    @Test(timeout = 1000L)
    public void testKingSongData() throws IOException
    {
        byte[] kingSongData = new byte[]{0x00, 0x01, (byte)0xFF, (byte)0xF8, (byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x18, (byte)0x4B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0E, (byte)0xF9, (byte)0xC0, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8, (byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x18, (byte)0x4B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0E, (byte)0xF9, (byte)0xC0, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8, (byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x18, (byte)0x4B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0E, (byte)0xF9, (byte)0xC0, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8};
        ByteArrayInputStream in = new ByteArrayInputStream(kingSongData);

        //Read all the packets in data (it's always the same data though ;))
        for(int i = 0; i < 3; i++)
        {
            LoggableData result = codec.decode(in);
            Assert.assertTrue(result instanceof GotwayKingSongLoggableData);
            Assert.assertEquals(62.19, result.getVoltage(), 0.001);
            Assert.assertEquals(0.14, result.getCurrent(), 0.001);
            Assert.assertEquals(0.0, result.getSpeed(), 0.001);
            Assert.assertEquals(0.0, result.getTrip(), 0.001);
            Assert.assertEquals(31.824, result.getTemperature(), 0.001);

            Assert.assertEquals(kingSongData.length - (i+1) * 24, in.available());
        }
    }

}
