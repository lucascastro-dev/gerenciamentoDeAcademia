package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.servicos.aluno.ConsultaDeAlunos;
import gerenciamentoDeAcademia.servicos.turma.ConsultaDeTurma;
import gerenciamentoDeAcademia.servicos.turma.MontadorDeTurma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    ConsultaDeAlunos consultaDeAlunos;

    @PostMapping("/montarTurma")
    public ResponseEntity<String> turmaMontada(@RequestBody TurmaDto turmaDto) {
        montadorDeTurma.montar(turmaDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Turma cadastrada com sucesso!");
    }

    @GetMapping("/listarTurmas")
    public List<TurmaDto> listarTurma() {
        return consultaDeTurma.listarTurmas().stream().map(TurmaDto::new).collect(Collectors.toList());
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

//    @PutMapping("/turma/{id}/adicionarAluno")
//    public ResponseEntity adicionarAlunoNaTurma(@PathVariable Long id, @RequestParam String cpf) {
//        Optional<Turma> optionalTurma = consultaDeTurma.buscarTurmaPorId(id);
//
//        if (optionalTurma.isPresent()) {
//            Turma turma = optionalTurma.get();
//
//            Optional<AlunoDto> optionalAluno = Optional.ofNullable(consultaDeAlunos.consultaAlunoPorCpf(cpf));
//
//            if (optionalAluno.isPresent()) {
//                AlunoDto alunoExistente = optionalAluno.get();
//
//                turma.getAlunos().add(alunoExistente);
//
//                Turma turmaAtualizada = montadorDeTurma.montar(turma);
//
//                return ResponseEntity.ok(turmaAtualizada);
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @DeleteMapping("/turmas/{id}/removerAluno")
//    public ResponseEntity removerAlunoDaTurma(@PathVariable Long id, @RequestParam String cpf) {
//        Optional<Turma> optionalTurma = consultaDeTurma.buscarTurmaPorId(id);
//
//        if (optionalTurma.isPresent()) {
//            Turma turma = optionalTurma.get();
//
//            Optional<AlunoDto> optionalAluno = Optional.ofNullable(consultaDeAlunos.consultaAlunoPorCpf(cpf));
//
//            if (optionalAluno.isPresent()) {
//                AlunoDto alunoExistente = optionalAluno.get();
//
//                turma.getAlunos().remove(alunoExistente);
//
//                Turma turmaAtualizada = montadorDeTurma.montar(turma);
//
//                return ResponseEntity.ok(turmaAtualizada);
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
}