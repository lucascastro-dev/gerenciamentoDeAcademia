package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.servicos.CadastradorDeAluno;
import gerenciamentoDeAcademia.servicos.ConsultaDeAlunos;
import gerenciamentoDeAcademia.servicos.DesmatricularAluno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
public class GerenciarAlunoController {
    @Autowired
    CadastradorDeAluno cadastradorDeAluno;
    @Autowired
    DesmatricularAluno desmatricularAluno;
    @Autowired
    ConsultaDeAlunos consultaDeAlunos;

    @PostMapping("/matricularAluno")
    public Aluno aluno(@RequestBody AlunoDto alunoDto) {
        return cadastradorDeAluno.cadastrar(alunoDto);
    }

    @DeleteMapping("/desmatricularAluno/{cpf}")
    public ResponseEntity<String> desmatricularAlunoPorCpf(@PathVariable("cpf") String cpf) {
        desmatricularAluno.excluirCadastro(cpf);

        return new ResponseEntity<>("Aluno desmatriculado com sucesso!", HttpStatus.OK);
    }

    @GetMapping("/consultarAluno")
    public List<Aluno> listarAlunos() {
        return consultaDeAlunos.listarAlunos();
    }

    @GetMapping("/consultarAluno/{cpf}")
    public ResponseEntity<Aluno> consultarAlunoPorCpf(@PathVariable("cpf") String cpf) {
        var aluno = consultaDeAlunos.consultaAlunoPorCpf(cpf);

        return aluno != null ? ResponseEntity.ok(aluno) : ResponseEntity.notFound().build();
    }
}
