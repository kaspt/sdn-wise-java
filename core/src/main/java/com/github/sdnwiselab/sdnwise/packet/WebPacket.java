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

import com.github.sdnwiselab.sdnwise.util.NodeAddress;

/**
 * This class models a Web packet. The web packet is used for application
 * layer messages.
 *
 * @author Tobias Kasper
 */
public class WebPacket extends NetworkPacket {


    private static final byte MESSAGE_ID_INDEX = 0,
            START_PAYLOAD_INDEX = 1;

    /**
     * This constructor initialize a web packet starting from a byte array.
     *
     * @param data the byte array representing the web packet.
     */
    public WebPacket(final byte[] data) {
        super(data);
    }

    /**
     * This constructor initialize a web packet starting from a int array.
     *
     * @param data the int array representing the web packet, all int are
     * casted to byte.
     */
    public WebPacket(final int[] data) {
        super(data);
    }

    /**
     * This constructor initialize a web packet starting from a NetworkPacket.
     *
     * @param data the NetworkPacket representing the web packet.
     */
    public WebPacket(final NetworkPacket data) {
        super(data.toByteArray());
    }

    /**
     * This constructor initialize a web packet. The type of the packet is set
     * to SDN_WISE_DATA.
     *
     * @param net Network ID of the packet
     * @param src source address of the packet
     * @param dst destination address of the packet
     * @param payload the byte[] containing the payload of the packet
     */
    public WebPacket(final int net, final NodeAddress src,
                     final NodeAddress dst,
                     final byte messageId,
                     final byte[] payload) {
        super(net, src, dst);
        setTyp(WEB_REQUEST);
        setMessageID(messageId);
        setPayload(payload, 0,START_PAYLOAD_INDEX, payload.length);
    }

    /**
     * Setter for the Message ID to identify the response from the node.
     *
     * @param id The Id of the packet
     * @return the packet itself
     */
    public final WebPacket setMessageID(byte id){
        setPayloadAt(id, MESSAGE_ID_INDEX);
        return this;
    }

    public final byte getMessageID(){
        return getPayloadAt(MESSAGE_ID_INDEX);
    }


    /**
     * Returns the payload of the packet as a byte array.
     *
     * @return the payload of the packet
     */
    public final byte[] getData() {
        return getPayloadFromTo(START_PAYLOAD_INDEX, getLen());
    }
}
