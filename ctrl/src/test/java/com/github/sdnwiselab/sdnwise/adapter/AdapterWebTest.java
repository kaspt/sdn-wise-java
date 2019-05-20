package com.github.sdnwiselab.sdnwise.adapter;

import com.github.sdnwiselab.sdnwise.packet.InetAdapterPacket;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdapterWebTest {

//    private AdapterWeb dut = null;
//
//    private Observer observer;
//
//    private myObserverMock selfmockedObserver;
//
//    private List<SocketTestAdapter> clients = new ArrayList<>();
//
//    private final String hostip = "fe80::250:56ff:fec0:8";
//
//    private final int port = 8888;

    ///////////////////
    private static AdapterWeb dut = null;

    private static Observer observer;

    private static myObserverMock selfmockedObserver;

    private static List<SocketTestAdapter> clients = new ArrayList<>();

    private static final String hostip = "fe80::250:56ff:fec0:8";

    private static byte[] hostip_arr;

    private static final int port = 8888;

    private static final int backlog = 5;


    //@BeforeEach
    @BeforeAll
    static void setUp() {
        System.out.println("setUpall");
        Map<String, String> conf = new HashMap<>();
        conf.put("IS_SERVER", "true");
        conf.put("IP", hostip);
        conf.put("PORT", String.valueOf(port));
        conf.put("BACKLOG", String.valueOf(backlog));

        try {
            hostip_arr = InetAddress.getByName(hostip).getAddress();
            dut = new AdapterWeb(conf);
        }catch (UnknownHostException e){
            fail("can't open server socket");
        }
        observer = mock(Observer.class);
        dut.addObserver(observer);

        dut.open();
        SocketTestAdapter adapter = new SocketTestAdapter(hostip, port);
        clients.add(adapter);
    }

    @AfterAll
    static void tearDown(){
        System.out.println("after.all");
        dut.close();
    }

    public class  myObserverMock implements Observer{
        @Override
        public void update(Observable observable, Object o) {
            dut.send(((InetAdapterPacket)o).toByteArray());
        }
    }

    @Test
    void send() throws IOException {
        selfmockedObserver = new myObserverMock();
        dut.addObserver(selfmockedObserver);

        System.out.println("test.send");
        SocketTestAdapter client = clients.get(0);
        client.openSocket();
        PacketCreator creator = new PacketCreator();
        byte[] data = new PacketCreator().createClientPacket(5);
        byte[] exp_payload = new byte[5];
        System.arraycopy(data, 1, exp_payload, 0, exp_payload.length);

        client.send(data);
        verify(observer, timeout(2000).times(1))
                .update(Mockito.<Observable>any(), Mockito.<Object>any());

        byte[] received_array =  client.receiveInetPacket();
        InetAdapterPacket packet = new InetAdapterPacket(received_array);

        byte[] payload = packet.getPayload();

        assertArrayEquals(exp_payload, payload);

    }

    @Test
    void identfy() throws Exception{
        Map<String, String> conf = new HashMap<>();
        conf.put("IS_SERVER", "true");
        conf.put("IP", hostip);
        conf.put("PORT", String.valueOf(port));
        conf.put("BACKLOG", String.valueOf(backlog));
        AdapterWeb dut;

        dut = new AdapterWeb(conf);
        Observer observerMock = mock(Observer.class);
        dut.addObserver(observerMock);

        byte[] payload = new byte[]{(byte)1,(byte)2,(byte)3};
        InetAdapterPacket packet = new InetAdapterPacket(payload, (byte)64);
        assertFalse(dut.identifyAddapter(packet));
        packet.setSdnWiseAddress(hostip_arr);
        assertFalse(dut.identifyAddapter(packet));
        packet.setSdnWisePort(port);
        assertTrue(dut.identifyAddapter( packet));
    }

    private class PacketCreator{

        int defaultPayloadLenght = 5;
        public byte[] createInetAdapterPacket(){
            byte [] payload = new byte[defaultPayloadLenght];
            new Random().nextBytes(payload);
            InetAddress address;
            try {
                address = InetAddress.getLocalHost();
            }catch (Exception ex){
                fail(ex.toString());
                throw new IllegalArgumentException();
            }
            InetAdapterPacket data = new InetAdapterPacket(payload,(byte) 64);

            return data.toByteArray();
        }

        public  byte[] createClientPacket(int payloadLength){

            byte [] data = new byte[payloadLength + 1];
            //new Random().nextBytes(data);
            for(int i=1; i <= payloadLength; i++){
                data[i] = (byte) i;
            }
            data[0] = (byte)payloadLength;
            return data;
        }

    }
}
