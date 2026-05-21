package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.DashboardResumoDto;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AlunoRepository alunoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final TurmaRepository turmaRepository;

    @GetMapping("/resumo")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'dashboard:visualizar')")
    public DashboardResumoDto resumo() {
        long alunosAtivos = alunoRepository.count();
        long funcionarios = funcionarioRepository.count();
        long turmas = turmaRepository.count();
        long funcionariosAtivos = funcionarioRepository.findAll().stream()
                .filter(f -> Boolean.TRUE.equals(f.getCadastroAtivo()))
                .count();

        return new DashboardResumoDto(alunosAtivos, funcionarios, funcionariosAtivos, turmas);
    }
}
