/*
 * Copyright (C) 2016 Seby
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
package com.github.sdnwiselab.sdnwise.packet;

import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Tests for the BeaconPacket class.
 *
 * @author Sebastiano Milardo
 */
public final class ReportPacketTest {

    /**
     * Test of toByteArray method, of class ReportPacket.
     */
    @Test
    public void testToByteArray01() {
        ReportPacket instance = new ReportPacket(1, new NodeAddress("0.2"),
                new NodeAddress("0.0"), 2, 1);
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        map.put(new NodeAddress("0.3"), new byte[]{(byte) 1, (byte) 42, (byte) 43});
        map.put(new NodeAddress("0.4"), new byte[]{(byte) 2, (byte) 52, (byte) 53});
        instance.setNeighbors(map);
        String expResult = "[1, 31, 0, 0, 0, 2, 2, 100, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0,"
                + " 3, 1, 42, 43, 0, 4, 2, 52, 53]";
        String result = Arrays.toString(instance.toByteArray());
        assertEquals(expResult, result);
    }
    
    /**
     * Test of toByteArray method, of class ReportPacket.
     * Temperature
     */
    @Test
    public void testToByteArray02() {
        ReportPacket instance = new ReportPacket(1, new NodeAddress("0.2"),
                new NodeAddress("0.0"), 2, 1);
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        map.put(new NodeAddress("0.3"), new byte[]{(byte) 1, (byte) 42, (byte) 43});
        map.put(new NodeAddress("0.4"), new byte[]{(byte) 2, (byte) 52, (byte) 53});
        instance.setTemperature(new byte[] {13, 37});
        instance.setNeighbors(map);
        String expResult = "[1, 31, 0, 0, 0, 2, 2, 100, 0, 0, 2, 1, 13, 37, 0, 0, 0, 0, 0, 0, 2, 0,"
                + " 3, 1, 42, 43, 0, 4, 2, 52, 53]";
        String result = Arrays.toString(instance.toByteArray());
        assertEquals(expResult, result);
    }
    
    /**
     * Test of toByteArray method, of class ReportPacket.
     * Humidity
     */
    @Test
    public void testToByteArray03() {
        ReportPacket instance = new ReportPacket(1, new NodeAddress("0.2"),
                new NodeAddress("0.0"), 2, 1);
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        map.put(new NodeAddress("0.3"), new byte[]{(byte) 1, (byte) 42, (byte) 43});
        map.put(new NodeAddress("0.4"), new byte[]{(byte) 2, (byte) 52, (byte) 53});
        instance.setHumidity(new byte[] {13, 37});
        instance.setNeighbors(map);
        String expResult = "[1, 31, 0, 0, 0, 2, 2, 100, 0, 0, 2, 1, 0, 0, 13, 37, 0, 0, 0, 0, 2, 0,"
                + " 3, 1, 42, 43, 0, 4, 2, 52, 53]";
        String result = Arrays.toString(instance.toByteArray());
        assertEquals(expResult, result);
    }
    
    /**
     * Test of toByteArray method, of class ReportPacket.
     * Light1
     */
    @Test
    public void testToByteArray04() {
        ReportPacket instance = new ReportPacket(1, new NodeAddress("0.2"),
                new NodeAddress("0.0"), 2, 1);
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        map.put(new NodeAddress("0.3"), new byte[]{(byte) 1, (byte) 42, (byte) 43});
        map.put(new NodeAddress("0.4"), new byte[]{(byte) 2, (byte) 52, (byte) 53});
        instance.setLight1(new byte[] {13, 37});
        instance.setNeighbors(map);
        String expResult = "[1, 31, 0, 0, 0, 2, 2, 100, 0, 0, 2, 1, 0, 0, 0, 0, 13, 37, 0, 0, 2, 0,"
                + " 3, 1, 42, 43, 0, 4, 2, 52, 53]";
        String result = Arrays.toString(instance.toByteArray());
        assertEquals(expResult, result);
    }
    
    /**
     * Test of toByteArray method, of class ReportPacket.
     * Light2
     */
    @Test
    public void testToByteArray05() {
        ReportPacket instance = new ReportPacket(1, new NodeAddress("0.2"),
                new NodeAddress("0.0"), 2, 1);
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        map.put(new NodeAddress("0.3"), new byte[]{(byte) 1, (byte) 42, (byte) 43});
        map.put(new NodeAddress("0.4"), new byte[]{(byte) 2, (byte) 52, (byte) 53});
        instance.setLight2(new byte[] {13, 37});
        instance.setNeighbors(map);
        String expResult = "[1, 31, 0, 0, 0, 2, 2, 100, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 13, 37, 2, 0,"
                + " 3, 1, 42, 43, 0, 4, 2, 52, 53]";
        String result = Arrays.toString(instance.toByteArray());
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getTemperature, of class ReportPacket.
     */
    @Test
    public void getTemperatureTest01() {
        ReportPacket instance = new ReportPacket(1, new NodeAddress("0.2"),
                new NodeAddress("0.0"), 2, 1);
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        map.put(new NodeAddress("0.3"), new byte[]{(byte) 1, (byte) 42, (byte) 43});
        map.put(new NodeAddress("0.4"), new byte[]{(byte) 2, (byte) 52, (byte) 53});
        byte[] expResult = new byte[] {13, 37};
        instance.setTemperature(expResult);
        instance.setNeighbors(map);
        byte[] result = instance.getTemperature();
        assertArrayEquals(expResult, result);
    }
    
    /**
     * Test of getTemperature, of class ReportPacket.
     * 
     */
    @Test
    public void getTemperatureTest02() {
        ReportPacket instance = new ReportPacket(1, new NodeAddress("0.2"),
                new NodeAddress("0.0"), 2, 1);
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        map.put(new NodeAddress("0.3"), new byte[]{(byte) 1, (byte) 42, (byte) 43});
        map.put(new NodeAddress("0.4"), new byte[]{(byte) 2, (byte) 52, (byte) 53});
        byte[] expResult = new byte[] {0, 0};
        instance.setHumidity(new byte[] {13, 37});
        instance.setNeighbors(map);
        byte[] result = instance.getTemperature();
        assertArrayEquals(expResult, result);
    }
    
    /**
     * Test of getHumidity, of class ReportPacket.
     */
    @Test
    public void getHumidity01() {
        ReportPacket instance = new ReportPacket(1, new NodeAddress("0.2"),
                new NodeAddress("0.0"), 2, 1);
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        map.put(new NodeAddress("0.3"), new byte[]{(byte) 1, (byte) 42, (byte) 43});
        map.put(new NodeAddress("0.4"), new byte[]{(byte) 2, (byte) 52, (byte) 53});
        byte[] expResult = new byte[] {13, 37};
        instance.setHumidity(expResult);
        instance.setNeighbors(map);
        byte[] result = instance.getHumidity();
        assertArrayEquals(expResult, result);
    }
    
    /**
     * Test of getLight1, of class ReportPacket.
     */
    @Test
    public void getLight1Test01() {
        ReportPacket instance = new ReportPacket(1, new NodeAddress("0.2"),
                new NodeAddress("0.0"), 2, 1);
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        map.put(new NodeAddress("0.3"), new byte[]{(byte) 1, (byte) 42, (byte) 43});
        map.put(new NodeAddress("0.4"), new byte[]{(byte) 2, (byte) 52, (byte) 53});
        byte[] expResult = new byte[] {13, 37};
        instance.setLight1(expResult);
        instance.setNeighbors(map);
        byte[] result = instance.getLight1();
        assertArrayEquals(expResult, result);
    }
    
    /**
     * Test of getLight2 of class ReportPacket.
     */
    @Test
    public void getLight2Test01() {
        ReportPacket instance = new ReportPacket(1, new NodeAddress("0.2"),
                new NodeAddress("0.0"), 2, 1);
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        map.put(new NodeAddress("0.3"), new byte[]{(byte) 1, (byte) 42, (byte) 43});
        map.put(new NodeAddress("0.4"), new byte[]{(byte) 2, (byte) 52, (byte) 53});
        byte[] expResult = new byte[] {13, 37};
        instance.setLight2(expResult);
        instance.setNeighbors(map);
        byte[] result = instance.getLight2();
        assertArrayEquals(expResult, result);
    }
    
    /**
     * Test of getNeighborsSize of class ReportPacket.
     */
    @Test
    public void getNeighborsSizeTest01() {
        ReportPacket instance = new ReportPacket(1, new NodeAddress("0.2"),
                new NodeAddress("0.0"), 2, 1);
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        map.put(new NodeAddress("0.3"), new byte[]{(byte) 1, (byte) 42, (byte) 43});
        map.put(new NodeAddress("0.4"), new byte[]{(byte) 2, (byte) 52, (byte) 53});
        byte[] expResult = new byte[] {13, 37};
        instance.setLight2(expResult);
        instance.setNeighbors(map);
        assertEquals(2, instance.getNeigborsSize());
    }
}
