# Firebase Emulator Starter Script for PowerShell
# Run this in PowerShell to start Firebase Emulator with correct Java version

# Set Java path to Android Studio JBR (Java 25)
$env:PATH = "C:\Program Files\Android\Android Studio\jbr\bin;$env:PATH"

# Verify Java version
Write-Host "Checking Java version..."
java -version

# Start Firebase Emulator
Write-Host ""
Write-Host "Starting Firebase Emulators..."
Write-Host ""

cd "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"
firebase emulators:start

# Keep window open if error
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error starting emulator!"
    Read-Host "Press Enter to exit"
}
