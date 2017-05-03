package com.example.saber.sockettest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final  int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final  int MESSAGE_SOCKET_CONNECTED = 2;

    private Button btnSendMsg;
    private TextView tvMsg;
    private EditText etMsg;

    private PrintWriter mPrintWriter;
    private Socket mClientSocket;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_RECEIVE_NEW_MSG:
                    tvMsg.setText(tvMsg.getText()+(String)msg.obj);
                    break;
                case MESSAGE_SOCKET_CONNECTED:
                    btnSendMsg.setEnabled(true);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMsg = (TextView) findViewById(R.id.tv_msg);
        etMsg = (EditText) findViewById(R.id.et_msg);
        btnSendMsg = (Button) findViewById(R.id.btn_send);
        btnSendMsg.setOnClickListener(this);

        Intent service = new Intent(this,TCPServerService.class);
        startService(service);

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectTCPServer();
            }
        }).start();
    }

    /**
     * 连接到服务器，new Socket(本地ip地址，端口号);，创建PrintWriter输出流
     * PrinterWriter--BufferedWriter--OutputStreamWriter--getOutputStream
     */
    private void connectTCPServer() {
        Socket socket = null;
        while(socket == null) {
            try {
                socket = new Socket("192.168.0.111", 8688);//localhost为本地ip
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
                System.out.println("connect server success");
            } catch (IOException e) {
                SystemClock.sleep(1000);
                System.out.println("connect tcp server failed,retry...");
            }
        }
        //接收服务端消息（BufferedReader--InputStreamReader--getInputStream）
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //当客户端没有退出的时候，Activity没有被销毁
            while(!MainActivity.this.isFinishing()){
                String msg = br.readLine();
                System.out.println("receive:"+msg);
                if(msg != null){
                    String time = formatDateTime(System.currentTimeMillis());
                    final String showedMsg = "server"+ time + ":"+msg+"\n";
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG,showedMsg).sendToTarget();
                }
            }
            System.out.println("quit...");
            if(mPrintWriter != null){
                mPrintWriter.close();
            }
            if(br != null){
                br.close();
            }
            if(socket != null){
                socket.close();
            }

        }catch (IOException e){
            e.printStackTrace();
        }

    }


    @Override
    protected void onDestroy() {
        if(mClientSocket != null){
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            };
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_send){
            final  String msg = etMsg.getText().toString();
            if(!TextUtils.isEmpty(msg) && mPrintWriter != null){
                mPrintWriter.println(msg);
                etMsg.setText("");
                String time = formatDateTime(System.currentTimeMillis());
                final String showedMsg = "self" + time +":"+msg + "\n";
                tvMsg.setText(tvMsg.getText() + showedMsg);
            }
        }
    }

    private String formatDateTime(long l) {
        return  new SimpleDateFormat("(HH:mm:ss)").format(new Date(l));
    }


}
