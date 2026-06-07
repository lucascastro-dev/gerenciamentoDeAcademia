package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.DadosCertificadoDto;
import gerenciamentoDeAcademia.dto.ResultadoGeracaoCertificadoDto;

public interface IGeradorDeCertificados {
    ResultadoGeracaoCertificadoDto gerarCertificado(DadosCertificadoDto dadosCertificado);
}
