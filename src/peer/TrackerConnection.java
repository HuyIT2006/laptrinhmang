package peer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import common.MessageUtils;
import common.Protocol;
import common.PeerInfo;
import common.FileInfo;

public class TrackerConnection {
    public interface TrackerListener {
        void onPeerJoined(PeerInfo peer);
        void onPeerLeft(String username);
        void onSearchResult(List<FileInfo> results);
        void onRegistered(List<PeerInfo> peerList);
    }

    private String trackerIp;
    private int trackerPort;
    private TrackerListener listener;
    private Socket socket;
    private boolean isConnected;
    private Thread readThread;
    private String username;
    private BufferedReader reader;
    private BufferedWriter writer;

    public TrackerConnection(String trackerIp, int trackerPort, TrackerListener listener) {
        this.trackerIp = trackerIp;
        this.trackerPort = trackerPort;
        this.listener = listener;
        this.isConnected = false;
    }

    public void connect(String username, int chatPort, int filePort) {
        this.username = username;
        try {
            socket = new Socket(trackerIp, trackerPort);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            isConnected = true;
            
            String regMsg = "REGISTER" + Protocol.SEP + username + Protocol.SEP + chatPort + Protocol.SEP + filePort;
            MessageUtils.sendMessage(writer, regMsg);
            
            readThread = new Thread(() -> {
                try {
                    while (isConnected) {
                        String message = MessageUtils.receiveMessage(reader);
                        if (message == null) break;
                        
                        String[] parts = message.split(Pattern.quote(Protocol.SEP));
                        String cmd = parts[0];
                        
                        switch (cmd) {
                            case "REGISTER_OK":
                                List<PeerInfo> peers = new ArrayList<>();
                                if (parts.length > 1 && !parts[1].isEmpty()) {
                                    String[] peerStrings = parts[1].split(",");
                                    for (String pStr : peerStrings) {
                                        String[] pData = pStr.split(":");
                                        if (pData.length >= 4) {
                                            peers.add(new PeerInfo(pData[0], pData[1], Integer.parseInt(pData[2]), Integer.parseInt(pData[3])));
                                        }
                                    }
                                }
                                listener.onRegistered(peers);
                                break;
                            case "PEER_JOINED":
                                if (parts.length >= 5) {
                                    PeerInfo peer = new PeerInfo(parts[1], parts[2], Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
                                    listener.onPeerJoined(peer);
                                }
                                break;
                            case "PEER_LEFT":
                                if (parts.length >= 2) {
                                    listener.onPeerLeft(parts[1]);
                                }
                                break;
                            case "SEARCH_RESULT":
                                List<FileInfo> results = new ArrayList<>();
                                if (parts.length > 1 && !parts[1].equals("NONE") && !parts[1].isEmpty()) {
                                    String[] fileStrings = parts[1].split(",");
                                    for (String fStr : fileStrings) {
                                        String[] fData = fStr.split(":");
                                        if (fData.length >= 5) {
                                            results.add(new FileInfo(fData[0], Long.parseLong(fData[1]), fData[2], fData[3], Integer.parseInt(fData[4])));
                                        }
                                    }
                                }
                                listener.onSearchResult(results);
                                break;
                            case "SHARE_FILE_OK":
                                break;
                        }
                    }
                } catch (Exception e) {
                    if (isConnected) e.printStackTrace();
                } finally {
                    disconnect();
                }
            });
            readThread.start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shareFile(String filename, long fileSize) {
        if (!isConnected) return;
        try {
            String msg = "SHARE_FILE" + Protocol.SEP + filename + Protocol.SEP + fileSize;
            MessageUtils.sendMessage(writer, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchFile(String keyword) {
        if (!isConnected) return;
        try {
            String msg = "SEARCH_FILE" + Protocol.SEP + keyword;
            MessageUtils.sendMessage(writer, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (!isConnected) return;
        isConnected = false;
        try {
            MessageUtils.sendMessage(writer, "UNREGISTER" + Protocol.SEP + username);
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
