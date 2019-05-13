package com.github.sdnwiselab.sdnwise.packet;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.MAX_PACKET_LENGTH;


/**
 * This class represents a packet with a socket ID.
 *
 * @author Tobias Kasper
 */
public class InetAdapterPacket {

    /**
     * The length of the different field in the InetAdapterPacket.
     */
    public static final byte IPv6ADDRES_LEN = 16,
            PORT_LEN = 2,
            IPVERSION_LEN = 1,
            HEADER_LENGTH = 40;

    public static final int IPv6Version = 6,
            IPv4Version= 4;

    public static final int PORTMAX_SIZE = 65535;

    /**
     * The indexes of the different fields in the packet.
     */
    public static final int NET_INDEX = NetworkPacket.NET_INDEX,
            LEN_INDEX = 1,
            IPVERS_CLIENT_INDEX = 2,
            PORT_CLIENT_INDEX = 3,
            IPADDR_CLIENT_INDEX = 5,
            IPVERS_SDNWISE_INDEX = 21,
            PORT_SDNWISE_INDEX = 22,
            IPADDR_SDNWISE_INDEX = 24,
            PAYLOAD_INDEX = (HEADER_LENGTH);

    private final byte[] data;


    public InetAdapterPacket(byte[] payload, byte netID){
        this.data = new byte[HEADER_LENGTH + payload.length];
        setPayload(payload);
        setLen((byte)(HEADER_LENGTH + payload.length));
        setNet(netID);
    }

    public InetAdapterPacket(byte[] d){
        data = new byte[MAX_PACKET_LENGTH];
        setArray(d);
    }

    /**
     * Fills the InetAdapterPacket with the content of a byte array.
     *
     * @param array an array representing the packet
     */
    public final void setArray(final byte[] array) {
        if (isInetAdapterPacket(array)) {
            if (array.length <= MAX_PACKET_LENGTH && array.length
                    >= HEADER_LENGTH) {
                setNet(array[NET_INDEX]);
                setLen(array[LEN_INDEX]);
                setClientIPVersion(array[IPVERS_CLIENT_INDEX]);
                setClientPort(array[PORT_CLIENT_INDEX], array[PORT_CLIENT_INDEX + 1]);
                setClientAddress(Arrays.copyOfRange(array,
                        IPADDR_CLIENT_INDEX,
                        IPv6ADDRES_LEN + IPADDR_CLIENT_INDEX));
                setSdnWiseIPVersion(array[IPVERS_SDNWISE_INDEX]);
                setSdnWisePort(array[PORT_SDNWISE_INDEX], array[PORT_SDNWISE_INDEX+ 1]);
                setSdnWiseAddress(Arrays.copyOfRange(array,
                        IPADDR_SDNWISE_INDEX,
                        IPv6ADDRES_LEN + IPADDR_SDNWISE_INDEX));

                setPayload(Arrays.copyOfRange(array, HEADER_LENGTH,
                        getLen()));
            } else {
                throw new IllegalArgumentException("Invalid array size: "
                        + array.length);
            }
        } else {
            System.arraycopy(array, 0, data, 0, array.length);
        }
    }

    public boolean isInetAdapterPacket(byte[] data){
        return (Byte.toUnsignedInt(data[NET_INDEX]) > NetworkPacket.THRES);
    }

    public boolean isInetAdapterPacket(){
        return (Byte.toUnsignedInt(data[NET_INDEX]) > NetworkPacket.THRES);
    }

    public InetAdapterPacket setClientIPVersion(byte ipVersion){
        data[IPVERS_CLIENT_INDEX] = ipVersion;
        return this;
    }

    public byte getIpClientIPVersion(){
        return  data[IPVERS_CLIENT_INDEX];
    }

    public int getClientPort(){
        return  ((data[PORT_CLIENT_INDEX] & 0xff) << Byte.SIZE) | (data[PORT_CLIENT_INDEX +1] & 0xff);
    }

    public InetAdapterPacket setClientPort(int port) {
        if( (0 > port) || (port > 65535)){
            throw new IllegalArgumentException("Port is out of range, Port:" + port);
        }
        data[PORT_CLIENT_INDEX] = (byte) (port >> Byte.SIZE);
        data[PORT_CLIENT_INDEX + 1] = (byte) (port);
        return this;
    }

    public InetAdapterPacket setClientPort(byte portHith, byte portLow){
        data[PORT_CLIENT_INDEX] = portHith;
        data[PORT_CLIENT_INDEX + 1] = portLow;
        return this;
    }

    public byte[] getClientAddress(){
        byte[] addr =  Arrays.copyOfRange(data,
                IPADDR_CLIENT_INDEX,
                IPv6ADDRES_LEN + IPADDR_CLIENT_INDEX);
        return addr;
    }

    public InetAdapterPacket setClientAddress(final byte[] adr){
        if(adr.length == IPv6ADDRES_LEN){
            System.arraycopy(adr, 0, data, IPADDR_CLIENT_INDEX, adr.length);
        }else {
            throw new IllegalArgumentException(
                    "address length is invalid. (length="
                            + adr.length + ")");
        }
        return this;
    }

    public InetAdapterPacket setSdnWiseIPVersion(byte ipVersion){
        data[IPVERS_SDNWISE_INDEX] = ipVersion;
        return this;
    }

    public byte getSdnWiseIPVersion(){
        return  data[IPVERS_SDNWISE_INDEX];
    }

    public int getSdnWisePort(){
        return  ((data[PORT_SDNWISE_INDEX] & 0xff) << Byte.SIZE) |
                (data[PORT_SDNWISE_INDEX +1] & 0xff);
    }

    public InetAdapterPacket setSdnWisePort(int port) {
        if( (0 > port) || (port > 65535)){
            throw new IllegalArgumentException("Port is out of range, Port:" + port);
        }
        data[PORT_SDNWISE_INDEX] = (byte) (port >> Byte.SIZE);
        data[PORT_SDNWISE_INDEX + 1] = (byte) (port);
        return this;
    }

    public InetAdapterPacket setSdnWisePort(byte portHith, byte portLow){
        data[PORT_SDNWISE_INDEX] = portHith;
        data[PORT_SDNWISE_INDEX + 1] = portLow;
        return this;
    }

    public byte[] getSdnWiseAddress(){
        byte[] addr =  Arrays.copyOfRange(data,
                IPADDR_SDNWISE_INDEX,
                IPv6ADDRES_LEN + IPADDR_SDNWISE_INDEX);
        return addr;
    }

    public InetAdapterPacket setSdnWiseAddress(final byte[] adr){
        if(adr.length == IPv6ADDRES_LEN){
            System.arraycopy(adr, 0, data, IPADDR_SDNWISE_INDEX, adr.length);
        }else {
            throw new IllegalArgumentException(
                    "address length is invalid. (length="
                            + adr.length + ")");
        }
        return this;
    }

    public InetAdapterPacket setPayload(byte[] payload) {
        if(payload.length + HEADER_LENGTH > data.length){
            throw new IllegalArgumentException("payload length is bigger " +
                    "than data length");
        }
        System.arraycopy(payload, 0, data, PAYLOAD_INDEX, payload.length);
        setLen((byte)(payload.length + HEADER_LENGTH));
        return this;
    }

    public final byte[] getPayload(){
        return Arrays.copyOfRange(data, HEADER_LENGTH, getLen());
    }


    public final int getNet() {
        return Byte.toUnsignedInt(data[NET_INDEX]);
    }

    public final InetAdapterPacket setNet(final byte value){
        data[NET_INDEX] = value;
        return this;
    }


    public final int getLen() {
        if(isInetAdapterPacket()){
            return Byte.toUnsignedInt(data[LEN_INDEX]);
        }else {
            return data.length;
        }
    }

    /**
     * Sets the length of the message.
     *
     * @param value an integer representing the length of the message.
     * @return the packet itself
     */
    public final InetAdapterPacket setLen(final byte value) {
        int v = Byte.toUnsignedInt(value);
        if (v <= MAX_PACKET_LENGTH && v > 0) {
            data[LEN_INDEX] = value;
        } else {
            throw new IllegalArgumentException("Invalid length: " + v);
        }
        return this;
    }



    /**
     * Returns a String representation of the NetworkPacket.
     *
     * @return a String representation of the NetworkPacket
     */
    @Override
    public final String toString() {
        return Arrays.toString(toIntArray());
    }

    /**
     * Returns a byte array representation of the InetAdaperPacket.
     *
     * @return a byte array representation of the InetAdaperPacket
     */
    public final byte[] toByteArray() {
        return Arrays.copyOf(data, getLen());
    }

    /**
     * Returns an int array representation of the InetAdaperPacket.
     *
     * @return a int array representation of the InetAdaperPacket
     */
    public final int[] toIntArray() {
        int[] tmp = new int[getLen()];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = Byte.toUnsignedInt(data[i]);
        }
        return tmp;
    }

}
