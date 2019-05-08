package com.github.sdnwiselab.sdnwise.packet;

import javax.naming.OperationNotSupportedException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * This class represents a packet with a socket ID.
 *
 * @author Tobias Kasper
 */
public class InetAdapterPacket {

    /**
     * The length of the different field in the InetAdapterPacket.
     */
    public static final int DFLT_HDR_LEN = 1,
            DFLT_INET6ADDRES_LEN = 16,
            DFLT_PORT_LEN = 2;

    public static final byte HEADER_LENGTH =
            DFLT_INET6ADDRES_LEN + DFLT_HDR_LEN + DFLT_PORT_LEN;

    /**
     * The indexes of the different fields in the packet.
     */
    public static final int HEADER_LENGTH_INDEX = 0,
            PORT_INDEX = 1,
            ADDRESS_INDEX = 2;

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

    private void setHeaderLength(byte header){
        data[HEADER_LENGTH_INDEX] = header;
    }

    private void setInetAddress(InetAddress adr){
        byte[] tmp = adr.getAddress();
        System.arraycopy(tmp, 0,data, ADDRESS_INDEX, tmp.length );
    }

    public InetAddress getInetAdress() throws UnknownHostException {
        byte[] addrbytearr = new byte[DFLT_INET6ADDRES_LEN];
        System.arraycopy(data, ADDRESS_INDEX, data, 0, DFLT_INET6ADDRES_LEN);
        InetAddress adr = InetAddress.getByAddress(addrbytearr);
        return adr;
    }

    private int getPayloadIndex(){
        return (HEADER_LENGTH_INDEX +1);
    }

    public byte[] setPayload(byte[] payload) {
        if(payload.length + HEADER_LENGTH > data.length){
            throw new IllegalArgumentException("payload length is bigger " +
                    "than data length");
        }
        System.arraycopy(payload, 0, data, getPayloadIndex(), payload.length);
        return Arrays.copyOfRange(data, getPayloadIndex(), data.length);
    }

    public byte[] getPayload(){

        return data;
    }

    /**
     * Returns the length of the message.
     *
     * @return an integer representing the length of the message
     */
    public final int getLen() {
        return data.length;
    }

    /**
     * Set the Port
     *
     * @param port
     */
    public void setPort(int port){
        byte[] portarr = ByteBuffer.allocate(DFLT_PORT_LEN).putInt(port).array();
        data[PORT_INDEX] = portarr[0];
        data[PORT_INDEX+1] = portarr[1];
    }

    public int getPort(){
        return  ((data[PORT_INDEX] & 0xff) << Byte.SIZE) | (data[PORT_INDEX+1] & 0xff);
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
     * Returns a byte array representation of the NetworkPacket.
     *
     * @return a byte array representation of the NetworkPacket
     */
    public final byte[] toByteArray() {
        return Arrays.copyOf(data, getLen());
    }

    /**
     * Returns an int array representation of the NetworkPacket.
     *
     * @return a int array representation of the NetworkPacket
     */
    public final int[] toIntArray() {
        int[] tmp = new int[getLen()];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = Byte.toUnsignedInt(data[i]);
        }
        return tmp;
    }

}
