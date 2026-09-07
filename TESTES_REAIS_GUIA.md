# Testes Firestore Rules — Guia de Execução

## 🎯 Objetivo

Validar Firestore Rules contra **emulador real** (não simulado).
Resultados são 100% fiéis ao comportamento em production.

---

## ⚡ Passos Rápidos (15 min total)

### 1️⃣ Instalar Firebase CLI

**PowerShell (como Admin):**
```powershell
npm install -g firebase-tools
firebase --version
```

Aguarde até ver a versão (ex: `13.5.0`). Saia do terminal.

---

### 2️⃣ Iniciar Emulador Firebase

**PowerShell — Terminal 1 (deixar rodando):**

```powershell
cd C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota

firebase emulators:start --only firestore,auth
```

Aguarde aparecer:
```
✔ Firestore Emulator running at http://127.0.0.1:8080
✔ Authentication Emulator running at http://127.0.0.1:9099
✔ Emulator UI running at http://127.0.0.1:4000
```

**NÃO FECHE ESTE TERMINAL** — emulador roda aqui.

---

### 3️⃣ Compilar e Rodar App Android

**Android Studio — Terminal 2:**

```bash
cd C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota

# Compilar
.\gradlew.bat :app:assembleDebug

# Rodar em emulador/device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Ou via Android Studio:
# Run → Run 'app'
```

**Verificar conexão ao emulador** (Logcat):
```
✅ Firestore connected to emulator at 10.0.2.2:8080
✅ Auth connected to emulator at 10.0.2.2:9099
```

Se não aparecer → MinhaRotaApp.kt não foi modificado corretamente.

---

### 4️⃣ Rodar Testes

**Opção A: Testes em Kotlin (Recomendado)**

Android Studio:
```
View → Tool Windows → Logcat
  → Search: FIRESTORE_TEST
```

Terminal:
```bash
# Rodar testes unitários
.\gradlew.bat :app:testDebugUnitTest

# Resultado:
# BUILD SUCCESSFUL
```

**Opção B: Testes Integrados em Activity**

Se quiser rodar contra emulador real (mais lento, mais fiel):

1. Android Studio → abrir `FirestoreRulesTestActivity.kt`
2. Clique com botão direito → Run
3. Ler logs em **Logcat** (tag: `FIRESTORE_TEST`)

---

### 5️⃣ Ver Resultados em Tempo Real

**Browser — http://localhost:4000**

- **Firestore** → visualizar collections criadas pelo teste
- **Authentication** → ver usuários de teste criados
- **Console** → ver erros de regras em tempo real

---

## 📊 Esperado vs Realidade

| TEST | Regra Atual | Esperado | Resultado Real |
|------|------------|----------|----------------|
| 1. CREATE turno válido | ✅ ALLOW | ✅ ALLOW | ❓ Rodar teste |
| 2. CREATE sem id | ❌ DENY | ❌ DENY | ❓ Rodar teste |
| 3. UPDATE isPro | ❌ DENY | ❌ DENY | ❓ Rodar teste |
| 4. READ outro user | ❌ DENY | ❌ DENY | ❓ Rodar teste |
| 5. CREATE sem auth | ❌ DENY | ❌ DENY | ❓ Rodar teste |
| 6. DELETE próprio | ✅ ALLOW | ✅ ALLOW | ❓ Rodar teste |

---

## 🐛 Problemas Comuns

### Erro: "firebase: command not found"
→ Firebase CLI não instalou. Rodar:
```powershell
npm install -g firebase-tools
```

### Erro: "Firestore connected to PRODUCTION, not emulator"
→ MinhaRotaApp.kt não tem código de emulator.

**Fix:**
```kotlin
// Verificar se isso está em MinhaRotaApp.kt:
if (BuildConfig.DEBUG) {
    FirebaseFirestore.getInstance().firestoreSettings = settings
    FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
}
```

### Erro: "Failed to connect to emulator"
→ Terminal 1 (emulador) não está rodando.

**Fix:** Voltar ao Terminal 1 e rodar:
```powershell
firebase emulators:start --only firestore,auth
```

### App conecta a Firebase REAL em vez de emulador
→ App está em modo RELEASE ou não é DEBUG.

**Fix:**
```bash
# Garantir que é debug
.\gradlew.bat :app:assembleDebug

# Limpar dados antigos
adb shell pm clear com.raffastudioproducoes.minharota
```

---

## ✅ Checklist de Execução

- [ ] Firebase CLI instalado (`firebase --version`)
- [ ] Emulador rodando em Terminal 1 (portas 8080, 9099, 4000 livres)
- [ ] MinhaRotaApp.kt modificado com código emulator
- [ ] App compilado e instalado
- [ ] Logcat mostra "✅ Firestore connected to emulator"
- [ ] Testes rodados (unitários ou Activity)
- [ ] Resultados documentados em firebase/FIRESTORE_RULES_TEST_RESULTS.txt

---

## 📝 Documentar Resultados

Copiar logs do Logcat para arquivo:

```
firebase/FIRESTORE_RULES_TEST_RESULTS.txt

═══════════════════════════════════════════════════
FIRESTORE RULES TESTS — Results
═══════════════════════════════════════════════════

[TEST 1] CREATE Turno with valid data
  ✅ PASS — Turno criado com sucesso

[TEST 2] CREATE Turno WITHOUT id field (should DENY)
  ✅ PASS — Correctly DENIED: PERMISSION_DENIED

... (todos os 6 testes)

═══════════════════════════════════════════════════
RESULTS
═══════════════════════════════════════════════════
✅ PASSED: 5
❌ FAILED: 1
📊 TOTAL:  6

⚠️ 1 test failed — Check rules
═══════════════════════════════════════════════════
```

---

## 🎯 Depois dos Testes

### Se tudo passou ✅
1. Documentar achados
2. Publicar regras no Firebase Console
3. Fazer commit (sem push)

### Se algum falhou ❌
1. Revisar erro em browser (http://localhost:4000)
2. Corrigir regra em `firebase/firestore.rules`
3. Emulador reload automático
4. Rodar teste novamente

---

## 💡 Dica Final

**Deixar emulador rodando durante TODA sessão de desenvolvimento.**

Assim qualquer mudança em código/regras é testada em tempo real.

```bash
# Terminal 1: Emulador (deixar aberto)
firebase emulators:start --only firestore,auth

# Terminal 2: Desenvolvimento (Android Studio, testes, etc)
```

---

## 📁 Arquivos Preparados

- ✅ `MinhaRotaApp.kt` — conecta ao emulador
- ✅ `FirestoreRulesTestActivity.kt` — testes integrados
- ✅ `firebase.json` — config emulador
- ✅ `.firebaserc` — referência projeto
- ✅ `RUN_FIRESTORE_TESTS.bat` — script inicialização
- ✅ `FIRESTORE_RULES_TEST_CASES.md` — casos manuais
- ✅ `TESTES_REAIS_GUIA.md` — este arquivo

---

**Pronto para começar?**

Vá para Passo 1️⃣ (Instalar Firebase CLI) e siga a sequência.
