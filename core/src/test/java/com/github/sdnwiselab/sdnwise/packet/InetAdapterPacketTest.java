package com.github.sdnwiselab.sdnwise.packet;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class InetAdapterPacketTest {

    private InetAdapterPacket dut;


    private byte[] getTestPayload(){

        return new byte[] { (byte)0x55, (byte)0x0f, (byte)0x10 };
    }

    private final String clientIPadr_str = "fe80::250:56ff:fec0:8";

    private final int testPort = 12345;

    private  InetAddress clientadr;


    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception
    {
        byte[] payload = getTestPayload();
        //clientadr = InetAddress.getByName(clientIPadr_str);
        //clientadr = InetAddress.getLocalHost();
        clientadr = InetAddress.getByName(clientIPadr_str);

        dut = new InetAdapterPacket(payload, clientadr, testPort);
    }



    @org.junit.jupiter.api.Test
    void toByteArray() {
    }

    @org.junit.jupiter.api.Test
    void toIntArray() {
    }

    @org.junit.jupiter.api.Test
    void getPayload() {
        byte[] res = dut.getPayload();
        assertArrayEquals(res, getTestPayload());
    }

    @org.junit.jupiter.api.Test
    void getInetAdress() {
        InetAddress res = dut.getInetAdress();
        assertEquals(clientadr, res);

    }

    @org.junit.jupiter.api.Test
    void getLen() {
    }

    @org.junit.jupiter.api.Test
    void getPort() {
        int res = dut.getPort();
        assertEquals(testPort, res);
    }

    @org.junit.jupiter.api.Test
    void setPort() {
        Random rand = new Random();
        int  expected = rand.nextInt(65535);
        dut.setPort(expected);
        assertEquals(expected, dut.getPort());

    }


}