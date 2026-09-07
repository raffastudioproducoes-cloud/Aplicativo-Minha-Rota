# 🎯 TESTE REAL — MinhaRota-PRO

Complete end-to-end test com dados reais sendo salvos em Firestore.

---

## ⚡ Quick Start (3 terminais)

### Terminal 1: Firebase Emulator
```bash
firebase emulator:start
```
Aguarde até ver:
```
✔ Firestore Emulator running at 10.0.2.2:8080
✔ Auth Emulator running at 10.0.2.2:9099
```

### Terminal 2: Build e Launch App
```bash
./gradlew installDebug -q
adb shell am start -n com.raffastudioproducoes.minharota/.MainActivity
```

### Terminal 3: Monitorar Firestore (opcional)
```bash
adb logcat -s "Firestore" -v short
```

---

## 🧪 Teste Completo

Siga o guia passo-a-passo em: **TESTE_MANUAL_COMPLETO.md**

### Resumo Rápido:
1. **Criar Conta** → Email: `teste-real@test.local` / Senha: `Teste@12345`
2. **Login** → Entra no dashboard
3. **Novo Turno** → Preenche dados de turno com corridas
4. **Ver em Firestore** → Dados aparecem no banco
5. **Editar** → Confirma `updatedAt` atualiza
6. **Testar Validações** → Tenta salvar valores negativos (é bloqueado)
7. **Testar XSS** → Injeta `<script>` (é escapado/sanitizado)
8. **Deletar** → Remove do banco

---

## 📊 Firestore Emulator UI

Abra em navegador:
```
http://localhost:4400/firestore
```

Ver dados em tempo real:
- usuarios/
- usuarios/{uid}/turnos/

---

## 📈 Validações a Testar

### Firestore Rules ✅
- [ ] Valores negativos bloqueados (ganhoBruto: -9999)
- [ ] Campo isPro protegido contra escrita
- [ ] Array corridas limitado a 100 items
- [ ] Timestamps obrigatórios (createdAt, updatedAt)

### XSS Prevention ✅
- [ ] `<script>alert('XSS')</script>` é escapado/removido
- [ ] Event handlers bloqueados
- [ ] HTML entities convertidas

### Network ✅
- [ ] Cleartext traffic bloqueado (exceto emulator)
- [ ] Certificados validados (production)

---

## 🔍 Monitorar Logs

**Firestore Emulator:**
```bash
adb logcat -s "Firestore" -d | grep -E "set|update|delete"
```

**Firebase:**
```bash
adb logcat -s "Firebase" -d | grep -E "Auth|Firestore"
```

**Network:**
```bash
adb logcat -s "OkHttp" -d | grep -i "certificatePinner"
```

---

## ✅ Checklist de Sucesso

- [ ] App inicia sem erro
- [ ] Criar conta funciona
- [ ] Login funciona
- [ ] Turno criado e salvo em Firestore
- [ ] Dados aparecem no banco com campos corretos
- [ ] Edição atualiza `updatedAt`
- [ ] XSS payload é sanitizado
- [ ] Valores negativos são bloqueados (Firestore Rules)
- [ ] isPro field é protegido
- [ ] Deletar remove do banco

**Se todos✅:** Pronto para produção! 🚀

---

## 📁 Arquivos Importantes

```
TESTE_MANUAL_COMPLETO.md    ← Guia passo-a-passo detalhado
MONITOR_FIRESTORE.bat       ← Script para monitorar Firestore
firebase/firestore.rules    ← Regras Firestore implementadas
SecurityUtils.kt            ← XSS sanitization
FirebaseHttpClient.kt       ← Certificate pinning
network_security_config.xml ← Network security
```

---

## 🐛 Troubleshooting

| Erro | Solução |
|------|---------|
| "Cleartext traffic blocked" | Network config já ativo ✓ |
| "Connection refused" | Verificar firebase emulator:start |
| "Auth timeout" | Problema dev-only, ver ISSUE_FIREBASE_AUTH_TIMEOUT.md |
| "PERMISSION_DENIED" | Firestore Rules estão bloqueando (verificar validação) |
| "Port 8080 in use" | firebase emulator:kill + emulator:start |

---

## 📞 Referências

- **Test Guide:** TESTE_MANUAL_COMPLETO.md (completo)
- **Implementation:** SUMMARY_IMPLEMENTACAO.md
- **Test Results:** RELATORIO_TESTES_FINAIS.md
- **PR Details:** PR_SECURITY_IMPLEMENTATION.md

---

**Tempo estimado:** 15-20 minutos

Bom teste! 🎉
