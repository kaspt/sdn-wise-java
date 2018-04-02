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
package com.github.sdnwiselab.sdnwise.packet;

import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.REPORT;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.plaf.synth.SynthSeparatorUI;

/**
 * This class models a Report packet.
 *
 * @author Sebastiano Milardo
 */
public class ReportPacket extends BeaconPacket {

    /**
     * The maximum number of neighbors allowed in a single packet is 35.
     */
    private static final byte MAX_NEIG = 19,
            NEIGH_INDEX = 10, // initially was 2 changed to 10 -> 4*2 bytes for sensor values
            NEIGH_SIZE = 5, // 2 for address 1 for rssi 2 for rx and tx stat
            RSSI_INDEX = 2,
            RX_COUNT_INDEX = 3,
            TX_COUNT_INDEX = 4,
            TEMPERATURE_INDEX = 2,
            HUMIDITY_INDEX = 4,
            LIGHT1_INDEX = 6,
            LIGHT2_INDEX = 8;

    /**
     * This constructor initialize a report packet starting from a byte array.
     *
     * @param data the byte array representing the report packet.
     */
    public ReportPacket(final byte[] data) {
        super(data);
    }

    /**
     * This constructor initialize a report packet. The type of the packet is
     * set to SDN_WISE_REPORT.
     *
     * @param net Network ID of the packet
     * @param src source address of the packet
     * @param dst destination address of the packet
     * @param distance the distance in hops between the source node and the sink
     * @param battery the battery level of the source node
     */
    public ReportPacket(final int net, final NodeAddress src,
            final NodeAddress dst,
            final int distance,
            final int battery) {
        super(net, src, dst, distance, battery);
        setDst(dst);
        setTyp(REPORT);
    }

    /**
     * This constructor initialize a report packet starting from a int array.
     *
     * @param data the int array representing the report packet, all int are
     * casted to byte.
     */
    public ReportPacket(final int[] data) {
        super(data);
    }

    /**
     * This constructor initialize a report packet starting from a
     * NetworkPacket.
     *
     * @param data the NetworkPacket representing the report packet.
     */
    public ReportPacket(final NetworkPacket data) {
        super(data.toByteArray());
    }

    /**
     * Getter for the number of neighbors of the source node.
     *
     * @return the number of neighbors.
     */
    public final int getNeigborsSize() {
        return Byte.toUnsignedInt(getPayloadAt(NEIGH_INDEX));
    }

    /**
     * Setter for the number of neighbors of the source node.
     *
     * @param value the number of neighbors.
     * @return the packet itself
     */
    public final ReportPacket setNeighbors(final int value) {
        if (value <= MAX_NEIG) {
            setPayloadAt((byte) value, NEIGH_INDEX);
            setPayloadSize((byte) (NEIGH_INDEX + value * NEIGH_SIZE + 1));
        } else {
            throw new IllegalArgumentException("Too many neighbors");
        }
        return this;
    }

    /**
     * Getter for the NodeAddress of the i-th node in the neighbor list.
     *
     * @param i the i-th node in the neighbors list
     * @return the NodeAddress of the i-th node in the neighbors list
     */
    public final NodeAddress getNeighborAddress(final int i) {
        if (i <= MAX_NEIG) {
            return new NodeAddress(
                    getPayloadAt(NEIGH_INDEX + 1 + (i * NEIGH_SIZE)),
                    getPayloadAt(NEIGH_INDEX + 2 + (i * NEIGH_SIZE)));
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }

    /**
     * Setter for the NodeAddress of the i-th node in the neighbor list.
     *
     * @param addr the address of the i-th NodeAddress.
     * @param i the position where the NodeAddress will be inserted.
     * @return the packet itself
     */
    public final ReportPacket setNeighborAddressAt(final NodeAddress addr,
            final int i) {
        if (i <= MAX_NEIG) {
            setPayloadAt(addr.getHigh(), (NEIGH_INDEX + 1 + (i * NEIGH_SIZE)));
            setPayloadAt(addr.getLow(), (NEIGH_INDEX + 2 + (i * NEIGH_SIZE)));
            if (getNeigborsSize() < i) {
                setNeighbors(i);
            }
            return this;
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }

    /**
     * Getter for the rssi value between the i-th node in the neighbor list and
     * the source node.
     *
     * @param i the i-th node in the neighbors list
     * @return the rssi value
     */
    public final int getLinkQuality(final int i) {
        if (i <= MAX_NEIG) {
            return getPayloadAt(NEIGH_INDEX + 1 + i* NEIGH_SIZE + RSSI_INDEX) & 0xFF;
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }

    /**
     * Setter for the rssi value between the i-th node in the neighbor list and
     * the source node.
     *
     * @param i the i-th node in the neighbors list.
     * @param value the weight of the link.
     * @return the packet itself
     */
    public final ReportPacket setLinkQualityAt(final byte value, final int i) {
        if (i <= MAX_NEIG) {
            setPayloadAt(value, NEIGH_INDEX + 1 + i* NEIGH_SIZE + RSSI_INDEX);
            if (getNeigborsSize() < i) {
                setNeighbors(i);
            }
            return this;
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }

    /**
     * Getter for the counter value of the packets received from the source node to the i-th node
     * in the neighbor list
     * @param i the i-ith node in the neighbors list
     * @return the rx count value
     */
    public final int getRxCount(final int i) {
        if (i <= MAX_NEIG) {
            return getPayloadAt(NEIGH_INDEX + 1 + i* NEIGH_SIZE + RX_COUNT_INDEX);
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }
    
    /**
     * Setter for the rx count value between the source node and the i-th node in the 
     * neighbor list
     *
     * @param i the i-th node in the neighbors list.
     * @param value the rx count
     * @return the packet itself
     */
    public final ReportPacket setRxCount(final byte value, final int i) {
        if (i <= MAX_NEIG) {
            setPayloadAt(value, NEIGH_INDEX + 1 + i* NEIGH_SIZE + RX_COUNT_INDEX);
            if (getNeigborsSize() < i) {
                setNeighbors(i);
            }
            return this;
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }

    /**
     * Getter for the counter value of the packets sent from the source node to the i-th node
     * in the neighbor list
     * @param i the i-ith node in the neighbors list
     * @return the tx count value
     */
    public final int getTxCount(final int i) {
        if (i <= MAX_NEIG) {
            return getPayloadAt(NEIGH_INDEX + 1 + i* NEIGH_SIZE + TX_COUNT_INDEX);
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }
    
    /**
     * Setter for the tx count value between the source node and the i-th node in the 
     * neighbor list
     *
     * @param i the i-th node in the neighbors list.
     * @param value the tx count
     * @return the packet itself
     */
    public final ReportPacket setTxCount(final byte value, final int i) {
        if (i <= MAX_NEIG) {
            setPayloadAt(value, NEIGH_INDEX + 1 + i* NEIGH_SIZE + TX_COUNT_INDEX);
            if (getNeigborsSize() < i) {
                setNeighbors(i);
            }
            return this;
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }
    
    /**
     * Setter for the Temperature of the source node
     * @param the temperature
     * @return the packet itself
     */
    public final ReportPacket setTemperature(final byte[] value) {
    	
        if (value.length == 2) {
            setPayloadAt(value[0], TEMPERATURE_INDEX);
            setPayloadAt(value[1], TEMPERATURE_INDEX + 1);
            return this;
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }
    
    /**
     * Getter for the Temperature of the source node
     * @return the two temperature bytes
     */
    public final byte[] getTemperature() {
    	return new byte[]{getPayloadAt(TEMPERATURE_INDEX), getPayloadAt(TEMPERATURE_INDEX + 1)};
    }
    
    /**
     * Getter for the Temperature of the source node as double
     * @return the temperature as double in °C
     */
    public final double getTemperatureAsDouble() {
    	int intVal = getIntValFrom2Bytes(TEMPERATURE_INDEX);
    	return ((double)(intVal / 10.0 - 396)/10.0);
    }
    
    /**
     * Setter for the humidity of the source node
     * @param the humidity as two bytes
     * @return the packet itself
     */
    public final ReportPacket setHumidity(final byte[] value) {
    	
        if (value.length == 2) {
            setPayloadAt(value[0], HUMIDITY_INDEX);
            setPayloadAt(value[1], HUMIDITY_INDEX + 1);
            return this;
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }
    
    /**
     * Getter for the humidity of the source node
     * @return the two humidity bytes
     */
    public final byte[] getHumidity() {
    	return new byte[]{getPayloadAt(HUMIDITY_INDEX), getPayloadAt(HUMIDITY_INDEX + 1)};
    }
    
    /**
     * Getter for the Humidity of the source node as double
     * @return the humidity as double in %
     */
    public final double getHumidityAsDouble() {
    	int intVal = getIntValFrom2Bytes(HUMIDITY_INDEX);
    	return -4+0.0405*(double)intVal-2.8e-6*((double)intVal)*((double)intVal);
    }
    
    /**
     * Setter for the light1 of the source node
     * @param the light1 as two bytes
     * @return the packet itself
     */
    public final ReportPacket setLight1(final byte[] value) {
    	
        if (value.length == 2) {
            setPayloadAt(value[0], LIGHT1_INDEX);
            setPayloadAt(value[1], LIGHT1_INDEX + 1);
            return this;
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }
    
    /**
     * Getter for the light1 of the source node
     * @return the two light1 bytes
     */
    public final byte[] getLight1() {
    	return new byte[]{getPayloadAt(LIGHT1_INDEX), getPayloadAt(LIGHT1_INDEX + 1)};
    }
    
    /**
     * Getter for the Light1 of the source node as double
     * @return the light1 as double 
     */
    public final double getLight1AsDouble() {
    	return (double)getIntValFrom2Bytes(LIGHT1_INDEX)*10.0/7.0;
    }
    
    /**
     * Setter for the light2 of the source node
     * @param the light2 as two bytes
     * @return the packet itself
     */
    public final ReportPacket setLight2(final byte[] value) {
    	
        if (value.length == 2) {
            setPayloadAt(value[0], LIGHT2_INDEX);
            setPayloadAt(value[1], LIGHT2_INDEX + 1);
            return this;
        } else {
            throw new IllegalArgumentException(
                    "Index exceeds max number of neighbors");
        }
    }
    
    /**
     * Getter for the light2 of the source node
     * @return the two light2 bytes
     */
    public final byte[] getLight2() {
    	return new byte[]{getPayloadAt(LIGHT2_INDEX), getPayloadAt(LIGHT2_INDEX + 1)};
    }
    
    /**
     * Getter for the Light2 of the source node as double
     * @return the light2 as double 
     */
    public final double getLight2AsDouble() {
    	return (double) getIntValFrom2Bytes(LIGHT2_INDEX);
    }
    
    /**
     * Gets the list of Neighbors.
     *
     * @return an HashMap filled with the neighbors and their weights.
     */
    public final HashMap<NodeAddress, byte[]> getNeighbors() {
        HashMap<NodeAddress, byte[]> map = new HashMap<>();
        int nNeig = getNeigborsSize();
        for (int i = 0; i < nNeig; i++) {
            map.put(getNeighborAddress(i),
                    new byte[] {(byte)getLinkQuality(i), (byte)getRxCount(i), (byte)getTxCount(i)});
        }
        return map;
    }

    /**
     * Sets the list of Neighbors.
     *
     * @param map the map of neighbors to be set
     * @return the packet itself
     */
    public final ReportPacket setNeighbors(
            final HashMap<NodeAddress, byte[]> map) {
        int i = 0;
        for (Map.Entry<NodeAddress, byte[]> entry : map.entrySet()) {
            setNeighborAddressAt(entry.getKey(), i);
            setLinkQualityAt(entry.getValue()[0], i);
            setRxCount(entry.getValue()[1], i);
            setTxCount(entry.getValue()[2], i);
            i++;
        }
        setNeighbors((byte) map.size());
        return this;
    }
    
    public final int getIntValFrom2Bytes(int index) {
    	short msb = (short) (getPayloadAt(index + 1) & 0xFF);
    	short lsb = (short) (getPayloadAt(index) & 0xFF);

    	return  (0x00 << 32 | msb << 8) | lsb;
    }
}
