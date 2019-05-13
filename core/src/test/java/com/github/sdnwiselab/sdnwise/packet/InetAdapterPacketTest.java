package com.github.sdnwiselab.sdnwise.packet;

import java.net.InetAddress;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class InetAdapterPacketTest {

    private byte[] getTestPayload(){
        return new byte[] { (byte)0x55, (byte)0x0f, (byte)0x10 };
    }

    private InetAdapterPacket dut;

    private Exp_Results exp_results;

    private class Exp_Results {

        private final String sdnwiseIpAddr_str = "fe80::250:56ff:fec0:9";

        private final String clientIPaddr_str = "fe80::250:564f:fec0:6";

        public Exp_Results() {
            try {

                this.exp_ipaddress = InetAddress.getByName(sdnwiseIpAddr_str);

                this.clientIpaddr = InetAddress.getByName(clientIPaddr_str)
                        .getAddress();
                this.clientPort = 8888;
                this.sdnWiseIPaddr = InetAddress.getByName(sdnwiseIpAddr_str)
                        .getAddress();
            }catch (Exception e){
                fail("can't initialize test class");
            }
            this.sdnWisePort = 12344;
            payload = new byte[]{(byte)0x01, (byte)0x02, (byte)0x03 };
            this.exp_len =  payload.length + InetAdapterPacket.HEADER_LENGTH;
        }

        public byte NET_ID = 64;

        public byte[] payload;

        public byte clientIPvers = (byte)6;

        public byte[] clientIpaddr;

        public int clientPort;

        public byte sdnWiseIPvers = (byte)6;

        public byte[] sdnWiseIPaddr;

        public int sdnWisePort;

        public InetAddress exp_ipaddress;

        public int exp_len;

    }

    @org.junit.jupiter.api.BeforeEach
    void setUp()
    {
        exp_results = new Exp_Results();
        dut = new InetAdapterPacket(exp_results.payload, exp_results.NET_ID)
                .setClientIPVersion(exp_results.clientIPvers)
                .setClientAddress(exp_results.clientIpaddr)
                .setClientPort(exp_results.clientPort)
                .setSdnWiseIPVersion(exp_results.sdnWiseIPvers)
                .setSdnWiseAddress(exp_results.sdnWiseIPaddr)
                .setSdnWisePort(exp_results.sdnWisePort);
    }


    @org.junit.jupiter.api.Test
    void setClientPropteries() {
        Random rand = new Random();
        int  expected = rand.nextInt(65535);
        exp_results.clientPort = expected;
        dut.setClientPort(expected);
        assert_all();
    }

    @org.junit.jupiter.api.Test
    void toByteArray() throws Exception{
        byte[] res =  dut.toByteArray();
        assert_all();
        dut = new InetAdapterPacket(res);
        assert_all();
    }

    void assert_all(){
        assert_clientProperties();
        assert_sdnWiseProperties();
        assert_Len();
        assert_Payload();
    }

    void assert_clientProperties() {
        assertArrayEquals(exp_results.clientIpaddr, dut.getClientAddress());
        assertEquals(exp_results.clientIPvers, dut.getIpClientIPVersion());
        assertEquals(exp_results.clientPort, dut.getClientPort());
    }

    void assert_sdnWiseProperties() {
        assertArrayEquals(exp_results.sdnWiseIPaddr, dut.getSdnWiseAddress());
        assertEquals(exp_results.sdnWiseIPvers, dut.getSdnWiseIPVersion());
        assertEquals(exp_results.sdnWisePort, dut.getSdnWisePort());
    }

    void assert_Len() {
        assertEquals(exp_results.exp_len, dut.getLen());
    }

    void assert_Payload() {
        byte[] pay = dut.getPayload();
        assertArrayEquals(exp_results.payload, dut.getPayload());
    }


}