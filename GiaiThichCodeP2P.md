# KỊCH BẢN BẢO VỆ CODE: MÔ HÌNH TRUYỀN FILE P2P

Để bảo vệ với thầy, bạn hãy **mở thẳng các file code** ra và giải thích theo 2 giai đoạn dưới đây. Hệ thống P2P của bạn bao gồm 2 phần: (1) Máy chủ Tracker chỉ đóng vai trò "danh bạ" (lưu thông tin file), và (2) Các máy Peer tự động kết nối TRỰC TIẾP để truyền file.

---

## GIAI ĐOẠN 1: TÌM ĐỊA CHỈ IP CỦA PEER ĐANG GIỮ FILE
**Câu hỏi của thầy:** *"Làm sao máy A biết máy B đang giữ file gì để tải?"*

**Bạn mở file: `src/peer/TrackerConnection.java` (dòng 87)**
Hãy chỉ cho thầy hàm `shareFile`:
```java
public void shareFile(String filename, long fileSize) {
    if (isConnected) {
        try {
            // Chỉ gửi TÊN FILE và KÍCH THƯỚC lên máy chủ Tracker
            dos.writeUTF("SHARE_FILE" + Protocol.SEP + filename + Protocol.SEP + fileSize);
        } catch (IOException e) { ... }
    }
}
```
**🗣️ Giải thích:** "Thưa thầy, khi một Peer (máy B) muốn chia sẻ file, nó dùng hàm này để gửi **thông tin metadata** (chỉ có Tên File và Kích Thước) cho Tracker. Tuyệt đối không có đoạn code nào đọc nội dung file để gửi lên Tracker. Tracker sẽ tự động gắn thêm IP và Port của máy B vào danh sách."

---

## GIAI ĐOẠN 2: KẾT NỐI NGANG HÀNG (P2P) ĐỂ TRUYỀN FILE
**Câu hỏi của thầy:** *"Đoạn code nào chứng minh máy A tải file trực tiếp từ máy B (P2P) chứ không phải lấy qua Tracker?"*

### Bước 2.1: Máy A bắt đầu xin tải
**Bạn mở file: `src/peer/PeerMain.java` (dòng 105)**
```java
public void downloadFile(FileInfo fileInfo) {
    FileClient client = new FileClient();
    // TRUYỀN IP VÀ PORT CỦA MÁY B VÀO ĐÂY
    client.downloadFile(fileInfo.getIp(), fileInfo.getPort(), fileInfo.getFilename(), 
                        fileInfo.getSize(), sharedFileManager.getSharedFolderPath(), this);
}
```
**🗣️ Giải thích:** "Thưa thầy, sau khi tìm kiếm, Tracker trả về đối tượng `FileInfo` chứa IP và Port của máy B (máy đang giữ file). Trong hàm `downloadFile` của `PeerMain`, em trích xuất IP và Port này (`fileInfo.getIp()`, `fileInfo.getPort()`) để đưa thẳng cho `FileClient` xử lý."

### Bước 2.2: Máy A đâm thẳng kết nối tới Máy B (Bằng chứng thép)
**Bạn mở file: `src/peer/FileClient.java` (dòng 9 - 14)**
```java
public void downloadFile(String ip, int port, String filename, long expectedSize, ...) {
    try (
        // TẠO SOCKET KẾT NỐI TRỰC TIẾP TỚI IP VÀ PORT CỦA MÁY B
        Socket socket = new Socket(ip, port);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        DataInputStream dis = new DataInputStream(socket.getInputStream())
    ) {
        // Gửi lệnh yêu cầu tải file cho máy B
        dos.writeUTF("FILE_REQUEST" + Protocol.SEP + filename);
        // ... (phần dưới là vòng lặp while đọc byte lưu xuống file)
```
**🗣️ Giải thích:** "Đây là đoạn code lõi của mô hình P2P ạ. Thay vì kết nối tới IP của Tracker, hàm này gọi `new Socket(ip, port)` với IP của Máy B. Từ thời điểm dòng code này chạy, đường truyền dữ liệu chỉ tồn tại giữa Máy A và Máy B, Tracker không hề tham gia."

### Bước 2.3: Máy B đọc file từ ổ cứng và đẩy qua mạng cho Máy A
**Bạn mở file: `src/peer/FileServer.java` (dòng 32 và dòng 79)**
Chỉ vào hàm `run()` của FileServer:
```java
// Dòng 32: Máy B lúc nào cũng mở một ServerSocket để chờ máy khác tới xin file
serverSocket = new ServerSocket(port); 
// ...
Socket socket = serverSocket.accept(); // Có máy A kết nối tới
new Thread(new FileTransferTask(socket)).start(); // Tạo Thread đẩy file
```
Sau đó chỉ xuống `FileTransferTask` (dòng 83 - 89):
```java
// Dòng 83: Mở file gốc trên ổ cứng của Máy B
try (FileInputStream fis = new FileInputStream(file)) {
    byte[] buffer = new byte[8192];
    int bytesRead;
    // Dòng 88: Đọc liên tục từng khối byte từ ổ cứng...
    while ((bytesRead = fis.read(buffer)) != -1) {
        // ... và đẩy thẳng qua luồng mạng (Socket) sang Máy A
        dos.write(buffer, 0, bytesRead); 
        // ...
    }
}
```
**🗣️ Giải thích:** "Ở Máy B, `FileServer` đóng vai trò là server phục vụ truyền file. Khi Socket của Máy A gọi tới, nó tạo một Thread riêng (`FileTransferTask`). Trong Thread này, `FileInputStream` sẽ đọc trực tiếp các byte của file từ ổ cứng của Máy B, và `dos.write` đẩy các byte đó qua đường mạng sang Máy A. Không có bước nào tải file lên bộ nhớ đệm của Tracker cả."

---
**💡 Chốt hạ:** "Với 3 file `PeerMain`, `FileClient` và `FileServer`, em đã chứng minh được khi tải file, 2 Peer tự mở luồng kết nối Socket trực tiếp với nhau, hoàn toàn đúng với bản chất của mô hình Peer-to-Peer."
