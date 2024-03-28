package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.servicos.turma.AlteradorDeTurma;
import gerenciamentoDeAcademia.servicos.turma.ConsultaDeTurma;
import gerenciamentoDeAcademia.servicos.turma.ExluirTurma;
import gerenciamentoDeAcademia.servicos.turma.MontadorDeTurma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @PostMapping("/montarTurma")
    @ResponseStatus(HttpStatus.CREATED)
    public void montarTurma(@RequestBody TurmaDto turmaDto) {
        montadorDeTurma.montar(turmaDto);
    }

    @DeleteMapping("/excluirTurma/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirTurma(@PathVariable("id") Long id) {
        exluirTurma.excluir(id);
    }

    @PutMapping("/turma/Alterar")
    @ResponseStatus(HttpStatus.OK)
    public void alterarTurma(@RequestBody Turma turma) {
        alteradorDeTurma.alterarTurma(turma);
    }

    @PutMapping("/turma/adicionarAluno")
    @ResponseStatus(HttpStatus.OK)
    public void adicionarAlunoNaTurma(@RequestBody Turma turma) {
        alteradorDeTurma.adicionarAlunoNaTurma(turma);
    }

    @DeleteMapping("/turmas/removerAluno")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerAlunoDaTurma(@RequestBody Turma turma) {
        alteradorDeTurma.removerAlunoNaTurma(turma);
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
}
