package com.github.sdnwiselab.sdnwise.adaptation;

import com.github.sdnwiselab.sdnwise.adapter.AbstractAdapter;
import com.github.sdnwiselab.sdnwise.controlplane.ControlPlaneLayer;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;

public class AdaptationWeb extends ControlPlaneLayer {

    /**
     * Creates an adaptation object given two adapters.
     *
     * @param lower the adapter that receives messages from the internet
     * @param upper the adapter that receives messages from the forwarding layer
     */
    public AdaptationWeb(List<AbstractAdapter> lower,
                         List<AbstractAdapter> upper) {
        super("ADAWEB", lower, upper);
    }


    private byte[] createSocketMessage(Object arg){
        //Todo separate node response and the socket ID.
        return (byte[])arg;
    }

    private byte[] createUpperMessage(Object arg){
        //Todo Add Socket ID to the message.
        return (byte[])arg;
    }

    @Override
    protected void setupLayer() {
        //TODO Open the sockets.
    }

    @Override
    public void update(Observable o, Object arg) {
        boolean found = false;
        // Send message to lower tcp adapter.
        for (AbstractAdapter adapter : getUpper()) {
            if (o.equals(adapter)) {
                log(Level.INFO, "\u2193" + Arrays.toString((byte[]) arg));
                byte[] message = createSocketMessage(arg);
                for (AbstractAdapter ad : getLower()) {
                    //Todo Find correct lower Adapter/Socket
                    found = true;
                    ad.send(message);
                }
                break;
            }
        }
        // Send message to upper layer
        if (!found) {
            for (AbstractAdapter adapter : getLower()) {
                if (o.equals(adapter)) {
                    log(Level.INFO, "\u2191" + Arrays.toString((byte[]) arg));
                    // Todo identify socket and forward the info
                    byte[] message = createUpperMessage(arg);
                    for (AbstractAdapter ad : getUpper()) {
                        ad.send(message);
                    }
                    break;
                }
            }
        }


    }
}
