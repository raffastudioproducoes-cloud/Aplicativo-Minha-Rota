@echo off
REM Script para rodar testes Firestore contra emulador real
REM Uso: Duplo clique neste arquivo OU execute no PowerShell

setlocal enabledelayedexpansion

echo.
echo ====================================
echo Firestore Rules - Testes Reais
echo ====================================
echo.

REM Verificar se Node/npm está instalado
echo [1/4] Verificando Firebase CLI...
firebase --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Firebase CLI nao encontrado
    echo.
    echo Instale com:
    echo   npm install -g firebase-tools
    echo.
    pause
    exit /b 1
)
echo ✅ Firebase CLI encontrado

REM Iniciar emulador em background
echo.
echo [2/4] Iniciando emulador Firebase...
echo Emulador vai rodar nesta janela. Mantenha aberto durante testes.
echo.
echo Pressione qualquer tecla para iniciar emulador...
pause

REM Mudar para diretório do projeto
cd /d "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"

REM Iniciar emulador (fica aqui)
firebase emulators:start --only firestore,auth

REM Se chegar aqui, emulador foi interrompido
echo.
echo Emulador foi interrompido. Testes finalizados.
pause
