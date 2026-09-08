@echo off
setlocal enabledelayedexpansion

set ANDROID_SDK=C:\Users\rafae\AppData\Local\Android\Sdk
set AVDMANAGER=%ANDROID_SDK%\cmdline-tools\latest\bin\avdmanager.bat
set EMULATOR=%ANDROID_SDK%\emulator\emulator.exe

echo ========================================
echo 1. Criando emulador Pixel_5_API_34
echo ========================================

REM Criar AVD não-interativo
echo y | %AVDMANAGER% create avd -n Pixel_5_API_34 -k "system-images;android;34;google_apis" -d "pixel_5" --force

echo.
echo ========================================
echo 2. Iniciando emulador
echo ========================================
echo Deixe aberto enquanto rodam os testes...
echo.

start %EMULATOR% -avd Pixel_5_API_34 -no-snapshot-load

echo.
echo ========================================
echo Aguardando 120 segundos para emulador inicializar...
echo ========================================

timeout /t 120

echo.
echo Emulador deve estar pronto agora!
echo.

pause
