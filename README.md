# Movimento

Aplicação de movimentos manuais com **Java 25 (LTS), Spring Boot, Angular, H2, Hibernate e JWT**.

Permite consultar por mês/ano e incluir lançamentos com Produto e Cosif relacionados. O servidor gera número sequencial por competência, data/hora e usuário fixo `TESTE`. Após incluir, a tela atualiza a consulta e desabilita os campos.

## Aplicação online

Site: https://ao-jeferson.github.io/Java-Angular/

A interface é publicada no GitHub Pages e utiliza a API HTTPS hospedada em servidor Ubuntu próprio. A variável `API_URL` do repositório define o endereço da API. O workflow publica o site após os testes da `main` passarem.

## Requisitos

- Java 25 (LTS)
- Node.js 26 e npm
- Git

O Maven é baixado pelo wrapper incluído no projeto. No macOS com Homebrew, instale Java com `brew install openjdk@25`. O script de execução reconhece essa instalação automaticamente.

## Executar

Clone o repositório e instale as dependências do frontend:

```bash
git clone https://github.com/ao-jeferson/Java-Angular.git Movimento
cd Movimento
cd frontend
npm ci
cd ..
```

No primeiro terminal, execute a API:

```bash
bash run.sh backend
```

No segundo terminal, execute a interface:

```bash
bash run.sh frontend
```

Acesse **http://127.0.0.1:4200**.

Credenciais de demonstração para execução local:

| Campo | Valor |
| --- | --- |
| Usuário | `admin` |
| Senha | `Movimento@2026` |

O primeiro início da API cria um arquivo `.env` na raiz com essas credenciais. Para personalizar o acesso, altere `MOVIMENTO_USERNAME` e `MOVIMENTO_PASSWORD` nesse arquivo e reinicie a API. Um `.env` existente é preservado. As credenciais acima são exclusivas da demonstração local.

O banco H2 é criado em `backend/data/` ao usar o script e mantém os lançamentos após reiniciar. Produtos e Cosifs de demonstração são cadastrados automaticamente. Pare os processos com `Ctrl+C` em cada terminal.

## Backend em servidor próprio

O frontend fica no GitHub Pages; a API Java roda em um servidor Ubuntu próprio. A comunicação usa HTTPS através de Cloudflare Tunnel, e o banco H2 permanece em um volume persistente no servidor.

Para executar a API com Docker, configure o `.env` e rode na raiz:

```bash
docker compose up -d --build
```

A API escuta em `127.0.0.1:8083` por padrão. `MOVIMENTO_CORS_ORIGINS` deve conter a origem autorizada do frontend, por exemplo `https://ao-jeferson.github.io`. O túnel publica somente a API, sem expor portas adicionais do servidor.

## Testes

Com Java 25 disponível no terminal:

```bash
./mvnw verify
cd frontend
npm ci
npm test
npm run build
npx playwright install chromium
npm run test:e2e
```

No macOS, se necessário, configure o JDK antes dos testes:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

Os testes de navegador iniciam API e frontend próprios nas portas `18083` e `14200`, com banco em memória. Não precisam da aplicação local rodando. O relatório Java fica em `backend/target/site/jacoco/index.html`; o relatório de navegador em `frontend/playwright-report/index.html`.

## Estrutura

```text
backend/src/main/java/com/movimento/app/
  controller/    # Endpoints REST
  service/       # Regras e transações
  repository/    # Acesso ao banco
  model/         # Modelos e entidades JPA
  dto/           # Entrada e saída da API
  security/      # Validação de JWT e proteção de rotas
  config/        # Configuração da aplicação
  exception/     # Erros de negócio
frontend/src/app/
  pages/         # Login e movimentos
  components/    # Formulário, filtros e tabela
  services/      # Chamadas à API
  models/        # Tipos
  auth/          # Sessão, guard e interceptor
config-server/   # Spring Cloud Config, bônus opcional
```

## API

API: `http://127.0.0.1:8083`. Documentação interativa: `http://127.0.0.1:8083/swagger-ui/index.html`.

| Método | Rota | Uso |
| --- | --- | --- |
| POST | `/api/v1/auth/login` | Recebe `username` e `password`; retorna `accessToken` |
| GET | `/api/v1/produtos` | Produtos |
| GET | `/api/v1/produtos/{codigo}/cosifs` | Cosifs do produto |
| GET | `/api/v1/movimentos?mes=2&ano=2019` | Consulta por competência |
| POST | `/api/v1/movimentos` | Inclusão |

As rotas de negócio exigem `Authorization: Bearer <accessToken>`. Exemplo do corpo de inclusão:

```json
{
  "mes": 2,
  "ano": 2019,
  "codigoProduto": "P001",
  "codigoCosif": "11111111111",
  "descricao": "Lançamento de exemplo",
  "valor": "150.00"
}
```

Valores monetários e números de lançamento são strings no JSON para preservar precisão. A interface aceita vírgula ou ponto decimal, sem separador de milhar, e bloqueia letras e colagens inválidas. Mês e ano são selecionados em listas; os anos vão de 1900 até dez anos à frente do ano atual. São permitidos zero e valores negativos dentro dos limites do banco.

O JWT dura 15 minutos e fica somente na memória do navegador. Atualizar a página ou reiniciar a API exige novo login. A senha não é versionada; seu hash BCrypt é mantido em memória. O usuário autenticado controla o acesso, enquanto `COD_USUARIO` continua `TESTE`, conforme o enunciado.

O schema mantém as três tabelas e suas chaves compostas. `SMALLDATETIME` é representado por `TIMESTAMP` no H2. A numeração é protegida por transação e bloqueio no banco, com restrição única por mês/ano/número. As migrations são executadas pelo Flyway.

## Spring Cloud Config (bônus)

Inicie o servidor de configuração em outro terminal:

```bash
bash run.sh config
```

Inicie a API com o perfil cloud:

```bash
bash run.sh backend -Dspring-boot.run.profiles=cloud
```

Com um JWT válido, `GET /api/v1/info` retorna a mensagem carregada do Config Server. Esse perfil exige o servidor de configuração disponível; a execução padrão utiliza configuração local. Ambos usam as credenciais geradas no `.env`.

## GitHub Actions e releases

Cada push na `main` e cada pull request executam os testes Java, Angular e de navegador. Uma tag `v*` executa novamente as validações e publica uma release com os JARs e a interface compilada.

```bash
git tag v1.0.0
git push origin v1.0.0
```
