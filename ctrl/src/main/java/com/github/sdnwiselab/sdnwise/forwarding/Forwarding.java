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
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpiringMap;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    private void closeClientConnection(InetAdapterPacket data){
        data.setCommandClosePacket(true);
        getWebAdapter().send(data.toByteArray());
    }

    private  PacketManager packetManager = new PacketManager(this::closeClientConnection);

    private interface Callback {
        void callback(InetAdapterPacket packet);
    }

    private class PacketManager{

        private final static int MAX_MESSAGE_PER_NODE = 255;

        private final static int TIMEOUT_FOR_NODE_RESPONCE = 60;
        private final TimeUnit TIMEOUTUNIT_FOR_NODE_RESPONCE = TimeUnit.SECONDS;

        private volatile Map<NodeAddress,Map<Integer, MessageInfo>>
                messageData = new HashMap<>();

        private final Callback closeClient;

        public PacketManager(Callback closeClientCallback){
            closeClient = closeClientCallback;
        }

        private class MessageInfo {
            public byte messageID;
            public Instant timestamp;
            public byte[] inetAddapterPacket;


            public MessageInfo(final byte messageID,
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
                return new StringBuilder().append("(mid[")
                        .append(messageID).append("] inet[")
                        .append(Arrays.toString(inetAddapterPacket)).append("])")
                        .toString();
            }

        }

        private void print_mappinfo(){
            StringBuilder sb = new StringBuilder();
            sb.append("|");
            for( NodeAddress nodeAddress :messageData.keySet()){
                sb.append(nodeAddress).append("\t|\n");
                Map<Integer, MessageInfo> message_map = messageData.get(nodeAddress);
                for(Integer ID : message_map.keySet()){
                    sb.append("\t[").append(ID).append("][")
                            .append(message_map.get(ID).toString())
                            .append("]\n");
                }
                sb.append("\n----------------------------------------------");
            }
            log(Level.INFO, sb.toString());
        }


        public synchronized NetworkPacket manageInetAdapterPacket(InetAdapterPacket packet){
            print_mappinfo();
            NodeAddress address = mapping.getNodeAddress(
                    packet.getSdnWiseAddress(),
                    packet.getSdnWisePort());
            if(address == null){
                closeClient.callback(packet);
                throw new IllegalArgumentException("node address is not valid.");
            }
            int netId = mapping.getNodeNet(address);
            Map<Integer, MessageInfo> p = messageData.get(address);

            if(p==null){
                Map<Integer, MessageInfo> messageMap = ExpiringMap.builder()
                        .expiration(TIMEOUT_FOR_NODE_RESPONCE, TIMEOUTUNIT_FOR_NODE_RESPONCE)
                        .expirationListener(new ExpirationListener<Integer, MessageInfo>() {
                            public void expired(Integer key, MessageInfo value) {
                                closeClient.callback(
                                        new InetAdapterPacket(value.inetAddapterPacket));
                                log(Level.INFO,"timeout..." + key + ":" + value);
                            }
                        })
                        .build();
                messageData.put(address,messageMap);
                p = messageData.get(address);
            }

            int messageID = 0;
            while (p.containsKey(messageID)){
                messageID = messageID +1;
            }
            if(messageID < MAX_MESSAGE_PER_NODE){
                MessageInfo messageInfo = new MessageInfo((byte)messageID,
                        Arrays.copyOfRange(packet.toByteArray(), 0,
                                InetAdapterPacket.HEADER_LENGTH));
                p.put(messageID, messageInfo);
            }else {
                // Todo handle message overflow
                closeClient.callback(packet);
                throw new IllegalArgumentException("no free messages IDs");
            }

            WebPacket webPacket = new WebPacket(netId,
                    sinkAddress,
                    address,
                    (byte)messageID,
                    packet.getPayload());
            webPacket.setNxh(sinkAddress);

            return webPacket;
        }

        public synchronized InetAdapterPacket getInetPacket(WebPacket data){
            log(Level.INFO, "handle packet form sdn network");
            print_mappinfo();
            NodeAddress dest = data.getSrc();
            int messageID = data.getMessageID();
            byte[] payload = data.getData();

            Map<Integer, MessageInfo> messageMap = messageData.get(dest);
            MessageInfo mymessage = messageMap.get(messageID);

            byte[] packet = mymessage.inetAddapterPacket;
            byte[] bytes = new byte[packet.length + payload.length];
            System.arraycopy(packet, 0, bytes, 0, packet.length);
            System.arraycopy(payload, 0, bytes, packet.length, payload.length);
            messageMap.remove(messageID);

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
