package peer;

import java.net.Socket;
import common.Protocol;

public class ChatClient {
    public ChatSession connect(String ip, int port, String myUsername, String remoteUser, ChatServer.ChatListener listener) {
        try {
            Socket socket = new Socket(ip, port);
            ChatSession session = new ChatSession(socket, remoteUser, listener);
            
            // Sử dụng hàm sendMessage của session (đã quản lý BufferedWriter) thay vì tạo mới
            session.sendMessage(myUsername, "__CONNECT__");
            
            session.start();
            return session;
        } catch (Exception e) {
            System.err.println("Không thể kết nối đến peer tại " + ip + ":" + port);
            e.printStackTrace();
            return null;
        }
    }
}
