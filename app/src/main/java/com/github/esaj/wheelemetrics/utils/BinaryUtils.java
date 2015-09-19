package com.github.esaj.wheelemetrics.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Bunch of static utility methods for working with binary data
 *
 * @author esaj
 */
public class BinaryUtils
{
    private BinaryUtils()
    {
        //Never instantiate
    }

    public static void intTo32bitLE(int value, byte[] data, int offset)
    {
        data[0 + offset] = (byte)((value & 0x000000FF));
        data[1 + offset] = (byte)((value & 0x0000FF00) >> 8);
        data[2 + offset] = (byte)((value & 0x00FF0000) >> 16);
        data[3 + offset] = (byte)((value & 0xFF000000) >> 24);
    }

    public static void intTo32bitBE(int value, byte[] data, int offset)
    {
        data[0 + offset] = (byte)((value & 0xFF000000) >> 24);
        data[1 + offset] = (byte)((value & 0x00FF0000) >> 16);
        data[2 + offset] = (byte)((value & 0x0000FF00) >> 8);
        data[3 + offset] = (byte)((value & 0x000000FF));
    }

    public static void shortTo16bitLE(short value, byte[] data, int offset)
    {
        data[0 + offset] = (byte)((value & 0x00FF));
        data[1 + offset] = (byte)((value & 0xFF00) >> 8);
    }

    public static void shortTo16bitBE(short value, byte[] data, int offset)
    {
        data[0 + offset] = (byte)((value & 0xFF00) >> 8);
        data[1 + offset] = (byte)((value & 0x00FF));
    }

    public static int intFrom32bitLE(byte[] data, int offset)
    {
        return (data[0 + offset] & 0xFF) | (data[1 + offset] & 0xFF) << 8 | (data[2 + offset] & 0xFF) << 16 | (data[3 + offset] & 0xFF) << 24;
    }

    public static int intFrom32bitBE(byte[] data, int offset)
    {
        return (data[0 + offset] & 0xFF) << 24 | (data[1 + offset] & 0xFF) << 16 | (data[2 + offset] & 0xFF) << 8 | (data[3 + offset] & 0xFF);
    }

    public static short shortFrom16bitLE(byte[] data, int offset)
    {
        return (short)((data[0 + offset] & 0xFF) | (data[1 + offset] & 0xFF) << 8);
    }

    public static short shortFrom16bitBE(byte[] data, int offset)
    {
        return (short)(((data[0 + offset] & 0xFF) << 8) | (data[1 + offset] & 0xFF));
    }

    /**
     * Searches whether expectedBytes is found somewhere within given data (in full and
     * in same order) and returns the first index in data where expectedBytes begins
     * @param data              Data to inspect
     * @param expectedBytes     Bytes to look for
     * @param startIndex        Index to start the search from in data
     * @return True if match is found, otherwise false
     */
    public static int firstIndexOf(byte[] data, byte[] expectedBytes, int startIndex)
    {
        int hitIndex = 0;
        int index = startIndex;
        int firstIndex = -1;

        while(index < data.length)
        {
            byte read = data[index++];
            if(read == expectedBytes[hitIndex++])
            {
                if(hitIndex == 1)
                {
                    firstIndex = index-1;
                }

                if(hitIndex == expectedBytes.length)
                {
                    return firstIndex;
                }
            }
            else
            {
                hitIndex = 0;
                firstIndex = -1;
            }
        }

        return -1;
    }

    /**
     * Searches whether expectedBytes is found somewhere within given data (in full and
     * in same order).
     * @param data              Data to inspect
     * @param expectedBytes     Bytes to look for
     * @return True if match is found, otherwise false
     */
    public static boolean isInBytes(byte[] data, byte[] expectedBytes)
    {
        if(firstIndexOf(data,expectedBytes, 0) > -1)
        {
            return true;
        }
        return false;
    }

    /**
     * Checks if given value is between min and max (inclusive)
     * @param value     Value to check
     * @param min       Minimum allowed
     * @param max       Maximum allowed
     * @return True, if value is either minimum or maximum, or between them, otherwise false
     */
    public static boolean isInRange(Number value, Number min, Number max)
    {
        return value.doubleValue() >= min.doubleValue() && value.doubleValue() <= max.doubleValue();
    }

    /**
     * Reads the requested number of bytes from a stream into given array at given offset.
     * WARNING: Most InputStreams will wait forever at read(), if not enough data ever arrives, but
     * the stream won't close either.
     * @param in
     * @param buffer
     * @param bufferOffset
     * @param numBytes
     * @throws IOException
     */
    public static void readNumberOfBytes(InputStream in, byte [] buffer, int bufferOffset, int numBytes) throws IOException
    {
        int read = 0;
        while (read < numBytes)
        {
            int actual = in.read(buffer, read + bufferOffset, numBytes - read);
            if(actual == -1)
            {
                throw new IOException("End of stream reached");
            }
            read += actual;
        }
    }
}
