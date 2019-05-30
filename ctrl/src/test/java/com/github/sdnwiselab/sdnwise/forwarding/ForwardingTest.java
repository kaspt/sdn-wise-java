/*
 * Copyright (C) 2019 SDN-WISE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.sdnwiselab.sdnwise.forwarding;

import static junit.framework.TestCase.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.sdnwiselab.sdnwise.adapter.AbstractAdapter;
import com.github.sdnwiselab.sdnwise.adapter.AdapterCooja;
import com.github.sdnwiselab.sdnwise.adapter.AdapterTcp;
import com.github.sdnwiselab.sdnwise.configuration.Configurator;
import com.github.sdnwiselab.sdnwise.loader.SdnWise;
import com.github.sdnwiselab.sdnwise.mapping.AbstractMapping;
import com.github.sdnwiselab.sdnwise.packet.DataPacket;
import com.github.sdnwiselab.sdnwise.packet.InetAdapterPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.packet.WebPacket;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;


import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ForwardingTest {


    private Forwarding fwd;

    private AbstractAdapter adaWeb, adaNode, adaContr;

    private AbstractMapping mapping;

    @BeforeAll
    void setUp() {
        System.out.println("setUp.ForwardingTest");
        List<AbstractAdapter> upper = new LinkedList<>();


        adaContr = mock(AbstractAdapter.class);

        upper.add(adaContr);

        List<AbstractAdapter> lowers = new LinkedList<>();
        adaNode = mock(AbstractAdapter.class);

        lowers.add(adaNode);

        adaWeb = mock(AbstractAdapter.class);
        lowers.add(adaWeb);

        mapping = mock(AbstractMapping.class);

        fwd = new Forwarding(lowers, upper, mapping);

    }

    @AfterEach
    void teardown(){
        reset(adaContr);
        reset(adaWeb);
        reset(adaNode);
    }


    @Test
    void testFactory() {
        System.out.println("testFactory.ForwardingTest");
        InputStream is = null;
        is = SdnWise.class.getResourceAsStream(
                "/configTestFile.json");
        Configurator conf = Configurator.load(is);
        Forwarding fwd = ForwardingFactory.getForwarding(conf);
        assertEquals(1, fwd.getUpper().size());
        assertEquals(2, fwd.getLower().size());
    }

    /**
     *  Inet -> sdn-wise
     */
    @Test
    void update_dataPacket_fromInet() {
        System.out.println("update_dataPacket_fromInet.ForwardingTest");

        PacketCreator packetCreator = new PacketCreator();
        String ip_sdnwise = "fe80::250:56ff:fec0:8";
        int port_sdnwise = 8888;
        InetAdapterPacket packet_to_send = packetCreator.getInetPacket(
                new InetSocketAddress(ip_sdnwise, port_sdnwise));

        byte[] exp_payload = packet_to_send.getPayload();
        NodeAddress exp_address = new NodeAddress(1,2);

        // when(mapping.getNodeAddress(any(), any())).thenReturn(exp_address);
        when(mapping.getNodeAddress(any(),anyInt())).thenReturn(exp_address);
        when(adaNode.getAdapterIdentifier()).thenReturn("ADAPT_NODE");
        when(adaWeb.getAdapterIdentifier()).thenReturn("ADAPT_WEB");

        // Invoke Action
        fwd.update(adaWeb, packet_to_send.toByteArray());

        ArgumentCaptor<byte[]> argument = ArgumentCaptor.forClass(byte[].class);



        verify(adaContr, never()).send(any());
        verify(adaWeb, never()).send(any());
        verify(adaNode, times(1)).send(argument.capture());
        WebPacket resultPacket = new WebPacket(argument.getValue());


        assertEquals(exp_address, resultPacket.getDst());
        assertArrayEquals(exp_payload, resultPacket.getData());

    }


    /**
     * controller -> sdn-wise
     */
    @ParameterizedTest
    @MethodSource("controllerPackets")
    void update_controllerPacket_formController(NetworkPacket packet) {
        System.out.println("update_controllerPacket_formController.ForwardingTest");


        fwd.update(adaContr, packet.toByteArray());

        verify(adaContr, never()).send(any());


        verify(adaWeb, never()).send(any());
        // verfiy compare ref or value?
        verify(adaNode, times(1)).send(packet.toByteArray());

    }

    /**
     * parameter list
     */
    static Stream<Arguments> controllerPackets(){
        PacketCreator pc = new PacketCreator();
        List<Arguments> argumentsList = new ArrayList<>();
        int[] cont_to_nodepackets = new int[]{4,5,6};
        NodeAddress src = new NodeAddress("2.3");
        NodeAddress dest = new NodeAddress("7.8");
        for(int i: cont_to_nodepackets){
            argumentsList.add(
                    Arguments.of(
                            pc.getControllerPacket(src, dest, i)));
        }
        return argumentsList.stream();
    }

    /**
     * sdn-wise -> controller
     */
    @Test
    void update_controllerPacket_toController() {
        fail("Not yet implemented");
    }


    @ParameterizedTest
    @MethodSource("sdnwisePackets_form_nodes")
    void update_form_SDNWISE(NetworkPacket packet,
                             InetAdapterPacket exp_inet) {

        NodeAddress exp_address = new NodeAddress(7,5);
        InetSocketAddress exp_socketAddr = new InetSocketAddress(7575);

        //when(mapping.getNodeAddress(any())).thenReturn(exp_address);
        when(mapping.getSocketAddress(any(NodeAddress.class))).thenAnswer(
                invocation -> {
                    NodeAddress na = (NodeAddress)invocation.getArgument(0);
                    if (na.intValue() == 75) {
                        return exp_socketAddr;
                    }else {
                        return exp_socketAddr;
                    }
                }
        );
        // Invoke Action
        fwd.update(adaNode, packet.toByteArray());

        if(exp_inet == null){
            verify(adaContr, times(1)).send(packet.toByteArray());
            verify(adaWeb, never()).send(any());
        }else {
            verify(adaWeb, times(1)).send(exp_inet.toByteArray());
            verify(adaContr, never()).send(any());
        }
        verify(adaNode, never()).send(any());
    }

    /**
     * parameter list
     */
    static Stream<Arguments> sdnwisePackets_form_nodes(){
        PacketCreator pc = new PacketCreator();
        List<Arguments> argumentsList = new ArrayList<>();
        int[] cont_to_nodepackets = new int[]{4,5,6};
        NodeAddress src = new NodeAddress("2.3");
        NodeAddress dest = new NodeAddress("7.8");
        InetSocketAddress socketAddress = new InetSocketAddress(
                "fe80::250:56ff:fec0:8",7575);
        for(int i: cont_to_nodepackets){
            argumentsList.add(
                    Arguments.of(
                            pc.getControllerPacket(src, dest, i),
                            null
                    )
            );
        }

        DataPacket sdnwiseData = (DataPacket) pc.getDataPacket(src, dest);
        argumentsList.add(
                Arguments.of(
                        sdnwiseData,
                        (pc.getInetPacket(socketAddress)
                                .setPayload(sdnwiseData.getData()))
                )
        );
        return argumentsList.stream();
    }

    /**
     * sdn-wise -> Inet
     */
    @Test
    void update_dataPacket_toInet() {
        fail("Not yet implemented");
    }

}