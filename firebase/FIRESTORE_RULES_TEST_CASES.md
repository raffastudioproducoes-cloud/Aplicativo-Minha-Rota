# Firestore Rules - Casos de Teste

## Setup Emulador

```bash
# Terminal 1: Iniciar emulador
firebase emulators:start --only firestore,auth

# Terminal 2: Conectar ao Emulador UI (browser)
# http://localhost:4000
```

## Conectar Android App ao Emulador

```kotlin
// Em MinhaRotaApp.kt, adicionar antes de inicializar Firebase:
if (BuildConfig.DEBUG) {
    val settings = FirebaseFirestoreSettings.Builder()
        .setHost("10.0.2.2:8080")  // Emulador no host (visto do emulator)
        .setSslEnabled(false)
        .setPersistenceEnabled(false)
        .build()
    FirebaseFirestore.getInstance().firestoreSettings = settings
    
    // Também conectar Auth ao emulador
    FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
}
```

---

## Teste 1: CREATE Turno - Válido

**Cenário:** Usuário autenticado cria turno com dados corretos

**Executar:**
```kotlin
// Em teste ou via app
val turno = mapOf(
    "id" to "turno-uuid-123",
    "data" to "15/09/2024",
    "horaInicio" to "08:00",
    "horaFim" to "17:00",
    "houvePausa" to true,
    "horaInicioPausa" to "12:00",
    "horaFimPausa" to "13:00",
    "ganhoBruto" to 150.50,
    "custoRua" to 25.00,
    "ganhoLiquido" to 125.50,
    "corridas" to emptyList<Any>()
)

val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
FirebaseFirestore.getInstance()
    .collection("usuarios")
    .document(uid)
    .collection("turnos")
    .document(turno["id"] as String)
    .set(turno)
    .addOnSuccessListener { Log.d("TEST", "✅ CREATE válido passou") }
    .addOnFailureListener { Log.e("TEST", "❌ FALHA inesperada: ${it.message}") }
```

**Esperado:** ✅ SUCCESS

---

## Teste 2: CREATE Turno - Sem ID

**Cenário:** Turno sem campo `id` (deve falhar)

**Executar:**
```kotlin
val turno = mapOf(
    // "id" to "...",  // FALTA ID
    "data" to "15/09/2024",
    "horaInicio" to "08:00",
    "horaFim" to "17:00",
    "ganhoBruto" to 150.50,
    "custoRua" to 25.00,
    "ganhoLiquido" to 125.50
)

val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
FirebaseFirestore.getInstance()
    .collection("usuarios")
    .document(uid)
    .collection("turnos")
    .document("any-id")
    .set(turno)
    .addOnFailureListener { 
        Log.d("TEST", "✅ CREATE sem id foi REJEITADO (esperado)")
    }
```

**Esperado:** ❌ PERMISSION_DENIED

---

## Teste 3: CREATE Turno com Valor Negativo

**Cenário:** ganhoBruto negativo (deve falhar com regra de validação futura)

**Executar:**
```kotlin
val turno = mapOf(
    "id" to "turno-uuid-456",
    "data" to "15/09/2024",
    "horaInicio" to "08:00",
    "horaFim" to "17:00",
    "ganhoBruto" to -50.00,  // NEGATIVO!
    "custoRua" to 25.00,
    "ganhoLiquido" to -75.00
)

val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
FirebaseFirestore.getInstance()
    .collection("usuarios")
    .document(uid)
    .collection("turnos")
    .document(turno["id"] as String)
    .set(turno)
    .addOnSuccessListener { 
        Log.w("TEST", "⚠️ CREATE com valor negativo PASSOU (BUG: deveria rejeitar)")
    }
    .addOnFailureListener { 
        Log.d("TEST", "✅ CREATE com valor negativo foi REJEITADO (correto)")
    }
```

**Esperado (após fix):** ❌ PERMISSION_DENIED

**Atual:** ✅ SUCCESS (FALHA na validação — precisa ser adicionada)

---

## Teste 4: UPDATE Usuario - Tentar Modificar isPro

**Cenário:** Usuário tenta setar `isPro: true` em update (deve falhar)

**Executar:**
```kotlin
val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
FirebaseFirestore.getInstance()
    .collection("usuarios")
    .document(uid)
    .update(mapOf(
        "isPro" to true,  // BLOQUEADO
        "nome" to "Novo Nome"
    ))
    .addOnFailureListener { 
        Log.d("TEST", "✅ UPDATE isPro foi REJEITADO (esperado)")
    }
```

**Esperado:** ❌ PERMISSION_DENIED

---

## Teste 5: CREATE Usuario - Sem Auth

**Cenário:** Requisição sem autenticação (deve falhar)

**Executar:**
```kotlin
val anon = FirebaseAuth.getInstance().signOut()
val usuario = mapOf(
    "nome" to "Teste",
    "email" to "teste@example.com",
    "isPro" to false
)

FirebaseFirestore.getInstance()
    .collection("usuarios")
    .document("random-uid")
    .set(usuario)
    .addOnFailureListener { 
        Log.d("TEST", "✅ CREATE sem auth foi REJEITADO (esperado)")
    }
```

**Esperado:** ❌ PERMISSION_DENIED

---

## Teste 6: READ Turno - Outro Usuário

**Cenário:** Usuário A tenta ler turno de Usuário B (deve falhar)

**Executar:**
```kotlin
// Login como Usuário A
val uidA = FirebaseAuth.getInstance().currentUser?.uid ?: return

// Tentar ler turno de outro usuário (UID B já existe no Firestore)
val uidB = "outro-usuario-uid-xyz"  // Diferente de uidA

FirebaseFirestore.getInstance()
    .collection("usuarios")
    .document(uidB)
    .collection("turnos")
    .document("turno-123")
    .get()
    .addOnFailureListener { 
        Log.d("TEST", "✅ READ de outro usuário foi REJEITADO (esperado)")
    }
```

**Esperado:** ❌ PERMISSION_DENIED

---

## Teste 7: DELETE Turno - Válido

**Cenário:** Usuário deleta seu próprio turno (deve passar)

**Executar:**
```kotlin
val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
FirebaseFirestore.getInstance()
    .collection("usuarios")
    .document(uid)
    .collection("turnos")
    .document("turno-uuid-123")
    .delete()
    .addOnSuccessListener { 
        Log.d("TEST", "✅ DELETE válido passou")
    }
```

**Esperado:** ✅ SUCCESS

---

## Teste 8: CREATE Usuario - Outro Usuário Tenta

**Cenário:** Usuário A cria perfil para Usuário B (deve falhar)

**Executar:**
```kotlin
// Login como Usuário A
val uidA = FirebaseAuth.getInstance().currentUser?.uid ?: return
val uidB = "usuario-b-xyz"

val usuario = mapOf(
    "nome" to "Usuário B",
    "email" to "b@example.com"
)

FirebaseFirestore.getInstance()
    .collection("usuarios")
    .document(uidB)  // Tentando criar para outro UID
    .set(usuario)
    .addOnFailureListener { 
        Log.d("TEST", "✅ CREATE para outro usuário foi REJEITADO (esperado)")
    }
```

**Esperado:** ❌ PERMISSION_DENIED

---

## Resumo de Resultados

| Teste | Esperado | Status Atual | Ação |
|-------|----------|--------------|------|
| 1. CREATE turno válido | ✅ | ? | Rodar teste |
| 2. CREATE sem id | ❌ | ? | Rodar teste |
| 3. CREATE valor negativo | ❌ | ✅ (BUG) | Adicionar validação |
| 4. UPDATE isPro | ❌ | ? | Rodar teste |
| 5. CREATE sem auth | ❌ | ? | Rodar teste |
| 6. READ outro usuário | ❌ | ? | Rodar teste |
| 7. DELETE válido | ✅ | ? | Rodar teste |
| 8. CREATE para outro uid | ❌ | ? | Rodar teste |

---

## Passos para Executar

### Via Emulador Local (Recomendado)

1. **Instalar Firebase CLI** (uma vez)
   ```bash
   npm install -g firebase-tools
   ```

2. **Iniciar Emulador**
   ```bash
   cd C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota
   firebase emulators:start --only firestore,auth
   ```
   
   Saída esperada:
   ```
   ✔ Firestore Emulator running at http://127.0.0.1:8080
   ✔ Authentication Emulator running at http://127.0.0.1:9099
   ✔ Emulator UI running at http://127.0.0.1:4000
   ```

3. **Conectar App Android**
   - Abrir Android Studio
   - Rodar app em emulador (AVD)
   - App automaticamente testará regras

4. **Monitorar Logs**
   ```bash
   adb logcat | grep TEST
   ```

5. **Visualizar Dados**
   - Abrir http://localhost:4000 no browser
   - Clicar em "Firestore" para ver collections/documents

---

## Próximas Steps

- [ ] Instalar Firebase CLI
- [ ] Iniciar emulador
- [ ] Executar testes 1-8
- [ ] Documentar resultados
- [ ] Criar PR com fixes (validação de valores, etc)
- [ ] Publicar regras no Firebase Console
