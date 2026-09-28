package common;

import java.util.regex.Pattern;

/**
 * Lưu trữ thông tin của một peer.
 */
public class PeerInfo {
    private String username;
    private String ip;
    private int chatPort;
    private int filePort;
    private boolean online;

    public PeerInfo(String username, String ip, int chatPort, int filePort, boolean online) {
        this.username = username;
        this.ip = ip;
        this.chatPort = chatPort;
        this.filePort = filePort;
        this.online = online;
    }
    
    public PeerInfo(String username, String ip, int chatPort, int filePort) {
        this(username, ip, chatPort, filePort, true);
    }
    
    public PeerInfo(String ip, int chatPort, int filePort) {
        this("", ip, chatPort, filePort, true);
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getIp() { return ip; }
    public String getIpAddress() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public int getChatPort() { return chatPort; }
    public void setChatPort(int chatPort) { this.chatPort = chatPort; }

    public int getFilePort() { return filePort; }
    public void setFilePort(int filePort) { this.filePort = filePort; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    @Override
    public String toString() {
        return username + Protocol.SEP + ip + Protocol.SEP + chatPort + Protocol.SEP + filePort;
    }

    public static PeerInfo fromString(String s) {
        String[] parts = s.split(Pattern.quote(Protocol.SEP));
        if (parts.length >= 4) {
            String username = parts[0];
            String ip = parts[1];
            int chatPort = Integer.parseInt(parts[2]);
            int filePort = Integer.parseInt(parts[3]);
            return new PeerInfo(username, ip, chatPort, filePort, true);
        }
        return null;
    }
}
