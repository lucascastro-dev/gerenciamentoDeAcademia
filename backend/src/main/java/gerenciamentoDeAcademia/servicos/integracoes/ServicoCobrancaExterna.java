package gerenciamentoDeAcademia.servicos.integracoes;

import gerenciamentoDeAcademia.dto.MensalidadeResumoDto;
import gerenciamentoDeAcademia.dto.integracoes.CobrancaExternaDto;
import gerenciamentoDeAcademia.dto.integracoes.CriarCobrancaMensalidadeFormDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.CobrancaExterna;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.enums.StatusCobrancaExterna;
import gerenciamentoDeAcademia.enums.TipoCobrancaExterna;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.integracoes.ProvedorPagamento;
import gerenciamentoDeAcademia.infra.integracoes.RespostaCobrancaProvedor;
import gerenciamentoDeAcademia.infra.integracoes.SolicitacaoCobrancaExterna;
import gerenciamentoDeAcademia.repositorios.CobrancaExternaRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.servicos.financeiro.ServicoFinanceiro;
import gerenciamentoDeAcademia.servicos.plano.ServicoAssinaturaPlataforma;
import gerenciamentoDeAcademia.util.RelogioAplicacao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicoCobrancaExterna {

    private final CobrancaExternaRepository repository;
    private final InstituicaoRepository instituicaoRepository;
    private final ServicoFinanceiro servicoFinanceiro;
    private final ServicoAssinaturaPlataforma servicoAssinaturaPlataforma;
    private final ProvedorPagamento provedorPagamento;
    private final ServicoNotificacoes servicoNotificacoes;

    @Value("${app.integracoes.modo-local:true}")
    private boolean modoLocal;

    @Transactional
    public CobrancaExternaDto criarCobrancaMensalidadeAluno(Aluno aluno, Long instituicaoId) {
        return criarCobrancaMensalidadeAluno(aluno, instituicaoId, null, null, null);
    }

    @Transactional
    public CobrancaExternaDto criarCobrancaMensalidadeAluno(
            Aluno aluno,
            Long instituicaoId,
            Integer mesCompetencia,
            Integer anoCompetencia,
            CriarCobrancaMensalidadeFormDto form) {
        MensalidadeResumoDto resumo = servicoFinanceiro.resumoMensalidade(aluno.getCpf(), instituicaoId);
        ExcecaoDeDominio.quando(resumo.valorMensalidade() == null || resumo.valorMensalidade() <= 0,
                "Valor de mensalidade não configurado.");

        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));

        LocalDate hoje = RelogioAplicacao.hoje();
        int mes = mesCompetencia != null ? mesCompetencia : hoje.getMonthValue();
        int ano = anoCompetencia != null ? anoCompetencia : hoje.getYear();
        BigDecimal valor = BigDecimal.valueOf(resumo.valorMensalidade());

        String billingType = form != null && form.formaPagamento() != null ? form.formaPagamento() : "UNDEFINED";
        LocalDate vencimento = servicoFinanceiro.vencimentoCompetencia(aluno.getCpf(), instituicaoId, mes, ano);

        RespostaCobrancaProvedor resposta = provedorPagamento.criarCobranca(new SolicitacaoCobrancaExterna(
                TipoCobrancaExterna.MENSALIDADE_ALUNO,
                instituicaoId,
                aluno.getCpf(),
                aluno.getNome(),
                aluno.getEmail(),
                valor,
                "Mensalidade " + mes + "/" + ano,
                mes,
                ano,
                billingType,
                vencimento,
                form != null ? form.cartao() : null));

        CobrancaExterna cobranca = CobrancaExterna.builder()
                .instituicao(instituicao)
                .cpfAluno(aluno.getCpf())
                .tipo(TipoCobrancaExterna.MENSALIDADE_ALUNO)
                .status(StatusCobrancaExterna.PENDENTE)
                .valor(valor)
                .mesCompetencia(mes)
                .anoCompetencia(ano)
                .idExterno(resposta.idExterno())
                .urlPagamento(resposta.urlPagamento())
                .provedor(resposta.provedor())
                .billingType(resposta.billingType())
                .pixQrCode(resposta.pixQrCode())
                .pixCopiaCola(resposta.pixCopiaCola())
                .criadoEm(RelogioAplicacao.agora())
                .build();

        cobranca = repository.save(cobranca);

        String msg = "Turma360: mensalidade " + mes + "/" + ano + " disponível para pagamento.";
        if (aluno.getTelefone() != null) {
            servicoNotificacoes.avisarCobrancaWhatsApp(aluno.getTelefone(), msg);
        }
        if (aluno.getEmail() != null && !aluno.getEmail().isBlank()) {
            servicoNotificacoes.avisarCobrancaEmail(aluno.getEmail(), msg, resposta.urlPagamento());
        }

        return CobrancaExternaDto.of(cobranca, modoLocal);
    }

    @Transactional
    public CobrancaExternaDto criarCobrancaPlanoInstituicao(Long instituicaoId, PlanoInstituicaoTipo plano) {
        ExcecaoDeDominio.quando(plano == null || plano == PlanoInstituicaoTipo.TRIAL_7_DIAS,
                "Plano inválido para cobrança.");

        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));

        BigDecimal valor = valorPlano(plano);
        RespostaCobrancaProvedor resposta = provedorPagamento.criarCobranca(new SolicitacaoCobrancaExterna(
                TipoCobrancaExterna.PLANO_INSTITUICAO,
                instituicaoId,
                instituicao.getCnpj(),
                instituicao.getRazaoSocial(),
                instituicao.getEmail(),
                valor,
                "Plano Turma360 — " + plano.getDescricao(),
                null,
                null));

        CobrancaExterna cobranca = CobrancaExterna.builder()
                .instituicao(instituicao)
                .tipo(TipoCobrancaExterna.PLANO_INSTITUICAO)
                .status(StatusCobrancaExterna.PENDENTE)
                .valor(valor)
                .referencia(plano.name())
                .idExterno(resposta.idExterno())
                .urlPagamento(resposta.urlPagamento())
                .provedor(resposta.provedor())
                .criadoEm(RelogioAplicacao.agora())
                .build();

        return CobrancaExternaDto.of(repository.save(cobranca), modoLocal);
    }

    @Transactional(readOnly = true)
    public CobrancaExternaDto consultarCobrancaMensalidade(Long cobrancaId, Long instituicaoId, String cpfAluno) {
        CobrancaExterna cobranca = repository.findByIdAndInstituicao_Id(cobrancaId, instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Cobrança não encontrada."));
        ExcecaoDeDominio.quando(
                cobranca.getCpfAluno() == null || !cobranca.getCpfAluno().equals(cpfAluno.replaceAll("\\D", "")),
                "Cobrança não pertence a este aluno.");
        ExcecaoDeDominio.quando(
                cobranca.getTipo() != TipoCobrancaExterna.MENSALIDADE_ALUNO,
                "Cobrança inválida.");
        return CobrancaExternaDto.of(cobranca, modoLocal);
    }

    @Transactional
    public CobrancaExternaDto confirmarPagamento(Long cobrancaId, Long instituicaoId) {
        CobrancaExterna cobranca = repository.findByIdAndInstituicao_Id(cobrancaId, instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Cobrança não encontrada."));
        return confirmarPagamentoInterno(cobranca);
    }

    @Transactional
    public void processarWebhookAsaas(Map<String, Object> payload) {
        Object payment = payload.get("payment");
        if (!(payment instanceof Map<?, ?> paymentMap)) {
            log.warn("Webhook Asaas sem payment: {}", payload);
            return;
        }
        String idExterno = String.valueOf(paymentMap.get("id"));
        String status = String.valueOf(paymentMap.get("status"));
        if (!"RECEIVED".equalsIgnoreCase(status) && !"CONFIRMED".equalsIgnoreCase(status)) {
            return;
        }
        repository.findByIdExterno(idExterno).ifPresent(this::confirmarPagamentoInterno);
    }

    private CobrancaExternaDto confirmarPagamentoInterno(CobrancaExterna cobranca) {
        if (cobranca.getStatus() == StatusCobrancaExterna.PAGO) {
            return CobrancaExternaDto.of(cobranca, modoLocal);
        }
        cobranca.setStatus(StatusCobrancaExterna.PAGO);
        cobranca.setPagoEm(RelogioAplicacao.agora());

        if (cobranca.getTipo() == TipoCobrancaExterna.MENSALIDADE_ALUNO && cobranca.getCpfAluno() != null) {
            servicoFinanceiro.registrarBaixaManual(cobranca.getCpfAluno(), cobranca.getInstituicao().getId());
        } else if (cobranca.getTipo() == TipoCobrancaExterna.PLANO_INSTITUICAO && cobranca.getReferencia() != null) {
            PlanoInstituicaoTipo plano = PlanoInstituicaoTipo.valueOf(cobranca.getReferencia());
            servicoAssinaturaPlataforma.ativarPlano(cobranca.getInstituicao().getId(), plano);
        }

        return CobrancaExternaDto.of(repository.save(cobranca), modoLocal);
    }

    private BigDecimal valorPlano(PlanoInstituicaoTipo plano) {
        return switch (plano) {
            case MENSAL -> new BigDecimal("99.90");
            case TRIMESTRAL -> new BigDecimal("249.90");
            case SEMESTRAL -> new BigDecimal("449.90");
            case ANUAL -> new BigDecimal("799.90");
            default -> new BigDecimal("99.90");
        };
    }
}
