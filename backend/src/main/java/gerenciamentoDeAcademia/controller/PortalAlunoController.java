package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AlterarSenhaDto;
import gerenciamentoDeAcademia.dto.MensalidadeHistoricoItemDto;
import gerenciamentoDeAcademia.dto.MensalidadeResumoDto;
import gerenciamentoDeAcademia.dto.PortalAlunoDadosDto;
import gerenciamentoDeAcademia.dto.TurmaResumoDto;
import gerenciamentoDeAcademia.dto.integracoes.CobrancaExternaDto;
import gerenciamentoDeAcademia.dto.integracoes.CriarCobrancaMensalidadeFormDto;
import gerenciamentoDeAcademia.servicos.integracoes.ServicoCobrancaExterna;
import gerenciamentoDeAcademia.servicos.integracoes.ServicoNotificacoes;
import gerenciamentoDeAcademia.servicos.funcionario.AlteradorSenha;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.servicos.aluno.ServicoPortalAluno;
import gerenciamentoDeAcademia.servicos.financeiro.ServicoFinanceiro;
import gerenciamentoDeAcademia.servicos.programacao.ServicoProgramacaoAluno;
import gerenciamentoDeAcademia.dto.ItemProgramacaoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("portal-aluno")
public class PortalAlunoController {

    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private ServicoPortalAluno servicoPortalAluno;
    @Autowired
    private ServicoFinanceiro servicoFinanceiro;
    @Autowired
    private AlteradorSenha alteradorSenha;
    @Autowired
    private ServicoProgramacaoAluno servicoProgramacaoAluno;
    @Autowired
    private ServicoCobrancaExterna servicoCobrancaExterna;
    @Autowired
    private ServicoNotificacoes servicoNotificacoes;

    private Aluno alunoLogado(UsuarioAutenticado usuario) {
        ExcecaoDeDominio.quando(!usuario.isPortalAluno(), "Acesso exclusivo do portal do aluno.");
        Aluno aluno = alunoRepository.findByCpf(usuario.getUsername());
        ExcecaoDeDominio.quandoNulo(aluno, "Cadastro de aluno não encontrado.");
        return aluno;
    }

    @GetMapping("/meus-dados")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:dados')")
    public PortalAlunoDadosDto meusDados(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        Aluno aluno = alunoLogado(usuario);
        MensalidadeResumoDto financeiro = null;
        if (usuario.getInstituicaoId() != null) {
            try {
                financeiro = servicoFinanceiro.resumoMensalidade(aluno.getCpf(), usuario.getInstituicaoId());
            } catch (ExcecaoDeDominio ignored) {
                // sem matrícula financeira nesta instituição
            }
        }
        return PortalAlunoDadosDto.of(aluno, financeiro);
    }

    @GetMapping("/minhas-turmas")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:turmas')")
    public List<TurmaResumoDto> minhasTurmas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicoPortalAluno.listarMinhasTurmas(usuario);
    }

    @GetMapping("/minha-programacao")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:programacao')")
    public List<ItemProgramacaoDto> minhaProgramacao(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicoProgramacaoAluno.listarPorAlunoEInstituicao(
                alunoLogado(usuario), usuario.getInstituicaoId());
    }

    @PutMapping("/alterar-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:senha')")
    public void alterarSenha(@AuthenticationPrincipal UsuarioAutenticado usuario, @RequestBody AlterarSenhaDto dto) {
        alteradorSenha.alterarSenhaPortalAluno(alunoLogado(usuario).getCpf(), dto);
    }

    @GetMapping("/mensalidade")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:mensalidades')")
    public MensalidadeResumoDto mensalidade(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        Aluno aluno = alunoLogado(usuario);
        ExcecaoDeDominio.quandoNulo(usuario.getInstituicaoId(), "Instituição não identificada na sessão.");
        return servicoFinanceiro.resumoMensalidade(aluno.getCpf(), usuario.getInstituicaoId());
    }

    @GetMapping("/mensalidades/historico")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:mensalidades')")
    public List<MensalidadeHistoricoItemDto> historicoMensalidades(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @org.springframework.web.bind.annotation.RequestParam Integer ano) {
        Aluno aluno = alunoLogado(usuario);
        ExcecaoDeDominio.quandoNulo(usuario.getInstituicaoId(), "Instituição não identificada na sessão.");
        ExcecaoDeDominio.quandoNulo(ano, "Informe o ano.");
        return servicoFinanceiro.listarHistoricoAnual(aluno.getCpf(), usuario.getInstituicaoId(), ano);
    }

    @GetMapping("/pagamento-info")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:pagamento')")
    public Map<String, Object> pagamentoInfo() {
        var status = servicoNotificacoes.status();
        String message = status.modoLocal()
                ? "Modo de teste local: gere um link simulado e confirme o pagamento sem Asaas."
                : (status.asaasAtivo()
                ? "Pagamento online via Asaas (PIX, boleto ou cartão)."
                : "Pagamento online indisponível. Procure a secretaria da instituição.");
        return Map.of(
                "message", message,
                "modoLocal", status.modoLocal(),
                "asaasAtivo", status.asaasAtivo());
    }

    @PostMapping("/cobranca/mensalidade")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:pagamento')")
    public CobrancaExternaDto criarCobrancaMensalidade(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer mes,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer ano,
            @org.springframework.web.bind.annotation.RequestBody(required = false) CriarCobrancaMensalidadeFormDto form) {
        Aluno aluno = alunoLogado(usuario);
        ExcecaoDeDominio.quandoNulo(usuario.getInstituicaoId(), "Instituição não identificada na sessão.");
        return servicoCobrancaExterna.criarCobrancaMensalidadeAluno(
                aluno, usuario.getInstituicaoId(), mes, ano, form);
    }

    @GetMapping("/cobranca/mensalidade/{cobrancaId}")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:pagamento')")
    public CobrancaExternaDto consultarCobrancaMensalidade(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long cobrancaId) {
        Aluno aluno = alunoLogado(usuario);
        ExcecaoDeDominio.quandoNulo(usuario.getInstituicaoId(), "Instituição não identificada na sessão.");
        return servicoCobrancaExterna.consultarCobrancaMensalidade(
                cobrancaId, usuario.getInstituicaoId(), aluno.getCpf());
    }

    @PostMapping("/cobranca/mensalidade/{cobrancaId}/simular-pagamento")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:pagamento')")
    public CobrancaExternaDto simularPagamentoMensalidade(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long cobrancaId) {
        ExcecaoDeDominio.quandoNulo(usuario.getInstituicaoId(), "Instituição não identificada na sessão.");
        ExcecaoDeDominio.quando(!servicoNotificacoes.status().modoLocal(),
                "Simulação disponível apenas em modo local.");
        return servicoCobrancaExterna.confirmarPagamento(cobrancaId, usuario.getInstituicaoId());
    }
}
