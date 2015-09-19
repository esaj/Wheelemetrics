package com.github.esaj.wheelemetrics.protocol.resolver;

import com.github.esaj.wheelemetrics.protocol.codec.ProtocolCodec;

/**
 * @author esaj
 */
public interface ProtocolResolver
{
    /**
     * Number of bytes of data this resolver needs at minimum to be able
     * to distinguish whether the protocol matches or not
     * @return  Number of bytes
     */
    public int getRequiredMinimumBytes();

    /**
     * Runs matching on given data to resolve protocol
     * @param data     Data to inspect
     * @return  Value between 0 and 1 (inclusive), representing the match probability
     *          (0 = definitely no match, 1 = perfect match)
     */
    public double match(byte[] data);

    /**
     * Returns an instance of ProtocolCodec that this resolver uses
     * @return  ProtocolCodec-instance
     */
    public ProtocolCodec getProtocolCodec();

    /**
     * Returns the name of the wheel (specific wheel or EUC-brand) that uses this prootocol
     * @return
     */
    String getWheelName();
}
