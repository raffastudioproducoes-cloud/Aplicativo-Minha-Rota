<div align="center">

# MinhaRota PRO

### Gestão financeira e operacional para quem vive de entregas.

[![Version](https://img.shields.io/badge/version-1.1.0--release-2563EB)](app/build.gradle.kts)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Android](https://img.shields.io/badge/Android-minSdk%2026-3DDC84?logo=android&logoColor=white)](app/build.gradle.kts)
[![Firebase](https://img.shields.io/badge/Firebase-Auth%20%2B%20Firestore-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![CI](https://img.shields.io/badge/GitHub%20Actions-release-2088FF?logo=githubactions&logoColor=white)](.github/workflows/android-build.yml)
[![License](https://img.shields.io/badge/license-proprietary-0F172A)](#licen%C3%A7a)

</div>

---

## Sumário

1. [Apresentação](#apresentação)
2. [Objetivo](#objetivo)
3. [Público-alvo](#público-alvo)
4. [Funcionalidades principais](#funcionalidades-principais)
5. [Tecnologias](#tecnologias)
6. [Arquitetura e estrutura](#arquitetura-e-estrutura)
7. [Configuração do ambiente](#configuração-do-ambiente)
8. [Desenvolvimento e testes](#desenvolvimento-e-testes)
9. [Build e distribuição](#build-e-distribuição)
10. [Segurança e privacidade](#segurança-e-privacidade)
11. [Assinaturas e IA](#assinaturas-e-ia)
12. [Documentação](#documentação)
13. [Roadmap](#roadmap)
14. [Licença](#licença)
15. [Contato](#contato)

---

## Apresentação

**MinhaRota PRO** é um aplicativo Android para controle financeiro e gestão da
rotina de motoristas e entregadores por aplicativo. O produto reúne turnos,
ganhos, custos, metas, contas e manutenção do veículo em uma experiência móvel
voltada à realidade de quem trabalha na rua.

Versão atual: **v1.1.0-release** · Idioma: **Português Brasileiro** · Plataforma: **Android**

## Objetivo

Transformar registros dispersos da rotina de entregas em informações úteis para
decisão, permitindo ao profissional:

- acompanhar ganhos brutos, custos e resultado líquido por turno;
- organizar reservas financeiras por objetivo;
- planejar contas, dívidas e metas diárias;
- controlar quilometragem e manutenção do veículo;
- analisar desempenho e identificar horários mais rentáveis;
- manter dados de perfil sincronizados quando autenticado.

## Público-alvo

- Motoboys e entregadores por aplicativo
- Motoristas de aplicativo
- Profissionais autônomos que trabalham por turnos
- Entregadores que utilizam moto, carro ou bicicleta
- Trabalhadores que precisam separar custos pessoais e operacionais

## Funcionalidades principais

| Módulo | Recursos |
| --- | --- |
| **Hoje** | Registro do turno, horas trabalhadas, ganhos, despesas e resultado líquido |
| **Caixinhas** | Distribuição dos ganhos em reservas financeiras por objetivo |
| **Contas e dívidas** | Organização de compromissos e cálculo de metas de pagamento |
| **Conta diária** | Acompanhamento dos valores necessários para cumprir objetivos financeiros |
| **Extrato** | Histórico consolidado de movimentações |
| **Garagem** | Quilometragem e controle de manutenção preventiva do veículo |
| **Gráficos** | Indicadores de desempenho, evolução dos ganhos e horários de maior retorno |
| **Perfil** | Dados pessoais permitidos e autenticação com Firebase |
| **Planos** | Apresentação dos planos Free, Premium e Pro; compras pagas ainda indisponíveis |
| **Ajuda** | Orientações de uso dentro do aplicativo |

## Tecnologias

- **Kotlin 2.4.10**
- **Jetpack Compose** com Material 3
- **Arquitetura MVVM**
- **SharedPreferences** com serialização JSON para persistência local
- **Firebase Authentication** e **Cloud Firestore**
- **Google Credential Manager** para autenticação
- **JUnit 4** para testes unitários
- **Gradle** com Android Gradle Plugin
- **GitHub Actions** para build de release assinado

## Arquitetura e estrutura

```text
Aplicativo-Minha-Rota/
├── .github/workflows/             # CI e geração do APK de release
├── app/
│   ├── build.gradle.kts           # configuração Android e assinatura por ambiente
│   └── src/
│       ├── main/java/com/raffastudioproducoes/minharota/
│       │   ├── data/local/        # persistência e migração local
│       │   ├── domain/            # modelos e políticas de domínio
│       │   ├── repository/        # acesso controlado a dados remotos
│       │   ├── services/          # serviços da aplicação
│       │   ├── ui/                # telas, componentes, navegação e tema
│       │   └── util/              # utilitários compartilhados
│       └── test/                  # testes unitários e regressões de segurança
├── gradle/wrapper/                # Gradle Wrapper versionado
├── INSTRUCOES_SECRETS_GITHUB.md   # configuração segura da CI
└── README.md
```

Princípios adotados:

- UI sem responsabilidade de conceder assinatura;
- regras sensíveis isoladas em políticas de domínio;
- perfil separado de permissões e dados de assinatura;
- segredos fornecidos apenas por variáveis de ambiente ou GitHub Actions Secrets;
- nenhuma chave privada ou credencial incorporada ao APK.

## Configuração do ambiente

Requisitos:

- Android Studio compatível com as versões declaradas no projeto;
- JDK 17;
- Android SDK configurado;
- `google-services.json` obtido no projeto Firebase correto.

Clone o repositório:

```bash
git clone https://github.com/raffastudioproducoes-cloud/Aplicativo-Minha-Rota.git
cd Aplicativo-Minha-Rota
```

Para desenvolvimento local, coloque temporariamente o `google-services.json`
em `app/google-services.json`. O arquivo é ignorado pelo Git e nunca deve ser
commitado. Remova cópias desnecessárias após o uso.

## Desenvolvimento e testes

No Linux ou macOS:

```bash
./gradlew test
./gradlew assembleDebug
```

No Windows PowerShell:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

Antes de qualquer publicação, execute também uma varredura de segredos e revise
o diff para garantir que arquivos locais não foram adicionados.

## Build e distribuição

O workflow [android-build.yml](.github/workflows/android-build.yml) executa o
build de release em pushes e pull requests direcionados à branch `main`.

Secrets obrigatórios:

| Secret | Finalidade |
| --- | --- |
| `GOOGLE_SERVICES_JSON` | Configuração Firebase codificada em Base64 |
| `ANDROID_KEYSTORE_BASE64` | Keystore de upload codificado em Base64 |
| `ANDROID_KEYSTORE_PASSWORD` | Senha do keystore |
| `ANDROID_KEY_ALIAS` | Alias da chave de upload |
| `ANDROID_KEY_PASSWORD` | Senha da chave |

O APK é produzido em `app/build/outputs/apk/release/app-release.apk` e publicado
como artefato privado do workflow. Consulte
[INSTRUCOES_SECRETS_GITHUB.md](INSTRUCOES_SECRETS_GITHUB.md) para a preparação
segura das credenciais.

## Segurança e privacidade

- Keystore, senhas, Base64 e `google-services.json` não são versionados.
- A assinatura do release não possui credenciais padrão no código.
- O workflow valida os cinco secrets antes de iniciar o build.
- Dados de assinatura não fazem parte do modelo público de perfil.
- Atualizações remotas de perfil passam por uma lista explícita de campos permitidos.
- Concessões antigas geradas pelo checkout simulado são removidas do armazenamento local.
- Dados operacionais não são migrados para a nuvem sem autorização validada por backend.
- Logs, documentação, issues e chats não devem conter credenciais ou dados pessoais sensíveis.

O aplicativo deverá evoluir de acordo com os princípios da **LGPD**, incluindo
minimização de dados, finalidade explícita, controle de acesso, exclusão de conta
e transparência sobre tratamento e retenção.

## Assinaturas e IA

Premium e Pro permanecem visíveis para apresentar a evolução planejada do
produto, mas estão marcados como **“Assinaturas em breve”**. O aplicativo não
simula pagamentos e não permite que o cliente conceda acesso pago a si próprio.

A IA também está temporariamente desativada. Ela somente deverá retornar por meio
de backend protegido, sem chave de provedor incorporada ao APK, com autenticação,
limites de uso, auditoria e proteção contra abuso. As compras deverão utilizar o
Google Play Billing com validação server-side antes de liberar qualquer direito.

## Documentação

| Documento | Conteúdo |
| --- | --- |
| [README](README.md) | Visão geral, arquitetura, operação, segurança e roadmap |
| [Secrets do GitHub](INSTRUCOES_SECRETS_GITHUB.md) | Preparação do Firebase, keystore e CI de release |

Este projeto adota **Documentação Viva**: uma funcionalidade não é considerada
concluída sem atualizar o README, a documentação técnica aplicável e o histórico
de mudanças quando ele existir.

## Roadmap

- Google Play Billing com verificação server-side
- Backend seguro para autorização de planos Premium e Pro
- IA via backend protegido, com limites e auditoria
- Sincronização segura e granular de dados operacionais
- Criptografia reforçada para dados locais sensíveis
- Backup e restauração controlados pelo usuário
- Testes instrumentados e ampliação da cobertura unitária
- Política de Privacidade e fluxos LGPD para publicação
- Distribuição pela Google Play com Play App Signing

## Licença

Software proprietário © Raffa Studio Produções — MinhaRota PRO.
Uso, cópia, modificação ou distribuição somente com autorização expressa do
titular.

## Contato

**Raffa Studio Produções**

E-mail: **contato.raffasp@gmail.com**

---

Desenvolvido para apoiar quem faz o Brasil se movimentar.
