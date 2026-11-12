# DECISIONS — Escopo de nível pleno

## Monólito modular em vez de microsserviços

O desafio é uma unidade de negócio pequena, com consistência forte entre recebível e liquidação. Um monólito modular reduz infraestrutura e torna a transação ACID explícita. As fronteiras de domínio ficam separadas para permitir extração futura sem introduzir complexidade agora.

## PostgreSQL no ambiente entregue e H2 para execução local/testes

O banco relacional pedido pelo enunciado é PostgreSQL e é o banco do Docker Compose. H2 é usado como fallback local para que a API possa ser executada sem infraestrutura externa e para o teste de contexto. As migrations são exercitadas no contexto Spring; antes da entrega final deve ser feita uma execução também contra PostgreSQL.

## Estratégia de idempotência

A chave fica no header `Idempotency-Key`, com índice único e associação ao recebível. Além da busca prévia, o recebível é lido com lock pessimista e existe `UNIQUE(receivable_id)`. Isso evita duplicidade na escala do case sem exigir Redis ou uma tabela distribuída de locks.

## Prazo em meses e vencimento opcional

Os golden cases fornecem meses diretamente e a fórmula é mensal. Por isso `termMonths` é autoritativo. O campo `maturityDate` é mantido para o fluxo operacional, mas sua relação com o prazo precisa de decisão do negócio; derivá-lo automaticamente agora criaria uma regra não especificada.

## Sem liquidação parcial, estorno ou autenticação

O enunciado pede a liquidação de um recebível, auditabilidade e imutabilidade, mas não define parcialidade, estorno ou perfis de usuário. A primeira versão suporta uma liquidação integral e não expõe alteração/exclusão. Autenticação, autorização e trilha de identidade do operador são perguntas abertas para um projeto real.

## Sem frontend antes do backend

O painel depende dos contratos do backend. Implementar primeiro o domínio, API, banco e testes reduz retrabalho e protege a parte com maior peso na rubrica. O frontend será uma camada fina de simulação e extrato, sem duplicar regras financeiras.

## Itens de sênior/staff conscientemente adiados

Não foram priorizados optimistic locking demonstrado, métricas de negócio, integração externa resiliente completa, CI, C4, EDA, sharding, caching distribuído, arquitetura para um milhão de transações por minuto e post-mortem do Anexo B. Esses itens são reconhecidos como evolução possível, mas o esforço foi concentrado no contrato de pleno e na defesa do código implementado.
