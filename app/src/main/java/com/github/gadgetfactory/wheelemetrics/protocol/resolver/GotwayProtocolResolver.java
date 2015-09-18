package com.github.gadgetfactory.wheelemetrics.protocol.resolver;

import com.github.gadgetfactory.wheelemetrics.protocol.codec.GotwayProtocolCodec;
import com.github.gadgetfactory.wheelemetrics.protocol.codec.ProtocolCodec;
import com.github.gadgetfactory.wheelemetrics.utils.BinaryUtils;

/**
 * @author esaj
 */
public class GotwayProtocolResolver implements com.github.gadgetfactory.wheelemetrics.protocol.resolver.ProtocolResolver
{
    @Override
    public int getRequiredMinimumBytes()
    {
        return 48*2-1;
    }

    @Override
    public double match(byte[] data)
    {
        //Scan for tags
        int msgTagIndex = BinaryUtils.firstIndexOf(data, GotwayProtocolCodec.MSG_TAG, 0);
        if(msgTagIndex < 0)
        {
            return 0;
        }

        int odoTagIndex = BinaryUtils.firstIndexOf(data, GotwayProtocolCodec.ODO_TAG, 0);
        if(odoTagIndex < 0)
        {
            return 0;
        }

        //Tags found, check for proper data
        double match = 0.4;

        //Voltage should be found in 2 bytes right after msg-tag within range 40...75V (accounting for very empty batteries or overvolted ;))
        int index = msgTagIndex + GotwayProtocolCodec.MSG_TAG.length;
        int value = BinaryUtils.shortFrom16bitBE(data, index);
        if(BinaryUtils.isInRange(value, 4000, 7500))
        {
            match += 0.1;
        }
        index += 2;

        //Speed value in next two bytes, should be between -50...50km/h large enough range? ;)
        double doubleValue = BinaryUtils.shortFrom16bitBE(data, index) / 100.0D * 3.6D;
        if(BinaryUtils.isInRange(doubleValue, -50.00, 50.00))
        {
            match += 0.1;
        }
        index += 2;

        //Trip value in meters is in next 4 bytes, probably no-one has ridden over 1000km without turning off the wheel  ;)
        value = BinaryUtils.intFrom32bitBE(data, index);
        if(BinaryUtils.isInRange(value, 0, 1000000))
        {
            match += 0.1;
        }
        index += 4;

        //Current value in next two bytes, should be between -50...50A
        value = (data[index] << 8) & 0xFF | (data[index + 1] & 0xFF);
        if(BinaryUtils.isInRange(value, -5000, 5000))
        {
            match += 0.1;
        }
        index += 2;

        //Temperature value in next two bytes, should be between -50...100C
        doubleValue = BinaryUtils.shortFrom16bitBE(data, index) / 340.0D + 36.53D;
        if(BinaryUtils.isInRange(doubleValue, -50, 100))
        {
            match += 0.1;
        }

        //Odo should be found after the odo-tag, let's assume no one has ridden over 100000km
        index = odoTagIndex + GotwayProtocolCodec.ODO_TAG.length;
        value = BinaryUtils.intFrom32bitBE(data, index);
        if(BinaryUtils.isInRange(value, 0, 1000000))
        {
            match += 0.1;
        }

        //If everything matches, match should be 1.0 here
        return match;
    }

    @Override
    public ProtocolCodec getProtocolCodec()
    {
        return new GotwayProtocolCodec();
    }

    @Override
    public String getWheelName()
    {
        return "Gotway";
    }
}
