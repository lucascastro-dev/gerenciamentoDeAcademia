package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AlunoTurmaProfessorDto;
import gerenciamentoDeAcademia.dto.ProfessorResumoDto;
import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.dto.TurmaResumoDto;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.turma.AlteradorDeTurma;
import gerenciamentoDeAcademia.servicos.turma.ConsultaDeTurma;
import gerenciamentoDeAcademia.servicos.turma.ExluirTurma;
import gerenciamentoDeAcademia.servicos.turma.MontadorDeTurma;
import gerenciamentoDeAcademia.servicos.turma.ServicoTurmaProfessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("turma")
public class GerenciarTurmaController {

    @Autowired
    MontadorDeTurma montadorDeTurma;
    @Autowired
    ConsultaDeTurma consultaDeTurma;
    @Autowired
    ExluirTurma exluirTurma;
    @Autowired
    AlteradorDeTurma alteradorDeTurma;
    @Autowired
    TurmaRepository turmaRepository;
    @Autowired
    InstituicaoRepository instituicaoRepository;
    @Autowired
    ServicoTurmaProfessor servicoTurmaProfessor;

    @GetMapping("/professores")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:gerenciar')")
    public List<ProfessorResumoDto> professoresDaInstituicao(@RequestParam("instituicaoId") Long instituicaoId) {
        return instituicaoRepository.findProfessoresAtivosPorInstituicao(instituicaoId).stream()
                .map(ProfessorResumoDto::of)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/professor")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:gerenciar')")
    public void vincularProfessor(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String cpf = body != null ? body.get("cpfProfessor") : null;
        alteradorDeTurma.vincularProfessor(id, cpf);
    }

    @GetMapping("/professor/minhas")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:consultar')")
    public List<TurmaResumoDto> minhasTurmas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        String cpf = usuario.getUsername();
        return turmaRepository.findByProfessor_Cpf(cpf).stream()
                .map(TurmaResumoDto::of)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/alunos")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:consultar')")
    public List<AlunoTurmaProfessorDto> alunosDaTurma(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicoTurmaProfessor.listarAlunos(id, usuario);
    }

    @PostMapping("/professor/{id}/alunos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:gerenciar-alunos')")
    public void adicionarAlunoProfessor(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        servicoTurmaProfessor.adicionarAluno(id, body != null ? body.get("cpf") : null, usuario);
    }

    @DeleteMapping("/professor/{id}/alunos/{cpf}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:gerenciar-alunos')")
    public void removerAlunoProfessor(
            @PathVariable Long id,
            @PathVariable String cpf,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        servicoTurmaProfessor.removerAluno(id, cpf, usuario);
    }

    @PostMapping("/montarTurma")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:gerenciar')")
    public void montarTurma(@RequestBody TurmaDto turmaDto) {
        montadorDeTurma.montar(turmaDto);
    }

    @DeleteMapping("/excluirTurma/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:gerenciar')")
    public void excluirTurma(@PathVariable("id") Long id) {
        exluirTurma.excluir(id);
    }

    @PutMapping("/turma/Alterar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:gerenciar')")
    public void alterarTurma(@RequestBody Turma turma) {
        alteradorDeTurma.alterarTurma(turma);
    }

    @PutMapping("/turma/adicionarAluno")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:gerenciar')")
    public void adicionarAlunoNaTurma(@RequestBody Turma turma) {
        alteradorDeTurma.adicionarAlunoNaTurma(turma);
    }

    @DeleteMapping("/turmas/removerAluno")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:gerenciar')")
    public void removerAlunoDaTurma(@RequestBody Turma turma) {
        alteradorDeTurma.removerAlunoNaTurma(turma);
    }

    @GetMapping("/listarTurmas")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:consultar')")
    public List<Turma> listarTurma(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(required = false) Long instituicaoId,
            @RequestParam(required = false) String professorCpf,
            @RequestParam(required = false) List<String> dias) {
        if (instituicaoId != null || (professorCpf != null && !professorCpf.isBlank())
                || (dias != null && !dias.isEmpty())) {
            return consultaDeTurma.listarTurmas(usuario, instituicaoId, professorCpf, dias);
        }
        if (usuario != null && usuario.isOperadorPlataforma()) {
            return consultaDeTurma.listarTurmas();
        }
        return consultaDeTurma.listarTurmas(usuario, null, null, null);
    }

    @GetMapping("/consultarTurmaCodigo/{id}")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:consultar')")
    public ResponseEntity consultarTurmaPorId(@PathVariable Long id) {
        var turma = consultaDeTurma.buscarTurmaPorId(id);
        return turma != null ? ResponseEntity.ok(turma) : ResponseEntity.notFound().build();
    }

    @GetMapping("/consultarTurmaModalidade/{modalidade}")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'turma:consultar')")
    public ResponseEntity consultarTurmaPorModalidade(@PathVariable String modalidade) {
        var turma = consultaDeTurma.buscarTurmaPorModalidade(modalidade);
        return turma != null ? ResponseEntity.ok(turma) : ResponseEntity.notFound().build();
    }
}
