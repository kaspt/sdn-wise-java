package com.github.sdnwiselab.sdnwise.packet;

import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebPacketTest {

    @Test
    void constructor(){
        byte[] payload = new byte[]{(byte)6,(byte)7,(byte)8};
        WebPacket webPacket = new WebPacket(0,
                new NodeAddress("1.2"),
                new NodeAddress("3.4"),
                (byte)5,
                payload);

    
    }



    @Test
    void setMessageID() {
        byte[] payload = new byte[]{(byte) 0x10, (byte)0x55};
        byte exp_messageID = 2;
        WebPacket instance = new WebPacket(1,
                new NodeAddress("0.0"),
                new NodeAddress("0.0"),
                (byte) 1,
                payload);

        instance.setMessageID(exp_messageID);

        assertEquals(exp_messageID, instance.getMessageID());
        assertArrayEquals(payload, instance.getData());
    }

    @Test
    void getMessageID() {
        byte[] payload = new byte[]{(byte) 0x10, (byte)0x55};
        WebPacket instance = new WebPacket(1,
                new NodeAddress("0.0"),
                new NodeAddress("0.0"),
                (byte) 3,
                payload);
        assertEquals(3, instance.getMessageID());
        assertArrayEquals(payload, instance.getData());
    }

    @Test
    void getData() {
        byte[] exp_payload = new byte[]{(byte) 0x12, (byte)0x35};
        WebPacket instance = new WebPacket(1,
                new NodeAddress("0.0"),
                new NodeAddress("0.0"),
                (byte) 1,
                exp_payload);
        assertArrayEquals(exp_payload, instance.getData());
    }

}