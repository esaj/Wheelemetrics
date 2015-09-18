package com.github.gadgetfactory.wheelemetrics.bluetooth;

import com.github.gadgetfactory.wheelemetrics.protocol.codec.ProtocolDataRelay;

/**
 * @author esaj
 */
public class ProtocolDataRelayBluetoothObserver extends BluetoothObserverAdapter
{
    private final ProtocolDataRelay relay;

    public ProtocolDataRelayBluetoothObserver(ProtocolDataRelay relay)
    {
        this.relay = relay;
    }

    @Override
    public void dataReceived(byte[] data)
    {
        relay.dataReceived(data);
    }
}
