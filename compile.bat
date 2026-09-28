@echo off
if not exist bin mkdir bin
echo Dang bien dich source code...
cd src
javac -source 8 -target 8 -d ../bin -encoding UTF-8 common/*.java tracker/*.java peer/*.java gui/*.java
cd ..
echo Bien dich hoan tat. Chay run_tracker.bat va run_peer.bat de thu nghiem.
pause
