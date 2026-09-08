# 🔧 Java Version Fix

Firebase CLI precisa Java 21+ (você tem Java 8)

## Solução Rápida

### Opção 1: PowerShell Script (Recomendado)
```powershell
cd "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"
.\START_EMULATOR.ps1
```

Automático — define Java correto e inicia emulator.

---

### Opção 2: Manual no PowerShell

```powershell
$env:PATH = "C:\Program Files\Android\Android Studio\jbr\bin;$env:PATH"
cd "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"
firebase emulators:start
```

---

### Opção 3: Bash
```bash
export PATH="/c/Program Files/Android/Android Studio/jbr/bin:$PATH"
cd "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"
firebase emulators:start
```

---

## ✅ Esperado

Quando rodar corretamente:
```
✔ Emulator Hub is running at localhost:4400
✔ Firestore Emulator running at 10.0.2.2:8080
✔ Authentication Emulator running at 10.0.2.2:9099
```

---

**Use: START_EMULATOR.ps1** ← Mais fácil! 🎉
