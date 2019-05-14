package com.github.sdnwiselab.sdnwise.adapter;

import com.github.sdnwiselab.sdnwise.packet.InetAdapterPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;

import java.io.*;
import java.net.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Level;

public class AdapterWeb extends AbstractAdapter{
    /**
     * Destination ip address. For when the adapter acts as client.
     */
    private final String ip;

    private final byte[] ip_bytearr;

    private final byte ipVersion;

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
     * </ol>
     *
     * @param conf contains the serial port configuration data.
     */
    public AdapterWeb(final Map<String, String> conf)
            throws UnknownHostException {
        isServer = Boolean.parseBoolean(conf.get("IS_SERVER"));
        ip = conf.get("IP");
        port = Integer.parseInt(conf.get("PORT"));
        backlog = 5;
        ip_bytearr = InetAddress.getByName(ip).getAddress();
        if (InetAddress.getByName(ip) instanceof Inet4Address){
            ipVersion = (byte) 4;
        }else if (InetAddress.getByName(ip) instanceof Inet6Address){
            ipVersion = (byte) 6;
        }else {
            throw new IllegalArgumentException("is not a ip address "
                    + ip );
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
        super.update(o, arg);
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
                        dos.write(data);
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

            @Override
            public void run() {
                try {
                    InputStream in = clientSocket.getInputStream();
                    DataInputStream dis = new DataInputStream(in);
                    while (!isStopped()) {

                        int len = Byte.toUnsignedInt(dis.readByte());
                        byte[] payload;
                        if(len > 0) {
                            payload = new byte[len];
                            dis.readFully(payload, 0, len );
                        }else {
                            throw new IllegalArgumentException(
                                    "received packet has illegal length");
                        }

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
                } catch (IOException ex) {
                    log(Level.SEVERE, ex.toString());
                }
            }
        }
    }
}
