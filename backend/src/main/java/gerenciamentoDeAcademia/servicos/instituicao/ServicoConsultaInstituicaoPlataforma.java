package gerenciamentoDeAcademia.servicos.instituicao;

import gerenciamentoDeAcademia.dto.AssinaturaPlataformaDto;
import gerenciamentoDeAcademia.dto.AtualizarPlanoInstituicaoRequest;
import gerenciamentoDeAcademia.dto.AtualizarStatusFinanceiroRequest;
import gerenciamentoDeAcademia.dto.InstituicaoDetalheDto;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.servicos.plano.ServicoAssinaturaPlataforma;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicoConsultaInstituicaoPlataforma {

    private final InstituicaoRepository instituicaoRepository;
    private final ServicoAssinaturaPlataforma servicoAssinaturaPlataforma;

    public InstituicaoDetalheDto consultarDetalhePorCnpj(String cnpj) {
        Instituicao instituicao = instituicaoRepository.findByCnpj(normalizarCnpj(cnpj));
        ExcecaoDeDominio.quandoNulo(instituicao, "Instituição não cadastrada na plataforma.");
        AssinaturaPlataformaDto assinatura = null;
        try {
            assinatura = servicoAssinaturaPlataforma.consultar(instituicao.getId());
        } catch (Exception ignored) {
            // sem assinatura ainda
        }
        return InstituicaoDetalheDto.of(instituicao, assinatura);
    }

    @Transactional
    public InstituicaoDetalheDto atualizarStatusFinanceiro(AtualizarStatusFinanceiroRequest request) {
        ExcecaoDeDominio.quandoNulo(request, "Dados obrigatórios.");
        ExcecaoDeDominio.quandoNulo(request.getStatusFinanceiro(), "Informe o status financeiro.");
        Instituicao instituicao = instituicaoRepository.findByCnpj(normalizarCnpj(request.getCnpj()));
        ExcecaoDeDominio.quandoNulo(instituicao, "Instituição não encontrada.");
        instituicao.setStatusFinanceiro(request.getStatusFinanceiro());
        instituicaoRepository.save(instituicao);
        AssinaturaPlataformaDto assinatura = null;
        try {
            assinatura = servicoAssinaturaPlataforma.consultar(instituicao.getId());
        } catch (Exception ignored) {
            // opcional
        }
        return InstituicaoDetalheDto.of(instituicao, assinatura);
    }

    @Transactional
    public InstituicaoDetalheDto atualizarPlanoPorCnpj(AtualizarPlanoInstituicaoRequest request) {
        ExcecaoDeDominio.quandoNulo(request, "Dados obrigatórios.");
        ExcecaoDeDominio.quandoNulo(request.getPlano(), "Informe o plano.");
        String cnpj = normalizarCnpj(request.getCnpj());
        ExcecaoDeDominio.quandoNuloOuVazio(cnpj, "Informe o CNPJ da instituição.");

        Instituicao instituicao = instituicaoRepository.findByCnpj(cnpj);
        ExcecaoDeDominio.quandoNulo(instituicao, "Instituição não encontrada.");
        ExcecaoDeDominio.quando(!Boolean.TRUE.equals(instituicao.getCadastroAtivo()),
                "Ative o cadastro da instituição antes de definir o plano.");

        AssinaturaPlataformaDto assinatura = servicoAssinaturaPlataforma.ativarPlano(instituicao, request.getPlano());
        return InstituicaoDetalheDto.of(instituicao, assinatura);
    }

    private String normalizarCnpj(String cnpj) {
        return cnpj != null ? cnpj.replaceAll("\\D", "") : "";
    }
}
