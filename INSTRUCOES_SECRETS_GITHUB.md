# ⚡ Diretrizes de Integração: Configuração de Assinatura Release no GitHub Actions

Para que o GitHub Actions consiga compilar a versão **Release** assinada do aplicativo **Minha Rota PRO** sem expor informações sensíveis publicamente, é obrigatório configurar variáveis secretas no repositório.

## Passo a Passo para Configuração

1. Acesse o seu repositório no GitHub: `raffastudioproducoes-cloud/Aplicativo-Minha-Rota`.
2. Vá até a aba **Settings** (Configurações).
3. No menu lateral esquerdo, expanda **Secrets and variables** e clique em **Actions**.
4. Clique no botão verde **New repository secret** para cada uma das chaves listadas abaixo.

## Chaves a serem configuradas

| Nome do Secret | Descrição | Valor |
|---|---|---|
| `GOOGLE_SERVICES_JSON` | Conteúdo Base64 do `google-services.json` baixado do Firebase. | Gerar localmente; nunca versionar o arquivo. |
| `ANDROID_KEYSTORE_BASE64` | Conteúdo Base64 do novo keystore de upload. | Gerar fora do repositório e guardar backup seguro. |
| `ANDROID_KEYSTORE_PASSWORD` | Senha nova e exclusiva do keystore de produção. | Nunca documentar ou reutilizar. |
| `ANDROID_KEY_ALIAS` | Alias da nova chave dentro do keystore. | Guardar somente como secret. |
| `ANDROID_KEY_PASSWORD` | Senha nova e exclusiva da chave. | Nunca documentar ou reutilizar. |

## Criar a nova chave de upload

Como o aplicativo ainda não foi publicado, crie uma chave inédita e não reutilize senhas já expostas:

```bash
keytool -genkeypair -v -keystore minha-rota-upload.jks -alias minha-rota-upload -keyalg RSA -keysize 4096 -validity 10000
```

O comando solicitará as senhas sem gravá-las no terminal. Guarde o arquivo e as senhas em dois backups seguros. Não crie o keystore dentro da pasta do projeto.

## Como obter os valores Base64

Prefira copiar o Base64 diretamente para a área de transferência, sem criar
arquivos intermediários. No Windows (PowerShell):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\caminho-seguro\minha-rota-upload.jks")) | Set-Clipboard
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\caminho-seguro\google-services.json")) | Set-Clipboard
```

Cadastre um secret por vez e limpe a área de transferência após cada operação:

```powershell
Set-Clipboard -Value ""
```

No Linux, use uma ferramenta de área de transferência disponível no ambiente e
evite imprimir o Base64 no terminal, gravá-lo no histórico ou criar um arquivo
`.b64`.

> **Aviso de Segurança:** keystore, senhas, arquivos Base64 e `google-services.json` nunca devem ser enviados ao repositório, anexados em issues ou compartilhados por chat. Apenas a esteira de CI/CD utiliza os secrets para preparar o build temporariamente.
