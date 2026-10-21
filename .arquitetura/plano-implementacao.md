# Plano de implementação - Movimentos Manuais

Data: 20/10/2026. Versão: 1. Estado: planejamento concluído; implementação pendente.

## 1. Objetivo e evidências

Construir a aplicação Java + Angular solicitada no desafio BNP Paribas & Antlia, cobrindo os requisitos obrigatórios, desejáveis e bônus. Escolhas do usuário: H2, Hibernate e testes.

Fonte: `Desafio Java - BNP Paribas & Antlia 2.pdf`, páginas 2 a 5. O documento foi lido por OCR e os diagramas de tela e dados foram inspecionados visualmente. A pasta contém somente o PDF; não há código, manifests ou projeto Git observado. As instruções fornecidas pelo usuário aplicam-se à execução.

Escopo desta etapa: produzir o plano. Não foram implementados serviços nem executados testes.

## 2. Matriz de requisitos

| ID | Origem | Requisito | Entrega / evidência prevista |
| --- | --- | --- | --- |
| R01 | Obrigatório, p. 2 | Angular 8 ou superior | Aplicação Angular com tela funcional |
| R02 | Obrigatório, p. 2 | Java 8 ou superior, Spring Boot, API REST | Serviço executável e contratos HTTP documentados |
| R03 | Obrigatório, p. 2 | Banco H2, Oracle ou SQL Server | H2 escolhido pelo usuário; persistência local em arquivo |
| R04 | Desejável, p. 2 | Microsserviço orientado a DDD | Serviço de movimentos autônomo, domínio e casos de uso separados |
| R05 | Desejável, p. 2 | ORM | Spring Data JPA + Hibernate |
| R06 | Bônus, p. 2 | Testes unitários | Regras de negócio, casos de uso e componentes Angular |
| R07 | Bônus, p. 2 | Componentes Spring Cloud | Config Server e cliente, com demonstração reproduzível |
| R08 | Obrigatório, p. 3 | Consulta de movimentos por mês e ano | GET filtrado; testes de isolamento entre competências |
| R09 | Obrigatório, p. 3 | Grid alimentado automaticamente por consulta ao banco | Consulta inicial, ao mudar competência válida e após inclusão |
| R10 | Obrigatório, p. 4a-b | Novo habilita digitação, incluindo mês/ano | Estados de consulta, edição e envio |
| R11 | Obrigatório, p. 4c | Combos Produto e Cosif dependentes | Produto mostra DES_PRODUTO; Cosif mostra COD_COSIF - (COD_CLASSIFICACAO) |
| R12 | Obrigatório, p. 4d | Número = último lançamento do mês/ano + 1 | Numeração calculada no servidor, compartilhada entre produtos |
| R13 | Obrigatório, p. 4e | Valor e descrição digitados | Formulário e validação no servidor |
| R14 | Obrigatório, p. 4f | Data/hora da gravação | Gerada no servidor usando relógio injetável |
| R15 | Obrigatório, p. 4g | Usuário fixo TESTE | Atribuído pelo servidor |
| R16 | Obrigatório, p. 4h | Após incluir, atualizar grid e desabilitar campos | Nova consulta ao banco e retorno ao estado de consulta |
| R17 | Obrigatório, p. 5 | Respeitar tabelas, tipos, chaves e cardinalidade | Migrations e testes reais de persistência |
| R18 | Permitido, p. 5 | Produtos/Cosifs previamente cadastrados | Carga demonstrativa com relacionamentos válidos |
| R19 | Protótipo, p. 3 | Botões Limpar, Novo, Incluir e colunas do grid | Interface reproduzindo o fluxo e a estrutura visual |
| R20 | Entrega e avaliação, p. 2 | Fonte, organização, manutenção e SOLID | README, decisões, testes e pacote/repositório pronto para entrega |

## 3. Stack e decisões

| Tema | Escolha proposta | Motivo |
| --- | --- | --- |
| Java | Java 21 | Atende ao mínimo do enunciado e permite uma base moderna |
| Backend | Spring Boot, Spring MVC, Bean Validation | REST, validação e injeção de dependências |
| Versões Spring | Release estável do Boot com release train Cloud compatível, fixadas em F01 | Conferir matriz oficial antes de gerar o projeto; usar BOMs |
| Build | Maven Wrapper | Comandos reproduzíveis no ambiente do avaliador |
| Persistência | Spring Data JPA e Hibernate gerenciados pelo Boot | ORM e transações; infraestrutura encapsulada |
| Banco | H2 em arquivo no perfil local; H2 em memória no perfil de testes | Demonstração mantém dados após reinício; testes isolados |
| Schema | Flyway; Hibernate ddl-auto=validate | Schema versionado e validação do mapeamento |
| Frontend | Angular em versão estável suportada, TypeScript estrito, Reactive Forms | Componentes pequenos e validação explícita |
| Versões frontend | Angular, Node, TypeScript e RxJS compatíveis, fixados em F01 | Registrar Node e manter package-lock.json |
| Testes | JUnit, Mockito, testes Spring/H2, suíte Angular e Playwright | Cobrir regras, banco, API e fluxo real no navegador |
| Arquitetura | Um serviço de negócio com DDD tático e portas/adaptadores | Fronteiras claras para o tamanho do desafio |
| Spring Cloud | Config Server separado e cliente no backend, perfil cloud | Bônus com uso observável de configuração externa |
| Documentação | OpenAPI compatível com Boot e README | Facilitar avaliação e reprodução |

O perfil local deve funcionar com configuração própria. O perfil cloud deve executar com Config Server, ter instruções e teste de integração; dependência adicionada sem uso não conclui R07. O Config Server é infraestrutura de apoio ao único serviço de negócio.

Fixar patches e conferir disponibilidade/compatibilidade na F01, inclusive Flyway, OpenAPI e ferramentas de testes. Fontes oficiais consultadas:

- https://docs.spring.io/spring-boot/system-requirements.html
- https://spring.io/projects/spring-cloud/
- https://angular.dev/reference/versions

## 4. Camadas e estrutura

```text
backend/
  src/main/java/.../movimentos/
    domain/                 regras, objetos de valor e portas de repositório
    application/            incluir/consultar movimentos; consultar catálogos
    infrastructure/
      persistence/          entidades JPA, chaves compostas e adaptadores
      configuration/        Spring, transações, Clock e perfis
    presentation/rest/      controllers, DTOs, mapeadores e tratamento de erros
  src/main/resources/db/migration/
frontend/
  src/app/core/             cliente HTTP, configuração e tratamento de erros
  src/app/features/movimentos/
    data-access/            serviços e tipos da API
    ui/                     formulário e grid
    pages/                  composição e estados da tela
config-server/              bônus Spring Cloud
docs/                       contratos, decisões e evidências
.arquitetura/               plano e checkpoints
```

Fluxo: Angular → controller REST → caso de uso → domínio/porta → adaptador JPA → H2.

O domínio não depende de HTTP, Spring ou JPA. A aplicação orquestra regras por interfaces; adaptadores implementam persistência. Entidades JPA não são respostas HTTP. Usar injeção por construtor, funções pequenas e mapeadores explícitos; criar interfaces para fronteiras reais. Transação abrange validação relacional, numeração e gravação. Testes de arquitetura verificam a direção das dependências.

## 5. Modelo relacional

Preservar nomes e chaves do diagrama, mesmo quando os exemplos visuais exibem códigos maiores que o tamanho definido no banco. Dados demonstrativos de produto terão até quatro caracteres.

| Tabela | Colunas e tipos do enunciado | Chaves |
| --- | --- | --- |
| PRODUTO | COD_PRODUTO CHAR(4); DES_PRODUTO VARCHAR(30); STA_STATUS CHAR(1) | PK COD_PRODUTO |
| PRODUTO_COSIF | COD_PRODUTO CHAR(4); COD_COSIF CHAR(11); COD_CLASSIFICACAO CHAR(6); STA_STATUS CHAR(1) | PK (COD_PRODUTO, COD_COSIF); FK para PRODUTO |
| MOVIMENTO_MANUAL | DAT_MES NUMERIC(2,0); DAT_ANO NUMERIC(4,0); NUM_LANCAMENTO NUMERIC(18,0); COD_PRODUTO CHAR(4); COD_COSIF CHAR(11); DES_DESCRICAO VARCHAR(50); DAT_MOVIMENTO SMALLDATETIME; COD_USUARIO VARCHAR(15); VAL_VALOR NUMERIC(18,2) | PK (DAT_MES, DAT_ANO, NUM_LANCAMENTO, COD_PRODUTO, COD_COSIF); FK composta para PRODUTO_COSIF |

No diagrama, descrição/status de produto e classificação/status de Cosif aceitam nulo; campos de movimento são obrigatórios. Preservar essa nulabilidade. Relacionamentos: Produto 1:N ProdutoCosif; ProdutoCosif 1:N MovimentoManual.

SMALLDATETIME é um tipo do modelo original: no H2 usar o equivalente TIMESTAMP, documentando a adaptação ao motor escolhido e o armazenamento de data/hora. Clock com zona configurada e formato de resposta documentado evitam dependência da zona do computador.

Usar BigDecimal para valores monetários e tipo Java capaz de representar NUMERIC(18,0) para lançamentos. Retornar número de lançamento como string no JSON para evitar perda de precisão no JavaScript. Valor monetário também será string decimal canônica no contrato, com conversão controlada no formulário; não calcular dinheiro com ponto flutuante.

### Numeração e concorrência

Primeiro lançamento da competência = 1; demais = MAX(NUM_LANCAMENTO) + 1, filtrando apenas mês e ano, independentemente de produto ou Cosif.

A PK composta do PDF permite repetir um número em produtos diferentes; a regra de negócio pede sequência única por competência. Proposta: preservar a PK original e acrescentar UNIQUE(DAT_MES, DAT_ANO, NUM_LANCAMENTO), documentado como reforço de integridade. Concorrência usa tentativa transacional de cálculo/inserção com flush e repetição limitada somente para colisão dessa constraint. Cada repetição começa nova transação e recalcula MAX, sem reutilizar transação marcada para rollback. Conflitos esgotados retornam erro controlado. Testar simultaneamente competência vazia e competência existente, inclusive entre produtos diferentes. Não usar synchronized como única proteção.

## 6. Contratos REST propostos

| Método e rota | Entrada | Resultado |
| --- | --- | --- |
| GET /api/v1/produtos | Sem parâmetros | Produtos e descrições |
| GET /api/v1/produtos/{codigo}/cosifs | Código de produto | Cosifs associados e classificações |
| GET /api/v1/movimentos?mes=2&ano=2019 | Competência obrigatória | Lista ordenada por lançamento, com produto, descrição do produto, descrição e valor |
| POST /api/v1/movimentos | mes, ano, codigoProduto, codigoCosif, descricao, valor | 201 com movimento persistido e campos gerados |

O cliente não determina usuário, data/hora ou número. Campos desconhecidos/gerados enviados no POST devem ser rejeitados com erro claro. Consulta vazia retorna 200 e lista vazia. Erros padronizados no formato Problem Details, com mensagens por campo: 400 para entrada inválida; 404 para catálogo inexistente; 422 para combinação produto/Cosif inválida; 409 para conflito de concorrência esgotado; 500 para falha inesperada sem stack trace público.

Validações: mês 1 a 12, ano 1 a 9999, códigos dentro dos comprimentos do schema, produto/Cosif existentes e relacionados, descrição não vazia com até 50 caracteres, valor dentro de NUMERIC(18,2) sem arredondamento silencioso. Repetir as validações pertinentes no frontend para usabilidade; servidor é autoridade.

## 7. Comportamento da tela e decisões sobre lacunas

1. Ao abrir: competência atual como filtro inicial e consulta automática; formulário de inclusão desabilitado.
2. Filtros de consulta de mês/ano permanecem disponíveis acima do grid. O formulário de inclusão mantém seus próprios mês/ano e os demais campos. Essa distinção permite consultar com o formulário desabilitado, como exige o enunciado.
3. Novo: habilita formulário, sugere competência selecionada, permite alterar mês/ano e coloca foco no primeiro campo.
4. Produto: carrega Cosifs do produto; ao trocar, limpa a seleção anterior. Cancelar requisição anterior/descartar resposta obsoleta para não mostrar Cosifs do produto errado.
5. Limpar: limpa campos de inclusão e mantém edição; sem apagar dados do banco.
6. Incluir: valida e envia uma vez; controles de envio bloqueados enquanto a requisição está em andamento.
7. Sucesso: selecionar competência incluída, fazer nova consulta ao banco e desabilitar formulário. Se a gravação tiver sucesso e a recarga falhar, informar os dois resultados e oferecer recarregar, sem reenviar a inclusão.
8. Erro de inclusão: preservar preenchimento e mostrar mensagem acionável. Exibir estados de carregamento, lista vazia e erro.
9. Grid: mês, ano, código do produto, descrição do produto, número do lançamento, descrição e valor; formatação pt-BR, labels acessíveis e navegação por teclado.

Suposições explícitas, pois o PDF não define essas regras: primeiro número 1; competência inicial atual; Limpar mantém edição; permitir zero e valores negativos que caibam no schema; não filtrar por STA_STATUS sem regra que defina seu significado. Não impor ano mínimo arbitrário. A carga demo usará status A, sem tratar isso como regra do enunciado.

## 8. Fases de execução e aceite

Todas as fases de implementação estão PENDENTES. Registrar comandos e resultados reais no checkpoint ao executar; comandos abaixo são sugeridos e ainda não foram executados.

| ID | Objetivo e trabalho | Pré-requisito | Arquivos / contratos | Validação e critério de aceite | Reversão |
| --- | --- | --- | --- | --- | --- |
| F01 | Inventariar JDK/Node/Maven/Git; fixar versões compatíveis; criar esqueletos backend/frontend, wrappers, perfis e estrutura de documentação | Plano | pom.xml, package.json, configurações de ferramentas, README | Ambos compilam/iniciam; versões e comandos registrados; sem banco externo obrigatório | Reverter somente arquivos da fase, preservando PDF e trabalho prévio |
| F02 | Criar migrations, carga demo, modelo de domínio e entidades JPA com chaves compostas; H2 arquivo/teste | F01 | migrations, domain, infrastructure/persistence | Schema validado pelo Hibernate; PK/FK/tipos/nulabilidade testados; dados locais sobrevivem reinício | Restaurar cópia do H2 antes de alterações; recriar apenas banco de teste descartável |
| F03 | Implementar casos de uso, numeração transacional, validações, controllers e erros | F02 | application, REST, DTOs, adaptadores | API insere e consulta por competência; rejeita vínculo inválido; testes de dinheiro, relógio e concorrência aprovados | Reverter código da fase e restaurar banco de demonstração quando necessário |
| F04 | Construir tela Angular, combos dependentes, filtros, grid, botões e estados | F03 e contrato estabilizado | feature movimentos, cliente HTTP | Fluxo completo no navegador; grid vem da API; sucesso desabilita campos; erros preservam formulário | Reverter arquivos da feature |
| F05 | Integrar Spring Cloud Config Server/Client e configuração externa demonstrativa | F03 | config-server, perfil cloud, BOM e documentação | Perfil cloud lê propriedade externa observável; cenário local continua executável; teste cobre indisponibilidade esperada no perfil cloud | Remover seleção do perfil cloud; reverter arquivos da fase |
| F06 | Completar testes de comportamento, arquitetura e ponta a ponta; relatório de cobertura e CI | F04-F05 | testes backend/frontend/E2E e workflow | Suítes e builds aprovados; rastreabilidade de R01-R20; regras críticas cobertas, incluindo concorrência real | Reverter apenas mudanças da fase; testes não alteram banco local |
| F07 | Preparar entrega, README, OpenAPI, decisões, instruções cloud e evidências | F06 | README, docs, scripts de execução e pacote | Reproduzir do zero seguindo README, executar testes e demonstrar requisitos; fonte pronta para ZIP/Git | Regerar pacote; publicação remota somente quando solicitada |

Cada fase inclui seus testes relevantes; F06 consolida lacunas e valida integração. Para cada mudança, executar somente os checks pertinentes e registrar falhas reais. Não avançar marcando aceite sem evidência.

## 9. Plano de testes

| Nível | Cenários obrigatórios de aceite |
| --- | --- |
| Domínio/unidade | Competência válida/inválida; descrição; precisão/escala de valor; primeiro lançamento e incremento; Clock fixo; usuário TESTE |
| Aplicação | Produto/Cosif inexistente e vínculo incompatível; campos gerados pelo servidor; falhas não persistem parcialmente |
| Persistência H2 | Chaves compostas, FK, UNIQUE da competência, limites do schema, BigDecimal exato, consulta ordenada sem misturar competências |
| Concorrência | Inserções simultâneas na mesma competência vazia/existente; produtos diferentes; nenhuma numeração repetida; falhas e retry limitado |
| REST | Status HTTP, validação, JSON, lista vazia, erros padronizados e rejeição de campos controlados pelo servidor |
| Angular | Novo/Limpar/Incluir; disabled/loading; troca de produto limpa Cosif; resposta HTTP obsoleta; validação e formato monetário |
| E2E | Abrir e consultar; incluir; confirmar grid atualizado e campos desabilitados; mudar competência; entrada inválida e falha de rede |
| Infraestrutura | Inicialização limpa, migrations, reinício com persistência, perfil de teste isolado e configuração Cloud consumida |
| Arquitetura | Domínio independente; controllers não acessam repositório JPA; JPA não vaza para DTOs |

Proposta de gate: ao menos 80% de cobertura de linhas/branches de domain e application, sem excluir regras críticas para atingir meta. Registrar relatórios reais; cobertura complementa cenários de negócio.

Comandos sugeridos: `./mvnw verify` no backend; `npm ci`, comando de testes não interativo e `npm run build` no frontend; `npx playwright test` para E2E. Scripts definitivos serão criados conforme a versão fixada. CI deverá usar os mesmos comandos documentados.

## 10. Entrega e checkpoint

README deverá conter requisitos de ambiente, comandos de execução/testes, portas, acesso local, reset seguro do H2, carga demo, exemplo de request, arquitetura, perfis local/test/cloud, configuração de zona, opções assumidas e matriz de requisitos. Console H2 restrito ao desenvolvimento. Logs sem dados sensíveis; health check e identificador de erro para diagnóstico. CORS limitado às origens de desenvolvimento necessárias.

Não há requisito de autenticação: COD_USUARIO permanece TESTE. Docker/Compose pode facilitar a demonstração do perfil cloud como melhoria de entrega; sua inclusão não substitui os comandos locais.

| Tarefa | Estado | Evidência | Próximo passo |
| --- | --- | --- | --- |
| P00 - Leitura do desafio e plano | CONCLUÍDA | PDF/OCR; inspeção visual das páginas 3 e 5; documento atual | Executar F01 quando solicitado |
| F01-F07 | PENDENTES | Nenhum código ou teste executado nesta etapa | Iniciar por F01 |

Comando de passagem sugerido:

> Execute a fase F01 de `.arquitetura/plano-implementacao.md`. Confira o estado atual e as instruções do projeto, preserve arquivos existentes, fixe versões compatíveis, prepare a estrutura, execute as validações da fase e atualize o checkpoint com evidências. Pare antes de F02.
