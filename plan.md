# Plano técnico — SRM Credit Engine

## Objetivo

Construir uma plataforma de cessão de crédito multimoedas para a operação da SRM Asset. A primeira fase de implementação será o backend, que concentra os critérios de maior peso para uma vaga de desenvolvedor pleno: corretude do motor financeiro, precisão decimal, persistência relacional, liquidação transacional, idempotência, auditabilidade, API REST e testes.

A solução será um monólito modular, adequado ao esforço-alvo do desafio. Não serão introduzidos microsserviços, eventos distribuídos ou camadas sem responsabilidade real.

## Escopo de implementação

### Fase atual — backend

- Java 21 e Spring Boot.
- API REST com documentação OpenAPI/Swagger.
- PostgreSQL como banco relacional e migrations versionadas.
- Recebíveis com cedente, valor de face em BRL, tipo, prazo e status.
- Tipos de recebível implementados pelo padrão Strategy:
  - Duplicata Mercantil: spread mensal de 1,5%.
  - Cheque Pré-datado: spread mensal de 2,5%.
- Motor com taxa base mensal configurável, juros compostos e `BigDecimal`.
- Simulação sem efeito colateral.
- Gestão de taxas de câmbio BRL/USD com vigência.
- Liquidação com transação ACID, idempotência por `Idempotency-Key` e registro imutável dos valores usados.
- Tratamento global de erros e validação de entrada.
- Extrato paginado com filtros por período, cedente e moeda.
- Testes dos três golden cases, de bordas, arredondamento e idempotência.

### Fase posterior — frontend

O painel de operador será servido pelo próprio Spring Boot como recursos estáticos, sem introduzir um segundo toolchain. A interface terá formulário de simulação em tempo real, resultado monetário destacado e grid de liquidações paginado no servidor com filtros por cedente, moeda e período. A interface consumirá apenas a API pública do backend; regras financeiras permanecerão no domínio do servidor.

## Decisões de precisão e negócio

- O valor de face dos recebíveis será armazenado em BRL.
- A liquidação poderá ocorrer em BRL ou USD.
- A cotação será expressa como BRL por 1 USD; a conversão BRL para USD será uma divisão.
- O prazo será um número inteiro de meses.
- A taxa base será mensal e inicialmente configurada em 1,00% (`0.0100`).
- Os spreads serão mensais e representados como `0.0150` e `0.0250`.
- A fórmula será `valorFace / (1 + taxaBase + spread)^prazo`.
- A aplicação usará `BigDecimal`; nenhum valor financeiro usará `double` ou `float`.
- Para os golden cases, o arredondamento será `HALF_EVEN`, com duas casas, somente no resultado final em BRL.
- No cross-currency, o valor presente em BRL será arredondado antes da conversão para USD, conforme o enunciado.
- A taxa efetivamente usada será capturada e persistida na liquidação; o histórico não será recalculado com uma taxa posterior.
- A unicidade da chave de idempotência será garantida também no banco, para que o controle não dependa somente da aplicação.

## Arquitetura e estrutura

```text
src/main/java/com/srm/creditengine/
├── CreditEngineApplication.java
├── config/                 # OpenAPI, propriedades e configuração de beans
├── domain/
│   ├── model/              # Entidades, enums e value objects
│   ├── pricing/            # Contrato Strategy e estratégias por tipo
│   └── exception/          # Exceções de negócio
├── application/
│   ├── dto/                # Requests e responses da API
│   └── service/             # Casos de uso e transações
├── infrastructure/
│   └── persistence/         # JPA repositories e consultas do extrato
└── interfaces/
    └── rest/                # Controllers e handler global

src/main/resources/
├── db/migration/            # Migrations Flyway
├── static/index.html        # Shell do painel do operador
├── static/styles.css        # Design responsivo do painel
├── static/app.js            # Estado, integração REST e paginação
└── application.yml

src/test/java/                # Testes unitários e de integração
static/                       # Painel operacional servido pelo Spring Boot
SPEC.md, REVIEW.md, ...       # Documentação do desafio
```

A camada `domain` não dependerá de controllers. A camada `application` coordenará casos de uso e transações. A persistência ficará atrás de repositórios. O relatório poderá usar uma consulta projetada/nativa própria, sem acoplar a regra de cálculo ao ORM.

## Fluxos principais

1. **Simulação:** validar entrada → localizar Strategy → obter taxa vigente quando necessário → calcular valor presente, deságio e valor final → responder sem persistir.
2. **Liquidação:** validar entrada e `Idempotency-Key` → verificar idempotência → carregar recebível → impedir estado inválido → calcular com os dados vigentes → persistir liquidação com valores e câmbio efetivo → mudar status do recebível → confirmar a transação.
3. **Extrato:** receber filtros e paginação → consultar somente liquidações persistidas → retornar projeção própria com valores auditáveis.

## Interface futura

O dashboard seguirá uma estética de ferramenta operacional financeira: fundo claro neutro, tipografia sans-serif legível, acentos em azul-petróleo e estados monetários com cores semânticas discretas. O layout priorizará um painel de simulação fixo e uma área de extrato orientada a leitura, sem imagens decorativas, porque o produto é uma ferramenta interna.

## Build e execução

- Maven Wrapper será versionado para que o projeto seja executável sem Maven global.
- Docker Compose definirá aplicação e PostgreSQL para a entrega do candidato.
- O perfil local permitirá testes com banco H2 apenas quando isso não mascarar comportamento específico do PostgreSQL; a integração principal usará PostgreSQL via Testcontainers se o ambiente permitir.
- O health endpoint será público e simples para execução e publicação.
- `README.md` documentará comandos, variáveis, endpoints, migrations e exemplos.

## Fora do escopo desta vaga

Observabilidade avançada, circuit breaker completo, optimistic locking demonstrado, arquitetura orientada a eventos, sharding, design para um milhão de transações por minuto e post-mortem do Anexo B não serão priorizados. Serão registrados em `DECISIONS.md` como itens avaliados e conscientemente não implementados por pertencerem às expectativas de sênior/staff ou excederem o esforço-alvo.
