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
package com.github.sdnwiselab.sdnwise.adapter;

import com.github.sdnwiselab.sdnwise.controlplane.ControlPlaneLogger;
import com.github.sdnwiselab.sdnwise.packet.InetAdapterPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;

import java.io.*;
import java.net.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.MAX_PACKET_LENGTH;

public class AdapterWeb extends AbstractAdapter{
    /**
     * Destination ip address. For when the adapter acts as client.
     */
    private final String ip;

    private byte[] ip_bytearr;

    private byte ipVersion;

    /**
     * Boolean used to set the behaviour of the adapter. The adapter can act as
     * a server or a client.
     */
    private final boolean isServer;
    /**
     * TCP port of the adapter.
     */
    private final int port;

    /**
     * Backlog number of the ServerSocket.
     */
    private final int backlog;

    /**
     * Manages TCP connections.
     */
    private InternalTcpElement tcpElement;

    /**
     * Creates an AdapterTCP object. The conf map is used to pass the
     * configuration settings for the TPC socket as strings. Specifically the
     * needed parameters are:
     * <ol>
     * <li>IS_SERVER</li>
     * <li>IP</li>
     * <li>PORT</li>
     * <li>BACKLOG</li>
     * </ol>
     *
     * @param conf contains the serial port configuration data.
     */
    public AdapterWeb(final Map<String, String> conf) {
        isServer = Boolean.parseBoolean(conf.get("IS_SERVER"));
        ip = conf.get("IP");
        port = Integer.parseInt(conf.get("PORT"));
        backlog = Integer.parseInt(conf.get("BACKLOG"));
        adapterIdentifier = conf.get("ADAPTER_ID");
        try {
            ip_bytearr = InetAddress.getByName(ip).getAddress();
            if (InetAddress.getByName(ip) instanceof Inet4Address){
                ipVersion = (byte) 4;
            }else if (InetAddress.getByName(ip) instanceof Inet6Address){
                ipVersion = (byte) 6;
            }else {
                throw new IllegalArgumentException("is not a ip address "
                        + ip );
            }
        }catch (IOException ex){
            log(Level.SEVERE, ex.toString());
        }
    }

    public AdapterWeb(InetSocketAddress address,
                      int backlog,
                      boolean isServer){
        this.isServer = isServer;
        this.backlog = backlog;
        port = address.getPort();

        ip = address.getAddress().getHostAddress();
        this.adapterIdentifier = address.toString();
        //log(Level.INFO, this.adapterIdentifier);
        ip_bytearr = address.getAddress().getAddress();
        try {
            ip_bytearr = InetAddress.getByName(ip).getAddress();
            if (InetAddress.getByName(ip) instanceof Inet4Address){
                ipVersion = (byte) 4;
            }else if (InetAddress.getByName(ip) instanceof Inet6Address){
                ipVersion = (byte) 6;
            }else {
                throw new IllegalArgumentException("is not a ip address "
                        + ip );
            }
        }catch (IOException ex){
            log(Level.SEVERE, ex.toString());
        }
    }


    @Override
    public final boolean close() {
        tcpElement.stop();
        setActive(false);
        return true;
    }

    @Override
    public final boolean open() {
        if (!isActive()) {
            if (isServer) {
                tcpElement = new InternalTcpElementServer();
            } else {
                tcpElement = new InternalTcpElementClient();
            }
            tcpElement.addObserver(this);
            new Thread(tcpElement).start();
            setActive(true);
        }
        return true;
    }

    @Override
    public final void send(final byte[] data) {
        if (isActive()) {
            tcpElement.send(data);
        }
    }

    @Override
    public void update(final Observable o, final Object arg) {
        InetAdapterPacket packet = ((InetAdapterPacket)arg)
                .setSdnWiseIPVersion(this.ipVersion)
                .setSdnWisePort(this.port)
                .setSdnWiseAddress(this.ip_bytearr);
        super.update(o, packet.toByteArray());
    }

    public boolean identifyAddapter(InetAdapterPacket packet){
        if(packet.getSdnWisePort() == this.port){
            if(Arrays.equals(packet.getSdnWiseAddress(),
                    this.ip_bytearr)){
                return true;
            }
        }
        return false;

    }

    /**
     * Models a generic TCP network element.
     */
    private abstract class InternalTcpElement extends Observable
            implements Runnable, Observer {

        /**
         * Manages the status of the element.
         */
        private boolean isStopped;

        /**
         * Sends a byte array.
         *
         * @param data the array to be sent
         */
        public abstract void send(final byte[] data);

        /**
         * Checks if the TCP element is still running.
         *
         * @return the status of the TCP element
         */
        public synchronized boolean isStopped() {
            return isStopped;
        }

        @Override
        public final void update(final Observable o, final Object arg) {
            System.out.println(arg);
            setChanged();
            notifyObservers(arg);
        }

        /**
         * Stops the TCP element.
         */
        public void stop() {
            isStopped = true;
        }
    }

    /**
     * Models a TCP client.
     */
    private class InternalTcpElementClient extends InternalTcpElement {

        /**
         * TCP Socket.
         */
        private Socket socket;

        @Override
        public void send(final byte[] data) {
            try {
                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                dos.write(data);
            } catch (IOException ex) {
                log(Level.SEVERE, ex.toString());
            }
        }

        @Override
        public void run() {
            try {
                socket = new Socket(ip, port);
                InputStream in = socket.getInputStream();
                DataInputStream dis = new DataInputStream(in);
                while (!isStopped()) {
                    byte[] data = new NetworkPacket(dis).toByteArray();
                    setChanged();
                    notifyObservers(data);
                }
            } catch (IOException ex) {
                log(Level.SEVERE, ex.toString());
            }

        }
    }

    /**
     * Models a TCP server.
     */
    private class InternalTcpElementServer extends InternalTcpElement {

        /**
         * TCP server socket.
         */
        private ServerSocket serverSocket = null;

        /**
         * A list of client sockets.
         */
        private final List<Socket> clientSockets = new LinkedList<>();

        /**
         * A list of removable client sockets.
         */
        private final List<Socket> removableSockets = new LinkedList<>();

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port,
                        backlog,
                        InetAddress.getByName(ip));
            } catch (IOException e) {
                throw new UnsupportedOperationException("Cannot open port "
                        + port, e);
            }
            Socket clientSocket;
            while (!isStopped()) {
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    if (isStopped()) {
                        return;
                    }
                    throw new UnsupportedOperationException(
                            "Error accepting client connection", e);
                }
                clientSockets.add(clientSocket);
                InternalTcpElementServer.WorkerRunnable wr =
                        new InternalTcpElementServer.WorkerRunnable(clientSocket);
                wr.addObserver(this);
                new Thread(wr).start();
            }
        }

        @Override
        public synchronized void stop() {
            super.stop();
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new UnsupportedOperationException(
                        "Error closing server", e);
            }
        }

        @Override
        public void send(final byte[] data) {
            // Todo Find first, don't iterate over the hole list
            log(Level.INFO,
                    "send to web open client sockets:("
                            + clientSockets.size()+
                            ")");
            clientSockets.stream().forEach((sck) -> {
                InetAdapterPacket packet = new InetAdapterPacket(data);
                InetSocketAddress remoteaddress =
                        (InetSocketAddress)sck.getRemoteSocketAddress();
                try {
                    if(Arrays.equals(packet.getClientAddress(),
                            remoteaddress.getAddress().getAddress())
                            && (packet.getClientPort()== remoteaddress.getPort())){
                        OutputStream out = sck.getOutputStream();
                        DataOutputStream dos = new DataOutputStream(out);

                        byte[] payload = packet.getPayload();
                        byte[] response = new byte[payload.length +1];
                        response[0] = (byte)payload.length;
                        System.arraycopy(payload, 0, response, 1, payload.length);
                        ControlPlaneLogger.LogTimeStamp("web_out"
                                + Arrays.toString(payload));
                        dos.write(response);

                    }
                } catch (IOException ex) {
                    log(Level.SEVERE, ex.toString());
                    removableSockets.add(sck);
                }
            });

            if (!removableSockets.isEmpty()) {
                clientSockets.removeAll(removableSockets);
                removableSockets.clear();
            }
        }

        /**
         * Notifies the observes that a new packet has arrived.
         */
        private class WorkerRunnable extends Observable implements Runnable {

            /**
             * Reference to the clientSocket.
             */
            private final Socket clientSocket;

            /**
             * Creates a new WorkerRunnable given a clientSocket.
             *
             * @param socket the clientSocket
             */
            WorkerRunnable(final Socket socket) {
                clientSocket = socket;
            }

            private byte[] getPayload(DataInputStream dis) throws IOException{

                int len = Byte.toUnsignedInt(dis.readByte());
                if(len > 0) {
                    byte[] payload = new byte[len];
                    dis.readFully(payload, 0, len);
                    ControlPlaneLogger.LogTimeStamp("web_in"
                            + Arrays.toString(payload));
                    return payload;
                }else {
                    throw new IllegalArgumentException(
                            "received packet has illegal length");
                }
            }

            @Override
            public void run() {
                try {
                    InputStream in = clientSocket.getInputStream();
                    DataInputStream dis = new DataInputStream(in);
                    while (!isStopped()) {
                        byte[] payload = getPayload(dis);

                        InetSocketAddress cliadr =
                                (InetSocketAddress)clientSocket.getRemoteSocketAddress();
                        byte clientIpvers;
                        if (cliadr.getAddress() instanceof Inet4Address){
                            clientIpvers = (byte) 4;
                        }else if(cliadr.getAddress() instanceof Inet6Address){
                            clientIpvers = (byte)6;
                        }else {
                            throw new InvalidParameterException("unknown typ of client address");
                        }

                        InetAdapterPacket data = new InetAdapterPacket(payload,
                                (byte) 64)
                                .setClientAddress(cliadr.getAddress().getAddress())
                                .setClientPort(cliadr.getPort())
                                .setClientIPVersion(clientIpvers);
                        setChanged();
                        notifyObservers(data);
                    }
                } catch (IOException ex) {}
                finally {
                    try {
                        clientSocket.shutdownOutput();
                        clientSocket.shutdownInput();
                        clientSocket.close();
                        log(Level.INFO, "client is closed");
                    }catch (IOException ioex){
                        log(Level.INFO, "error closing client socket");
                    }
                }
            }
        }
    }
}
