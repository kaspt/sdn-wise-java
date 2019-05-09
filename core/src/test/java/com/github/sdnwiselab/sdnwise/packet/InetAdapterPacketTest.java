package com.github.sdnwiselab.sdnwise.packet;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class InetAdapterPacketTest {

    private InetAdapterPacket dut;


    private byte[] getTestPayload(){

        return new byte[] { (byte)0x55, (byte)0x0f, (byte)0x10 };
    }

    private final String clientIPadr_str = "fe80::250:56ff:fec0:8";

    private final int testPort = 12345;

    private InetAddress clientadr;

    private int exp_len;

    @BeforeAll
    static void setUpall(){

    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception
    {
        byte[] payload = getTestPayload();
        //clientadr = InetAddress.getByName(clientIPadr_str);
        //clientadr = InetAddress.getLocalHost();
        clientadr = InetAddress.getByName(clientIPadr_str);
        exp_len = payload.length + InetAdapterPacket.HEADER_LENGTH;
        dut = new InetAdapterPacket(payload, clientadr, testPort);
    }

    @org.junit.jupiter.api.Test
    void toByteArray() {

    }

    @org.junit.jupiter.api.Test
    void getPayload() {
        byte[] res = dut.getPayload();
        assertArrayEquals(res, getTestPayload());
    }

    @org.junit.jupiter.api.Test
    void getInetAdress() throws Exception{
        InetAddress res = dut.getInetAdress();
        assertEquals(clientadr, res);
    }

    @org.junit.jupiter.api.Test
    void getLen() {
        int res = dut.getLen();
        assertEquals(exp_len, res);
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

    /**
     * Test the Constructor with byte[]
     *
     * @throws Exception
     */
    @org.junit.jupiter.api.Test
    void testGetInstance() throws Exception{
        // Create bytearray witch represent a InetAdapterPacket
        final byte[] exp_payload = new byte[10];
        new Random().nextBytes(exp_payload);

        final byte exp_NET = 64;
        final byte exp_HEADLEN = InetAdapterPacket.HEADER_LENGTH;
        final int exp_PORT = 12345;

        final byte exp_LEN = (byte) (InetAdapterPacket.HEADER_LENGTH + exp_payload.length);
        final byte[] packet = new byte[exp_LEN];

        packet[InetAdapterPacket.NET_INDEX] = exp_NET;
        packet[InetAdapterPacket.HLEN_INDEX] = (byte)(packet.length - exp_payload.length);
        byte[] exp_port_arr = ByteBuffer.allocate(2).putShort((short) exp_PORT).array();
        System.arraycopy(exp_port_arr, 0, packet, InetAdapterPacket.PORT_INDEX, 2);
        packet[InetAdapterPacket.LEN_INDEX] = exp_LEN;

        byte[] exp_IPadd = InetAddress.getByName(clientIPadr_str).getAddress();
        System.arraycopy(exp_IPadd, 0, packet,InetAdapterPacket.IPADR_INDEX,  exp_IPadd.length );

        InetAdapterPacket res = new InetAdapterPacket(packet);

        assertEquals(exp_NET, res.getNet());
        assertEquals(exp_LEN, res.getLen());
        assertEquals(exp_PORT, res.getPort());

    }

    private short bytearraytoshort(byte[] arr){
        ShortBuffer shortBuffer =
                ByteBuffer.wrap(arr)
                        .order(ByteOrder.BIG_ENDIAN)
                        .asShortBuffer();
        short[] array = new short[shortBuffer.remaining()];
        return shortBuffer.get();
    }




}