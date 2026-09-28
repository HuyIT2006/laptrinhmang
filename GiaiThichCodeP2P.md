# BẢNG GIẢI THÍCH MÃ NGUỒN: MÔ HÌNH TRUYỀN FILE P2P

Tài liệu này được soạn ra để bạn dùng làm "phao cứu sinh" khi báo cáo và đối chất với thầy giáo. Nó giải thích cặn kẽ cách hệ thống của bạn hoạt động ĐÚNG CHUẨN mô hình mạng ngang hàng (P2P - Peer to Peer).

---

## 1. TỔNG QUAN KIẾN TRÚC
Dự án của bạn là một hệ thống **P2P Hybrid (lai)** giống hệt với cơ chế của BitTorrent thời đầu:
- **Tracker Server:** Đóng vai trò là cái "danh bạ" (Directory). Nó CHỈ lưu thông tin (Metadata) về việc "Ai đang giữ file gì, IP là bao nhiêu, Port nào". Nó **tuyệt đối không** lưu trữ file gốc hay làm trung gian truyền file.
- **Peer (Các máy trạm):** Vừa làm Client (tải file) vừa làm Server (cho máy khác tải). Khi cần tải file, Peer A sẽ hỏi Tracker xem Peer B ở đâu, sau đó **Peer A tự động kết nối TRỰC TIẾP tới Peer B để tải file**, không qua Tracker.

---

## 2. CHỨNG MINH CODE: LUỒNG HOẠT ĐỘNG P2P
Thầy giáo sẽ hỏi: *"Chỗ nào chứng minh file được tải trực tiếp giữa 2 máy?"*
Bạn hãy mở các file sau và giải thích theo các bước:

### BƯỚC 1: Lấy thông tin IP của máy chủ file từ Tracker
- Mở file: `src/common/FileInfo.java`
- **Giải thích:** Khi tìm kiếm một file, Tracker trả về một đối tượng `FileInfo`. Bạn hãy chỉ cho thầy thấy đối tượng này có chứa biến `ownerIp` và `ownerFilePort`. Nghĩa là máy muốn tải file đã biết chính xác "nhà" của máy đang giữ file ở đâu.

### BƯỚC 2: Khởi tạo tải file (Bắt đầu kết nối P2P)
- Mở file: `src/peer/PeerMain.java` - **Hàm `downloadFile(FileInfo fileInfo)` (Khoảng dòng 105)**
- **Giải thích:** Hàm này lấy `IP` và `Port` từ đối tượng `FileInfo` truyền vào và khởi tạo `FileClient` để tải.
```java
// Code trong PeerMain.java
public void downloadFile(FileInfo fileInfo) {
    FileClient client = new FileClient();
    // TRUYỀN TRỰC TIẾP IP VÀ PORT CỦA MÁY KHÁC VÀO ĐÂY
    client.downloadFile(fileInfo.getIp(), fileInfo.getPort(), ...); 
}
```

### BƯỚC 3: Mở kết nối Socket ngang hàng (Core P2P)
- Mở file: `src/peer/FileClient.java` - **Hàm `downloadFile` (Dòng 10)**
- **Giải thích:** Đây là bằng chứng thép. Máy khách tạo một Socket kết nối ĐÂM THẲNG tới IP và Port của máy giữ file (không phải IP của Tracker).
```java
// Code trong FileClient.java
try (Socket socket = new Socket(ip, port);
     DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
     DataInputStream dis = new DataInputStream(socket.getInputStream())) {
    
    // Yêu cầu tải file
    dos.writeUTF("FILE_REQUEST" + Protocol.SEP + filename);
    // ... Đọc dữ liệu file ...
```

### BƯỚC 4: Máy giữ file đẩy dữ liệu (FileServer)
- Mở file: `src/peer/FileServer.java` - **Class `FileTransferTask` (Khoảng dòng 83)**
- **Giải thích:** Ở phía bên kia (máy giữ file), khi app bật lên, `FileServer` đã luôn luôn mở một `ServerSocket` ở một cái `port` riêng biệt (ví dụ port 7000) để chờ các máy khác tới xin file (`serverSocket.accept()`).
- Khi `FileClient` ở trên kết nối tới, máy giữ file sẽ dùng `FileInputStream` đọc trực tiếp file từ ổ cứng, ném vào `buffer` và bơm qua mạng (`dos.write(buffer, 0, bytesRead)`) trả về cho máy kia.

---

## 3. CÂU HỎI THƯỜNG GẶP CỦA THẦY VÀ CÁCH TRẢ LỜI

**Thầy hỏi:** *"Nếu tắt Tracker đi trong lúc đang tải file thì sao?"*
**Bạn trả lời:** *"Dạ thưa thầy, file vẫn tải bình thường ạ. Vì Tracker chỉ có nhiệm vụ cung cấp địa chỉ IP lúc ban đầu thôi. Khi hai máy (hai Peer) đã mở Socket với nhau (`FileClient` kết nối tới `FileServer` của Peer kia) thì dòng chảy dữ liệu hoàn toàn độc lập, tắt Tracker đi kết nối này vẫn không đứt."*

**Thầy hỏi:** *"Tại sao em không cho tải file qua Tracker luôn cho dễ code?"*
**Bạn trả lời:** *"Nếu tải qua Tracker thì đó là mô hình Client-Server truyền thống rồi ạ, lúc đó Tracker sẽ bị quá tải (bottleneck) về băng thông nếu có hàng nghìn máy tải file cùng lúc. Bằng cách thiết kế `FileClient` và `FileServer` nằm trong cùng một app Peer, hệ thống của em tuân thủ đúng mô hình P2P: mỗi máy vừa là người tải (Client), vừa là người chia sẻ (Server), giúp giảm tải cho mạng trung tâm."*
