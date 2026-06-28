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
| `ANDROID_KEYSTORE_BASE64` | O conteúdo codificado em Base64 do arquivo `.keystore`. | *(Cole o conteúdo do arquivo `app/release.keystore.b64` gerado)* |
| `ANDROID_KEYSTORE_PASSWORD` | Senha do keystore de produção. | `MinhaRota@2025Prod!` |
| `ANDROID_KEY_ALIAS` | O "Alias" da chave dentro do keystore. | `minharota_alias` |
| `ANDROID_KEY_PASSWORD` | Senha da chave específica. | `MinhaRota@2025Prod!` |

## Como obter o Base64 do Keystore
Se precisar gerar o Base64 novamente em ambiente Linux/Mac, utilize o comando:
```bash
base64 -w 0 release.keystore > release.keystore.b64
```
No Windows (PowerShell):
```powershell
[convert]::ToBase64String((Get-Content -path "release.keystore" -Encoding byte)) > release.keystore.b64
```

> **Aviso de Segurança:** O arquivo físico `release.keystore` gerado localmente **não** deve ser commitado no repositório (ele deve ser incluído no `.gitignore`). Apenas a esteira de CI/CD utilizará o secret `ANDROID_KEYSTORE_BASE64` para decodificar e assinar o APK durante a compilação.
