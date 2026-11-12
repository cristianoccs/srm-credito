# SRM Credit Engine

Motor de precificação e liquidação auditável de recebíveis em BRL e USD, desenvolvido para o desafio técnico da SRM Asset em nível pleno.

## Status atual

O backend está implementado e executável. Ele contém cadastro de recebíveis, atualização manual de câmbio, simulação de preço sem efeito colateral, liquidação transacional idempotente, auditoria imutável, extrato paginado e documentação OpenAPI. O painel frontend será desenvolvido depois que o backend estiver estabilizado.

A suíte atual possui 8 testes automatizados: três golden cases, duas validações de borda, dois cenários de idempotência e um teste de contexto Spring/Flyway. A última execução passou com 0 falhas.

## Stack

A aplicação usa Java 21, Spring Boot 3.4, Spring Web, Bean Validation, Spring Data JPA, Flyway, PostgreSQL e Springdoc OpenAPI. H2 fica disponível para execução local rápida e testes; o Docker Compose usa PostgreSQL 16.

A arquitetura é um monólito modular em camadas. Controllers lidam com HTTP, services coordenam casos de uso e transações, o domínio contém o motor e as Strategies, e repositories isolam a persistência.

## Como executar localmente

Requisitos: JDK 21. Maven global não é necessário porque o Maven Wrapper está versionado.

```bash
./mvnw test
./mvnw spring-boot:run
```

A execução local usa H2 em memória por padrão e escuta em `http://localhost:3000`. O health check é `GET /actuator/health`, a documentação é `http://localhost:3000/swagger-ui.html` e o JSON OpenAPI é `GET /api-docs`.

## Como executar com PostgreSQL

Com Docker instalado:

```bash
docker compose up --build
```

A aplicação ficará em `http://localhost:3000` e o PostgreSQL em `localhost:5432`. As migrations Flyway são aplicadas automaticamente. O arquivo `.env.example` documenta as variáveis que podem ser sobrescritas.

## Endpoints principais

| Método | Rota | Finalidade |
|---|---|---|
| `POST` | `/api/receivables` | Cadastrar recebível em BRL |
| `GET` | `/api/receivables/{id}` | Consultar recebível e status |
| `POST` | `/api/exchange-rates` | Registrar taxa BRL/USD com vigência |
| `POST` | `/api/pricing/simulations` | Simular sem persistir liquidação |
| `POST` | `/api/settlements` | Liquidar com `Idempotency-Key` |
| `GET` | `/api/settlements` | Extrato paginado e filtrável |

O extrato aceita `from`, `to`, `assignor`, `currency`, `page` e `size`. O parâmetro `to` é inclusivo na data civil informada; internamente o limite superior é convertido para o início do dia seguinte em UTC.

## Exemplo de fluxo

Registrar a cotação:

```bash
curl -X POST http://localhost:3000/api/exchange-rates \
  -H 'Content-Type: application/json' \
  -d '{"baseCurrency":"BRL","quoteCurrency":"USD","rate":5.4321,"validFrom":"2026-01-01T00:00:00Z"}'
```

Criar uma duplicata de R$ 100.000,00 com prazo de 3 meses:

```bash
curl -X POST http://localhost:3000/api/receivables \
  -H 'Content-Type: application/json' \
  -d '{"assignor":"Cedente Demo","faceValue":100000,"termMonths":3,"type":"DUPLICATA_MERCANTIL"}'
```

Liquidar em USD substituindo `<id>` pelo identificador retornado:

```bash
curl -X POST http://localhost:3000/api/settlements \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: settlement-demo-001' \
  -d '{"receivableId":"<id>","paymentCurrency":"USD"}'
```

Repetir a requisição com a mesma chave retorna a liquidação existente sem criar uma segunda linha. Reutilizar a chave para outro recebível ou outra moeda retorna `409 Conflict`.

## Precisão financeira

Valores monetários e taxas são calculados com `BigDecimal`. O motor nunca usa `double` ou `float` para o domínio financeiro. O prazo é inteiro em meses, a taxa base é 1,00% ao mês e os spreads são 1,5% para Duplicata Mercantil e 2,5% para Cheque Pré-datado.

O arredondamento dos golden cases é `HALF_EVEN` em duas casas somente no resultado final em BRL. Para pagamento em USD, o valor presente em BRL é arredondado antes de ser dividido pela cotação BRL/USD, conforme o enunciado. A liquidação guarda o câmbio efetivamente usado, portanto o histórico não muda quando uma cotação nova é cadastrada.

## Golden cases

Os testes em `src/test/java/com/srm/creditengine/domain/pricing/PricingEngineTest.java` verificam:

- C1: Duplicata de R$ 100.000,00 por 3 meses → valor presente R$ 92.859,94 e deságio R$ 7.140,06.
- C2: Cheque de R$ 25.000,00 por 2 meses → valor presente R$ 23.337,77 e deságio R$ 1.662,23.
- C3: Duplicata de R$ 100.000,00 por 3 meses, câmbio 5,4321 → valor presente R$ 92.859,94 e pagamento US$ 17.094,67.

## Estrutura

```text
src/main/java/com/srm/creditengine/
├── config/                 # OpenAPI e propriedades
├── domain/                 # enums, exceções, Strategy e motor
├── application/            # DTOs e casos de uso
├── infrastructure/         # entidades JPA, repositories e projeção do extrato
└── interfaces/rest/         # controllers e erros HTTP
```

As decisões e premissas estão em `SPEC.md` e `DECISIONS.md`. O review reverso do trecho do Anexo A está em `REVIEW.md`, e a colaboração com IA está registrada em `AI_USAGE.md`.
