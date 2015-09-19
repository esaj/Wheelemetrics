package com.github.esaj.wheelemetrics.utils;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author esaj
 */
public class StringUtilsTest
{
    @Test
    public void testGetStringFromFixedPoint()
    {
        Assert.assertEquals("1234.", StringUtils.getStringFromFixedPoint(1234, 0));
        Assert.assertEquals("12.34", StringUtils.getStringFromFixedPoint(1234, 2));
        Assert.assertEquals("0.01234", StringUtils.getStringFromFixedPoint(1234, 5));

        Assert.assertEquals("-1234.", StringUtils.getStringFromFixedPoint(-1234, 0));
        Assert.assertEquals("-12.34", StringUtils.getStringFromFixedPoint(-1234, 2));
        Assert.assertEquals("-0.01234", StringUtils.getStringFromFixedPoint(-1234, 5));
    }


}
