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
package com.github.sdnwiselab.sdnwise.mapping;

import com.github.sdnwiselab.sdnwise.configuration.ConfigMapping;
import com.github.sdnwiselab.sdnwise.configuration.Configurator;
import com.github.sdnwiselab.sdnwise.loader.SdnWise;

public class MappingFactory {

    private static AbstractMapping instance = null;

    public static final AbstractMapping getMapping(final Configurator config){
        if(instance != null){
            return instance;
        }else {
            ConfigMapping conf =  config.getMapping();
            String type =  conf.getResource().get("TYPE");
            if(type == null){
                throw new UnsupportedOperationException(
                        "Error in config file");
            }
            switch (type){
                case "STATIC":
                    instance = getStaticMapping(conf);
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Error in config file");
            }
            return instance;
        }
    }

    private static MappingStatic getStaticMapping(final ConfigMapping conf){
        String file = conf.getResource().get("FILE");
        String filepath = SdnWise.class.getResource(file).getPath();

        return  new MappingStatic(filepath);

    }

    private MappingFactory(){

    }
}
