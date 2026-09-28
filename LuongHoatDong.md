# Luồng hoạt động của Ứng dụng P2P Chat & Truyền File

Ứng dụng của chúng ta sử dụng mô hình **Hybrid P2P**. Tức là:
- **Giống Napster/BitTorrent Tracker**: Chúng ta có một máy chủ trung tâm (`Tracker Server`) chỉ để giữ danh bạ ai đang online và ai đang có file gì. Tracker không giữ nội dung file.
- **Giống Skype/Gnutella**: Việc gửi tin nhắn (chat) và truyền dữ liệu file diễn ra *hoàn toàn trực tiếp* giữa các người dùng (Peers) với nhau, không đi qua Tracker.

Dưới đây là luồng hoạt động chi tiết từ đầu đến cuối:

## 1. Giai đoạn Khởi động & Đăng ký (Register)
1. **Khởi động Tracker**: Khởi chạy `TrackerServer`. Tracker sẽ mở cổng `5000` (mặc định) để chờ các người dùng (Peer) kết nối.
2. **Peer khởi động**: Một Peer (ví dụ: `Alice`) chạy ứng dụng GUI. Nhập tên đăng nhập và các thông số cổng.
3. **Mở Server tại Peer**: Máy của Alice tự động mở 2 Server thu nhỏ chạy ngầm:
   - `ChatServer`: Lắng nghe kết nối chat từ người khác.
   - `FileServer`: Lắng nghe yêu cầu tải file từ người khác.
4. **Đăng ký với Tracker**: 
   - Máy Alice gửi tin nhắn `REGISTER` lên Tracker báo danh.
   - Tracker ghi nhận Alice đang online.
   - Tracker trả về cho Alice danh sách những người đang online trước đó (`REGISTER_OK`).
   - Tracker "loa" cho những người khác biết Alice vừa vào mạng bằng tin nhắn `PEER_JOINED`.

## 2. Giai đoạn Chat P2P trực tiếp
Giả sử Alice muốn chat với Bob:
1. Giao diện của Alice đã có tên Bob trong danh sách (do Tracker cung cấp).
2. Khi Alice bấm chat với Bob, máy Alice đóng vai trò làm `ChatClient` tạo một kết nối mạng trực tiếp tới `ChatServer` đang chạy ngầm trên máy Bob.
3. Thông điệp kết nối đầu tiên được gửi đi: `CHAT|Alice|__CONNECT__` (để máy Bob biết đây là ai).
4. Sau đó, mỗi khi Alice hoặc Bob gõ tin nhắn, dữ liệu text được truyền trực tiếp qua lại thông qua đường ống (Socket) này. Tốc độ chat rất nhanh vì không có máy chủ trung gian.

## 3. Giai đoạn Chia sẻ File (Share File)
1. Khi Alice bấm "Thêm File" để chia sẻ, file đó được hệ thống âm thầm copy vào thư mục riêng biệt (VD: thư mục `shared_Alice/`).
2. Alice gửi tin nhắn `SHARE_FILE` lên Tracker báo cáo: *"Tôi tên Alice, đang có file Report.pdf, dung lượng 10MB"*.
3. Tracker thêm thông tin đó vào danh mục mục lục của nó. Nội dung file vẫn nằm hoàn toàn tại máy Alice.

## 4. Giai đoạn Tìm kiếm File (Search File)
1. Bob cần tìm file có chữ "Report". Bob nhập từ khóa vào ô tìm kiếm và bấm Tìm.
2. Máy Bob gửi thông điệp `SEARCH_FILE|Report` lên Tracker.
3. Tracker quét qua danh mục mục lục và thấy Alice đang giữ file đó.
4. Tracker gửi trả lại cho Bob tin nhắn `SEARCH_RESULT` chứa: Tên file, dung lượng, và *địa chỉ IP + Cổng FileServer của Alice*.

## 5. Giai đoạn Truyền File Trực tiếp (Download)
Khi Bob nháy đúp tải file của Alice từ bảng kết quả:
1. Máy Bob (`FileClient`) dùng địa chỉ IP lấy được để kết nối trực tiếp đến cổng `FileServer` trên máy Alice.
2. Bob gửi: `FILE_REQUEST|Report.pdf`.
3. Máy Alice kiểm tra xem trong thư mục chia sẻ có file đó thật không. Nếu có, trả lời: `FILE_ACCEPT` cùng dung lượng file.
4. Ngay lập tức, máy Alice đọc nội dung file từ ổ cứng, băm ra thành các khúc nhỏ 8KB và xả liên tục qua mạng.
5. Máy Bob nhận dữ liệu, ghi dần ra ổ cứng vào thư mục tải về của mình, đồng thời cập nhật thanh Progress Bar theo phần trăm.
6. Khi hoàn tất, máy Bob gửi `FILE_COMPLETE` cho Alice.

---

**Kết luận**: Nhờ mô hình này, Tracker Server dù có cấu hình yếu vẫn phục vụ được hàng ngàn người, vì băng thông nặng nề nhất (tải file) đã được giải quyết trực tiếp giữa các Peer (người dùng).
