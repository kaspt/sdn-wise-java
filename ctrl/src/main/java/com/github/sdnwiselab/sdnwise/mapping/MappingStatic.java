package com.github.sdnwiselab.sdnwise.mapping;

import com.github.sdnwiselab.sdnwise.util.NodeAddress;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingStatic extends AbstractMapping {

    private Map<NodeAddress, InetSocketAddress> lookuptable = new HashMap<>();

    public MappingStatic(String resourcefilename){
        String csvFile = "test.csv";
        String line = "";
        String cvsSplitBy = ",";
        HashMap<String, String> list = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] country = line.split(cvsSplitBy);

                //System.out.println(country[0] +"  "  + country[1]);
                list.put(country[0], country[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<InetSocketAddress> getAllAddresses() {
        return null;
    }

    @Override
    public NodeAddress getNodeAddress(InetSocketAddress addr) {
        return null;
    }

    @Override
    public InetSocketAddress getSocketAddress(NodeAddress addr) {
        return null;
    }
}
