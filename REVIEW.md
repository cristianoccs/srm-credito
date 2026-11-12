# Review reverso — Anexo A

## Contexto

O trecho mistura acesso a dados, regra financeira, integração de câmbio e efeitos transacionais no controller. A severidade abaixo considera risco de perda financeira, duplicidade, corrupção de auditoria e exposição de dados.

## 1. Crítica — SQL Injection

`receivableId`, `currency` e valores derivados são interpolados diretamente nas queries. Um atacante pode alterar a consulta de recebível ou o insert/update, ler dados ou executar comandos não previstos. A correção é usar queries parametrizadas/JPA repositories, validar UUID e enum na borda e nunca concatenar valores de entrada em SQL. O mesmo princípio deve ser aplicado a filtros do extrato.

## 2. Crítica — ausência de transação atômica

O insert de settlement e o update do recebível são duas operações independentes. Se o insert funcionar e o update falhar, existe liquidação registrada com recebível ainda aberto; se a ordem mudar, o problema é inverso. A correção é um caso de uso transacional (`@Transactional`) envolvendo as duas operações e constraints no banco. Falha deve gerar rollback e status de erro, não sucesso.

## 3. Crítica — exceção engolida e sucesso falso

O `catch` ignora qualquer exceção e responde `200 { ok: true }`. O operador recebe confirmação mesmo quando o banco, o câmbio ou a mudança de status falhou. Isso impede retry correto e transforma erro técnico em divergência financeira. A correção é propagar exceções classificadas, registrar log estruturado sem dados sensíveis e retornar status semântico; a resposta de sucesso só deve ser emitida depois do commit lógico do caso de uso.

## 4. Crítica — falta de idempotência

Retry de rede ou duplo clique cria nova linha porque não existe chave de requisição nem constraint única. A correção é exigir `Idempotency-Key`, persistir a chave com `UNIQUE`, associá-la ao payload relevante e devolver a mesma liquidação em replay. Reuso da chave com outro payload deve ser `409`.

## 5. Crítica — condição de corrida e dupla liquidação

Duas requisições simultâneas podem ler o mesmo recebível aberto antes de qualquer update e ambas inserirem settlement. Além da idempotência, é preciso proteger o recebível com lock de linha ou estratégia otimista e manter `UNIQUE(receivable_id)` como última barreira. O conflito deve ser convertido em resposta previsível.

## 6. Alta — uso inadequado de `double` e `toFixed`

`double` não representa exatamente valores decimais financeiros e `toFixed` apenas formata uma string no fim; ele não corrige os arredondamentos já feitos no cálculo. A correção é usar `BigDecimal`/decimal desde a entrada, explicitar `HALF_EVEN` e arredondar em pontos definidos pela especificação. O câmbio também precisa ter precisão própria.

## 7. Alta — percentuais e unidade do prazo não estão definidos

`BASE_RATE = 1.0` e spreads `1.5`/`2.5` podem ser interpretados como 100%, 1% ou 1,5%, e `term` não informa se são meses, dias ou anos. Isso pode multiplicar o deságio. A correção é armazenar percentuais decimais (`0.0100`, `0.0150`, `0.0250`), declarar meses inteiros e cobrir golden cases.

## 8. Alta — cotação não é auditada

`getLatestRate("USD")` pega uma taxa sem registrar valor, identificador ou instante. O histórico pode produzir resultado diferente em uma reconsulta, e não há prova de qual cotação sustentou a operação. A correção é selecionar a taxa por vigência, persistir valor e identificador na liquidação e impedir recálculo do passado.

## 9. Alta — “latest” pode não ser a taxa correta

A taxa mais recente global pode estar fora da janela de vigência, ser de outro par, estar stale ou ter sido publicada depois do instante da operação. A correção é modelar `baseCurrency`, `quoteCurrency`, `validFrom`, timezone e política de fallback. Ausência de cotação deve interromper a liquidação de USD.

## 10. Média — validação de entrada insuficiente

Não há validação de campos obrigatórios, UUID, moeda suportada, valor positivo, prazo positivo ou tamanho de identificadores. Também não há tratamento para body inválido ou recebível inexistente. A correção é validar DTOs na borda e mapear erros para `400`/`404` consistentes.

## 11. Média — status e elegibilidade não são verificados

O trecho calcula e liquida qualquer ID retornado, inclusive um recebível já liquidado ou inexistente. A correção é verificar existência, status `OPEN` e regras de elegibilidade sob lock dentro da transação.

## 12. Média — auditoria incompleta

A tabela recebe somente valor, moeda e ID. Faltam valor de face, valor presente, deságio, taxa base, spread, câmbio efetivo, timestamps, chave de idempotência e origem da cotação. Sem esses dados, a operação não é reproduzível nem auditável. A correção é persistir um snapshot imutável dos inputs e outputs relevantes.

## 13. Média — regra financeira no controller

O controller conhece tipos, spreads, fórmula e conversão. Isso dificulta testes, aumenta acoplamento e torna a adição de outro recebível arriscada. A correção é um serviço de aplicação e um motor de domínio com Strategy por tipo; o controller deve apenas validar, delegar e mapear resposta.

## 14. Média — integração externa sem resiliência ou timeout

Uma chamada de câmbio sem timeout/retry controlado pode prender threads. Retry cego também pode repetir efeitos se a integração deixar de ser somente leitura. A correção é definir timeout, política de retry para falhas transitórias, circuit breaker quando necessário e comportamento explícito: sem taxa confiável, não liquidar.

## 15. Baixa, mas relevante — observabilidade e códigos semânticos

Não há correlation ID, métricas de latência/volume ou logs úteis, e todo resultado é `200`. A correção é log estruturado, métrica de liquidações e erros com códigos estáveis, sem registrar dados sensíveis. Isso deve apoiar operação sem mascarar falhas.
