package com.github.sdnwiselab.sdnwise.adaptation;

import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.*;

import com.github.sdnwiselab.sdnwise.adapter.AbstractAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class AdaptationWebTest {

    private List<AbstractAdapter> uppers;

    private List<AbstractAdapter> lowers;

    private AdaptationWeb dut;

    @Before
    public void initadapters(){
        uppers = new LinkedList<>();
        AbstractAdapter upMock = mock(AbstractAdapter.class);
        uppers.add(upMock);

        lowers = new LinkedList<>();
        AbstractAdapter lowMock = mock(AbstractAdapter.class);
        lowers.add(lowMock);

        lowMock = mock(AbstractAdapter.class);
        lowers.add(lowMock);

        dut = new AdaptationWeb(lowers, uppers, 5);

    }

    private byte[] createPayload(){
        return new byte[] { (byte)0x00, (byte)0x0f, (byte)0x10 };
    }


    @Test
    public void testAda_sendMessageToFWD(){

        byte[] payload = createPayload();
        AbstractAdapter low = lowers.get(0);
        dut.update(lowers.get(0), payload);

        verify(uppers.get(0),
                times(1)).send(payload);

    }

    @Test
    public void testAda_sendMessageToSocket(){
        fail("Not yet implemented");
    }


    @Test
    public void testAda_openSocket(){

        AdaptationWeb instance = new AdaptationWeb(null, uppers, 5);


        byte[] expectedMessage = new byte[] { (byte)0x00, 0x0f, (byte)0x10 };
        byte[] error= new byte[] { (byte)0xff, 0x0f, (byte)0x10 };


        for (AbstractAdapter ad :uppers){
            ad.send(expectedMessage);
        }
        verify(uppers.get(0), times(1)).send(expectedMessage);

        // TODO create Adapter Mock
        // TODO Create Instance of AdaptationWeb ad uper layer
        // TODO create Socket Request
        // TODO validate received Data from Adapter

    }

    @Test
    public void testAda_openMultipleSockeets(){
        fail("not yet implemented");
    }

    @Test
    public void testAda_responses(){
        fail("not yet implemented");
    }

    @Test
    public void testAda_addNode(){
        fail("not yet implemented");
    }

    @Test
    public void test_Ada_removeNode(){
        fail("not yet implemented");
    }

}