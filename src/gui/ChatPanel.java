package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatPanel extends JPanel {
    private JTextArea txtChatHistory;
    private JTextField txtInput;
    private JButton btnSend;
    private JButton btnSendFile;
    private String remoteUser;
    private ChatSendListener listener;

    public interface ChatSendListener {
        void onSendMessage(String toUser, String message);
        void onSendFile(String toUser);
    }

    public ChatPanel(String remoteUser, ChatSendListener listener) {
        this.remoteUser = remoteUser;
        this.listener = listener;

        setLayout(new BorderLayout());

        txtChatHistory = new JTextArea();
        txtChatHistory.setEditable(false);
        txtChatHistory.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(txtChatHistory);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        txtInput = new JTextField();
        btnSend = new JButton("Gửi");
        btnSendFile = new JButton("Gửi File");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.add(btnSendFile);
        btnPanel.add(Box.createHorizontalStrut(5));
        btnPanel.add(btnSend);

        bottomPanel.add(txtInput, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        btnSend.addActionListener(e -> sendMessage());
        txtInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        btnSendFile.addActionListener(e -> {
            if (listener != null) {
                listener.onSendFile(remoteUser);
            }
        });
    }

    private void sendMessage() {
        String msg = txtInput.getText().trim();
        if (!msg.isEmpty() && listener != null) {
            listener.onSendMessage(remoteUser, msg);
            appendMessage("Tôi", msg, true);
            txtInput.setText("");
        }
    }

    public void appendMessage(String fromUser, String message, boolean isMine) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String time = sdf.format(new Date());
        String formattedMsg = String.format("[%s] %s: %s\n", time, fromUser, message);
        
        SwingUtilities.invokeLater(() -> {
            txtChatHistory.append(formattedMsg);
            txtChatHistory.setCaretPosition(txtChatHistory.getDocument().getLength());
        });
    }
}
