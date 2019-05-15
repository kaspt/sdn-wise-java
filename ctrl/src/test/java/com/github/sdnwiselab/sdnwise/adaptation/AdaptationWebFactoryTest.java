package com.github.sdnwiselab.sdnwise.adaptation;

import com.github.sdnwiselab.sdnwise.adapter.AdapterTcp;
import com.github.sdnwiselab.sdnwise.adapter.AdapterWeb;
import com.github.sdnwiselab.sdnwise.configuration.Configurator;
import com.github.sdnwiselab.sdnwise.loader.SdnWise;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class AdaptationWebFactoryTest {

    @org.junit.jupiter.api.Test
    void getAdaptationWeb(){
        InputStream is = null;

        is = SdnWise.class.getResourceAsStream("/configadaptaionTest.json");
        Configurator conf = Configurator.load(is);

        AdaptationWeb adaptation =
                AdaptationWebFactory.getAdaptationWeb(conf);

        assertEquals(1, adaptation.getLower().size());
        assertEquals(1, adaptation.getUpper().size());
        assertEquals(AdapterWeb.class, adaptation.getLower().get(0).getClass());
        assertEquals(AdapterTcp.class, adaptation.getUpper().get(0).getClass());


    }

}