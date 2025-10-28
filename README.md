# WePayU: Sistema de Folha de Pagamento

Este projeto foi desenvolvido para a disciplina de **Programação 2 (POO)** dos cursos de **Ciência da Computação**.  
O objetivo é implementar um **sistema de folha de pagamento**.

---

## Descrição do Projeto

O sistema gerencia informações de empregados, cartões de ponto, resultados de vendas e dados de sindicato, realizando o pagamento correto de acordo com o tipo de empregado e o método de pagamento escolhido.

O projeto é desenvolvido de forma **incremental**, em **iterações (milestones)**, nas quais novas *User Stories* e funcionalidades são adicionadas ao sistema.

---

## Funcionalidades

- **Cadastro de empregados** com diferentes tipos:
  - **Horista**: recebe por hora trabalhada (pagamento toda sexta-feira)
  - **Assalariado**: recebe salário fixo (pagamento no último dia útil do mês)
  - **Comissionado**: recebe salário fixo + comissão sobre vendas (pagamento a cada 2 sextas-feiras)
- **Cálculo automático** de salários, horas extras e comissões
- **Descontos sindicais**, incluindo taxas mensais e taxas de serviço ocasionais
- **Métodos de pagamento**:
  - Cheque pelos correios  
  - Cheque em mãos  
  - Depósito em conta bancária
- **Execução diária da folha de pagamento**, calculando valores devidos até a data informada
