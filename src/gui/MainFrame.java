package gui;

import common.FileInfo;
import common.PeerInfo;
import peer.ChatServer;
import peer.FileServer;
import peer.PeerMain;
import peer.TrackerConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame implements ChatPanel.ChatSendListener, FilePanel.FilePanelListener,
        TrackerConnection.TrackerListener, ChatServer.ChatListener, FileServer.FileTransferListener {

    private PeerMain peerMain;
    private DefaultListModel<String> peerListModel;
    private JList<String> listPeers;
    private JTabbedPane tabbedPane;
    private FilePanel filePanel;
    private Map<String, ChatPanel> chatPanels;
    private String username;

    public MainFrame(String username, String trackerIp, int trackerPort, int chatPort, int filePort) {
        this.username = username;
        this.chatPanels = new HashMap<>();

        setTitle("P2P Chat - " + username);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Khởi tạo PeerMain và các listener
        peerMain = new PeerMain(username, trackerIp, trackerPort, chatPort, filePort);
        peerMain.setTrackerListener(this);
        peerMain.setChatListener(this);
        peerMain.setFileTransferListener(this);

        initUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (peerMain != null) {
                    peerMain.stop();
                }
            }
        });

        // Bắt đầu PeerMain
        new Thread(() -> {
            try {
                peerMain.start();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Không thể khởi động Peer: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void initUI() {
        // Left Panel - Danh sách Peer
        JPanel pnlLeft = new JPanel(new BorderLayout());
        pnlLeft.setPreferredSize(new Dimension(200, 0));
        pnlLeft.setBorder(BorderFactory.createTitledBorder("Peers Online"));

        peerListModel = new DefaultListModel<>();
        listPeers = new JList<>(peerListModel);
        listPeers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedPeer = listPeers.getSelectedValue();
                    if (selectedPeer != null) {
                        String actualUsername = selectedPeer.split(" \\(")[0];
                        openChatTab(actualUsername);
                    }
                }
            }
        });
        pnlLeft.add(new JScrollPane(listPeers), BorderLayout.CENTER);
        add(pnlLeft, BorderLayout.WEST);

        // Center Panel - Tab Chat
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // Right Panel - File
        filePanel = new FilePanel(this);
        filePanel.setPreferredSize(new Dimension(300, 0));
        add(filePanel, BorderLayout.EAST);
    }

    private void openChatTab(String peerName) {
        if (!chatPanels.containsKey(peerName)) {
            ChatPanel chatPanel = new ChatPanel(peerName, this);
            chatPanels.put(peerName, chatPanel);
            tabbedPane.addTab(peerName, chatPanel);
        }
        tabbedPane.setSelectedComponent(chatPanels.get(peerName));
    }

    // --- ChatSendListener ---
    @Override
    public void onSendMessage(String toUser, String message) {
        new Thread(() -> {
            peerMain.sendChatMessage(toUser, message);
        }).start();
    }

    @Override
    public void onSendFile(String toUser) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            new Thread(() -> {
                // peerMain.shareFile(file.getAbsolutePath());
                // peerMain.initiateFileTransfer(toUser, file.getName());
                JOptionPane.showMessageDialog(this, "Tính năng gửi file trực tiếp đang được hoàn thiện.");
            }).start();
        }
    }

    // --- FilePanelListener ---
    @Override
    public void onAddFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            new Thread(() -> {
                try {
                    peerMain.shareFile(file.getAbsolutePath());
                    // Cập nhật giao diện sau khi share
                    List<FileInfo> sharedFiles = peerMain.getSharedFiles();
                    filePanel.updateSharedFiles(sharedFiles);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void onSearchFile(String keyword) {
        new Thread(() -> {
            peerMain.searchFile(keyword);
        }).start();
    }

    @Override
    public void onDownloadFile(int selectedRow) {
        FileInfo fileInfo = filePanel.getSearchResult(selectedRow);
        if (fileInfo != null) {
            new Thread(() -> {
                peerMain.downloadFile(fileInfo);
            }).start();
        }
    }

    // --- TrackerListener ---
    @Override
    public void onPeerJoined(PeerInfo peer) {
        SwingUtilities.invokeLater(() -> {
            if (!peer.getUsername().equals(username)) {
                String displayStr = peer.getUsername() + " (" + peer.getIp() + ") - Online";
                for (int i = 0; i < peerListModel.size(); i++) {
                    if (peerListModel.get(i).startsWith(peer.getUsername() + " (")) {
                        peerListModel.remove(i);
                        break;
                    }
                }
                peerListModel.addElement(displayStr);
            }
        });
    }

    @Override
    public void onPeerLeft(String leftUsername) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < peerListModel.size(); i++) {
                if (peerListModel.get(i).startsWith(leftUsername + " (")) {
                    peerListModel.remove(i);
                    break;
                }
            }
        });
    }

    @Override
    public void onRegistered(List<PeerInfo> peerList) {
        SwingUtilities.invokeLater(() -> {
            peerListModel.clear();
            for (PeerInfo p : peerList) {
                if (!p.getUsername().equals(username)) {
                    peerListModel.addElement(p.getUsername() + " (" + p.getIp() + ") - Online");
                }
            }
        });
    }

    @Override
    public void onSearchResult(List<FileInfo> results) {
        filePanel.updateSearchResults(results);
    }

    // --- ChatListener ---
    @Override
    public void onMessageReceived(String fromUser, String message) {
        SwingUtilities.invokeLater(() -> {
            openChatTab(fromUser);
            ChatPanel panel = chatPanels.get(fromUser);
            if (panel != null) {
                panel.appendMessage(fromUser, message, false);
            }
        });
    }

    @Override
    public void onChatConnectionEstablished(String fromUser, Socket socket) {
        // Đã được xử lý trong PeerMain, không cần cập nhật giao diện ngay
    }

    // --- FileTransferListener ---
    @Override
    public void onTransferProgress(String filename, int percent) {
        filePanel.updateProgress(percent, "Đang tải: " + filename);
    }

    @Override
    public void onTransferComplete(String filename) {
        filePanel.updateProgress(100, "Hoàn tất: " + filename);
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Tải file hoàn tất: " + filename);
        });
    }

    @Override
    public void onTransferError(String filename, String error) {
        filePanel.updateProgress(0, "Lỗi: " + filename);
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Lỗi tải file " + filename + ": " + error, "Lỗi", JOptionPane.ERROR_MESSAGE);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            if (login.isConfirmed()) {
                String username = login.getUsername();
                String trackerIp = login.getTrackerIp();
                int trackerPort = login.getTrackerPort();
                int chatPort = login.getChatPort();
                int filePort = login.getFilePort();

                MainFrame mainFrame = new MainFrame(username, trackerIp, trackerPort, chatPort, filePort);
                mainFrame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
