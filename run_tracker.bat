@echo off
:: Chuyển đến đúng thư mục chứa file bat này
cd /d "%~dp0"
echo Khoi dong Tracker Server...
java -cp bin tracker.TrackerServer
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Lỗi: Không thể chạy Java. Hãy chắc chắn bạn đã cài Java (JDK) và thêm vào PATH.
)
pause
