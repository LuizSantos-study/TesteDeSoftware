# PDV Farmácia - Backend

API REST de um PDV (ponto de venda) de farmácia, feita com Spring Boot 4, Java 21, JPA/Hibernate e PostgreSQL.

## Como rodar

### Pré-requisitos

- Java 21
- PostgreSQL rodando em `localhost:5432`
- Um banco chamado `db_farmacia`

### Configuração

Os dados de conexão ficam em `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/db_farmacia
spring.datasource.username=postgres
spring.datasource.password=1234
```

Ajuste usuário e senha conforme o seu PostgreSQL.

### Subir a aplicação

```bash
cd backend
./mvnw spring-boot:run
```

Na inicialização, o Hibernate cria/atualiza as tabelas e o `data.sql` carrega os dados iniciais (produtos, formas de pagamento, funcionários e clientes).

- API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### Rodar os testes

```bash
cd backend
./mvnw test
```

Os testes usam H2 em memória, sem precisar do PostgreSQL.

### Usuários de exemplo

| Funcionário | E-mail | Senha | Cargo |
|---|---|---|---|
| Lucas Lima | lucas.caixa@farmacia.com | 123456 | OPERADOR_CAIXA |
| Dra. Beatriz Santos | beatriz.farma@farmacia.com | 123456 | FARMACEUTICO |
| Marcos Oliveira | marcos.gerente@farmacia.com | admin123 | GERENTE |
| Dr. Rafael Costa | rafael.farma@farmacia.com | 123456 | FARMACEUTICO |

## Regras de negócio

### Funcionário e autenticação

- O login é feito diretamente pela entidade `Funcionario`, com e-mail e senha.
- Campos obrigatórios no cadastro: nome, e-mail, senha e cargo.
- A senha deve ter no mínimo 4 caracteres.
- O e-mail é único.
- Se o cargo for `FARMACEUTICO`, o CRF é obrigatório.
- O CPF é opcional.
- A senha nunca é retornada nas respostas da API.
- Depois do cadastro, o front deve voltar para a tela de login.

### Venda

- Toda venda nasce com status `PENDENTE`.
- Status possíveis: `PENDENTE`, `FINALIZADA` e `CANCELADO`.
- O ID da venda é gerado pelo banco e nunca é enviado no corpo da requisição.
- O corpo de criação e atualização usa apenas IDs das entidades relacionadas (funcionário, forma de pagamento, produtos).
- O preço de cada item vem sempre do cadastro do produto.
- Subtotal do item = preço unitário x quantidade.
- Total da venda = soma dos itens ativos - desconto.
- O desconto não pode ser negativo nem maior que o subtotal dos itens.
- Uma venda finalizada não pode ser alterada.
- Uma venda cancelada não pode ser finalizada nem ter itens alterados.
- Para finalizar, a venda precisa ter pelo menos um item ativo.

### Medicamentos controlados (SNGPC)

- Venda com medicamento controlado exige o número da receita.
- Também exige um farmacêutico, seja como funcionário da venda ou como farmacêutico responsável.
- Pagamentos por TEF ou POS passam por autorização do gateway antes de finalizar.

### Auditoria: nada é excluído

- Não existe exclusão física de `Venda` nem de `ItemVenda`. Os repositórios não têm método de delete e não há endpoint `DELETE`.
- Cancelar uma venda só muda o status dela para `CANCELADO`. A venda e todos os itens continuam no banco.
- Cada item tem um status: `ATIVO` ou `CANCELADO`.
- Cancelar um item só muda o status dele para `CANCELADO`. O registro permanece na venda.
- Itens cancelados não entram no cálculo do total.
- No `PUT /api/vendas/{id}`, itens que não vierem na lista são cancelados, nunca excluídos.

## Endpoints

### Funcionários

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/funcionarios` | Lista funcionários |
| GET | `/api/funcionarios/{id}` | Busca por ID |
| GET | `/api/funcionarios/farmaceuticos` | Lista farmacêuticos |
| POST | `/api/funcionarios` | Cadastra funcionário |
| POST | `/api/funcionarios/login` | Autentica por e-mail e senha |

### Vendas

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/vendas` | Lista vendas |
| GET | `/api/vendas/{id}` | Busca venda |
| POST | `/api/vendas` | Cria venda |
| PUT | `/api/vendas/{id}` | Atualiza venda |
| POST | `/api/vendas/{id}/itens` | Adiciona item |
| PUT | `/api/vendas/{id}/itens/{itemId}/cancelar` | Cancela item |
| PUT | `/api/vendas/{id}/desconto` | Aplica desconto |
| PUT | `/api/vendas/{id}/finalizar` | Finaliza venda |
| PUT | `/api/vendas/{id}/cancelar` | Cancela venda |

### Consultas

| Método | Rota |
|---|---|
| GET | `/api/clientes`, `/api/clientes/{id}`, `/api/clientes/cpf/{cpf}` |
| GET | `/api/produtos`, `/api/produtos/{id}` |
| GET | `/api/formas-pagamento`, `/api/formas-pagamento/ativas`, `/api/formas-pagamento/{id}` |

## Exemplos de corpo

Cadastro de funcionário (`POST /api/funcionarios`):

```json
{
  "nome": "Ana",
  "email": "ana@farmacia.com",
  "senha": "1234",
  "cargo": "FARMACEUTICO",
  "crf": "CRF-GO 11111"
}
```

Login (`POST /api/funcionarios/login`):

```json
{ "email": "ana@farmacia.com", "senha": "1234" }
```

Criar venda (`POST /api/vendas`):

```json
{
  "clienteId": 1,
  "funcionarioId": 1,
  "formaPagamentoId": 1,
  "desconto": 0,
  "itens": [
    { "produtoId": 4, "qtdVendida": 2 },
    { "produtoId": 9, "qtdVendida": 1 }
  ]
}
```

Adicionar item (`POST /api/vendas/{id}/itens`):

```json
{ "produtoId": 5, "quantidade": 2 }
```

Aplicar desconto (`PUT /api/vendas/{id}/desconto`):

```json
{ "desconto": 5.00 }
```

## Estrutura do backend

```
backend/src/main/java/testesoftware/com/backend
  controller   endpoints REST
  service      regras de negócio
  repository   acesso ao banco (JPA)
  model        entidades
  dto          corpos de entrada e resposta
  enums        status, cargos, tipos
  exception    erros de domínio e tratador global
```
