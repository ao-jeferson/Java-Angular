export interface Competencia {
  mes: number;
  ano: number;
}
export interface Produto {
  codigo: string;
  descricao: string | null;
}
export interface Cosif {
  codigo: string;
  classificacao: string | null;
}
export interface NovoMovimento extends Competencia {
  codigoProduto: string;
  codigoCosif: string;
  descricao: string;
  valor: string;
}
export interface Movimento extends NovoMovimento {
  numeroLancamento: string;
  descricaoProduto: string | null;
  dataMovimento: string;
  codigoUsuario: string;
}
