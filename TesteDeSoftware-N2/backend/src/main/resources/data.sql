-- ============================================================================
-- SCRIPT DE CARGA INICIAL DE DADOS - PDV FARMÁCIA (PostgreSQL / DBeaver)
-- Execute este script no DBeaver após o Spring Boot criar as tabelas (ddl-auto=update)
-- ============================================================================

-- 1. PRODUTOS DA FARMÁCIA (Controlados SNGPC, Referência, Genéricos, Similares e Perfumaria)
INSERT INTO produtos (id, nome, principio_ativo, tipo, sngpc, preco) VALUES
(1, 'Rivotril 2mg (30 Comprimidos)', 'Clonazepam', 'REFERENCIA', true, 32.50),
(2, 'Diazepam 10mg (Genérico - 30 Comp.)', 'Diazepam', 'GENERICO', true, 18.00),
(3, 'Zolpidem 10mg (Genérico - 20 Comp.)', 'Hemitartarato de Zolpidem', 'GENERICO', true, 29.90),
(4, 'Dipirona Monoidratada 500mg Gotas 20ml', 'Dipirona Monoidratada', 'GENERICO', false, 8.50),
(5, 'Novalgina 1g (10 Comprimidos)', 'Dipirona Sódica', 'REFERENCIA', false, 24.00),
(6, 'Dorflex (36 Comprimidos)', 'Dipirona + Cafeína + Orfenadrina', 'SIMILAR', false, 16.90),
(7, 'Amoxicilina 500mg (21 Cápsulas)', 'Amoxicilina Tri-hidratada', 'GENERICO', true, 27.80),
(8, 'Tylenol 750mg (20 Comprimidos)', 'Paracetamol', 'REFERENCIA', false, 22.50),
(9, 'Ibuprofeno 600mg (20 Comprimidos)', 'Ibuprofeno', 'GENERICO', false, 14.90),
(10, 'Protetor Solar Facial FPS 60 50g', 'Filtro UV', 'PERFUMARIA', false, 59.90),
(11, 'Shampoo Anticaspa Dermatológico 200ml', 'Zinco Piritiona', 'PERFUMARIA', false, 34.00),
(12, 'Hidratante Corporal Neutro 400ml', 'Ureia + Glicerina', 'PERFUMARIA', false, 42.90)
ON CONFLICT (id) DO NOTHING;

-- 2. FORMAS DE PAGAMENTO DO PDV
INSERT INTO formas_pagamento (id, descricao, tipo, tipo_pagamento, ativo, permite_parcelamento) VALUES
(1, 'Dinheiro em Espécie', 'DINHEIRO', 'NAO_APLICAVEL', true, false),
(2, 'PIX Instantâneo', 'PIX', 'NAO_APLICAVEL', true, false),
(3, 'Cartão de Crédito (TEF)', 'CARTAO_CREDITO', 'TEF', true, true),
(4, 'Cartão de Débito (TEF)', 'CARTAO_DEBITO', 'TEF', true, false),
(5, 'Cartão de Crédito (POS Maquininha)', 'CARTAO_CREDITO', 'POS', true, true),
(6, 'Cartão de Débito (POS Maquininha)', 'CARTAO_DEBITO', 'POS', true, false)
ON CONFLICT (id) DO NOTHING;

-- 3. FUNCIONÁRIOS DA FARMÁCIA
INSERT INTO funcionarios (id, nome, cpf, crf, funcao) VALUES
(1, 'Lucas Lima', '444.555.666-77', NULL, 'OPERADOR_CAIXA'),
(2, 'Dra. Beatriz Santos', '555.666.777-88', 'CRF-GO 98765', 'FARMACEUTICO'),
(3, 'Marcos Oliveira', '666.777.888-99', NULL, 'GERENTE'),
(4, 'Dr. Rafael Costa', '777.888.999-00', 'CRF-GO 54321', 'FARMACEUTICO')
ON CONFLICT (id) DO NOTHING;

-- 4. CLIENTES CADASTRADOS
INSERT INTO clientes (id, nome, cpf, data_nasc, plano) VALUES
(1, 'João da Silva', '111.222.333-44', '1985-04-12', 'SAUDE'),
(2, 'Maria Albuquerque', '222.333.444-55', '1992-08-25', 'FIDELIDADE'),
(3, 'Carlos Eduardo', '333.444.555-66', '2001-01-10', 'NENHUM')
ON CONFLICT (id) DO NOTHING;

-- 5. USUÁRIOS DE ACESSO AO PDV (LOGIN)
INSERT INTO usuarios (id, nome, login, email, senha, perfil, ativo, funcionario_id) VALUES
(1, 'Lucas Lima (Caixa)', 'caixa', 'lucas.caixa@farmacia.com', '123456', 'OPERADOR_CAIXA', true, 1),
(2, 'Dra. Beatriz Santos (Farmacêutica)', 'farmaceutico', 'beatriz.farma@farmacia.com', '123456', 'FARMACEUTICO', true, 2),
(3, 'Marcos Oliveira (Gerente)', 'admin', 'marcos.gerente@farmacia.com', 'admin123', 'GERENTE', true, 3)
ON CONFLICT (id) DO NOTHING;

-- Ajuste das sequências (auto-incremento) do PostgreSQL após os inserts manuais
SELECT setval(pg_get_serial_sequence('produtos', 'id'), COALESCE((SELECT MAX(id) FROM produtos), 1), true);
SELECT setval(pg_get_serial_sequence('formas_pagamento', 'id'), COALESCE((SELECT MAX(id) FROM formas_pagamento), 1), true);
SELECT setval(pg_get_serial_sequence('funcionarios', 'id'), COALESCE((SELECT MAX(id) FROM funcionarios), 1), true);
SELECT setval(pg_get_serial_sequence('clientes', 'id'), COALESCE((SELECT MAX(id) FROM clientes), 1), true);
SELECT setval(pg_get_serial_sequence('usuarios', 'id'), COALESCE((SELECT MAX(id) FROM usuarios), 1), true);
