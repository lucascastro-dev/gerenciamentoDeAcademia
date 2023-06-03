package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.ConsultaDeTurma;
import gerenciamentoDeAcademia.servicos.MontadorDeTurma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
public class GerenciarTurmaController {

    @Autowired
    MontadorDeTurma montadorDeTurma;
    @Autowired
    ConsultaDeTurma consultaDeTurma;
    @Autowired
    TurmaRepository turmaRepository;
    @Autowired
    AlunoRepository alunoRepository;

    @PostMapping("/montarTurma")
    public Turma turmaMontada(@RequestBody TurmaDto turmaDto) {
        return montadorDeTurma.montar(turmaDto);
    }

    @GetMapping("/listarTurmas")
    public List<Turma> listarTurma() {
        return consultaDeTurma.listarTurmas();
    }

    @GetMapping("/consultarTurmaCodigo/{id}")
    public ResponseEntity consultarTurmaPorId(@PathVariable Long id) {
        var turma = consultaDeTurma.buscarTurmaPorId(id);

        return turma != null ? ResponseEntity.ok(turma) : ResponseEntity.notFound().build();
    }

    @GetMapping("/consultarTurmaModalidade/{modalidade}")
    public ResponseEntity consultarTurmaPorModalidade(@PathVariable String modalidade) {
        var turma = consultaDeTurma.buscarTurmaPorModalidade(modalidade);

        return turma != null ? ResponseEntity.ok(turma) : ResponseEntity.notFound().build();
    }

    @PutMapping("/turma/{id}/adicionarAluno")
    public ResponseEntity adicionarAlunoNaTurma(@PathVariable Long id, @RequestParam String cpf) {
        Optional<Turma> optionalTurma = turmaRepository.findById(id);

        if (optionalTurma.isPresent()) {
            Turma turma = optionalTurma.get();

            Optional<Aluno> optionalAluno = Optional.ofNullable(alunoRepository.findByCpf(cpf));

            if (optionalAluno.isPresent()) {
                Aluno alunoExistente = optionalAluno.get();

                turma.getAlunos().add(alunoExistente);

                Turma turmaAtualizada = turmaRepository.save(turma);

                return ResponseEntity.ok(turmaAtualizada);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/turmas/{id}/removerAluno")
    public ResponseEntity removerAlunoDaTurma(@PathVariable Long id, @RequestParam String cpf) {
        Optional<Turma> optionalTurma = turmaRepository.findById(id);

        if (optionalTurma.isPresent()) {
            Turma turma = optionalTurma.get();

            Optional<Aluno> optionalAluno = Optional.ofNullable(alunoRepository.findByCpf(cpf));

            if (optionalAluno.isPresent()) {
                Aluno alunoExistente = optionalAluno.get();

                turma.getAlunos().remove(alunoExistente);

                Turma turmaAtualizada = turmaRepository.save(turma);

                return ResponseEntity.ok(turmaAtualizada);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}