@echo off
:: Chuyển đến đúng thư mục chứa file bat này
cd /d "%~dp0"
echo Khoi dong Peer Client...
java -cp bin gui.MainFrame
pause
