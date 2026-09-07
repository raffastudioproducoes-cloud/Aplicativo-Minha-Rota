@echo off
REM Set Java 25 from Android Studio
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=C:\Program Files\Android\Android Studio\jbr\bin;%PATH%

REM Verify Java version
echo Verificando Java...
java -version

REM Start emulator
echo.
echo Iniciando emulador Firebase...
echo.

cd /d "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"
firebase emulators:start --only firestore,auth

pause
