# TODO — SRM Credit Engine

## Backend Java e fundação do projeto

- [x] Criar o projeto Java 21 com Spring Boot, Maven Wrapper e configuração de execução local.
- [x] Separar as responsabilidades em camadas de apresentação, aplicação, domínio e persistência.
- [x] Configurar PostgreSQL, Docker Compose, variáveis de ambiente e migrations versionadas.
- [x] Publicar health endpoint e documentação OpenAPI/Swagger.

## Domínio de recebíveis e precificação

- [x] Permitir cadastrar recebíveis com cedente, valor de face, vencimento/prazo, tipo e moeda de origem.
- [x] Implementar Strategy para Duplicata Mercantil e Cheque Pré-datado.
- [x] Aplicar a fórmula `Valor Presente = Valor de Face / (1 + Taxa Base + Spread)^Prazo` com taxa base de 1,00% ao mês, spreads de 1,5% e 2,5% ao mês e prazo em meses inteiros.
- [x] Usar `BigDecimal` em valores monetários e taxas, sem `double` ou `float` no domínio financeiro.
- [x] Aplicar arredondamento `HALF_EVEN` em duas casas somente no resultado final dos golden cases.
- [x] Permitir simulação sem criar liquidação.
- [x] Implementar conversão de BRL para USD somente depois do valor presente em BRL estar arredondado.
- [x] Automatizar os casos C1, C2 e C3 e validar os valores esperados ao centavo.
- [x] Cobrir tipos inválidos, prazo inválido, valores não positivos e casos de arredondamento.

## Câmbio

- [x] Armazenar taxas de câmbio com par de moedas, valor e data/hora de vigência.
- [x] Disponibilizar atualização manual de taxa por endpoint validado.
- [x] Selecionar a taxa válida para a liquidação e persistir a taxa efetivamente utilizada.
- [x] Retornar erro sem liquidar quando não houver taxa aplicável para USD.

## Liquidação, ACID e auditoria

- [x] Implementar endpoint de liquidação com `Idempotency-Key` obrigatório.
- [x] Garantir unicidade da chave de idempotência no banco.
- [x] Fazer o registro da liquidação e a mudança de status do recebível na mesma transação.
- [x] Repetir a mesma requisição sem criar segunda liquidação e responder de forma consistente.
- [x] Validar existência, status e elegibilidade do recebível antes da liquidação.
- [x] Registrar valores de face, valor presente, deságio, valor final, moedas, câmbio utilizado, timestamps e chave de idempotência.
- [x] Não disponibilizar operação de alteração de liquidação registrada.
- [x] Usar códigos semânticos: `400` para entrada inválida, `404` para recurso ausente, `409` para conflito de estado/idempotência e `5xx` para falhas não tratadas.
- [x] Implementar tratamento global de exceções com resposta de erro consistente.

## Extrato

- [x] Criar rota analítica de liquidações.
- [x] Implementar filtros por período, cedente e moeda.
- [x] Implementar paginação server-side.
- [x] Usar projeção/query própria para o extrato, evitando carregar entidades desnecessárias.

## Documentação exigida

- [x] `README.md` com setup, execução, variáveis, endpoints e exemplos.
- [x] `SPEC.md` com premissas, perguntas de negócio, precisão numérica e critérios de aceite.
- [x] `REVIEW.md` com os problemas do Anexo A em ordem de severidade, impacto e correção.
- [x] `AI_USAGE.md` com prompts estratégicos, um erro real da IA detectado e decisões não delegadas.
- [x] `DECISIONS.md` com cortes de escopo e justificativas.
- [x] Diagrama ER básico e exemplos de requisições.

## Frontend da fase posterior
- [x] Criar painel de operador com formulário de recebível, tipo, moeda e simulação do valor líquido em tempo real.
- [x] Exibir valor presente, deságio e valor final com formatação monetária.
- [x] Criar grid de transações com paginação server-side e filtros dinâmicos.
- [x] Separar apresentação, estado e acesso à API.

## Entrega e versionamento

- [ ] Organizar commits com Conventional Commits e escopo rastreável.
- [x] Adicionar Docker Compose para aplicação e banco.
- [x] Executar testes e build a partir de comandos documentados.
- [x] Revisar o repositório como preparação para a defesa técnica ao vivo.
