package gerenciamentoDeAcademia.infra.integracoes;

public interface ProvedorPagamento {
    boolean ativo();

    RespostaCobrancaProvedor criarCobranca(SolicitacaoCobrancaExterna solicitacao);
}
