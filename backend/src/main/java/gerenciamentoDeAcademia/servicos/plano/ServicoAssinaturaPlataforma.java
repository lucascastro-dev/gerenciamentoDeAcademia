package gerenciamentoDeAcademia.servicos.plano;

import gerenciamentoDeAcademia.dto.AssinaturaPlataformaDto;
import gerenciamentoDeAcademia.entidades.AssinaturaPlataforma;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
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
        LocalDate inicio = LocalDate.now();
        AssinaturaPlataforma assinatura = assinaturaRepository.findByInstituicao_Id(instituicaoId)
                .orElse(AssinaturaPlataforma.builder().instituicao(instituicao).build());
        assinatura.setPlano(plano);
        assinatura.setDataInicio(inicio);
        assinatura.setDataFim(plano.calcularFim(inicio));
        assinatura.setAtivo(true);
        return AssinaturaPlataformaDto.of(assinaturaRepository.save(assinatura));
    }
}
