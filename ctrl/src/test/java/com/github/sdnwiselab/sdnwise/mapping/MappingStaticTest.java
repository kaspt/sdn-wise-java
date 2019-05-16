package com.github.sdnwiselab.sdnwise.mapping;

import com.github.sdnwiselab.sdnwise.util.NodeAddress;

import org.junit.jupiter.api.*;
import org.junit.platform.engine.support.hierarchical.Node;

import java.net.InetSocketAddress;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MappingStaticTest {

    private static AbstractMapping dut;

    @BeforeAll
    static void setUp() {
        String file = "/mappinTestfile.csv";
        file = MappingStatic.class.getResource(file).getPath();
        dut = new MappingStatic(file);
    }

    @Test
    void getAllAddresses() {


        List<InetSocketAddress> expected = new LinkedList<>();
        expected.add(new InetSocketAddress("fe80::250:56ff:fec0:1", 9997));
        expected.add(new InetSocketAddress("fe81::250:56ff:fec0:2", 9998));
        expected.add(new InetSocketAddress("fe81::250:56ff:fec0:3", 9999));


        List<InetSocketAddress> result =  dut.getAllAddresses();

        int found[] = {0};
        result.forEach((res_entry)->{
            expected.forEach((exp_entry)->{
                if(res_entry.equals(exp_entry)){
                    found[0]++;
                };
            });
        });
        assertEquals(expected.size(), found[0]);

    }

    @Test
    void getNodeAddress() {

        InetSocketAddress req_addr =
                new InetSocketAddress("fe80::250:56ff:fec0:1", 9997);
        NodeAddress expected = new NodeAddress(1,1);
        NodeAddress result = dut.getNodeAddress(req_addr);
        assertEquals(expected, result);
        InetSocketAddress wrong_addres =
                new InetSocketAddress("fe81::250:56ff:fec0:1", 9998);
        result = dut.getNodeAddress(wrong_addres);
        assertNotEquals(expected, result);

    }

    @Test
    void getSocketAddress() {
        InetSocketAddress expected =
                new InetSocketAddress("fe80::250:56ff:fec0:1", 9997);
        NodeAddress req = new NodeAddress(1,1);

        InetSocketAddress result = dut.getSocketAddress(req);
        assertEquals(expected, result);

        NodeAddress wrong_addres = new NodeAddress(3,3);
        result = dut.getSocketAddress(wrong_addres);
        assertNotEquals(expected, result);
    }
}