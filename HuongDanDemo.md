# Hướng Dẫn Demo Chức Năng Truyền File P2P Giữa 2 Máy Tính

Tài liệu này hướng dẫn cách setup và demo ứng dụng P2P Chat & File Sharing trên 2 máy tính khác nhau trong cùng một mạng LAN (ví dụ: cùng kết nối vào một mạng Wi-Fi).

## Chuẩn Bị
1. Đảm bảo cả hai máy tính đều đang kết nối vào **cùng một mạng Wi-Fi** hoặc mạng LAN.
2. Copy toàn bộ thư mục dự án này sang máy tính thứ 2 (hoặc gửi file nén và giải nén ở máy thứ 2).
3. Đảm bảo cả 2 máy đều đã cài đặt Java (JDK/JRE).

---

## Bước 1: Khởi Động Tracker Server (Trên Máy 1)
Máy 1 sẽ đóng vai trò là máy chủ trung tâm (Tracker) để các Peer tìm thấy nhau.
1. Mở thư mục dự án trên Máy 1.
2. Chạy file `run_tracker.bat`.
3. Một cửa sổ terminal đen sẽ hiện ra báo hiệu Tracker đang chạy ở port 5000. Để nguyên cửa sổ này.

---

## Bước 2: Khởi Động Peer Đầu Tiên (Trên Máy 1)
1. Trên Máy 1, chạy file `run_peer.bat`.
2. Giao diện Đăng Nhập sẽ hiện ra. 
3. Chú ý dòng chữ màu xanh ở trên cùng: **"IP cua may ban: 192.168.x.x"**. 
   👉 **Hãy ghi chú lại địa chỉ IP này (ví dụ: 192.168.1.10).** Máy 2 sẽ cần IP này để kết nối tới.
4. Điền tên người dùng (ví dụ: `Máy 1`).
5. Phần **IP Tracker** cứ để mặc định là `127.0.0.1`.
6. Nhấn **Kết nối**. App sẽ mở ra.
7. Bạn có thể chọn file để chia sẻ lên hệ thống bằng nút **Add File** bên góc phải.

---

## Bước 3: Khởi Động Peer Thứ Hai (Trên Máy 2)
1. Mở thư mục dự án trên Máy 2, chạy file `run_peer.bat`.
2. Điền tên người dùng (ví dụ: `Máy 2`).
3. ⚠️ **QUAN TRỌNG:** Ở ô **IP Tracker (may chu)**, hãy **xoá** `127.0.0.1` và **điền địa chỉ IP của Máy 1** mà bạn đã ghi chú ở Bước 2 (ví dụ: `192.168.1.10`).
4. Nhấn **Kết nối**.

*(Lưu ý: Nếu Windows hiện bảng cảnh báo Tường Lửa (Windows Security Alert) trên cả 2 máy, hãy tick vào "Private networks" và bấm "Allow access" để không bị chặn kết nối).*

---

## Bước 4: Demo Truyền File P2P Cho Thầy Giáo Xem
1. Khi Máy 2 kết nối thành công, nhìn sang giao diện của Máy 1, bạn sẽ thấy ở cột bên trái (Peers Online) xuất hiện dòng chữ:
   👉 **Máy 2 (192.168.1.xxx) - Online**
2. Điều này chứng minh cho thầy thấy là hệ thống đã nhận diện được một máy khác trong mạng LAN kết nối tới (hiển thị rõ IP và trạng thái).
3. Trên **Máy 2**, gõ tên file mà Máy 1 đang chia sẻ vào ô tìm kiếm và bấm **Search**.
4. Chọn file tìm được và bấm **Download**.
5. Mở thư mục `shared_Máy 2` (hoặc thư mục chia sẻ tương ứng của user đó) để kiểm tra file đã được tải về trực tiếp từ Máy 1 sang Máy 2 thành công.

---
**Chúc bạn có một buổi demo thành công!**
