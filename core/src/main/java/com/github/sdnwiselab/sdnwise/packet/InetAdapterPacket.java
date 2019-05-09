package com.github.sdnwiselab.sdnwise.packet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
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
    public static final int DFLT_INET6ADDRES_LEN = 16,
            PORT_LEN = 2;

    /**
     * The length of the Header varies within IPv4 and IPv6 addresses are used.
     */
    public static final byte HEADER_LENGTH = 21;

    public static final int PORTMAX_SIZE = 65535;

    /**
     * The indexes of the different fields in the packet.
     */
    public static final int NET_INDEX = NetworkPacket.NET_INDEX,
            HLEN_INDEX = 1,
            LEN_INDEX = 2,
            PORT_INDEX = 3,
            IPADR_INDEX = 5,
            PAYLOAD_INDEX = (HEADER_LENGTH);

    private final byte[] data;


    public InetAdapterPacket(byte[] payload,
                             InetAddress clientAddress,
                             int port){

        this.data = new byte[HEADER_LENGTH + payload.length];
        setHeaderLength(HEADER_LENGTH);
        setPort(port);
        setInetAddress(clientAddress);
        setPayload(payload);
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
                setHeaderLength(HEADER_LENGTH);
                setPort(array[PORT_INDEX], array[PORT_INDEX + 1]);
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

    private void setHeaderLength(byte header){
        data[HLEN_INDEX] = header;
    }

    private void setInetAddress(InetAddress adr){
        byte[] tmp = adr.getAddress();
        System.arraycopy(tmp, 0,data, IPADR_INDEX, tmp.length );
    }

    public InetAddress getInetAdress() throws UnknownHostException {
        byte[] addarr =  Arrays.copyOfRange(data,
                IPADR_INDEX,
                DFLT_INET6ADDRES_LEN + IPADR_INDEX);
        return InetAddress.getByAddress(addarr);
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
     * Returns the length of the message.
     *
     * @return an integer representing the length of the message
     */
    public final int getLen() {
        if(isInetAdapterPacket()){
            return Byte.toUnsignedInt(data[LEN_INDEX]);
        }else {
            return data.length;
        }
    }

    public final InetAdapterPacket setNet(final byte value){
        data[NET_INDEX] = value;
        return this;
    }

    /**
     * Returns the NetworkId of the message.
     *
     * @return an integer representing the NetworkId of the message
     */
    public final int getNet() {
        return Byte.toUnsignedInt(data[NET_INDEX]);
    }


    /**
     * Set the Port
     *
     * @param port
     */
    public void setPort(int port) {
        if( (0 > port) || (port > 65535)){
            throw new IllegalArgumentException("Port is out of range, Port:" + port);
        }
        data[PORT_INDEX] = (byte) (port >> Byte.SIZE);
        data[PORT_INDEX + 1] = (byte) (port);
    }

    public InetAdapterPacket setPort(byte portHith, byte portLow){
        data[PORT_INDEX] = portHith;
        data[PORT_INDEX + 1] = portLow;
        return this;
    }


    public int getPort(){
        return  ((data[PORT_INDEX] & 0xff) << Byte.SIZE) | (data[PORT_INDEX +1] & 0xff);
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
