package peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer extends Thread {
    
    public interface ChatListener {
        void onMessageReceived(String fromUser, String message);
        void onChatConnectionEstablished(String fromUser, Socket socket);
    }
    
    private int port;
    private ChatListener listener;
    private ServerSocket serverSocket;
    private boolean isRunning;

    public ChatServer(int port, ChatListener listener) {
        this.port = port;
        this.listener = listener;
        this.isRunning = false;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("ChatServer đang lắng nghe trên cổng " + port);
            
            while (isRunning) {
                Socket socket = serverSocket.accept();
                // Khởi tạo ChatSession khi có kết nối mới từ peer khác
                ChatSession session = new ChatSession(socket, null, listener);
                session.start();
            }
        } catch (IOException e) {
            if (isRunning) {
                e.printStackTrace();
            }
        }
    }
    
    public void stopServer() {
        isRunning = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
