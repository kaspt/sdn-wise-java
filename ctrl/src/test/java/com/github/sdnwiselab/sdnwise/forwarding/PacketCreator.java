package com.github.sdnwiselab.sdnwise.forwarding;

import com.github.sdnwiselab.sdnwise.packet.*;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import org.junit.platform.engine.support.hierarchical.Node;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;


import static org.junit.jupiter.api.Assertions.fail;

public class PacketCreator {

    private final int NETID = 0;

    private byte[] getPayload(int payloadLength){
        byte[] data = new byte[payloadLength];
        for(int i=1; i < payloadLength; i++){
            data[i] = (byte) i;
        }
        return data;
    }

    public InetAdapterPacket getInetPacket(InetSocketAddress sdnwiseAddr){
        try {
            InetSocketAddress clientAddr = new InetSocketAddress(
                    InetAddress.getByName("fe80::250:56ff:fec0:8"),
                    9999);
            return getInetPacket(clientAddr, sdnwiseAddr);
        }catch (UnknownHostException ex){
            throw new InvalidParameterException("ip is invalid " +
                    ex.toString());
        }
    }


    public InetAdapterPacket getInetPacket(InetSocketAddress cliAddr,
                                           InetSocketAddress sdnwiseAddr){
        byte[] payload = getPayload(5);
        return  (new InetAdapterPacket(payload,(byte) 64))
                .setSdnWiseAddress(sdnwiseAddr.getAddress().getAddress())
                .setSdnWisePort(sdnwiseAddr.getPort())
                .setSdnWiseIPVersion((byte)6)
                .setClientAddress(cliAddr.getAddress().getAddress())
                .setClientPort(cliAddr.getPort())
                .setClientIPVersion((byte) 6);
    }

    public NetworkPacket getDataPacket(NodeAddress src, NodeAddress dest){
        byte[] payload = getPayload(5);
        return  new DataPacket(NETID, src, dest, payload);
    }

    public NetworkPacket getControllerPacket(NodeAddress src,
                                             NodeAddress dest,
                                             int type){
        NetworkPacket netPack = new NetworkPacket(NETID, src, dest);
        switch (type){
            case NetworkPacket.BEACON:
                return new BeaconPacket(netPack);
            case NetworkPacket.REPORT:
                return new ReportPacket(netPack);
            case NetworkPacket.REQUEST:
                return new RequestPacket(netPack);
            case NetworkPacket.RESPONSE:
                return new ResponsePacket(netPack);
            case NetworkPacket.OPEN_PATH:
                return new OpenPathPacket(netPack);
            case NetworkPacket.CONFIG:
                return new ConfigPacket(netPack);
            case NetworkPacket.REG_PROXY:
                return new RegProxyPacket(netPack);
            default:
                throw new InvalidParameterException("Type not supported");

        }
    }

}
