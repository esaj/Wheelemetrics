package com.github.esaj.wheelemetrics.protocol.resolver;

import com.github.esaj.wheelemetrics.protocol.codec.GotwayProtocolCodec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author esaj
 */
public class GotwayProtocolResolverTest
{
    private GotwayProtocolResolver resolver;

    @Before
    public void setUp()
    {
        resolver = new GotwayProtocolResolver();
    }

    @Test
    public void testGetRequiredBytes()
    {
        Assert.assertEquals("Please document your changes if this is altered in implementation", 48 * 2 - 1, resolver.getRequiredMinimumBytes());
    }

    @Test
    public void testWheelName()
    {
        Assert.assertEquals("Gotway", resolver.getWheelName());
    }

    @Test
    public void testGetProtocolCodec()
    {
        Assert.assertTrue(resolver.getProtocolCodec() instanceof GotwayProtocolCodec);
    }


    @Test
    public void testMatching()
    {
        byte[] perfectMatchData = new byte[]{(byte)0x04, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x19, (byte)0xA7, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xE0, (byte)0xF8, (byte)0xBD, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8, (byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x00, (byte)0x09, (byte)0x1A, (byte)0x9D, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        Assert.assertEquals(1.0D, resolver.match(perfectMatchData), 0.000001D);
    }

    @Test
    public void testNoMatch()
    {
        byte[] kingSongData = new byte[]{(byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x18, (byte)0x4B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0E, (byte)0xF9, (byte)0xC0, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8, (byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x18, (byte)0x4B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0E, (byte)0xF9, (byte)0xC0, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8, (byte)0x00, (byte)0x18, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x5A, (byte)0x55, (byte)0xAA, (byte)0x18, (byte)0x4B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0E, (byte)0xF9, (byte)0xC0, (byte)0x00, (byte)0x01, (byte)0xFF, (byte)0xF8};
        Assert.assertEquals(0.0, resolver.match(kingSongData), 0.00000001);
    }
}
