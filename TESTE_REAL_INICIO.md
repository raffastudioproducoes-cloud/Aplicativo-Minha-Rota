# 🎯 TESTE REAL — INICIAR AQUI

## ⚡ Comando Correto para Emulator

```bash
cd "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"
firebase emulators:start
```

**NÃO:** `firebase emulator:start` (comando antigo)  
**SIM:** `firebase emulators:start` (comando novo)

---

## 🚀 Setup em 3 Terminais

### Terminal 1: Firebase Emulator Suite
```powershell
cd "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"
firebase emulators:start
```

**Aguarde até ver:**
```
✔ Emulator Hub is running at localhost:4400
✔ Firestore Emulator is running at 10.0.2.2:8080
✔ Authentication Emulator is running at 10.0.2.2:9099
```

---

### Terminal 2: Build e Launch App
```powershell
cd "C:\Users\rafae\Documents\Projetos\StudioProjects\Aplicativo-Minha-Rota"
./gradlew installDebug -q
adb shell am start -n com.raffastudioproducoes.minharota/.MainActivity
```

**Aguarde:**
- App compila
- Instala no emulador
- Abre a tela de login

---

### Terminal 3: Monitorar Logs (Opcional)
```powershell
adb logcat -s "Firestore,Firebase" -v short
```

Mostra em tempo real:
- Criação de usuários
- Escrita em Firestore
- Erros de validação

---

## 🧪 Próximo Passo

1. Tenha os 3 terminais abertos
2. Siga: **TESTE_MANUAL_COMPLETO.md** passo-a-passo
3. Veja dados sendo salvos em tempo real

---

## 📊 Firestore UI Emulator

Abra em navegador enquanto testa:
```
http://localhost:4400/firestore
```

Ver collections:
- usuarios/
- usuarios/{UID}/turnos/

---

## ✅ Sucesso quando:

- ✅ App inicia
- ✅ Criar conta funciona
- ✅ Login funciona
- ✅ Turno criado aparece em Firestore
- ✅ Dados são salvos corretamente
- ✅ Edição atualiza `updatedAt`
- ✅ XSS é sanitizado
- ✅ Valores negativos são bloqueados

---

**Tempo estimado:** 15-20 minutos

Boa sorte! 🎉
