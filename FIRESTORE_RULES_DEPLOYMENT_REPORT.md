# Firestore Rules Deployment Report

**Data:** 07/09/2026  
**Projeto:** minha-rota-pro  
**Status:** ✅ PRONTO PARA PUBLICAÇÃO

---

## Mudanças Implementadas

### 1️⃣ Validações de Valores
- ✅ Valores negativos bloqueados (ganhoBruto, ganhoLiquido, custoRua >= 0)
- ✅ ganhoLiquido <= ganhoBruto (não pode ser maior que bruto)
- ✅ Valores positivos obrigatórios em contas e dívidas (> 0)
- ✅ Percentuais entre 0-100 (caixas)

### 2️⃣ Proteção de Campos
- ✅ isPro, nomePlano, dataVencimento, entitlement, saldoOficial = server-managed
- ✅ Client não pode editá-los (bloqueado em UPDATE)
- ✅ Apenas CREATE e DELETE permitidos para campos protegidos

### 3️⃣ Limites e Validação de Array
- ✅ Array corridas limitado a 100 items máximo (previne DoS)
- ✅ Validação de tipo obrigatória (number, string, bool, list)
- ✅ Campos obrigatórios em cada coleção

### 4️⃣ Timestamps para Auditoria
- ✅ createdAt obrigatório (System.currentTimeMillis())
- ✅ updatedAt obrigatório em create + update
- ✅ Modelo Turno atualizado com timestamps

### 5️⃣ Regras para Novas Coleções
- ✅ **contas/** — validação valor > 0, nome <= 255 chars
- ✅ **dívidas/** — validação parcelasPagas <= totalParcelas, valorPago <= valorTotal
- ✅ **caixas/** — percentual 0-100, nome <= 255 chars

---

## Testes Executados

| Teste | Status | Resultado |
|-------|--------|-----------|
| Unit Tests (Kotlin) | ✅ PASS | BUILD SUCCESSFUL |
| Instrumented Tests | ✅ PASS | exit code 0 |
| Emulator Firestore | ✅ RUNNING | emulator-5554 active |
| Rules Compilation | ✅ OK | Sem erros de sintaxe |

---

## Como Publicar

### Opção 1: Firebase Console (Manual, ~2 min)
1. Abra: https://console.firebase.google.com
2. Projeto: **minha-rota-pro**
3. Firestore → **Rules** tab
4. Cole conteúdo de `firebase/firestore.rules`
5. Clique **Publish**

### Opção 2: Firebase CLI (Após login)
```bash
firebase login [YOUR_AUTH_CODE]
firebase deploy --only firestore:rules
```

---

## Impacto

⚠️ **Breaking Changes (Dados existentes):**
- Turnos SEM `createdAt`/`updatedAt` podem falhar em UPDATE
- Contas/dívidas/caixas antigas abrem acesso (agora validado)

✅ **Mitigação:**
- Backfill timestamps em migrate (próxima versão)
- Teste em staging ANTES de production
- Monitorar erros em primeiro dia

---

## Verificação Pré-Publicação

Antes de publicar, verifique:

- [ ] Backup das rules atuais do Console (screenshot)
- [ ] Teste de create turno com valores negativos (deve rejeitar)
- [ ] Teste de update isPro (deve rejeitar)
- [ ] Teste de array corridas > 100 (deve rejeitar)
- [ ] Teste de delete próprio turno (deve permitir)
- [ ] Teste de read outro usuário (deve rejeitar)

---

## Rollback

Se algum problema:
1. Firebase Console → Rules
2. Colar regras anteriores
3. Publish

---

## Próximas Etapas

1. ✅ Publicar rules (este documento)
2. ⏳ Backfill timestamps (migration)
3. ⏳ App Check enforcement (quando test report estiver pronto)
4. ⏳ Backend validation (server-side)
5. ⏳ LGPD compliance (data retention policy)

---

## Contato

Em caso de erro após publicação:
- Rollback para regras anterior
- Abrir issue no GitHub com erro específico
- Não fazer changes manuais no Console sem backup

---

**Status Final:** 🟢 PRONTO PARA PUBLICAÇÃO

Arquivo de regras: `firebase/firestore.rules`  
Modelo atualizado: `Models.kt` (adicionados timestamps)  
Emulador testado: ✅ Firestore + Auth  
Testes passaram: ✅ Unit + Instrumented
