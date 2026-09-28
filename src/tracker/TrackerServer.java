package tracker;

import common.FileInfo;
import common.PeerInfo;
import common.Protocol;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TrackerServer {
    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, PeerInfo> onlinePeers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<FileInfo>> sharedFiles = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> connectionTimes = new ConcurrentHashMap<>();

    // GUI components
    private JFrame frame;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;
    private JLabel lblServerIp;
    private JLabel lblPeerCount;

    public TrackerServer() {
        initGUI();
    }

    private void initGUI() {
        frame = new JFrame("Tracker Server - Quan ly mang P2P");
        frame.setSize(700, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));
        frame.setLocationRelativeTo(null);

        // Top panel - Server info
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.setBorder(BorderFactory.createTitledBorder("Thong tin Server"));
        lblServerIp = new JLabel("IP Server: dang xac dinh...");
        lblStatus = new JLabel("Trang thai: Dang khoi dong...");
        lblPeerCount = new JLabel("So peer ket noi: 0");
        topPanel.add(lblServerIp);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(lblStatus);
        row2.add(Box.createHorizontalStrut(30));
        row2.add(lblPeerCount);
        topPanel.add(row2);
        frame.add(topPanel, BorderLayout.NORTH);

        // Center - Peer table
        String[] columns = {"Username", "Dia chi IP", "Cong Chat", "Cong File", "Thoi gian ket noi", "Trang thai"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sach Peer da ket noi"));
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private void updateTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Map.Entry<String, PeerInfo> entry : onlinePeers.entrySet()) {
                String user = entry.getKey();
                PeerInfo p = entry.getValue();
                String time = connectionTimes.getOrDefault(user, "N/A");
                tableModel.addRow(new Object[]{
                    user, p.getIpAddress(), p.getChatPort(), p.getFilePort(), time, "Online"
                });
            }
            lblPeerCount.setText("So peer ket noi: " + onlinePeers.size());
        });
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(Protocol.TRACKER_PORT);
            String serverIp = InetAddress.getLocalHost().getHostAddress();
            SwingUtilities.invokeLater(() -> {
                lblServerIp.setText("IP Server: " + serverIp + " | Port: " + Protocol.TRACKER_PORT);
                lblStatus.setText("Trang thai: Dang chay");
                lblStatus.setForeground(new Color(0, 128, 0));
            });
            System.out.println("Tracker Server started on " + serverIp + ":" + Protocol.TRACKER_PORT);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, this);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void registerPeer(String username, PeerInfo peerInfo, ClientHandler handler) {
        onlinePeers.put(username, peerInfo);
        sharedFiles.put(username, new ArrayList<>());
        clientHandlers.put(username, handler);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        connectionTimes.put(username, sdf.format(new Date()));
        System.out.println("Peer joined: " + username + " (" + peerInfo.getIpAddress() + ")");

        String joinMsg = Protocol.PEER_JOINED + Protocol.SEPARATOR + username + Protocol.SEPARATOR +
                         peerInfo.getIpAddress() + Protocol.SEPARATOR +
                         peerInfo.getChatPort() + Protocol.SEPARATOR +
                         peerInfo.getFilePort();
        broadcastToAll(joinMsg, username);

        StringBuilder sb = new StringBuilder(Protocol.REGISTER_OK + Protocol.SEPARATOR);
        boolean first = true;
        for (Map.Entry<String, PeerInfo> entry : onlinePeers.entrySet()) {
            if (!first) sb.append(",");
            PeerInfo p = entry.getValue();
            sb.append(entry.getKey()).append(":")
              .append(p.getIpAddress()).append(":")
              .append(p.getChatPort()).append(":")
              .append(p.getFilePort());
            first = false;
        }
        handler.sendMessage(sb.toString());
        updateTable();
    }

    public synchronized void unregisterPeer(String username) {
        if (username != null && onlinePeers.containsKey(username)) {
            onlinePeers.remove(username);
            sharedFiles.remove(username);
            clientHandlers.remove(username);
            connectionTimes.remove(username);
            broadcastToAll(Protocol.PEER_LEFT + Protocol.SEPARATOR + username, username);
            System.out.println("Peer disconnected: " + username);
            updateTable();
        }
    }

    public synchronized void addSharedFile(String username, FileInfo fileInfo) {
        List<FileInfo> files = sharedFiles.get(username);
        if (files != null) {
            files.add(fileInfo);
            System.out.println("User " + username + " shared file: " + fileInfo.getFileName());
        }
    }

    public String searchFiles(String keyword) {
        StringBuilder result = new StringBuilder();
        String keywordLower = keyword.toLowerCase();
        boolean found = false;

        for (Map.Entry<String, List<FileInfo>> entry : sharedFiles.entrySet()) {
            String ownerUser = entry.getKey();
            PeerInfo ownerInfo = onlinePeers.get(ownerUser);
            if (ownerInfo == null) continue;

            for (FileInfo fileInfo : entry.getValue()) {
                if (fileInfo.getFileName().toLowerCase().contains(keywordLower)) {
                    if (found) result.append(",");
                    result.append(fileInfo.getFileName()).append(":")
                          .append(fileInfo.getFileSize()).append(":")
                          .append(ownerUser).append(":")
                          .append(ownerInfo.getIpAddress()).append(":")
                          .append(ownerInfo.getFilePort());
                    found = true;
                }
            }
        }

        if (!found) return Protocol.SEARCH_RESULT + Protocol.SEPARATOR + "NONE";
        return Protocol.SEARCH_RESULT + Protocol.SEPARATOR + result.toString();
    }

    public void broadcastToAll(String message, String excludeUser) {
        for (Map.Entry<String, ClientHandler> entry : clientHandlers.entrySet()) {
            if (!entry.getKey().equals(excludeUser)) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    public static void main(String[] args) {
        gui.UITheme.apply();
        TrackerServer server = new TrackerServer();
        server.startServer();
    }
}