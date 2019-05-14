package com.github.sdnwiselab.sdnwise.mapping;

import com.github.sdnwiselab.sdnwise.util.NodeAddress;

import java.net.InetSocketAddress;
import java.util.List;

public abstract class AbstractMapping {

    public abstract List<InetSocketAddress> getAllAddresses();

    public abstract NodeAddress getNodeAddress(InetSocketAddress addr);

    public abstract InetSocketAddress getSocketAddress(NodeAddress addr);


}
