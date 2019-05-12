package com.github.sdnwiselab.sdnwise.packet;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class InetAdapterPacketTest {

    private byte[] getTestPayload(){
        return new byte[] { (byte)0x55, (byte)0x0f, (byte)0x10 };
    }

    private InetAdapterPacket dut;

    private Exp_Results exp_results;

    private class Exp_Results {

        public Exp_Results() throws Exception{
            this.exp_ipaddress = InetAddress.getByName(exp_ipaddress_str);
            exp_payload = new byte[]{(byte)0x55, (byte)0x0f, (byte)0x10 };
            this.exp_len =  exp_payload.length + InetAdapterPacket.HEADER_LENGTH;
        }

        public byte[] exp_payload;

        private final String exp_ipaddress_str = "fe80::250:56ff:fec0:8";

        public InetAddress exp_ipaddress;

        public int exp_port = 12345;

        public int exp_len;

        public byte exp_NET = 64;
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception
    {
        exp_results = new Exp_Results();
        dut = new InetAdapterPacket(exp_results.exp_payload,
                exp_results.exp_ipaddress,
                exp_results.exp_port,
                exp_results.exp_NET);
    }

   /* static Stream<Arguments> resources(){
        byte[] temp = new byte[] { (byte)0x55, (byte)0x0f, (byte)0x10 };
        return Stream.of(
                Arguments.of(1234, new byte[] { (byte)0x55, (byte)0x0f})
        );
    }*/


    @org.junit.jupiter.api.Test
    void getPayload() {
        byte[] res = dut.getPayload();
        assertArrayEquals(res, getTestPayload());
    }

    @org.junit.jupiter.api.Test
    void getInetAdress() throws Exception{
        InetAddress res = dut.getInetAdress();
        assertEquals(exp_results.exp_ipaddress, res);
    }

    @org.junit.jupiter.api.Test
    void getLen() {
        int res = dut.getLen();
        assertEquals(exp_results.exp_len, res);
    }

    @org.junit.jupiter.api.Test
    void getPort() {
        int res = dut.getPort();
        assertEquals(exp_results.exp_port, res);
    }

    @org.junit.jupiter.api.Test
    void setPort() {
        Random rand = new Random();
        int  expected = rand.nextInt(65535);
        dut.setPort(expected);
        assertEquals(expected, dut.getPort());
    }

    @org.junit.jupiter.api.Test
    void toByteArray() throws Exception{
        byte[] res =  dut.toByteArray();
        validate_all();
        dut = new InetAdapterPacket(res);
        validate_all();
    }

    /**
     * Check the different parameters of the Packet
     *
     * @throws Exception
     */
    private void validate_all() throws Exception{
        assertEquals(exp_results.exp_port, dut.getPort());
        assertEquals(exp_results.exp_len, dut.getLen());
        assertArrayEquals(exp_results.exp_payload, dut.getPayload());
        assertEquals(exp_results.exp_ipaddress, dut.getInetAdress());
    }

}