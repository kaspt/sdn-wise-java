package com.github.sdnwiselab.sdnwise.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigMapping {

    private final Map<String, String> resource = new HashMap<>();

    public final Map<String, String> getResource() {
        return Collections.unmodifiableMap(resource);
    }

}
