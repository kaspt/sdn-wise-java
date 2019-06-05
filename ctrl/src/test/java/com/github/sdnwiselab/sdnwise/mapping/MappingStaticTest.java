/*
 * Copyright (C) 2015 SDN-WISE
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
package com.github.sdnwiselab.sdnwise.mapping;


import com.github.sdnwiselab.sdnwise.configuration.Configurator;
import com.github.sdnwiselab.sdnwise.loader.SdnWise;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;

import org.junit.jupiter.api.*;

import java.io.InputStream;
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
    void getNodeAddress_ip_port() {
        InetSocketAddress req_addr =
                new InetSocketAddress("fe80::250:56ff:fec0:1", 9997);
        NodeAddress expected = new NodeAddress(1,1);
        byte[] addrbytearr = req_addr.getAddress().getAddress();
        NodeAddress result = dut.getNodeAddress(
                req_addr.getAddress().getAddress(),
                req_addr.getPort());
        assertEquals(expected, result);
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

    @Test
    void MappingFactory(){
        InputStream is = null;

        is = SdnWise.class.getResourceAsStream("/configTestFile.json");
        Configurator conf = Configurator.load(is);
        AbstractMapping mapping =
                MappingFactory.getMapping(conf);
        assertEquals(mapping.getClass(), MappingStatic.class );

    }

}