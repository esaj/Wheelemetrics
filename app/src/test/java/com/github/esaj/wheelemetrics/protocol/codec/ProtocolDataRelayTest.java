package com.github.esaj.wheelemetrics.protocol.codec;

import android.util.Log;

import com.github.esaj.wheelemetrics.data.LoggableData;
import com.github.esaj.wheelemetrics.protocol.LoggableDataPublisherCallback;
import com.github.esaj.wheelemetrics.utils.ThreadUtils;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author esaj
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class, ProtocolDataRelay.class})
public class ProtocolDataRelayTest
{
    private ProtocolDataRelay relay;

    @Mock
    private ProtocolCodec mockCodec;

    @Mock
    private LoggableDataPublisherCallback mockCallback;

    @Mock
    private LoggableData mockLoggableData;

    private ProtocolDataRelay.DataHandler internalDataHandler;
    private Thread internalThread;

    private Object sync = new Object();

    @Before
    public void setUp() throws IOException
    {
        //Need static mocking for Log, otherwise it will break tests, see https://sites.google.com/a/android.com/tools/tech-docs/unit-testing-support
        PowerMockito.mockStatic(Log.class);

        //Control for codec decoding
        Mockito.when(mockCodec.decode(Matchers.any(InputStream.class))).thenAnswer(new Answer<LoggableData>()
        {
            @Override
            public LoggableData answer(InvocationOnMock invocation) throws Throwable
            {
                synchronized(sync)
                {
                    sync.wait();
                }
                return mockLoggableData;
            }
        });
    }

    @After
    public void tearDown()
    {
        if(internalDataHandler != null)
        {
            internalDataHandler.stop();
        }

        if(internalThread != null)
        {
            if(internalThread.isAlive())
            {
                //Run the internal thread down "cleanly" to prevent unnecessary exceptions & error logging in tests
                synchronized(sync)
                {
                    sync.notify();
                }
                ThreadUtils.sleepIgnoringInterrupt(100);
                internalThread.interrupt();
                try
                {
                    internalThread.join(1000);
                }
                catch(InterruptedException e)
                {
                    Assert.fail("Internal thread join interrupted! Possible thread leak!");
                }
            }
        }
    }

    private void setupDefaultRelay() throws IOException
    {
        relay = new ProtocolDataRelay(mockCodec, mockCallback);
        internalDataHandler = Whitebox.getInternalState(relay, ProtocolDataRelay.DataHandler.class);
        internalThread = Whitebox.getInternalState(relay, Thread.class);
    }

    @Test(timeout = 500L)
    public void testDataRelayAndPublish() throws IOException
    {
        setupDefaultRelay();
        relay.dataReceived(new byte[]{1, 2, 3, 4});
        ThreadUtils.sleepIgnoringInterrupt(50);
        synchronized(sync)
        {
            sync.notify();
        }

        //atLeastOnce, because depending on thread timing, the DataHandler-loop may have time to run again to .decode
        Mockito.verify(mockCodec, Mockito.atLeastOnce()).decode(Matchers.any(InputStream.class));
        Mockito.verify(mockCallback).publishLoggableData(mockLoggableData);
    }

    @Test
    public void testThreadRestart() throws IOException
    {
        //Make the codec crash on first call, then return loggabledata
        Mockito.reset(mockCodec);
        Mockito.when(mockCodec.decode(Matchers.any(InputStream.class))).thenThrow(new IOException("Pipe broke")).thenReturn(mockLoggableData);
        relay = new ProtocolDataRelay(mockCodec, mockCallback);

        internalDataHandler = Whitebox.getInternalState(relay, ProtocolDataRelay.DataHandler.class);
        internalThread = Whitebox.getInternalState(relay, Thread.class);

        ThreadUtils.sleepIgnoringInterrupt(100);

        //New data received should cause a new thread to be created
        relay.dataReceived(new byte[]{1, 2, 3, 4});

        ThreadUtils.sleepIgnoringInterrupt(100);

        //New thread should be created
        Assert.assertFalse(internalThread.isAlive());

        internalThread = Whitebox.getInternalState(relay, Thread.class);
        Assert.assertTrue(internalThread.isAlive());


    }

}
