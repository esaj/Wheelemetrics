package com.github.gadgetfactory.wheelemetrics.utils;

/**
 * @author esaj
 */
public class StringUtils
{
    private StringUtils()
    {
        //Do not instantiate
    }

    /**
     * Creates a string representation of a fixed-point value, for example:
     * getStringFromFixedPoint(1234, 2) = "12.34"
     * getStringFromFixedPoint(1234, 5) = "0.01234"
     * @param value         Fixed point value
     * @param decimals      Number of decimals in value
     * @return String
     */
    public static String getStringFromFixedPoint(int value, int decimals)
    {
        StringBuilder str = new StringBuilder(8);

        char[] numberStr = null;

        if(value < 0)
        {
            //Special handling for minus sign, so that values like  -0.12 don't become "-.12"
            str.append("-");
            numberStr = Integer.toString(-value).toCharArray();
        }
        else
        {
            numberStr = Integer.toString(value).toCharArray();
        }


        if(numberStr.length <= decimals)
        {
            str.append(0);
        }
        else
        {
            for(int i = 0; i < numberStr.length - decimals; i++)
            {
                str.append(numberStr[i]);
            }
        }

        str.append(".");

        int startIndex = numberStr.length - decimals;
        if(startIndex >= 0)
        {
            for(int i = startIndex; i < numberStr.length; i++)
            {
                str.append(numberStr[i]);
            }
        }
        else
        {
            for(int i = startIndex; i < 0; i++)
            {
                str.append("0");
            }
            for(int i = 0; i < numberStr.length; i++)
            {
                str.append(numberStr[i]);
            }
        }

        return str.toString();
    }
}
