package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.DadosCertificadoDto;

public interface IGeradorDeCertificados {
    void gerarCertificado(DadosCertificadoDto dadosCertificado);
}
