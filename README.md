# SocketTest

1.Socket分为流式套接字和用户数据报套接字，分别对应于网络的传输控制中的TCP和UDP协议。
2.TCP协议是面向连接的协议，提供稳定的双向通信功能，TCP连接的建立需要经过“三次握手”。
3.UDP协议是无连接的，具有更好的效率，但不能保证数据一定能够正确传输，尤其是在网络拥塞的情况下。


4.服务端：
*1.创建ServerSocket，连接到端口
* 2.创建一个循环，不停接收客户端的连接请求：socket = serverSocket.accept
* 3.如果多个客户端同时访问的话：//把socket对象存入集合：sockets.add(socket);
* 4.//启动子线程  让子线程处理后续业务new WorkThread(socket).start();
     
5.客户端：
*1.连接到服务器，new Socket(本地ip地址，端口号);创建PrintWriter输出流PrinterWriter--BufferedWriter--OutputStreamWriter--getOutputStream
*2.接收服务端消息（BufferedReader--InputStreamReader--getInputStream）

6.前提是设备的IP地址互相可见。
