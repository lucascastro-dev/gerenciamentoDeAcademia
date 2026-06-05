package gerenciamentoDeAcademia.servicos.cobranca;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.AssinaturaPlataforma;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.TipoAcesso;
import gerenciamentoDeAcademia.repositorios.AssinaturaPlataformaRepository;
import gerenciamentoDeAcademia.entidades.MatriculaInstituicao;
import gerenciamentoDeAcademia.servicos.aluno.ServicoMatriculaInstituicao;
import gerenciamentoDeAcademia.servicos.financeiro.ServicoFinanceiro;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ServicoSituacaoCobranca {

    public static final String MSG_BLOQUEIO_ALUNO =
            "Acesso bloqueado por pendência financeira com a instituição. Entre em contato com o SAC.";
    public static final String MSG_BLOQUEIO_INSTITUICAO =
            "Acesso bloqueado por pendência com a plataforma. Entre em contato com o SAC da EduGestão Inteligente.";
    public static final String MSG_ALERTA_ALUNO =
            "Sua mensalidade está em atraso. Regularize em até 5 dias para evitar o bloqueio do acesso.";
    public static final String MSG_ALERTA_INSTITUICAO =
            "O plano desta instituição está em atraso. Renove em até 5 dias para evitar o bloqueio do sistema.";

    private final AssinaturaPlataformaRepository assinaturaRepository;
    private final ServicoFinanceiro servicoFinanceiro;
    private final ServicoMatriculaInstituicao servicoMatriculaInstituicao;

    @Value("${app.cobranca.dias-tolerancia:5}")
    private int diasTolerancia;

    public SituacaoCobranca situacaoPlanoInstituicao(Long instituicaoId) {
        if (instituicaoId == null) {
            return SituacaoCobranca.BLOQUEADO;
        }
        return assinaturaRepository.findByInstituicao_Id(instituicaoId)
                .map(this::situacaoAssinatura)
                .orElse(SituacaoCobranca.BLOQUEADO);
    }

    public SituacaoCobranca situacaoMensalidadeAluno(Aluno aluno, Long instituicaoId) {
        if (aluno == null || instituicaoId == null) {
            return SituacaoCobranca.BLOQUEADO;
        }
        MatriculaInstituicao matricula = servicoMatriculaInstituicao.obterOuMigrarLegado(aluno.getCpf(), instituicaoId);
        return servicoFinanceiro.situacaoMensalidade(matricula, LocalDate.now(), diasTolerancia);
    }

    public SituacaoCobranca situacaoParaLogin(TipoAcesso tipoAcesso, Long instituicaoId, Aluno aluno) {
        if (tipoAcesso == TipoAcesso.ALUNO) {
            return situacaoMensalidadeAluno(aluno, instituicaoId);
        }
        return situacaoPlanoInstituicao(instituicaoId);
    }

    public String mensagemBloqueioLogin(TipoAcesso tipoAcesso) {
        return tipoAcesso == TipoAcesso.ALUNO ? MSG_BLOQUEIO_ALUNO : MSG_BLOQUEIO_INSTITUICAO;
    }

    public String mensagemAlerta(TipoAcesso tipoAcesso, SituacaoCobranca situacao) {
        if (!situacao.exibeAlerta()) {
            return null;
        }
        return tipoAcesso == TipoAcesso.ALUNO ? MSG_ALERTA_ALUNO : MSG_ALERTA_INSTITUICAO;
    }

    private SituacaoCobranca situacaoAssinatura(AssinaturaPlataforma assinatura) {
        if (!Boolean.TRUE.equals(assinatura.getAtivo()) || assinatura.getDataFim() == null) {
            return SituacaoCobranca.BLOQUEADO;
        }
        LocalDate hoje = LocalDate.now();
        if (assinatura.getDataInicio() != null && hoje.isBefore(assinatura.getDataInicio())) {
            return SituacaoCobranca.BLOQUEADO;
        }
        if (!hoje.isAfter(assinatura.getDataFim())) {
            return SituacaoCobranca.ATIVO;
        }
        long diasAtraso = ChronoUnit.DAYS.between(assinatura.getDataFim(), hoje);
        if (diasAtraso <= diasTolerancia) {
            return SituacaoCobranca.EM_TOLERANCIA;
        }
        return SituacaoCobranca.BLOQUEADO;
    }
}
