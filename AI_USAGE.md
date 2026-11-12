# AI_USAGE — Engenharia da colaboração

## Como a IA foi usada

A IA foi usada como parceira de planejamento e implementação, não como autoridade do domínio. Primeiro foi feita uma leitura do enunciado para separar requisitos explícitos, expectativas de pleno, ambiguidades e itens de sênior/staff. Depois a IA ajudou a propor o escopo, a estrutura em camadas, o modelo relacional, os endpoints, as Strategies e os testes.

Prompts estratégicos usados no trabalho foram equivalentes a: “transforme o enunciado em um escopo de nível pleno, priorizando precisão financeira, idempotência, auditoria e testes”; “proponha um monólito Spring Boot em camadas com Strategy e PostgreSQL”; “implemente o cálculo com BigDecimal e valide os três golden cases”; e “revise o endpoint do Anexo A por severidade, impacto e correção”.

## Erros concretos detectados

A primeira estrutura colocou H2 somente no classpath de testes, enquanto a configuração padrão usava H2 para executar localmente. A suíte passou porque o teste tinha a dependência disponível, mas o `spring-boot:run` falhou com `Cannot load driver class: org.h2.Driver`. A falha foi encontrada ao executar o backend real, não apenas o build; H2 passou a estar disponível em runtime local e o Docker continua sobrescrevendo a conexão para PostgreSQL.

Outro erro apareceu no teste de idempotência gerado inicialmente: o mock devolvia a liquidação existente na segunda consulta da primeira execução, fazendo a primeira chamada ser tratada como replay. O teste falhou, a sequência de chamadas foi analisada e o mock foi trocado por um armazenamento em memória que só contém a liquidação depois do `save`. Depois disso a suíte passou com 8 testes e 0 falhas.

Também foi conferido o cálculo contra os golden cases. O resultado esperado só aparece quando o prazo e as taxas são interpretados em meses e como decimais; o caso USD exige arredondar o valor presente em BRL antes da divisão pelo câmbio.

## O que não foi delegado

As premissas financeiras, a política de arredondamento, a escolha da taxa vigente, o contrato de idempotência e a ordem de severidade do review não foram aceitos sem julgamento. A decisão de não implementar microsserviços, eventos, sharding e requisitos de sênior/staff também foi deliberada.

A IA não substitui a defesa técnica. Cada classe relevante deve ser explicável, os testes são a verificação objetiva e a documentação registra as limitações assumidas.
