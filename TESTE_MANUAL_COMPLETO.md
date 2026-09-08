# 🧪 TESTE MANUAL COMPLETO — MinhaRota-PRO

**Data:** 07/09/2026  
**Objetivo:** Validar fluxo real da aplicação com dados sendo salvos em Firestore  
**Duração Estimada:** 15-20 minutos

---

## 🚀 PRÉ-REQUISITOS

### Terminal 1: Firebase Emulator
```bash
cd "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"
firebase emulator:start
```

Aguarde até ver:
```
✔  Emulator Hub is running at localhost:4400
✔  Firestore Emulator is running at 10.0.2.2:8080
✔  Authentication Emulator is running at 10.0.2.2:9099
```

### Terminal 2: Build e Launch App
```bash
cd "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"
./gradlew installDebug -q
adb shell am start -n com.raffastudioproducoes.minharota/.MainActivity
```

### Terminal 3: Monitorar Firestore (outro terminal)
```bash
adb logcat -s "Firestore,Firebase" -v short
```

---

## 📱 FLUXO DE TESTE

### FASE 1: Criar Conta (2-3 minutos)

**Ação no App:**
1. Abrir app no emulador Android
2. Tela inicial → Botão "Criar Conta" ou "Sign Up"
3. Preencher:
   - Email: `teste-realuser@test.local`
   - Senha: `Teste@12345`
   - Confirmar senha: `Teste@12345`
4. Clicar "Criar Conta"

**Esperado:**
- ✅ Tela carrega
- ✅ Sem erro de email duplicado
- ✅ Redireciona para login ou perfil
- ❌ Nenhuma rejeição

**Monitorar:**
```
adb logcat -s "Firebase" -d | grep -i "createUserWithEmailAndPassword\|onSuccess\|onFailure"
```

---

### FASE 2: Login com Conta Criada (2-3 minutos)

**Ação no App:**
1. Tela de login
2. Email: `teste-realuser@test.local`
3. Senha: `Teste@12345`
4. Clicar "Login"

**Esperado:**
- ✅ Auth sucesso
- ✅ Redireciona para dashboard/home
- ✅ Mostra informações do usuário

**Monitorar:**
```
adb logcat -s "Firebase" -d | grep -i "signInWithEmailAndPassword\|onSuccess"
```

---

### FASE 3: Criar Turno (3-5 minutos)

**Ação no App:**
1. Dashboard → Botão "Novo Turno" ou "+"
2. Preencher dados:
   - Data: 07/09/2024
   - Hora Início: 08:00
   - Hora Fim: 17:00
   - Ganho Bruto: 250.50
   - Custo Rua: 50.00
   - Ganho Líquido: 200.50

3. Adicionar corridas:
   - Corrida 1: Uber, R$ 50.00
   - Corrida 2: 99Taxi, R$ 75.00
   - Corrida 3: Uber, R$ 75.50

4. Clicar "Salvar Turno"

**Esperado:**
- ✅ Turno criado
- ✅ Tela retorna à lista
- ✅ Novo turno aparece na lista
- ❌ Nenhum erro de validação

**Verificar Firestore:**
```bash
# Terminal separado
curl -X POST http://localhost:4400/firestore/_internal/query_serve \
  -H "Content-Type: application/json" \
  -d '{"collectionId":"turnos"}'
```

Ou via Firebase Emulator UI:
```
http://localhost:4400/firestore
```

**Esperado em Firestore:**
```json
{
  "id": "turno-XXXXX",
  "data": "07/09/2024",
  "horaInicio": "08:00",
  "horaFim": "17:00",
  "ganhoBruto": 250.50,
  "custoRua": 50.00,
  "ganhoLiquido": 200.50,
  "corridas": [
    {"id": "ride_1", "valor": 50.00, "app": "Uber"},
    {"id": "ride_2", "valor": 75.00, "app": "99Taxi"},
    {"id": "ride_3", "valor": 75.50, "app": "Uber"}
  ],
  "createdAt": 1725868234567,
  "updatedAt": 1725868234567
}
```

**Logcat:**
```
adb logcat -s "Firestore" -d | grep -i "DocumentSnapshot\|writeTransaction\|set\|batch"
```

---

### FASE 4: Testar Validações (Firestore Rules)

#### TESTE 4.1: Tentar salvar valores negativos
**Ação:** Editar turno, mudar:
- Ganho Bruto: `-9999`
- Clicar "Salvar"

**Esperado:**
- ❌ **BLOQUEADO por Firestore Rules**
- Erro na tela: "Valores não podem ser negativos"

**Logcat:**
```
adb logcat -s "Firestore" -d | grep -i "PERMISSION_DENIED\|validation"
```

---

#### TESTE 4.2: Tentar mudar isPro (protegido)
**Ação:** Via Firestore Emulator UI:
- Abrir: `http://localhost:4400/firestore`
- Editar usuário document
- Tentar adicionar: `"isPro": true`
- Salvar

**Esperado:**
- ❌ **BLOQUEADO por Firestore Rules**
- Erro: "Você não pode modificar este campo"

---

#### TESTE 4.3: Array DoS (limite corridas)
**Ação:** Editar turno, adicionar 100+ corridas

**Esperado:**
- ❌ **BLOQUEADO por regra: max 100**
- Erro: "Máximo 100 corridas permitidas"

---

### FASE 5: Editar Turno (2-3 minutos)

**Ação no App:**
1. Lista de turnos
2. Clicar no turno criado
3. Editar:
   - Ganho Bruto: 300.00
   - Ganho Líquido: 250.00
4. Salvar

**Esperado:**
- ✅ Valores atualizados
- ✅ `updatedAt` timestamp atualiza
- ✅ Sem erro

**Verificar Firestore:**
- Campo `updatedAt` deve ser novo timestamp
- `ganhoBruto` agora é 300.00

---

### FASE 6: Testar XSS em Entrada (2-3 minutos)

**Ação no App:**
1. Editar turno
2. Campo "Notas" ou "Descrição" (se existir)
3. Colar payload XSS:
```
<script>alert('XSS')</script>
```

**Esperado:**
- ✅ Input aceita
- ✅ Ao salvar: `<script>` é removido/escapado
- ✅ Em Firestore: dados sanitizados
- ❌ Nenhum alert JavaScript

**Verificar Firestore:**
```json
{
  "notas": "&lt;script&gt;alert('XSS')&lt;/script&gt;"  // Escapado
}
```

---

### FASE 7: Testar Certificate Pinning (Network)

**Ação:** Observar logs de conexão

**Esperado:**
```
✅ OkHttpClient connects to firestore.googleapis.com
✅ Certificate pinning verified
✅ SHA-256 match success
❌ Nenhuma advertência de certificado inválido
```

**Logcat:**
```
adb logcat -s "OkHttp,Network" -d | grep -i "CertificatePinner\|pin"
```

---

### FASE 8: Deletar Turno (1-2 minutos)

**Ação no App:**
1. Turno → Opção "Deletar"
2. Confirmar

**Esperado:**
- ✅ Turno removido da lista
- ✅ Firestore document deletado
- ❌ Nenhum erro

**Verificar Firestore:**
- Document não existe mais

---

### FASE 9: Logout e Verificação de Sessão

**Ação no App:**
1. Menu → Logout
2. Verificar se volta para login

**Esperado:**
- ✅ Sessão encerrada
- ✅ Token cleared
- ✅ Sem dados de usuário visíveis

---

## 📊 CHECKLIST DE VALIDAÇÃO

| Item | Esperado | Status |
|------|----------|--------|
| Criar conta sem erro | ✅ | ☐ |
| Login funciona | ✅ | ☐ |
| Turno criado em Firestore | ✅ | ☐ |
| Dados corretos no banco | ✅ | ☐ |
| Edição atualiza updatedAt | ✅ | ☐ |
| XSS bloqueado/sanitizado | ✅ | ☐ |
| Valores negativos bloqueados | ❌ (Firestore Rules) | ☐ |
| isPro field protegido | ❌ (Firestore Rules) | ☐ |
| Array DoS bloqueado | ❌ (Firestore Rules) | ☐ |
| Delete remove do banco | ✅ | ☐ |
| Logout limpa dados | ✅ | ☐ |

---

## 🔍 COMO MONITORAR FIRESTORE

### Opção 1: Firebase Emulator UI
```
http://localhost:4400/firestore
```
- Ver collections em tempo real
- Clicar em usuário → turnos
- Ver documentos sendo criados

### Opção 2: Logcat
```bash
adb logcat -s "Firestore" -v short | grep -i "set\|update\|delete"
```

### Opção 3: Firebase CLI
```bash
firebase firestore:list users --emulator
firebase firestore:list users/UID/turnos --emulator
```

---

## 🐛 Se Algo Falhar

### Erro: "Cleartext traffic blocked"
```
✓ Network Security Config já implementada
✓ Cleartext permitido apenas para 10.0.2.2
✓ Deve funcionar
```

### Erro: "Connection refused"
```
Verificar:
1. Emulator rodando? → firebase emulator:start
2. Port 8080 aberta? → netstat -an | grep 8080
3. ADB bridge ok? → adb shell netstat | grep 10.0.2.2:8080
```

### Erro: "PERMISSION_DENIED"
```
✓ Firestore Rules estão bloqueando
✓ Verificar logcat para detalhes
✓ Tente com dados válidos
```

### Erro: Auth não responde
```
✓ Problema conhecido (dev-only)
✓ Ver: ISSUE_FIREBASE_AUTH_TIMEOUT.md
✓ Aumentar timeout no QuickAuthTestActivity.kt
```

---

## 📸 Screenshots Esperados

### Tela 1: Login
```
[Campo Email]
[Campo Senha]
[Botão Login]
```

### Tela 2: Dashboard
```
[Foto/Info Usuário]
[Botão "Novo Turno"]
[Lista de Turnos Históricos]
```

### Tela 3: Novo Turno
```
[Data Picker: 07/09/2024]
[Hora Inicio: 08:00]
[Hora Fim: 17:00]
[Ganho Bruto: 250.50]
[Corridas: 3 items]
[Botão Salvar]
```

### Tela 4: Detalhe Turno
```
[Data: 07/09/2024]
[Horário: 08:00-17:00]
[Ganho: R$ 250.50 → R$ 200.50]
[Corridas: Uber (R$ 50), 99Taxi (R$ 75), Uber (R$ 75.50)]
[Botões: Editar, Deletar]
```

---

## ✅ SUCESSO!

Se todos os testes passarem:
- ✅ App funciona end-to-end
- ✅ Firestore Rules validam dados
- ✅ XSS Prevention funciona
- ✅ Network security ativo
- ✅ Pronto para produção

---

## 📝 Notas

- Salve logs para referência: `adb logcat > test_log.txt`
- Firestore Emulator UI persiste dados durante sessão
- Dados resetam quando emulator reinicia
- Use sempre email único para cada teste

**Tempo total estimado: 15-20 minutos**

Boa sorte com o teste! 🎉
