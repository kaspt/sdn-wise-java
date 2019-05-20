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
package com.github.sdnwiselab.sdnwise.forwarding;

import com.github.sdnwiselab.sdnwise.adapter.AbstractAdapter;
import com.github.sdnwiselab.sdnwise.adapter.AdapterCom;
import com.github.sdnwiselab.sdnwise.adapter.AdapterCooja;
import com.github.sdnwiselab.sdnwise.adapter.AdapterTcp;
import com.github.sdnwiselab.sdnwise.controlplane.ControlPlaneLayer;
import com.github.sdnwiselab.sdnwise.controlplane.ControlPlaneLogger;
import com.github.sdnwiselab.sdnwise.mapping.AbstractMapping;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;

/**
 *
 *
 * @author Tobias Kasper
 */
public class Forwarding extends ControlPlaneLayer {

    private final AbstractMapping mapping;

    /**
     * Creates an adaptation object given two adapters.
     *
     * @param lower the adapter that receives messages from the sensor network
     * @param upper the adapter that receives messages from the controller
     */
    public Forwarding(final List<AbstractAdapter> lower,
                      final List<AbstractAdapter> upper,
                      AbstractMapping mapping) {
        super("FWD", lower, upper);
        this.mapping = mapping;
        ControlPlaneLogger.setupLogger(getLayerShortName());
    }

    @Override
    protected void setupLayer() {
        //Notthing to do here
    }

    @Override
    public void update(Observable o, Object arg) {
        byte[] packet = (byte[])arg;
        if(NetworkPacket.isSdnWise(packet)){
            for(AbstractAdapter upper: getUpper()){
                if(o.equals(upper)){
                    managePacketfromController(packet);
                    return;
                }
            }
            NetworkPacket networkPacket = new NetworkPacket(packet);
            managePacket_from_SDNWISE(networkPacket);
        }else {
            managePacket_fromWeb(packet);
        }
    }

    private void managePacketfromController(byte[] data){
        log(Level.INFO, "\u2193" + "C to N" + Arrays.toString(data));
        getNodeAdapter().send(data);
    }

    private void managePacket_from_SDNWISE(NetworkPacket data){
        if(data.getTyp() > 0){
            log(Level.INFO, "sdn to contr" + Arrays.toString(data.toByteArray()));
            for(AbstractAdapter upper: getUpper()){
                upper.send(data.toByteArray());
            }
        }else {
            log(Level.INFO, "sdn to web" + Arrays.toString(data.toByteArray()));
            // Todo create Inet Adapterpacket
            // Todo find correct adapter.
            // send to web
        }
    }

    private void managePacket_fromWeb(byte[] packet){
        log(Level.INFO, "web to sdn" + Arrays.toString(packet));
        // Todo create sdnwise data packet, save infos, send to node adapter.
    }

    private AbstractAdapter getNodeAdapter(){
        for(AbstractAdapter lower: getLower()){
            if(lower instanceof AdapterCooja
                    || lower instanceof AdapterCom){
                return lower;
            }
        }
        throw new UnsupportedOperationException("Node adapter is not found");
    }

    private AbstractAdapter getWebAdapter(){
        for(AbstractAdapter lower: getLower()){
            if(lower instanceof AdapterTcp){
                return lower;
            }
        }
        throw new UnsupportedOperationException("Web adapter is not found");
    }

}
