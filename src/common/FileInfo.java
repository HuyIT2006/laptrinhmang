package common;

import java.util.regex.Pattern;

/**
 * Lưu trữ thông tin metadata của một file.
 */
public class FileInfo {
    private String filename;
    private long fileSize;
    private String ownerUsername;
    private String ownerIp;
    private int ownerFilePort;

    public FileInfo(String filename, long fileSize, String ownerUsername, String ownerIp, int ownerFilePort) {
        this.filename = filename;
        this.fileSize = fileSize;
        this.ownerUsername = ownerUsername;
        this.ownerIp = ownerIp;
        this.ownerFilePort = ownerFilePort;
    }
    
    public FileInfo(String filename, long fileSize, String ownerUsername) {
        this(filename, fileSize, ownerUsername, "", 0);
    }

    public String getFilename() { return filename; }
    public String getFileName() { return filename; }
    
    public long getFileSize() { return fileSize; }
    public long getSize() { return fileSize; }
    
    public String getOwnerUsername() { return ownerUsername; }
    public String getUsername() { return ownerUsername; }
    public String getPeerOwner() { return ownerUsername; }
    
    public String getOwnerIp() { return ownerIp; }
    public String getIp() { return ownerIp; }
    
    public int getOwnerFilePort() { return ownerFilePort; }
    public int getPort() { return ownerFilePort; }

    @Override
    public String toString() {
        return filename + Protocol.SEP + fileSize + Protocol.SEP + ownerUsername + Protocol.SEP + ownerIp + Protocol.SEP + ownerFilePort;
    }

    public static FileInfo fromString(String s) {
        String[] parts = s.split(Pattern.quote(Protocol.SEP));
        if (parts.length >= 5) {
            String filename = parts[0];
            long fileSize = Long.parseLong(parts[1]);
            String ownerUsername = parts[2];
            String ownerIp = parts[3];
            int ownerFilePort = Integer.parseInt(parts[4]);
            return new FileInfo(filename, fileSize, ownerUsername, ownerIp, ownerFilePort);
        }
        return null;
    }
}
