package com.github.sdnwiselab.sdnwise.mapping;

import com.github.sdnwiselab.sdnwise.util.NodeAddress;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class MappingStatic extends AbstractMapping {

    private Map<NodeAddress, InetSocketAddress> lookuptable = new HashMap<>();

    public MappingStatic(String mappingFile){
        fillLookuptable(mappingFile);
    }

    public void fillLookuptable(String file){
        String line = "";
        String cvsSplitBy = ";";


        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((line = br.readLine()) != null) {
                String[] splitedLine = line.split(cvsSplitBy);

                NodeAddress nodeAddress = new NodeAddress(splitedLine[0]);
                InetSocketAddress socketAddress =
                        new InetSocketAddress(splitedLine[1],
                                Integer.parseInt(splitedLine[2]));
                lookuptable.put(nodeAddress, socketAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<InetSocketAddress> getAllAddresses() {
        return new ArrayList<>(lookuptable.values());
    }

    @Override
    public NodeAddress getNodeAddress(InetSocketAddress addr) {
        try {
            return lookuptable.entrySet()
                    .stream()
                    .filter(entry -> addr.equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .findFirst().get();

        }catch (NoSuchElementException ex){
            //Todo make log entry
            return null;
        }
    }

    @Override
    public InetSocketAddress getSocketAddress(NodeAddress addr) {
        return lookuptable.get(addr);
    }
}
