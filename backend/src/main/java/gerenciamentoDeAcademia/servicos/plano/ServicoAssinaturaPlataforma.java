package gerenciamentoDeAcademia.servicos.plano;

import gerenciamentoDeAcademia.dto.AssinaturaPlataformaDto;
import gerenciamentoDeAcademia.entidades.AssinaturaPlataforma;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AssinaturaPlataformaRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ServicoAssinaturaPlataforma {

    private final AssinaturaPlataformaRepository assinaturaRepository;
    private final InstituicaoRepository instituicaoRepository;

    public boolean instituicaoComPlanoAtivo(Long instituicaoId) {
        return assinaturaRepository.findByInstituicao_Id(instituicaoId)
                .map(AssinaturaPlataforma::isVigente)
                .orElse(false);
    }

    public AssinaturaPlataformaDto consultar(Long instituicaoId) {
        return AssinaturaPlataformaDto.of(
                assinaturaRepository.findByInstituicao_Id(instituicaoId).orElse(null));
    }

    @Transactional
    public void garantirTrial(Instituicao instituicao) {
        if (assinaturaRepository.existsByInstituicao_Id(instituicao.getId())) {
            return;
        }
        LocalDate inicio = LocalDate.now();
        assinaturaRepository.save(AssinaturaPlataforma.builder()
                .instituicao(instituicao)
                .plano(PlanoInstituicaoTipo.TRIAL_7_DIAS)
                .dataInicio(inicio)
                .dataFim(PlanoInstituicaoTipo.TRIAL_7_DIAS.calcularFim(inicio))
                .ativo(true)
                .build());
    }

    @Transactional
    public AssinaturaPlataformaDto ativarPlano(Long instituicaoId, PlanoInstituicaoTipo plano) {
        ExcecaoDeDominio.quandoNulo(plano, "Informe o tipo de plano.");
        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));
        return ativarPlano(instituicao, plano);
    }

    @Transactional
    public AssinaturaPlataformaDto ativarPlano(Instituicao instituicao, PlanoInstituicaoTipo plano) {
        ExcecaoDeDominio.quandoNulo(instituicao, "Instituição não encontrada.");
        ExcecaoDeDominio.quandoNulo(plano, "Informe o tipo de plano.");
        validarTrialDisponivel(instituicao, plano);
        aplicarRegrasFinanceirasTrial(instituicao, plano);
        instituicaoRepository.save(instituicao);

        LocalDate inicio = LocalDate.now();
        Long instituicaoId = instituicao.getId();
        AssinaturaPlataforma assinatura = assinaturaRepository.findByInstituicao_Id(instituicaoId)
                .orElse(AssinaturaPlataforma.builder().instituicao(instituicao).build());
        assinatura.setPlano(plano);
        assinatura.setDataInicio(inicio);
        assinatura.setDataFim(plano.calcularFim(inicio));
        assinatura.setAtivo(true);
        return AssinaturaPlataformaDto.of(assinaturaRepository.save(assinatura));
    }

    public void validarTrialDisponivel(Instituicao instituicao, PlanoInstituicaoTipo plano) {
        if (plano == PlanoInstituicaoTipo.TRIAL_7_DIAS
                && Boolean.TRUE.equals(instituicao.getTrialUtilizado())) {
            throw new ExcecaoDeDominio(
                    "O teste grátis de 7 dias já foi utilizado por esta instituição.");
        }
    }

    private void aplicarRegrasFinanceirasTrial(Instituicao instituicao, PlanoInstituicaoTipo plano) {
        if (plano == PlanoInstituicaoTipo.TRIAL_7_DIAS) {
            instituicao.setTrialUtilizado(true);
            instituicao.setStatusFinanceiro(StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO);
        } else if (instituicao.getStatusFinanceiro() == null
                || instituicao.getStatusFinanceiro() == StatusFinanceiroInstituicao.NAO_APLICAVEL) {
            instituicao.setStatusFinanceiro(StatusFinanceiroInstituicao.PENDENTE_PAGAMENTO);
        }
    }
}
