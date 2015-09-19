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
public class GotwayProtocolCodecTest
{
    private GotwayProtocolCodec codec;

    @Before
    public void setUp()
    {
        codec = new GotwayProtocolCodec();
    }

    @Test(timeout = 1000L)
    public void testGotwayData() throws IOException
    {
        byte[] gotwayData = new byte[]{(byte)0x04, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x19, (byte)0xA7, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xE0, (byte)0xF8, (byte)0xBD, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8, (byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x00, (byte)0x09, (byte)0x1A, (byte)0x9D, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        ByteArrayInputStream in = new ByteArrayInputStream(gotwayData);

        LoggableData result = codec.decode(in);
        Assert.assertTrue(result instanceof GotwayKingSongLoggableData);
        Assert.assertEquals(65.67, result.getVoltage(), 0.001);
        Assert.assertEquals(-0.32, result.getCurrent(), 0.001);
        Assert.assertEquals(-0.036, result.getSpeed(), 0.001);
        Assert.assertEquals(0.0, result.getTrip(), 0.001);
        Assert.assertEquals(31.062, result.getTemperature(), 0.001);
        Assert.assertEquals(596.637, result.getOdo(), 0.0001);
    }
}
