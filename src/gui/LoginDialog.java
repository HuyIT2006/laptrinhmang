package gui;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

public class LoginDialog extends JDialog {
    private JTextField txtUsername;
    private JTextField txtTrackerIp;
    private JTextField txtTrackerPort;
    private JTextField txtChatPort;
    private JTextField txtFilePort;
    private boolean confirmed = false;

    public LoginDialog(JFrame parent) {
        super(parent, "Dang nhap P2P Chat", true);
        setLayout(new BorderLayout());
        setSize(450, 350);
        setLocationRelativeTo(parent);

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(230, 240, 255));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        try {
            String myIp = InetAddress.getLocalHost().getHostAddress();
            infoPanel.add(new JLabel("IP cua may ban: " + myIp));
        } catch (Exception e) {
            infoPanel.add(new JLabel("Khong xac dinh duoc IP"));
        }
        add(infoPanel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        panel.add(new JLabel("Ten nguoi dung:"));
        txtUsername = new JTextField();
        panel.add(txtUsername);

        panel.add(new JLabel("IP Tracker (may chu):"));
        txtTrackerIp = new JTextField("127.0.0.1");
        panel.add(txtTrackerIp);

        panel.add(new JLabel("Cong Tracker:"));
        txtTrackerPort = new JTextField("5000");
        panel.add(txtTrackerPort);

        panel.add(new JLabel("Cong Chat:"));
        txtChatPort = new JTextField("6000");
        panel.add(txtChatPort);

        panel.add(new JLabel("Cong File:"));
        txtFilePort = new JTextField("7000");
        panel.add(txtFilePort);

        add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnConnect = new JButton("Ket noi");
        JButton btnExit = new JButton("Thoat");

        btnConnect.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });
        btnExit.addActionListener(e -> dispose());

        btnPanel.add(btnConnect);
        btnPanel.add(btnExit);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private boolean validateInput() {
        if (txtUsername.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ten nguoi dung khong duoc de trong!", "Loi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            Integer.parseInt(txtTrackerPort.getText().trim());
            Integer.parseInt(txtChatPort.getText().trim());
            Integer.parseInt(txtFilePort.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cong phai la so!", "Loi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isConfirmed() { return confirmed; }
    public String getUsername() { return txtUsername.getText().trim(); }
    public String getTrackerIp() { return txtTrackerIp.getText().trim(); }
    public int getTrackerPort() { return Integer.parseInt(txtTrackerPort.getText().trim()); }
    public int getChatPort() { return Integer.parseInt(txtChatPort.getText().trim()); }
    public int getFilePort() { return Integer.parseInt(txtFilePort.getText().trim()); }
}