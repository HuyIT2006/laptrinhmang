package gui;

import common.FileInfo;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FilePanel extends JPanel {
    private JTable tblSharedFiles;
    private DefaultTableModel sharedFilesModel;
    private JTable tblSearchResults;
    private DefaultTableModel searchResultsModel;
    private JTextField txtSearch;
    private JProgressBar progressBar;
    private JLabel lblStatus;
    private FilePanelListener listener;
    private List<FileInfo> currentSearchResults;

    public interface FilePanelListener {
        void onAddFile();
        void onSearchFile(String keyword);
        void onDownloadFile(int selectedRow);
    }

    public FilePanel(FilePanelListener listener) {
        this.listener = listener;
        this.currentSearchResults = new ArrayList<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Shared files section
        JPanel pnlShared = new JPanel(new BorderLayout());
        pnlShared.setBorder(BorderFactory.createTitledBorder("File đang chia sẻ"));
        sharedFilesModel = new DefaultTableModel(new String[]{"Tên file", "Kích thước"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblSharedFiles = new JTable(sharedFilesModel);
        pnlShared.add(new JScrollPane(tblSharedFiles), BorderLayout.CENTER);
        
        JButton btnAddFile = new JButton("Thêm File");
        btnAddFile.addActionListener(e -> listener.onAddFile());
        JPanel pnlAddFile = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlAddFile.add(btnAddFile);
        pnlShared.add(pnlAddFile, BorderLayout.SOUTH);

        // Search section
        JPanel pnlSearch = new JPanel(new BorderLayout(5, 5));
        pnlSearch.setBorder(BorderFactory.createTitledBorder("Tìm kiếm file"));
        txtSearch = new JTextField();
        JButton btnSearch = new JButton("Tìm");
        btnSearch.addActionListener(e -> listener.onSearchFile(txtSearch.getText().trim()));
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(btnSearch, BorderLayout.EAST);

        // Results section
        JPanel pnlResults = new JPanel(new BorderLayout());
        pnlResults.setBorder(BorderFactory.createTitledBorder("Kết quả tìm kiếm"));
        searchResultsModel = new DefaultTableModel(new String[]{"Tên file", "Kích thước", "Người chia sẻ"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblSearchResults = new JTable(searchResultsModel);
        pnlResults.add(new JScrollPane(tblSearchResults), BorderLayout.CENTER);
        
        JButton btnDownload = new JButton("Tải về");
        btnDownload.addActionListener(e -> {
            int row = tblSearchResults.getSelectedRow();
            if (row >= 0) {
                listener.onDownloadFile(row);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một file để tải về.");
            }
        });
        JPanel pnlDownload = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlDownload.add(btnDownload);
        pnlResults.add(pnlDownload, BorderLayout.SOUTH);

        // Progress section
        JPanel pnlProgress = new JPanel(new BorderLayout());
        pnlProgress.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        lblStatus = new JLabel("Sẵn sàng");
        pnlProgress.add(progressBar, BorderLayout.CENTER);
        pnlProgress.add(lblStatus, BorderLayout.SOUTH);

        add(pnlShared);
        add(pnlSearch);
        add(pnlResults);
        add(pnlProgress);
    }

    public void updateSharedFiles(List<FileInfo> files) {
        SwingUtilities.invokeLater(() -> {
            sharedFilesModel.setRowCount(0);
            for (FileInfo f : files) {
                sharedFilesModel.addRow(new Object[]{f.getFilename(), f.getSize() + " bytes"});
            }
        });
    }

    public void updateSearchResults(List<FileInfo> results) {
        SwingUtilities.invokeLater(() -> {
            currentSearchResults = results;
            searchResultsModel.setRowCount(0);
            for (FileInfo f : results) {
                searchResultsModel.addRow(new Object[]{f.getFilename(), f.getSize() + " bytes", f.getPeerOwner()});
            }
        });
    }

    public void updateProgress(int percent, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(percent);
            lblStatus.setText(status);
        });
    }

    public FileInfo getSearchResult(int row) {
        if (row >= 0 && row < currentSearchResults.size()) {
            return currentSearchResults.get(row);
        }
        return null;
    }
}
