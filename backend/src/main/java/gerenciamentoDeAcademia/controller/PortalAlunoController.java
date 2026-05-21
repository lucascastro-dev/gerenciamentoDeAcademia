package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.MensalidadeResumoDto;
import gerenciamentoDeAcademia.dto.PortalAlunoDadosDto;
import gerenciamentoDeAcademia.dto.TurmaResumoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.financeiro.ServicoFinanceiro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
        return turmaRepository.findAll().stream()
                .filter(t -> t.getAlunos().stream().anyMatch(a -> cpf.equals(a.getCpf())))
                .map(TurmaResumoDto::of)
                .collect(Collectors.toList());
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
