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
