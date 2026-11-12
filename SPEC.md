# SPEC — SRM Credit Engine

## 1. Objetivo e unidade de operação

O sistema precifica e registra a liquidação de recebíveis cedidos à SRM. A unidade transacional da primeira versão é um recebível por requisição. O texto menciona lote, mas os endpoints e golden cases são definidos por título individual; processamento em lote fica fora do escopo desta entrega e pode ser adicionado sobre o mesmo caso de uso.

A simulação é uma operação sem efeito colateral. A liquidação é definitiva: cria uma linha de auditoria e altera o status do recebível na mesma transação. Não há endpoint de alteração ou exclusão de liquidação.

## 2. Premissas adotadas

**Moeda de origem.** O valor de face é armazenado em BRL. A primeira versão aceita pagamento em BRL ou USD. O par de câmbio é BRL/USD, isto é, a cotação representa quantos reais valem 1 dólar; converter BRL para USD significa dividir pelo câmbio.

**Prazo.** O prazo de precificação é um inteiro positivo em meses. `termMonths` é o campo autoritativo para o cálculo, pois os golden cases fornecem meses diretamente. `maturityDate` pode ser armazenado como dado operacional, mas não altera o resultado da fórmula nesta versão. Em um projeto real, o negócio precisaria decidir se o prazo deve ser derivado do vencimento e qual calendário deve ser aplicado.

**Taxa base e spread.** A taxa base inicial é 1,00% ao mês (`0.0100`) e fica configurável por propriedade de aplicação. Duplicata Mercantil usa spread mensal de 1,5% (`0.0150`); Cheque Pré-datado usa 2,5% (`0.0250`). As taxas são percentuais decimais, não números 1,0 ou 1,5.

**Fórmula.** Aplicamos `ValorPresente = ValorFace / (1 + TaxaBase + Spread)^Prazo`, com juros compostos mensais. O deságio em BRL é `ValorFace - ValorPresenteBRL`.

**Câmbio.** Na liquidação e na simulação USD, selecionamos a taxa cuja vigência é menor ou igual ao instante atual e que seja a mais recente entre as aplicáveis. Na liquidação, o instante e a taxa efetivamente usados são persistidos. Se não existir taxa aplicável, a operação falha sem criar liquidação.

**Arredondamento.** A aplicação usa `BigDecimal` e `RoundingMode.HALF_EVEN`. Para os golden cases, o valor presente em BRL é arredondado para duas casas somente no resultado final; o deságio é calculado a partir desse resultado. No cross-currency, convertemos o valor presente em BRL já arredondado e arredondamos o valor final em USD para duas casas.

**Idempotência.** `Idempotency-Key` é obrigatório no endpoint de liquidação, possui no máximo 160 caracteres e tem restrição `UNIQUE` no banco. Repetir a mesma chave para o mesmo recebível e moeda retorna a liquidação existente. Reusar a chave com outro payload retorna `409 Conflict`.

**Estado.** Um recebível aberto pode ser liquidado uma única vez. A restrição `UNIQUE(receivable_id)` em `settlements`, o lock pessimista da linha e a transação impedem duplicidade mesmo sob retry e concorrência. Liquidação parcial não é suportada nesta versão.

## 3. Perguntas para o negócio

- A taxa base vem de um contrato, de uma tabela diária ou de uma configuração administrável? Quem pode alterá-la?
- A taxa de câmbio deve ser a vigente no instante da liquidação, a cotação de fechamento ou uma cotação aprovada pela mesa?
- O provedor de câmbio real possui bid/ask, spread próprio, timezone e identificador de mercado que também precisam ser auditados?
- Recebíveis vencidos podem ser liquidados? Existe carência?
- O vencimento deve determinar o prazo com meses quebrados, dias corridos ou calendário comercial?
- A operação admite liquidação parcial, estorno ou cancelamento antes do vencimento?
- Quais perfis podem cadastrar taxas, simular e liquidar? Quais dados precisam de mascaramento?
- Qual é a política oficial para moedas com quantidade diferente de duas casas?
- O lote deve ser atômico inteiro ou cada recebível pode falhar independentemente?
- Qual retenção legal e trilha de auditoria são exigidas para registros financeiros?

## 4. Decisões de precisão e persistência

Na aplicação, toda operação monetária usa `BigDecimal` criado a partir de literais decimais. A potência usa `MathContext.DECIMAL128` para evitar arredondamento prematuro; a escala monetária e o modo de arredondamento são explícitos.

No PostgreSQL, valores monetários usam `NUMERIC(19,6)` para preservar o valor persistido e permitir extensão futura; respostas monetárias são normalizadas para duas casas. Taxas usam `NUMERIC(19,10)`. A liquidação guarda valor de face, valor presente, deságio, valor final, moeda de pagamento, taxa, identificador da taxa, instante e chave de idempotência.

A transação abrange a leitura protegida do recebível, cálculo, inserção da liquidação e alteração do status. A liquidação é imutável por contrato de aplicação: não existe operação de update/delete exposta.

## 5. Critérios de aceite

A entrega é aceita quando os três golden cases passam ao centavo e nenhum valor financeiro do domínio usa `double` ou `float`. Entradas inválidas retornam `400`, recurso inexistente retorna `404`, conflito de estado ou idempotência retorna `409`, ausência de câmbio para USD retorna erro sem persistência e falha em qualquer etapa não deixa a transação pela metade.

A repetição da mesma requisição de liquidação não cria segundo registro e devolve o mesmo identificador, valores e câmbio efetivo. O extrato permite período, cedente, moeda e paginação server-side. A API sobe com `./mvnw spring-boot:run` ou Docker Compose, aplica migrations automaticamente, expõe health check e documentação OpenAPI.
