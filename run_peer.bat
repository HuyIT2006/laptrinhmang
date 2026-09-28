@echo off
:: Chuyển đến đúng thư mục chứa file bat này
cd /d "%~dp0"
echo Khoi dong Peer Client...
java -cp bin gui.MainFrame
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Lỗi: Không thể chạy Java. Hãy chắc chắn bạn đã cài Java (JDK) và thêm vào PATH.
)
pause
