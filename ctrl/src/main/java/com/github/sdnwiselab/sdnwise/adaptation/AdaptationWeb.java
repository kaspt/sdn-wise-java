package com.github.sdnwiselab.sdnwise.adaptation;

import com.github.sdnwiselab.sdnwise.adapter.AbstractAdapter;
import com.github.sdnwiselab.sdnwise.adapter.AdapterWeb;
import com.github.sdnwiselab.sdnwise.controlplane.ControlPlaneLayer;
import com.github.sdnwiselab.sdnwise.controlplane.ControlPlaneLogger;
import com.github.sdnwiselab.sdnwise.mapping.AbstractMapping;
import com.github.sdnwiselab.sdnwise.packet.InetAdapterPacket;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdaptationWeb extends ControlPlaneLayer {

    protected static final Logger LOGGER = Logger.getLogger("ADAWEB");

    /**
     * Creates an adaptation object given two adapters.
     *
     * @param lower the adapter that receives messages from the internet
     * @param upper the adapter that receives messages from the forwarding layer
     */
    public AdaptationWeb(List<AbstractAdapter> lower,
                         List<AbstractAdapter> upper,
                         int backlog,
                         AbstractMapping mapping) {
        super("WEBADA", lower, upper);
        ControlPlaneLogger.setupLogger(getLayerShortName());
        this.defaultBacklog = backlog;
        this.mapping = mapping;
    }

    private final int defaultBacklog;

    private final AbstractMapping mapping;


    @Override
    protected void setupLayer() {
        AtomicInteger aBacklog = new AtomicInteger(this.defaultBacklog);
        mapping.getAllAddresses().forEach((address -> {
            AbstractAdapter adapter = new AdapterWeb(address,
                    aBacklog.get() ,
                    true);
            getLower().add(adapter);
            if(adapter.open()){
                adapter.addObserver(this);
            }
        }));
    }

    @Override
    public void update(Observable o, Object arg) {
        boolean found = false;
        // Send message to lower tcp adapter.
        for (AbstractAdapter upperAdapter : getUpper()) {
            if (o.equals(upperAdapter)) {
                log(Level.INFO, "\u2193" + Arrays.toString((byte[]) arg));
                InetAdapterPacket message = new InetAdapterPacket((byte[]) arg);

                for (AbstractAdapter lowerAdapter : getLower()) {
                    AdapterWeb adweb =  (AdapterWeb) lowerAdapter;
                    String adaid = lowerAdapter.getAdapterIdentifier();
                    // TODO identify adapter with identifier

                    if(adweb.identifyAddapter(message)){
                        found = true;
                        lowerAdapter.send(message.toByteArray());
                        break;
                    }
                }
                break;
            }
        }
        // Send message to upper layer (FWD)
        if (!found) {
            for (AbstractAdapter adapter : getLower()) {
                if (o.equals(adapter)) {
                    log(Level.INFO, "\u2191" + Arrays.toString((byte[]) arg));
                    for (AbstractAdapter ad : getUpper()) {
                        ad.send((byte[]) arg);
                    }
                    break;
                }
            }
        }

    }
}
