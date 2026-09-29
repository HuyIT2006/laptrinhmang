package peer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import common.FileInfo;

public class SharedFileManager {
    private String sharedFolderPath;

    public SharedFileManager(String sharedFolderPath) {
        this.sharedFolderPath = sharedFolderPath;
        File folder = new File(sharedFolderPath);
        if (!folder.exists()) {
            folder.mkdirs(); // Tạo thư mục nếu chưa tồn tại
        }
    }

    public List<FileInfo> getSharedFiles(String username, String ip, int filePort) {
        List<FileInfo> files = new ArrayList<>();
        File folder = new File(sharedFolderPath);
        File[] listOfFiles = folder.listFiles();
        
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    // Giả định Constructor FileInfo(String filename, long size, String username, String ip, int port)
                    FileInfo info = new FileInfo(file.getName(), file.length(), username, ip, filePort);
                    files.add(info);
                }
            }
        }
        return files;
    }

    public boolean addFile(String filePath) {
        File source = new File(filePath);
        if (!source.exists() || !source.isFile()) {
            return false;
        }

        File dest = new File(sharedFolderPath, source.getName());
        try {
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            boolean copied = dest.isFile() && dest.length() == source.length();
            if (copied) {
                System.out.println("Đã thêm file vào danh sách chia sẻ: " + dest.getAbsolutePath());
            }
            return copied;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasFile(String filename) {
        File file = new File(sharedFolderPath, filename);
        return file.exists() && file.isFile();
    }

    public File getFile(String filename) {
        return new File(sharedFolderPath, filename);
    }

    public String getSharedFolderPath() {
        return sharedFolderPath;
    }
}
