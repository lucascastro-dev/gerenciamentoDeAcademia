package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AlterarSenhaDto;
import gerenciamentoDeAcademia.dto.MensalidadeResumoDto;
import gerenciamentoDeAcademia.dto.PortalAlunoDadosDto;
import gerenciamentoDeAcademia.dto.TurmaResumoDto;
import gerenciamentoDeAcademia.servicos.funcionario.AlteradorSenha;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.financeiro.ServicoFinanceiro;
import gerenciamentoDeAcademia.servicos.programacao.ServicoProgramacaoAluno;
import gerenciamentoDeAcademia.dto.ItemProgramacaoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("portal-aluno")
public class PortalAlunoController {

    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private TurmaRepository turmaRepository;
    @Autowired
    private ServicoFinanceiro servicoFinanceiro;
    @Autowired
    private AlteradorSenha alteradorSenha;
    @Autowired
    private ServicoProgramacaoAluno servicoProgramacaoAluno;

    private Aluno alunoLogado(UsuarioAutenticado usuario) {
        ExcecaoDeDominio.quando(!usuario.isPortalAluno(), "Acesso exclusivo do portal do aluno.");
        Aluno aluno = alunoRepository.findByCpf(usuario.getUsername());
        ExcecaoDeDominio.quandoNulo(aluno, "Cadastro de aluno não encontrado.");
        return aluno;
    }

    @GetMapping("/meus-dados")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:dados')")
    public PortalAlunoDadosDto meusDados(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return PortalAlunoDadosDto.of(alunoLogado(usuario));
    }

    @GetMapping("/minhas-turmas")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:turmas')")
    public List<TurmaResumoDto> minhasTurmas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        String cpf = alunoLogado(usuario).getCpf();
        Long instituicaoId = usuario.getInstituicaoId();
        if (instituicaoId == null) {
            return List.of();
        }
        return turmaRepository.findByAlunos_CpfAndInstituicao_Id(cpf, instituicaoId).stream()
                .map(TurmaResumoDto::of)
                .collect(Collectors.toList());
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
        return servicoFinanceiro.resumoMensalidade(alunoLogado(usuario).getCpf());
    }

    @GetMapping("/pagamento-info")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno-portal:pagamento')")
    public Map<String, String> pagamentoInfo() {
        return Map.of(
                "message",
                "Pagamento online será integrado em breve (gateway). Enquanto isso, realize o pagamento na secretaria da instituição."
        );
    }
}
