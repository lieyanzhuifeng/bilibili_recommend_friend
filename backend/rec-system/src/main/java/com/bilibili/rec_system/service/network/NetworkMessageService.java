package com.bilibili.rec_system.service.network;

import com.bilibili.rec_system.entity.Message;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;

/**
 * 网络消息发送服务类
 * 负责通过网络包直接发送消息给接收方
 */
@Service
public class NetworkMessageService {

    private static final int SOCKET_TIMEOUT = 5000; // 5秒超时
    
    /**
     * 通过TCP网络包发送消息
     * @param message 消息对象
     * @param receiverAddress 接收方地址
     * @param receiverPort 接收方端口
     * @return 是否发送成功
     */
    public boolean sendMessageOverNetwork(Message message, String receiverAddress, int receiverPort) {
        Socket socket = null;
        ObjectOutputStream outputStream = null;
        
        try {
            // 创建Socket连接
            socket = new Socket();
            socket.connect(new InetSocketAddress(receiverAddress, receiverPort), SOCKET_TIMEOUT);
            
            // 设置读取超时
            socket.setSoTimeout(SOCKET_TIMEOUT);
            
            // 创建对象输出流
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            
            // 发送消息对象
            outputStream.writeObject(message);
            outputStream.flush();
            
            // 等待接收方确认
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            String response = (String) inputStream.readObject();
            
            // 检查是否收到正确的确认信息
            return "MESSAGE_RECEIVED".equals(response);
        } catch (Exception e) {
            System.err.println("通过TCP网络发送消息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // 关闭资源
            closeResource(outputStream);
            closeResource(socket);
        }
    }
    
    /**
     * 通过UDP网络包发送消息
     * @param message 消息对象
     * @param receiverAddress 接收方地址
     * @param receiverPort 接收方端口
     * @return 是否发送成功
     */
    public boolean sendMessageOverUDP(Message message, String receiverAddress, int receiverPort) {
        DatagramSocket socket = null;
        try {
            // 创建UDP Socket
            socket = new DatagramSocket();
            
            // 序列化消息对象
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(message);
            objectStream.flush();
            
            byte[] data = byteStream.toByteArray();
            
            // 创建数据报
            InetAddress address = InetAddress.getByName(receiverAddress);
            DatagramPacket packet = new DatagramPacket(data, data.length, address, receiverPort);
            
            // 发送数据报
            socket.send(packet);
            
            // UDP是无连接的，不保证送达，但我们假设发送成功
            return true;
        } catch (Exception e) {
            System.err.println("通过UDP网络发送消息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResource(socket);
        }
    }
    
    /**
     * 安全关闭资源
     * @param resource 可关闭的资源
     */
    private void closeResource(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                System.err.println("关闭资源时发生错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 安全关闭Socket资源
     * @param socket Socket资源
     */
    private void closeResource(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("关闭Socket时发生错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 安全关闭DatagramSocket资源
     * @param socket DatagramSocket资源
     */
    private void closeResource(DatagramSocket socket) {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}