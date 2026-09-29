package peer;

import java.io.File;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import common.FileInfo;
import common.PeerInfo;

public class PeerMain implements ChatServer.ChatListener, FileServer.FileTransferListener, TrackerConnection.TrackerListener {
    
    private String username;
    private String trackerIp;
    private int trackerPort;
    private int chatPort;
    private int filePort;
    private String sharedFolderPath;
    
    private TrackerConnection trackerConnection;
    private ChatServer chatServer;
    private FileServer fileServer;
    private SharedFileManager sharedFileManager;
    
    private Map<String, PeerInfo> onlinePeers;
    private Map<String, ChatSession> chatSessions;
    
    // Listeners từ GUI
    private ChatServer.ChatListener chatListener;
    private FileServer.FileTransferListener fileTransferListener;
    private TrackerConnection.TrackerListener trackerListener;
    
    public PeerMain(String username, String trackerIp, int trackerPort, int chatPort, int filePort, String sharedFolderPath) {
        this.username = username;
        this.trackerIp = trackerIp;
        this.trackerPort = trackerPort;
        this.chatPort = chatPort;
        this.filePort = filePort;
        this.sharedFolderPath = sharedFolderPath;
        
        this.onlinePeers = new HashMap<>();
        this.chatSessions = new HashMap<>();
        
        this.trackerConnection = new TrackerConnection(trackerIp, trackerPort, this);
        this.chatServer = new ChatServer(chatPort, this);
        this.fileServer = new FileServer(filePort, sharedFolderPath, this);
        this.sharedFileManager = new SharedFileManager(sharedFolderPath);
    }
    
    public PeerMain(String username, String trackerIp, int trackerPort, int chatPort, int filePort) {
        this(username, trackerIp, trackerPort, chatPort, filePort, "shared_" + username);
    }
    
    public void setChatListener(ChatServer.ChatListener listener) { this.chatListener = listener; }
    public void setFileTransferListener(FileServer.FileTransferListener listener) { this.fileTransferListener = listener; }
    public void setTrackerListener(TrackerConnection.TrackerListener listener) { this.trackerListener = listener; }
    
    public List<FileInfo> getSharedFiles() {
        return sharedFileManager.getSharedFiles(username, null, filePort);
    }
    
    public void start() {
        chatServer.start();
        fileServer.start();
        
        trackerConnection.connect(username, chatPort, filePort);
        
        List<FileInfo> existingFiles = getSharedFiles();
        for (FileInfo f : existingFiles) {
            trackerConnection.shareFile(f.getFilename(), f.getSize());
        }
    }
    
    public void stop() {
        trackerConnection.disconnect();
        chatServer.stopServer();
        fileServer.stopServer();
        
        for (ChatSession session : chatSessions.values()) {
            session.close();
        }
    }
    
    public void sendChatMessage(String toUser, String message) {
        ChatSession session = chatSessions.get(toUser);
        if (session == null) {
            PeerInfo peer = onlinePeers.get(toUser);
            if (peer != null) {
                ChatClient client = new ChatClient();
                session = client.connect(peer.getIp(), peer.getChatPort(), username, toUser, this);
                if (session != null) {
                    chatSessions.put(toUser, session);
                }
            }
        }
        if (session != null) {
            session.sendMessage(username, message);
        }
    }
    
    public void searchFile(String keyword) {
        trackerConnection.searchFile(keyword);
    }
    
    public void downloadFile(FileInfo fileInfo) {
        FileClient client = new FileClient();
        client.downloadFile(fileInfo.getIp(), fileInfo.getPort(), fileInfo.getFilename(), 
                            fileInfo.getSize(), sharedFileManager.getSharedFolderPath(), this);
    }
    
    public void shareFile(String filePath) {
        File file = new File(filePath);
        if (!sharedFileManager.addFile(filePath)) {
            throw new IllegalStateException("Không thể sao chép file vào thư mục "
                    + sharedFileManager.getSharedFolderPath());
        }

        File sharedFile = sharedFileManager.getFile(file.getName());
        if (!sharedFile.isFile()) {
            throw new IllegalStateException("File không tồn tại trong thư mục chia sẻ: "
                    + sharedFile.getAbsolutePath());
        }
        trackerConnection.shareFile(sharedFile.getName(), sharedFile.length());
    }

    // --- Forward các sự kiện tới GUI ---
    @Override
    public void onMessageReceived(String fromUser, String message) {
        if (chatListener != null) chatListener.onMessageReceived(fromUser, message);
    }

    @Override
    public void onChatConnectionEstablished(String fromUser, Socket socket) {
        if (!chatSessions.containsKey(fromUser) && Thread.currentThread() instanceof ChatSession) {
            chatSessions.put(fromUser, (ChatSession) Thread.currentThread());
        }
        if (chatListener != null) chatListener.onChatConnectionEstablished(fromUser, socket);
    }

    @Override
    public void onTransferProgress(String filename, int percent) {
        if (fileTransferListener != null) fileTransferListener.onTransferProgress(filename, percent);
    }

    @Override
    public void onTransferComplete(String filename) {
        File f = sharedFileManager.getFile(filename);
        if (f.exists()) {
            trackerConnection.shareFile(filename, f.length());
        }
        if (fileTransferListener != null) fileTransferListener.onTransferComplete(filename);
    }

    @Override
    public void onTransferError(String filename, String error) {
        if (fileTransferListener != null) fileTransferListener.onTransferError(filename, error);
    }

    @Override
    public void onPeerJoined(PeerInfo peer) {
        onlinePeers.put(peer.getUsername(), peer);
        if (trackerListener != null) trackerListener.onPeerJoined(peer);
    }

    @Override
    public void onPeerLeft(String username) {
        onlinePeers.remove(username);
        ChatSession session = chatSessions.remove(username);
        if (session != null) session.close();
        if (trackerListener != null) trackerListener.onPeerLeft(username);
    }

    @Override
    public void onSearchResult(List<FileInfo> results) {
        if (trackerListener != null) trackerListener.onSearchResult(results);
    }

    @Override
    public void onRegistered(List<PeerInfo> peerList) {
        for (PeerInfo p : peerList) {
            if (!p.getUsername().equals(this.username)) {
                onlinePeers.put(p.getUsername(), p);
            }
        }
        if (trackerListener != null) trackerListener.onRegistered(peerList);
    }
}
