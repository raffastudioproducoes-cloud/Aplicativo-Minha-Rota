# 🔧 Firebase Emulator — Portas Alternativas

Mudado para portas alternativas para evitar conflitos.

## ✅ Novas Portas

```
Firestore:   10.0.2.2:8180  (era 8080)
Auth:        10.0.2.2:9199  (era 9099)
Emulator UI: localhost:4100
Hub:         localhost:4500
```

## 📝 Arquivos Atualizados

- `firebase.json` ← Novas portas
- `MinhaRotaApp.kt` ← App agora aponta para 8180/9199

## 🚀 Rodar Emulator

PowerShell:
```powershell
cd "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"
$env:PATH = "C:\Program Files\Android\Android Studio\jbr\bin;$env:PATH"
firebase emulators:start
```

Bash:
```bash
export PATH="/c/Program Files/Android/Android Studio/jbr/bin:$PATH"
firebase emulators:start
```

## ✅ Esperado

```
✔ Firestore running at 0.0.0.0:8180
✔ Auth running at 0.0.0.0:9199
✔ Emulator Hub at localhost:4500
✔ Emulator UI at localhost:4100
```

## 🧪 Próximo

Terminal novo:
```powershell
./gradlew installDebug -q
adb shell am start -n com.raffastudioproducoes.minharota/.MainActivity
```

Depois: `TESTE_MANUAL_COMPLETO.md`

---

**App atualizado automaticamente para novas portas** ✓
