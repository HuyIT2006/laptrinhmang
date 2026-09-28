package peer;

import java.io.*;
import java.net.Socket;
import java.util.regex.Pattern;
import common.Protocol;

public class FileClient {
    public void downloadFile(String ip, int port, String filename, long expectedSize, String saveFolderPath, FileServer.FileTransferListener listener) {
        try (Socket socket = new Socket(ip, port);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            
            dos.writeUTF("FILE_REQUEST" + Protocol.SEP + filename);
            
            String response = dis.readUTF();
            
            String[] parts = response.split(Pattern.quote(Protocol.SEP));
            if (parts[0].equals("FILE_ACCEPT") && parts.length >= 3) {
                long fileSize = Long.parseLong(parts[2]);
                
                File saveDir = new File(saveFolderPath);
                if (!saveDir.exists()) {
                    saveDir.mkdirs();
                }
                
                File saveFile = new File(saveFolderPath, filename);
                try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalReceived = 0;
                    
                    while (totalReceived < fileSize) {
                        int bytesToRead = (int) Math.min(buffer.length, fileSize - totalReceived);
                        bytesRead = dis.read(buffer, 0, bytesToRead);
                        if (bytesRead == -1) break;
                        
                        fos.write(buffer, 0, bytesRead);
                        totalReceived += bytesRead;
                        int percent = (int) ((totalReceived * 100) / fileSize);
                        listener.onTransferProgress(filename, percent);
                    }
                }
                
                dos.writeUTF("FILE_COMPLETE" + Protocol.SEP + filename);
                listener.onTransferComplete(filename);
            } else if (parts[0].equals("FILE_REJECT") && parts.length >= 3) {
                listener.onTransferError(filename, parts[2]);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            listener.onTransferError(filename, e.getMessage());
        }
    }
}
