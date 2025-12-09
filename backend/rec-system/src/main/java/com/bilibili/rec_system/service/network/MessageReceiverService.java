package com.bilibili.rec_system.service.network;

import com.bilibili.rec_system.entity.Message;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MessageReceiverService {
    private static final int LISTEN_PORT = 9000;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean running = false;

    public void startService() {
        if (running) {
            System.out.println("消息接收服务已在运行中...");
            return;
        }

        try {
            serverSocket = new ServerSocket(LISTEN_PORT);
            executorService = Executors.newCachedThreadPool();
            running = true;
            System.out.println("🚀 消息接收服务已启动，监听端口: " + LISTEN_PORT);

            // 启动接受客户端连接的线程
            executorService.submit(this::acceptClients);
        } catch (IOException e) {
            System.err.println("启动消息接收服务失败: " + e.getMessage());
            stopService();
        }
    }

    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔗 新的客户端连接: " + clientSocket.getInetAddress());

                // 为每个客户端启动一个处理线程
                executorService.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("接受客户端连接时出错: " + e.getMessage());
                }
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        ObjectInputStream objectInputStream = null;
        ObjectOutputStream objectOutputStream = null;

        try {
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            // 读取消息对象
            Object receivedObject = objectInputStream.readObject();
            
            // 检查接收到的对象类型
            if (receivedObject instanceof Message) {
                Message message = (Message) receivedObject;
                System.out.println("📨 收到新消息:");
                System.out.println("   发送者ID: " + message.getSenderId());
                System.out.println("   接收者ID: " + message.getReceiverId());
                System.out.println("   消息内容: " + message.getContent());
                System.out.println("   发送时间: " + message.getSendTime());
                
                // 发送确认消息
                objectOutputStream.writeObject("MESSAGE_RECEIVED");
                objectOutputStream.flush();
            } else {
                System.err.println("接收到未知类型对象: " + receivedObject.getClass().getName());
                // 发送错误确认
                objectOutputStream.writeObject("INVALID_MESSAGE");
                objectOutputStream.flush();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("处理客户端消息时出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (objectInputStream != null) objectInputStream.close();
                if (objectOutputStream != null) objectOutputStream.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("关闭客户端连接时出错: " + e.getMessage());
            }
        }
    }

    public void stopService() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
            System.out.println("🛑 消息接收服务已停止");
        } catch (IOException e) {
            System.err.println("停止消息接收服务时出错: " + e.getMessage());
        }
    }
}