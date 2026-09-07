@echo off
REM Monitor Firestore Emulator in real-time
REM Run this in a separate terminal while testing

setlocal enabledelayedexpansion

echo.
echo ========================================
echo Firebase Firestore Monitor
echo ========================================
echo.
echo Monitoring Firestore Emulator at:
echo   http://localhost:4400/firestore
echo.
echo Firestore Collections:
echo   - usuarios/
echo   - usuarios/{uid}/turnos/
echo.

:loop
cls
echo ========================================
echo FIRESTORE MONITOR — %date% %time%
echo ========================================
echo.

echo [USUARIOS Collection]
call :query_collection usuarios

echo.
echo [TURNOS (all users)]
call :query_collection usuarios --recursive

echo.
echo Refreshing in 5 seconds...
echo (Press Ctrl+C to stop)
echo.

timeout /t 5 /nobreak
goto loop

:query_collection
echo Querying %1...
echo.
curl -s http://localhost:4400/firestore/_internal/query_serve ^
  -H "Content-Type: application/json" ^
  -d "{\"collectionId\":\"%1\", \"pageSize\": 10}" 2>nul | ^
  findstr /r /c:"\"fields\":" /c:"\"name\":" /c:"\"stringValue\":" /c:"\"doubleValue\":" || echo (No data)
echo.
exit /b

:end
echo Monitor stopped.
pause
