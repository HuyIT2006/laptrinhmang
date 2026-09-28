package tracker;

import common.FileInfo;
import common.MessageUtils;
import common.PeerInfo;
import common.Protocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.regex.Pattern;

/**
 * Luồng xử lý cho từng peer (client) kết nối đến Tracker.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private TrackerServer server;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public ClientHandler(Socket socket, TrackerServer server) {
        this.socket = socket;
        this.server = server;
        try {
            // Sử dụng mã hóa UTF-8 để hỗ trợ tiếng Việt
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        } catch (IOException e) {
            closeEverything();
        }
    }

    @Override
    public void run() {
        try {
            // Vòng lặp nhận và xử lý tin nhắn từ client
            while (socket.isConnected()) {
                String message = MessageUtils.receiveMessage(bufferedReader);
                if (message == null) {
                    break;
                }

                // Cắt thông điệp theo SEPARATOR
                String[] parts = message.split(Pattern.quote(Protocol.SEPARATOR));
                String command = parts[0];

                switch (command) {
                    case Protocol.REGISTER:
                        // REGISTER|username|chatPort|filePort
                        username = parts[1];
                        int chatPort = Integer.parseInt(parts[2]);
                        int filePort = Integer.parseInt(parts[3]);
                        String ip = socket.getInetAddress().getHostAddress();
                        
                        PeerInfo peerInfo = new PeerInfo(ip, chatPort, filePort);
                        server.registerPeer(username, peerInfo, this);
                        break;
                        
                    case Protocol.SHARE_FILE:
                        // SHARE_FILE|filename|fileSize
                        String filename = parts[1];
                        long fileSize = Long.parseLong(parts[2]);
                        
                        FileInfo fileInfo = new FileInfo(filename, fileSize, username);
                        server.addSharedFile(username, fileInfo);
                        
                        // Phản hồi SHARE_FILE_OK
                        sendMessage(Protocol.SHARE_FILE_OK + Protocol.SEPARATOR + filename);
                        break;
                        
                    case Protocol.SEARCH_FILE:
                        // SEARCH_FILE|keyword
                        String keyword = parts[1];
                        String searchResult = server.searchFiles(keyword);
                        sendMessage(searchResult);
                        break;
                        
                    case Protocol.UNREGISTER:
                        // UNREGISTER (do client chủ động gọi)
                        closeEverything();
                        break;
                        
                    default:
                        System.out.println("Unknown command received: " + command);
                }
            }
        } catch (Exception e) {
            System.out.println("Connection error for " + (username != null ? username : "unknown client"));
        } finally {
            closeEverything();
        }
    }

    /**
     * Gửi tin nhắn đến peer thông qua MessageUtils.
     */
    public void sendMessage(String msg) {
        try {
            MessageUtils.sendMessage(bufferedWriter, msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Đóng tất cả kết nối và xóa thông tin của peer này khỏi server.
     */
    private void closeEverything() {
        if (username != null) {
            server.unregisterPeer(username);
            username = null; // Tránh gọi nhiều lần
        }
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
