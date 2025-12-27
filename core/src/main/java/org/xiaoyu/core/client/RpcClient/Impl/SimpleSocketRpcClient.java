package org.xiaoyu.core.client.RpcClient.Impl;

import org.xiaoyu.common.message.RpcRequest;
import org.xiaoyu.common.message.RpcResponse;
import org.xiaoyu.core.client.RpcClient.RpcClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SimpleSocketRpcClient implements RpcClient {
    private String host; // 主机地址
    private int port;

    public SimpleSocketRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        try {
            Socket socket = new Socket(host, port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(request);
            oos.flush();

            return (RpcResponse) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() {

    }
}
