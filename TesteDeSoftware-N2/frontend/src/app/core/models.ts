export type FuncaoFuncionario = 'OPERADOR_CAIXA' | 'FARMACEUTICO' | 'GERENTE';
export type StatusVenda = 'PENDENTE' | 'FINALIZADA' | 'CANCELADO';
export type StatusItemVenda = 'ATIVO' | 'CANCELADO';
export type TipoProduto = 'REFERENCIA' | 'GENERICO' | 'SIMILAR' | 'PERFUMARIA';
export type PlanoCliente = 'SAUDE' | 'FIDELIDADE' | 'NENHUM';
export type MeioPagamento = 'PIX' | 'DINHEIRO' | 'CARTAO_CREDITO' | 'CARTAO_DEBITO';
export type ModalidadeIntegracao = 'TEF' | 'POS' | 'NAO_APLICAVEL';

export interface Funcionario {
  id: number;
  nome: string;
  email: string;
  cpf: string | null;
  crf: string | null;
  funcao: FuncaoFuncionario;
}

export interface LoginResponse {
  autenticado: boolean;
  mensagem: string;
  funcionario: Funcionario;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface CadastroFuncionarioRequest {
  nome: string;
  email: string;
  senha: string;
  cpf?: string;
  cargo: FuncaoFuncionario;
  crf?: string;
}

export interface Cliente {
  id: number;
  nome: string;
  cpf: string;
  dataNasc: string;
  plano: PlanoCliente;
}

export interface Produto {
  id: number;
  nome: string;
  principioAtivo: string;
  tipo: TipoProduto;
  sngpc: boolean;
  preco: number;
}

export interface FormaPagamento {
  id: number;
  descricao: string;
  tipo: MeioPagamento;
  tipoPagamento: ModalidadeIntegracao;
  ativo: boolean;
  permiteParcelamento: boolean;
}

export interface ItemVendaRequest {
  produtoId: number;
  qtdVendida: number;
}

export interface VendaRequest {
  clienteId: number | null;
  funcionarioId: number;
  formaPagamentoId: number;
  desconto: number;
  numeroReceita: string | null;
  farmaceuticoResponsavelId: number | null;
  itens: ItemVendaRequest[];
}

export interface ItemVenda {
  id: number;
  numeroItem: number;
  produtoId: number;
  produtoNome: string;
  quantidade: number;
  precoUnitario: number;
  subtotal: number;
  status: StatusItemVenda;
}

export interface Venda {
  id: number;
  dataHora: string;
  clienteId: number | null;
  clienteNome: string | null;
  funcionario: Funcionario;
  formaPagamentoId: number;
  formaPagamentoDescricao: string;
  itens: ItemVenda[];
  desconto: number;
  valorTotal: number;
  status: StatusVenda;
  numeroReceita: string | null;
  farmaceuticoResponsavel: Funcionario | null;
}

export interface ApiError {
  status?: number;
  erro?: string;
  mensagem?: string;
}
