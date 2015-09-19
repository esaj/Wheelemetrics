package com.github.esaj.wheelemetrics.utils;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Arrays;

/**
 * @author esaj
 */
public class BinaryUtilsTest
{
    @Test
    public void test32BitIntConversions()
    {
        int TESTVALUE = 12345678;//4E 61 BC 00, 00 BC 61 4E
        byte[] data = new byte[4];
        BinaryUtils.intTo32bitLE(TESTVALUE, data, 0);
        Assert.assertTrue(Arrays.equals(data, new byte[]{0x4E, 0x61, (byte)0xBC, 0x0}));
        Assert.assertEquals(TESTVALUE, BinaryUtils.intFrom32bitLE(data, 0));

        BinaryUtils.intTo32bitBE(TESTVALUE, data, 0);
        Assert.assertTrue(Arrays.equals(data, new byte[]{0x0, (byte)0xBC, (byte)0x61, 0x4E}));
        Assert.assertEquals(TESTVALUE, BinaryUtils.intFrom32bitBE(data, 0));

        TESTVALUE = -12345678;//B2 9E 43 FF, FF 43 9E B2
        data = new byte[4];
        BinaryUtils.intTo32bitLE(TESTVALUE, data, 0);
        Assert.assertTrue(Arrays.equals(data, new byte[]{(byte) 0xB2, (byte) 0x9E, (byte)0x43, (byte)0xFF}));
        Assert.assertEquals(TESTVALUE, BinaryUtils.intFrom32bitLE(data, 0));

        BinaryUtils.intTo32bitBE(TESTVALUE, data, 0);
        Assert.assertTrue(Arrays.equals(data, new byte[]{(byte) 0xFF, (byte) 0x43, (byte)0x9E, (byte)0xB2}));
        Assert.assertEquals(TESTVALUE, BinaryUtils.intFrom32bitBE(data, 0));
    }

    @Test
    public void test16BitIntConversions()
    {
        short TESTVALUE = 12345;	//30 39, 39 30
        byte[] data = new byte[2];
        BinaryUtils.shortTo16bitLE(TESTVALUE, data, 0);
        Assert.assertTrue(Arrays.equals(data, new byte[]{0x39, 0x30}));
        Assert.assertEquals(TESTVALUE, BinaryUtils.shortFrom16bitLE(data, 0));

        BinaryUtils.shortTo16bitBE(TESTVALUE, data, 0);
        Assert.assertTrue(Arrays.equals(data, new byte[]{0x30, 0x39}));
        Assert.assertEquals(TESTVALUE, BinaryUtils.shortFrom16bitBE(data, 0));

        TESTVALUE = -12345;	//CF C7, C7 CF
        data = new byte[2];
        BinaryUtils.shortTo16bitLE(TESTVALUE, data, 0);
        Assert.assertTrue(Arrays.equals(data, new byte[]{(byte) 0xC7, (byte) 0xCF}));
        Assert.assertEquals(TESTVALUE, BinaryUtils.shortFrom16bitLE(data, 0));

        BinaryUtils.shortTo16bitBE(TESTVALUE, data, 0);
        Assert.assertTrue(Arrays.equals(data, new byte[]{(byte) 0xCF, (byte) 0xC7}));
        Assert.assertEquals(TESTVALUE, BinaryUtils.shortFrom16bitBE(data, 0));
    }

    @Test
    public void testFirstIndexOf()
    {
        byte[] data = new byte[]{0, 1, 2, 3, 4, 5, 6};

        Assert.assertEquals(3, BinaryUtils.firstIndexOf(data, new byte[]{3, 4, 5}, 0));
        Assert.assertEquals(4, BinaryUtils.firstIndexOf(data, new byte[]{4, 5}, 0));
        Assert.assertEquals(1, BinaryUtils.firstIndexOf(data, new byte[]{1}, 0));
        Assert.assertEquals(-1, BinaryUtils.firstIndexOf(data, new byte[]{3, 4, 1}, 0));
    }

    @Test
    public void testIsInBytes()
    {
        byte[] data = new byte[]{0, 1, 2, 3, 4, 5, 6};
        for(int i = 0; i < 6; i++)
        {
            Assert.assertTrue(BinaryUtils.isInBytes(data, new byte[]{(byte)i}));
        }

        Assert.assertTrue(BinaryUtils.isInBytes(data, new byte[]{1, 2, 3}));
        Assert.assertTrue(BinaryUtils.isInBytes(data, new byte[]{4, 5, 6}));
        Assert.assertFalse(BinaryUtils.isInBytes(data, new byte[]{4, 5, 6, 7}));
        Assert.assertFalse(BinaryUtils.isInBytes(data, new byte[]{-1}));
    }
}
