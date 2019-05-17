package com.github.sdnwiselab.sdnwise.forwarding;

import com.github.sdnwiselab.sdnwise.configuration.Configurator;
import com.github.sdnwiselab.sdnwise.loader.SdnWise;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForwardingTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFactory() {
        InputStream is = null;
        is = SdnWise.class.getResourceAsStream(
                "/configTestFile.json");
        Configurator conf = Configurator.load(is);

        Forwarding fwd = ForwardingFactory.getForwarding(conf);

        assertEquals(1, fwd.getUpper().size());
        assertEquals(2, fwd.getLower().size());

    }

    @Test
    void update() {
    }
}