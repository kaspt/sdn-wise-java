package com.github.sdnwiselab.sdnwise.adapter;

import com.github.sdnwiselab.sdnwise.packet.InetAdapterPacket;

import java.io.*;
import java.net.Socket;

public class SocketTestAdapter {

    private final String ip;

    private final int port;

    private Socket socket;

    public SocketTestAdapter(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public void openSocket() throws IOException{
        socket = new Socket(ip, port);
    }

    public void send(byte[] data) throws IOException{
        OutputStream out = socket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        dos.write(data);
    }

    public byte[] receiveInetPacket() throws IOException{
        InputStream din = socket.getInputStream();
        DataInputStream dis = new DataInputStream(din);
        int net = Byte.toUnsignedInt(dis.readByte());
        int len = Byte.toUnsignedInt(dis.readByte());

        byte[] data = new byte[len];
        if(len >0){
            data[0] = (byte)net;
            data[1] = (byte)len;
            dis.readFully(data, 2, len-2);
        }
        return data;
    }


}
