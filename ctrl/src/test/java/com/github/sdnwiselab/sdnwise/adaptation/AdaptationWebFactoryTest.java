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
package com.github.sdnwiselab.sdnwise.adaptation;

import com.github.sdnwiselab.sdnwise.adapter.AdapterTcp;
import com.github.sdnwiselab.sdnwise.adapter.AdapterWeb;
import com.github.sdnwiselab.sdnwise.configuration.Configurator;
import com.github.sdnwiselab.sdnwise.loader.SdnWise;

import java.io.InputStream;


import static org.junit.jupiter.api.Assertions.*;

public class AdaptationWebFactoryTest {

    @org.junit.jupiter.api.Test
    void getAdaptationWeb(){
        InputStream is = null;

        is = SdnWise.class.getResourceAsStream("/configTestFile.json");
        Configurator conf = Configurator.load(is);


        AdaptationWeb adaptation =
                AdaptationWebFactory.getAdaptationWeb(conf);

        assertEquals(1, adaptation.getLower().size());
        assertEquals(1, adaptation.getUpper().size());
        assertEquals(AdapterWeb.class, adaptation.getLower().get(0).getClass());
        assertEquals(AdapterTcp.class, adaptation.getUpper().get(0).getClass());


    }

}