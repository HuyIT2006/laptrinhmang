@echo off
:: Chuyển đến đúng thư mục chứa file bat này
cd /d "%~dp0"
echo Khoi dong Tracker Server...
java -cp bin tracker.TrackerServer
pause
