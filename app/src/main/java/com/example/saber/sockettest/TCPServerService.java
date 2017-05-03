package com.example.saber.sockettest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class TCPServerService extends Service {

    private boolean mIsServiceDestoryed = false;
    private String[] mDefinedMessages = new String[]{
            "你好啊,哈哈","请问你叫什么名字?","今天北京天气不错啊.","你知道么，我是可以和多个人同时聊天的哦。","给你讲个笑话。"
    };

    public TCPServerService() {
    }

    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestoryed = true;
        super.onDestroy();
    }


    /**
     * 服务端要做的事：
     * 1.创建ServerSocket，连接到端口
     * 2.创建一个循环，不停接收客户端的连接请求：socket = serverSocket.accept
     * 3.如果多个客户端同时访问的话：//把socket对象存入集合：sockets.add(socket);
     * 4.//启动子线程  让子线程处理后续业务new WorkThread(socket).start();
     */
    private class TcpServer implements Runnable{

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            //监听本地8688端口
            try {
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                System.err.println("establish tcp server failed,port:8688");
                e.printStackTrace();
                return;
            }

            while(!mIsServiceDestoryed){

                try {
                    //接收客户端请求
                    final Socket client = serverSocket.accept();
                    System.out.println("accept");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 子线程中处理的后续业务
     * 回复客户端
     * @param client
     */
    private void responseClient(Socket client) throws IOException {
        //用于接收客户端消息
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //用于向客户端发送消息
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())),true);
        out.println("欢迎来到聊天室");
        while(!mIsServiceDestoryed){
            String str = in.readLine();
            System.out.println("msg from client:"+str);
            if(str == null){
                //客户端断开连接
                break;
            }
            int i = new Random().nextInt(mDefinedMessages.length);
            String msg = mDefinedMessages[i];
            out.println(msg);
            System.out.println("send:"+msg);
            System.out.println("client quit.");
            out.close();
            in.close();
            client.close();
        }


    }

}
