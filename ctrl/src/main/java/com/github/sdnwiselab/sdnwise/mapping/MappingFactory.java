package com.github.sdnwiselab.sdnwise.mapping;

import com.github.sdnwiselab.sdnwise.configuration.ConfigMapping;
import com.github.sdnwiselab.sdnwise.configuration.Configurator;

public class MappingFactory {

    public static final AbstractMapping getMapping(final Configurator config){
         ConfigMapping conf =  config.getMapping();
         AbstractMapping mapping;
         String type =  conf.getResource().get("TYPE");
         switch (type){
             case "STATIC":
                 mapping = getStaticMapping(conf);
                 break;
             default:
                 throw new UnsupportedOperationException(
                         "Error in config file");
         }
        return mapping;
    }

    private static MappingStatic getStaticMapping(final ConfigMapping conf){
        String file = conf.getResource().get("FILE");
        return new MappingStatic(file);
    }

    private MappingFactory(){

    }
}
