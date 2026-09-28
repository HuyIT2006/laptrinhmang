package peer;

import java.io.*;
import java.net.Socket;
import common.MessageUtils;
import common.Protocol;
import java.util.regex.Pattern;

public class ChatSession extends Thread {
    private Socket socket;
    private String remoteUser;
    private ChatServer.ChatListener listener;
    private boolean isRunning;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ChatSession(Socket socket, String remoteUser, ChatServer.ChatListener listener) {
        this.socket = socket;
        this.remoteUser = remoteUser;
        this.listener = listener;
        this.isRunning = true;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                String message = MessageUtils.receiveMessage(reader);
                if (message == null) break;

                String[] parts = message.split(Pattern.quote(Protocol.SEP));
                if (parts.length >= 3 && parts[0].equals("CHAT")) {
                    String fromUser = parts[1];
                    String text = parts[2];
                    
                    if (this.remoteUser == null) {
                        this.remoteUser = fromUser;
                        listener.onChatConnectionEstablished(fromUser, socket);
                    }
                    
                    if (!text.equals("__CONNECT__")) {
                        listener.onMessageReceived(fromUser, text);
                    }
                }
            }
        } catch (Exception e) {
            if (isRunning) e.printStackTrace();
        } finally {
            close();
        }
    }

    public void sendMessage(String fromUser, String text) {
        try {
            String msg = "CHAT" + Protocol.SEP + fromUser + Protocol.SEP + text;
            MessageUtils.sendMessage(writer, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
