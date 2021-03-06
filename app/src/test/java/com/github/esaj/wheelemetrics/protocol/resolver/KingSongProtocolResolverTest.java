package com.github.esaj.wheelemetrics.protocol.resolver;

import com.github.esaj.wheelemetrics.protocol.codec.KingSongProtocolCodec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author esaj
 */
public class KingSongProtocolResolverTest
{
    private KingSongProtocolResolver resolver;

    @Before
    public void setUp()
    {
        resolver = new KingSongProtocolResolver();
    }


    @Test
    public void testGetRequiredBytes()
    {
        Assert.assertEquals("Please document your changes if this is altered in implementation", 24 * 2 - 1, resolver.getRequiredMinimumBytes());
    }

    @Test
    public void testWheelName()
    {
        Assert.assertEquals("King Song", resolver.getWheelName());
    }

    @Test
    public void testGetProtocolCodec()
    {
        Assert.assertTrue(resolver.getProtocolCodec() instanceof KingSongProtocolCodec);
    }


    @Test
    public void testGotwayMatch()
    {
        byte[] gotwayData = new byte[]{(byte)0x04, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x19, (byte)0xA7, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xE0, (byte)0xF8, (byte)0xBD, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8, (byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x00, (byte)0x09, (byte)0x1A, (byte)0x9D, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        Assert.assertTrue(resolver.match(gotwayData) < 0.9);
    }

    @Test
    public void testKingSongMatch()
    {
        byte[] kingSongData = new byte[]{(byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x18, (byte)0x4B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0E, (byte)0xF9, (byte)0xC0, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8, (byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x18, (byte)0x4B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0E, (byte)0xF9, (byte)0xC0, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8, (byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x18, (byte)0x4B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0E, (byte)0xF9, (byte)0xC0, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8};
        Assert.assertEquals(1.0, resolver.match(kingSongData), 0.00000001);
    }
}
