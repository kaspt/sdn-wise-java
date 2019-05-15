package com.github.sdnwiselab.sdnwise.adaptation;

import com.github.sdnwiselab.sdnwise.adapter.*;
import com.github.sdnwiselab.sdnwise.configuration.ConfigAdaptationWeb;
import com.github.sdnwiselab.sdnwise.configuration.Configurator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AdaptationWebFactory {
    /**
     * Contains the configuration parameters of the class.
     */
    private static ConfigAdaptationWeb conf;

    /**
     * Returns an adaptation object given a configAdaptation object. If one of
     * the adapter cannot be instantiated then this method throws an
     * UnsupportedOperationException.
     *
     * @param c contains the configurations for the adaptation object
     * @return an adaptation object
     */
    public static AdaptationWeb getAdaptationWeb(final Configurator c) {
        conf = c.getAdaptationweb();
        List<AbstractAdapter> lower = getAdapters(conf.getLowers());
        List<AbstractAdapter> upper = getAdapters(conf.getUppers());
        int backlog = Integer.parseInt(conf.getDefaultBacklog().get("BACKLOG"));
        return new AdaptationWeb(lower, upper, backlog);
    }

    /**
     * Returns an adapter depending on the options specified. The supported
     * types at the moment are "UDP/TCP" for udp/tcp communication and "COM" for
     * serial port communication. "COOJA" adapter is still under development.
     * Details regarding the adapters are contained in the c map.
     *
     * @param c the type of adapter that will be instantiated.
     * @return an adapter object
     */
    private static AbstractAdapter getAdapter(final Map<String, String> c) {
        switch (c.get("TYPE")) {
            case "UDP":
                return new AdapterUdp(c);
            case "TCP":
                return new AdapterTcp(c);
            case "INET":
                return new AdapterWeb(c);
            default:
                throw new UnsupportedOperationException(
                        "Error in configuration file: "
                                + "Unsupported Adapter of type "
                                + c.get("TYPE"));
        }
    }

    /**
     * Returns a list of adapters depending on the options specified.
     *
     * @param c a list of maps containing the parameters for each of the adapter
     * @return a list of Abstract Adapters
     */
    private static List<AbstractAdapter> getAdapters(
            final List<Map<String, String>> c) {
        List listAdapters = new LinkedList<>();
        c.stream().forEach((map) -> {
            listAdapters.add(getAdapter(map));
        });
        return listAdapters;
    }

    /**
     * Private constructor.
     */
    private AdaptationWebFactory() {
        // Nothing to do here
    }
}
