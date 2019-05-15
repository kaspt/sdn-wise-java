package com.github.sdnwiselab.sdnwise.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigAdaptationWeb extends ConfigAdaptation{

    private final Map<String, String> sockets = new HashMap<>();


    public final Map<String, String> getDefaultBacklog(){
        return Collections.unmodifiableMap(sockets);
    }

}
