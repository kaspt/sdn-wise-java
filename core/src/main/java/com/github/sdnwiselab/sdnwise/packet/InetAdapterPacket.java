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
    public static final int DFLT_HDR_LEN = 1,
            DFLT_INET6ADDRES_LEN = 16,
            PORT_LEN = 2;

    public static final byte HEADER_LENGTH = 21;

    public static final int PORTMAX_SIZE = 65535;

    /**
     * The indexes of the different fields in the packet.
     */
    public static final int HLEN_INDEX = 1,
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
        return data.length;
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
