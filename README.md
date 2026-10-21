# Movimento

Aplicação para consultar e registrar movimentos manuais por mês e ano, relacionando produtos e códigos Cosif. Cada lançamento recebe um número sequencial dentro da competência.

## Aplicação online

Acesse: [Movimento](https://ao-jeferson.github.io/Java-Angular/#/login).

A interface está no GitHub Pages e a API HTTPS está hospedada em servidor próprio.

## Acesso

Credenciais de demonstração para o site publicado e a execução local:

| Campo | Valor |
| --- | --- |
| Usuário | `admin` |
| Senha | `Movimento@2026` |

Na execução local, o primeiro início da API cria um `.env` na raiz com essas credenciais. Para personalizar o acesso, altere `MOVIMENTO_USERNAME` e `MOVIMENTO_PASSWORD` e reinicie a API. Um `.env` existente é preservado.

## Linguagem ubíqua

Os termos abaixo são usados na interface, nas regras de negócio e na API.

| Termo | Significado no projeto |
| --- | --- |
| Movimento manual | Registro financeiro incluído pelo usuário. Também chamado de lançamento na interface. |
| Competência | Par formado por mês e ano ao qual o movimento pertence. É a base da consulta e da numeração. |
| Produto | Item do catálogo que identifica a categoria financeira do movimento. |
| Cosif | Código contábil do catálogo associado a um produto. A seleção depende do produto escolhido. |
| Código de classificação | Identificador exibido junto ao Cosif para distinguir sua classificação no catálogo. |
| Número de lançamento | Sequência gerada pelo sistema dentro de cada competência, independentemente do produto. |
| Descrição | Texto informado pelo usuário para explicar o movimento. |
| Valor | Quantia monetária do movimento, com até duas casas decimais; admite zero e valores negativos. |
| Data do movimento | Data e hora de registro, geradas pelo servidor. |
| Usuário autenticado | Pessoa que acessa a aplicação com suas credenciais e pode consultar e incluir movimentos. |
| Usuário do lançamento | Identificador gravado no movimento; nesta aplicação usa o valor fixo `TESTE`. |
| Inclusão | Operação que valida e grava um novo movimento e gera seu número. |
| Consulta por competência | Listagem dos movimentos do mês e ano selecionados. |

## Requisitos

- Java 25 (LTS)
- Node.js 26 e npm
- Git

O Maven é baixado pelo wrapper incluído no projeto. Configure `JAVA_HOME` para apontar para o JDK 25 e disponibilize `java` no `PATH`.

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

O banco H2 é criado em `backend/data/` ao usar o script e mantém os lançamentos após reiniciar. Produtos e Cosifs de demonstração são cadastrados automaticamente. Pare os processos com `Ctrl+C` em cada terminal.

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

Os testes de navegador iniciam API e frontend próprios nas portas `18083` e `14200`, com banco em memória. Não precisam da aplicação local rodando. O relatório Java fica em `backend/target/site/jacoco/index.html`; o relatório de navegador em `frontend/playwright-report/index.html`.

## Tecnologias

Java 25 (LTS), Spring Boot, Angular, H2, Hibernate e JWT. Flyway gerencia as migrations; Spring Cloud Config oferece configuração externa opcional.

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
config-server/   # Configuração externa opcional
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

O JWT dura 15 minutos e fica somente na memória do navegador. Atualizar a página ou reiniciar a API exige novo login. A senha não é versionada; seu hash BCrypt é mantido em memória. O usuário autenticado controla o acesso. O campo `COD_USUARIO` usa o valor fixo `TESTE`.

O schema mantém as três tabelas e suas chaves compostas. `SMALLDATETIME` é representado por `TIMESTAMP` no H2. A numeração é protegida por transação e bloqueio no banco, com restrição única por mês/ano/número. As migrations são executadas pelo Flyway.

## Backend em servidor próprio

O frontend fica no GitHub Pages; a API Java roda em um servidor próprio. A comunicação usa HTTPS através de Cloudflare Tunnel, e o banco H2 permanece em um volume persistente no servidor.

Para executar a API com Docker, configure o `.env` e rode na raiz:

```bash
docker compose up -d --build
```

A API escuta em `127.0.0.1:8083` por padrão. `MOVIMENTO_CORS_ORIGINS` deve conter a origem autorizada do frontend, por exemplo `https://ao-jeferson.github.io`. O túnel publica somente a API, sem expor portas adicionais do servidor.

## Spring Cloud Config

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

Para uma nova versão, crie e envie uma tag ainda não utilizada no formato `vMAJOR.MINOR.PATCH`.

A variável `API_URL` do repositório define o endereço HTTPS da API para o frontend. O workflow do GitHub Pages publica o site após os testes da `main` passarem.
