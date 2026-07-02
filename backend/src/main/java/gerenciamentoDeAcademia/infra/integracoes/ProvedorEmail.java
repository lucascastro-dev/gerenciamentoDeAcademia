package gerenciamentoDeAcademia.infra.integracoes;

import gerenciamentoDeAcademia.dto.integracoes.IntegracoesStatusDto;

public interface ProvedorEmail {
    boolean ativo();

    void enviar(MensagemEmail mensagem);
}
