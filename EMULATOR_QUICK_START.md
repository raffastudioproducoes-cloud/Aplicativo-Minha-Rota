# Firebase Emulator — Quick Start

## ⚡ Mais Rápido Possível

### Passo 1: Instalar Firebase CLI (1 vez, ~1 min)

```bash
# Windows (PowerShell como Admin)
npm install -g firebase-tools

# Verificar
firebase --version
```

### Passo 2: Iniciar Emulador (30 seg)

```bash
cd C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota

firebase emulators:start --only firestore,auth
```

**Saída esperada:**
```
✔ Firestore Emulator running at http://127.0.0.1:8080
✔ Authentication Emulator running at http://127.0.0.1:9099
✔ Emulator UI running at http://127.0.0.1:4000
```

**Deixar rodando** — vai esperar requisições.

---

### Passo 3: Conectar App Android ao Emulador

#### Opção A: Via Android Studio (mais fácil)

1. Abrir `app/src/main/java/com/raffastudioproducoes/minharota/MinhaRotaApp.kt`

2. Adicionar após `FirebaseApp.initializeApp(context)`:

```kotlin
if (BuildConfig.DEBUG) {
    // Emulator host — 10.0.2.2 é o IP do host visto do emulator
    try {
        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setHost("10.0.2.2:8080")
            .setSslEnabled(false)
            .setPersistenceEnabled(false)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings
        
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
        
        Log.d("Firebase", "✅ Emulator connected")
    } catch (e: Exception) {
        Log.e("Firebase", "❌ Emulator connection failed: ${e.message}")
    }
}
```

3. Sincronizar Gradle → Run no emulador Android

#### Opção B: Via Device/Emulator (sem código)

Se usar Firebase Console real (não local):
- App vai direto para Firebase Console (não testa regras locais)
- Pula este passo, vá para Passo 4

---

### Passo 4: Rodar Testes

#### Via Android Studio (Recomendado)

```
Run → Edit Configurations
  → Select "Android Tests" or "Unit Tests"
  → Click "FirestoreRulesTest"
  → Run
```

Ou direto no terminal:

```bash
# Testes unitários
.\gradlew.bat :app:testDebugUnitTest

# Testes instrumentados (em emulador real)
.\gradlew.bat :app:connectedDebugAndroidTest
```

#### Monitorar Logs

```bash
adb logcat | grep -i "firestore\|test\|auth"
```

---

### Passo 5: Visualizar Dados (Browser)

Abrir: **http://localhost:4000**

- Aba "Firestore" → ver collections, documents, regras aplicadas
- Aba "Authentication" → ver usuários criados
- Console → erros de regras em tempo real

---

## 🎯 Testes Manuais (Sem Código)

Direto no Emulator UI:

1. Abrir **http://localhost:4000**
2. Ir em "Firestore" → "Add Collection"
3. Criar: `usuarios/<uid>/turnos/<turno-id>`
4. Tentar operações:
   - ✅ Write com id → deve passar
   - ❌ Write sem id → deve falhar
   - ✅ Read próprio UID → deve passar
   - ❌ Read outro UID → deve falhar

---

## 📊 Checklist de Testes

Ao rodar emulador, validar:

- [ ] **TEST 1:** CREATE turno válido → ✅ PASS
- [ ] **TEST 2:** CREATE turno sem id → ❌ DENY
- [ ] **TEST 3:** CREATE turno valor negativo → ✅ PASS (BUG atual, deve ser ❌)
- [ ] **TEST 4:** UPDATE isPro → ❌ DENY
- [ ] **TEST 5:** CREATE sem auth → ❌ DENY
- [ ] **TEST 6:** READ outro usuario → ❌ DENY
- [ ] **TEST 7:** DELETE proprio turno → ✅ PASS
- [ ] **TEST 8:** CREATE usuario outro uid → ❌ DENY

---

## 🐛 Problemas Comuns

### "firestore: command not found"
→ Firebase CLI não instalado. Rodar: `npm install -g firebase-tools`

### "Failed to connect to Firestore Emulator"
→ Emulador não está rodando. Terminal 1 ainda tem `firebase emulators:start`?

### "Firestore Rules parse error"
→ Sintaxe de rules inválida. Ver error no Emulator UI console.

### "App tenta conectar a Firebase real, não emulador"
→ Código de emulator connection não foi adicionado em MinhaRotaApp.kt

---

## ✅ Próximas Steps

1. Instalar Firebase CLI
2. Iniciar emulador
3. Rodar testes (FirestoreRulesTest.kt)
4. Validar resultados via http://localhost:4000
5. Documentar achados
6. **Criar versão melhorada das regras** com validações
7. Testar versão corrigida
8. Publicar no Firebase Console (com autorização)

---

## 📁 Arquivos Criados

- `firebase.json` — configuração emulador
- `.firebaserc` — projeto reference
- `firebase/FIRESTORE_RULES_TEST_CASES.md` — casos de teste
- `app/src/test/java/.../FirestoreRulesTest.kt` — testes unitários
- `EMULATOR_QUICK_START.md` — este arquivo

---

## 💡 Dica Final

**Deixar emulador rodando em um terminal separado durante toda a sessão de desenvolvimento.**  
Assim qualquer mudança em código é testada em tempo real contra as regras.

```bash
# Terminal 1: Emulador roda aqui continuamente
firebase emulators:start --only firestore,auth

# Terminal 2: Android Studio, testes, etc
```
