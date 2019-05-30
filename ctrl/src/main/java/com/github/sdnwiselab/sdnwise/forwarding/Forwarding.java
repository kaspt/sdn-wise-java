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
package com.github.sdnwiselab.sdnwise.forwarding;

import com.github.sdnwiselab.sdnwise.adapter.AbstractAdapter;
import com.github.sdnwiselab.sdnwise.controlplane.ControlPlaneLayer;
import com.github.sdnwiselab.sdnwise.controlplane.ControlPlaneLogger;
import com.github.sdnwiselab.sdnwise.mapping.AbstractMapping;
import com.github.sdnwiselab.sdnwise.packet.InetAdapterPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.packet.WebPacket;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import org.javatuples.Pair;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author Tobias Kasper
 */
public class Forwarding extends ControlPlaneLayer {

    /**
     * To avoid garbage collector.
     */
    protected static final Logger LOGGER = Logger.getLogger("FWD");

    private final AbstractMapping mapping;

    private final NodeAddress sinkAddress;

    /**
     * Creates an adaptation object given two adapters.
     *
     * @param lower the adapter that receives messages from the sensor network
     * @param upper the adapter that receives messages from the controller
     */
    public Forwarding(final List<AbstractAdapter> lower,
                      final List<AbstractAdapter> upper,
                      AbstractMapping mapping) {
        super("FWD", lower, upper);
        this.mapping = mapping;
        sinkAddress = new NodeAddress("0.1");
        ControlPlaneLogger.setupLogger(getLayerShortName());
    }

    @Override
    protected void setupLayer() {
        //Notthing to do here
    }

    @Override
    public void update(Observable o, Object arg) {
        byte[] packet = (byte[])arg;
        if(NetworkPacket.isSdnWise(packet)){
            for(AbstractAdapter upper: getUpper()){
                if(o.equals(upper)){
                    managePacket_fromController(packet);
                    return;
                }
            }
            NetworkPacket networkPacket = new NetworkPacket(packet);
            managePacket_fromSDNWISE(networkPacket);
        }else {
            managePacket_fromWeb(packet);
        }
    }

    private void managePacket_fromController(byte[] data){
        log(Level.INFO, "\u2193" + Arrays.toString(data));
        getNodeAdapter().send(data);
    }

    private void managePacket_fromSDNWISE(NetworkPacket data){
        if(data.getTyp() > 0){
            log(Level.INFO, "sdn to contr"
                    + Arrays.toString(data.toByteArray()));
            for(AbstractAdapter upper: getUpper()){
                upper.send(data.toByteArray());
            }
        }else {
            log(Level.INFO, "sdn to web"
                    + Arrays.toString(data.toByteArray()));
            // Todo create Inet Adapterpacket
            // Todo find correct adapter.
            // send to web
        }
    }

    private void managePacket_fromWeb(byte[] packet){
        log(Level.INFO, "web to sdn" + Arrays.toString(packet));
        InetAdapterPacket inetAdapterPacket = new InetAdapterPacket(packet);

        WebPacket webPacket =
                packetManager.manageInetAdapterPacket(inetAdapterPacket);
        getNodeAdapter().send(webPacket.toByteArray());
    }

    private PacketManager packetManager = new PacketManager();

    private class PacketManager{

        private final int MAX_MESSAGE_PER_NODE = 255;

        private Map<NodeAddress,
                Pair<BitSet,
                        List<MessageInfos>>> messageData = new HashMap<>();

        private class MessageInfos{
            byte messageID;
            Instant timestamp;
            byte[] inetAddapterPacket;
            public MessageInfos(final byte messageID,
                                final byte[] inetAddapterPacket){
                this.messageID = messageID;
                this.inetAddapterPacket = inetAddapterPacket;
                this.timestamp = Instant.now();
            }
        }

        public WebPacket manageInetAdapterPacket(InetAdapterPacket packet){

            NodeAddress address = mapping.getNodeAddress(packet.getSdnWiseAddress(),
                    packet.getSdnWisePort());
            if(address == null){
                throw new IllegalArgumentException("node address is not valid.");
            }

            int netId = mapping.getNodeNet(address);

            Pair<BitSet, List<MessageInfos>> p = messageData.get(address);
            BitSet bitSet = null;
            if(p == null){
                messageData.put(address,
                        new Pair<BitSet, List<MessageInfos>>(
                                new BitSet(MAX_MESSAGE_PER_NODE),
                                new ArrayList<>()));
                bitSet = messageData.get(address).getValue0();
            }else {
                bitSet = p.getValue0();
            }
            int messageID = bitSet.nextClearBit(0);
            if(messageID < MAX_MESSAGE_PER_NODE){
                bitSet.set(messageID);
                MessageInfos messageInfo = new MessageInfos((byte)messageID,
                        Arrays.copyOfRange(packet.toByteArray(), 0,
                                InetAdapterPacket.HEADER_LENGTH));
                messageData.get(address).getValue1().add(messageInfo);
            }else {
                // Todo handle message overflow
                throw new IllegalArgumentException("no free messages IDs");
            }
            byte[] payload = packet.getPayload();


            WebPacket webPacket = new WebPacket(netId,
                    sinkAddress,
                    address,
                    (byte)messageID,
                    packet.getPayload());

            return webPacket;
        }
        public InetAdapterPacket getInetPacket(byte messageID,
                                               NodeAddress src,
                                               byte[] payload){
            // TODo get infos from map ,remove message from map,

            InetAdapterPacket packet = new InetAdapterPacket(payload, (byte)64);


            return packet;
        }
    }


    /**
     * private atributte for optimising
     */
    private AbstractAdapter nodeAdapter = null;

    private AbstractAdapter getNodeAdapter(){
        if(nodeAdapter != null){
            return nodeAdapter;
        }else {
            for (AbstractAdapter a : getLower()) {
                if (a.getAdapterIdentifier().equals("ADAPT_NODE")) {
                    nodeAdapter = a;
                    return nodeAdapter;
                }
            }
        }
        throw new UnsupportedOperationException("Node adapter not found");
    }

    /**
     * private atributte for optimising
     */
    private AbstractAdapter webAdapter = null;

    private AbstractAdapter getWebAdapter(){
        if(webAdapter != null){
            return webAdapter;
        }else {
            for(AbstractAdapter a: getLower()){
                if(a.getAdapterIdentifier().equals("ADAPT_WEB")){
                    webAdapter = a;
                    return webAdapter;
                }
            }
        }
        throw new UnsupportedOperationException("Web adapter not found");
    }

}
