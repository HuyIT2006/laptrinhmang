package common;

import java.io.*;
import java.net.Socket;
import java.util.regex.Pattern;

/**
 * Các hàm tiện ích để định dạng và xử lý tin nhắn.
 */
public class MessageUtils {
    
    public static String createMessage(String type, String... params) {
        StringBuilder sb = new StringBuilder(type);
        for (String param : params) {
            sb.append(Protocol.SEP).append(param);
        }
        return sb.toString();
    }
    
    public static String[] parseMessage(String message) {
        if (message == null) return new String[0];
        return message.split(Pattern.quote(Protocol.SEP));
    }
    
    public static void sendMessage(BufferedWriter writer, String message) throws IOException {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }
    
    public static String receiveMessage(BufferedReader reader) throws IOException {
        return reader.readLine();
    }
    
    // Thêm các hàm helper cho Socket để tương thích ngược
    public static void sendMessage(Socket socket, String message) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        sendMessage(writer, message);
    }

    public static String receiveMessage(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        return receiveMessage(reader);
    }
}
