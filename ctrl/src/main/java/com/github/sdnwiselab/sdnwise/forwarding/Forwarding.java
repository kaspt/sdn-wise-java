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
import com.github.sdnwiselab.sdnwise.packet.DataPacket;
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

    public final NodeAddress getSinkAddress(){
        return sinkAddress;
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
        log(Level.INFO, "from contr" + Arrays.toString(data));
        //log(Level.INFO, "\u2193" + Arrays.toString(data));
        getNodeAdapter().send(data);
    }

    private void managePacket_fromSDNWISE(NetworkPacket data){
        if(data.getTyp() > 0 & data.getTyp() != NetworkPacket.WEB_REQUEST){
            log(Level.INFO, "sdn to contr"
                    + Arrays.toString(data.toByteArray()));
            for(AbstractAdapter upper: getUpper()){
                upper.send(data.toByteArray());
            }
        }else {
            log(Level.INFO, "sdn to web"
                    + Arrays.toString(data.toByteArray()));
            InetAdapterPacket packet =
                    packetManager.getInetPacket(new WebPacket(data));
            getWebAdapter().send(packet.toByteArray());
        }
    }

    private void managePacket_fromWeb(byte[] packet){
        log(Level.INFO, "web to sdn" + Arrays.toString(packet));
        InetAdapterPacket inetAdapterPacket = new InetAdapterPacket(packet);

        NetworkPacket webPacket =
                packetManager.manageInetAdapterPacket(inetAdapterPacket);
        getNodeAdapter().send(webPacket.toByteArray());
    }

    private  PacketManager packetManager = new PacketManager();

    private class PacketManager{

        private final static int MAX_MESSAGE_PER_NODE = 255;

        private  Map<NodeAddress, Pair<BitSet, List<MessageInfos>>> messageData = new HashMap<>();

        private class MessageInfos{
            public byte messageID;
            public Instant timestamp;
            public byte[] inetAddapterPacket;

            public MessageInfos(final byte messageID,
                                final byte[] inetAddapterPacket){
                this.messageID = messageID;
                this.inetAddapterPacket = inetAddapterPacket;
                this.timestamp = Instant.now();
            }

            public byte getMessageID() {
                return messageID;
            }

            @Override
            public String toString(){
                return "mID:"+ messageID
                        + "inet:["
                        + Arrays.toString(inetAddapterPacket)+"]";
            }

        }

        private void print_mappinfo(){
            log(Level.INFO, "messageData.size: " + messageData.size());
            for(NodeAddress naddr: messageData.keySet()){
                log(Level.INFO, "map keys:" + naddr.toString());
                log(Level.FINE, "fine:");
                log(Level.INFO, "bitset:"
                        + messageData.get(naddr).getValue0().toString());

                for(MessageInfos minfo : messageData.get(naddr).getValue1()){
                    log(Level.INFO, "Minof" + minfo.toString());
                }
            }
            for(int i =messageData.size();i > 0;i--){

                log(Level.INFO, "messageData.size: " + messageData.size());

            }
        }

        public NetworkPacket manageInetAdapterPacket(InetAdapterPacket packet){
            print_mappinfo();
            NodeAddress address = mapping.getNodeAddress(
                    packet.getSdnWiseAddress(),
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
            webPacket.setNxh(sinkAddress);

            return webPacket;
        }


        public InetAdapterPacket getInetPacket(WebPacket data){
            print_mappinfo();
            NodeAddress dest = data.getSrc();
            byte messageID = data.getMessageID();
            byte[] payload = data.getData();

            Pair<BitSet, List<MessageInfos>> pair = messageData.get(dest);
            pair.getValue0().clear(messageID);

            List<MessageInfos> infosList = pair.getValue1();

            MessageInfos mymessage = infosList
                    .stream()
                    .filter(mess -> mess.getMessageID() == messageID)
                    .findFirst().orElse(null);

            byte[] packet = mymessage.inetAddapterPacket;
            byte[] bytes = new byte[packet.length + payload.length];
            System.arraycopy(packet, 0, bytes, 0, packet.length);
            System.arraycopy(payload, 0, bytes, packet.length, payload.length);
            infosList.remove(mymessage);

            return new InetAdapterPacket(bytes);
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
