package gerenciamentoDeAcademia.servicos.plano;

import gerenciamentoDeAcademia.dto.AssinaturaPlataformaDto;
import gerenciamentoDeAcademia.entidades.Academia;
import gerenciamentoDeAcademia.entidades.AssinaturaPlataforma;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AcademiaRepository;
import gerenciamentoDeAcademia.repositorios.AssinaturaPlataformaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ServicoAssinaturaPlataforma {

    private final AssinaturaPlataformaRepository assinaturaRepository;
    private final AcademiaRepository academiaRepository;

    public boolean instituicaoComPlanoAtivo(Long instituicaoId) {
        return assinaturaRepository.findByAcademia_Id(instituicaoId)
                .map(AssinaturaPlataforma::isVigente)
                .orElse(false);
    }

    public AssinaturaPlataformaDto consultar(Long instituicaoId) {
        return AssinaturaPlataformaDto.of(
                assinaturaRepository.findByAcademia_Id(instituicaoId).orElse(null));
    }

    @Transactional
    public void garantirTrial(Academia academia) {
        if (assinaturaRepository.existsByAcademia_Id(academia.getId())) {
            return;
        }
        LocalDate inicio = LocalDate.now();
        assinaturaRepository.save(AssinaturaPlataforma.builder()
                .academia(academia)
                .plano(PlanoInstituicaoTipo.TRIAL_7_DIAS)
                .dataInicio(inicio)
                .dataFim(PlanoInstituicaoTipo.TRIAL_7_DIAS.calcularFim(inicio))
                .ativo(true)
                .build());
    }

    @Transactional
    public AssinaturaPlataformaDto ativarPlano(Long instituicaoId, PlanoInstituicaoTipo plano) {
        ExcecaoDeDominio.quandoNulo(plano, "Informe o tipo de plano.");
        Academia academia = academiaRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));
        LocalDate inicio = LocalDate.now();
        AssinaturaPlataforma assinatura = assinaturaRepository.findByAcademia_Id(instituicaoId)
                .orElse(AssinaturaPlataforma.builder().academia(academia).build());
        assinatura.setPlano(plano);
        assinatura.setDataInicio(inicio);
        assinatura.setDataFim(plano.calcularFim(inicio));
        assinatura.setAtivo(true);
        return AssinaturaPlataformaDto.of(assinaturaRepository.save(assinatura));
    }
}
