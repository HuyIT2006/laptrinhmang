package peer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;
import common.Protocol;

public class FileServer extends Thread {
    public interface FileTransferListener {
        void onTransferProgress(String filename, int percent);
        void onTransferComplete(String filename);
        void onTransferError(String filename, String error);
    }

    private int port;
    private String sharedFolderPath;
    private FileTransferListener listener;
    private ServerSocket serverSocket;
    private boolean isRunning;

    public FileServer(int port, String sharedFolderPath, FileTransferListener listener) {
        this.port = port;
        this.sharedFolderPath = sharedFolderPath;
        this.listener = listener;
        this.isRunning = false;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("FileServer đang lắng nghe tải file trên cổng " + port);

            while (isRunning) {
                Socket socket = serverSocket.accept();
                new Thread(new FileTransferTask(socket)).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                e.printStackTrace();
            }
        }
    }

    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class FileTransferTask implements Runnable {
        private Socket socket;

        public FileTransferTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String filenameForError = "Unknown";
            try (DataInputStream dis = new DataInputStream(socket.getInputStream());
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                 
                String request = dis.readUTF();
                
                String[] parts = request.split(Pattern.quote(Protocol.SEP));
                if (parts.length >= 2 && parts[0].equals("FILE_REQUEST")) {
                    String filename = parts[1];
                    filenameForError = filename;
                    File file = new File(sharedFolderPath, filename);
                    
                    if (file.exists() && file.isFile()) {
                        long fileSize = file.length();
                        dos.writeUTF("FILE_ACCEPT" + Protocol.SEP + filename + Protocol.SEP + fileSize);
                        
                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            long totalSent = 0;
                            
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                dos.write(buffer, 0, bytesRead);
                                totalSent += bytesRead;
                                int percent = (int) ((totalSent * 100) / fileSize);
                                listener.onTransferProgress(filename, percent);
                            }
                            dos.flush();
                        }
                        
                        String completeMsg = dis.readUTF();
                        if (completeMsg.equals("FILE_COMPLETE" + Protocol.SEP + filename)) {
                            listener.onTransferComplete(filename);
                        }
                    } else {
                        dos.writeUTF("FILE_REJECT" + Protocol.SEP + filename + Protocol.SEP + "File not found");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onTransferError(filenameForError, e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
