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
//    private final int hostport = 8888;

    ///////////////////
    private static AdapterWeb dut = null;

    private static Observer observer;

    private static myObserverMock selfmockedObserver;

    private static List<SocketTestAdapter> clients = new ArrayList<>();

    private static final String hostip = "fe80::250:56ff:fec0:8";

    private static final int hostport = 8888;



    //@BeforeEach
    @BeforeAll
    static void setUp() {
        System.out.println("setUpall");
        Map<String, String> conf = new HashMap<>();
        conf.put("IS_SERVER", "true");
        conf.put("IP", hostip);
        conf.put("PORT", String.valueOf(hostport));

        try {
            dut = new AdapterWeb(conf);
        }catch (UnknownHostException e){
            fail("can't open server socket");
        }

        observer = mock(Observer.class);

        // selfmockedObserver = new myObserverMock();
        //dut.addObserver(selfmockedObserver);

        dut.addObserver(observer);
        // Start a new Thread with the socket server.
        dut.open();

        SocketTestAdapter adapter = new SocketTestAdapter(hostip, hostport);
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
            System.out.println("received" + o.toString());
        }
    }

    @Test
    void send() throws IOException {
        System.out.println("test.send");
        SocketTestAdapter client = clients.get(0);
        client.openSocket();
        PacketCreator creator = new PacketCreator();
        byte[] data = new PacketCreator().createClientPacket(5);

        client.send(data);

        verify(observer, timeout(1000).times(1))
                .update(Mockito.<Observable>any(), Mockito.<Object>any());

    }

    @Test
    void receive() throws IOException{
        System.out.println("test.receive");
        SocketTestAdapter client = clients.get(0);
        client.openSocket();
        PacketCreator creator = new PacketCreator();
        byte[] data = new PacketCreator().createClientPacket(5);

        dut.send(data);

        byte[] result =  client.receive();

        assertArrayEquals(data, result);

    }

    @Test
    void receivedMultiple() throws IOException{
        System.out.println("test.receive");
        SocketTestAdapter client = clients.get(0);
        client.openSocket();
        client.openSocket();

        byte[] data = new PacketCreator().createInetAdapterPacket();

        dut.send(data);

        byte[] result =  client.receive();

        assertArrayEquals(data, result);
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

            InetAdapterPacket data = new InetAdapterPacket(payload,
                    address, 8888,(byte) 64);

            return data.toByteArray();
        }

        public  byte[] createClientPacket(int length){
            byte [] data = new byte[length];
            new Random().nextBytes(data);
            data[0] = (byte)length;
            return data;
        }

    }
}
