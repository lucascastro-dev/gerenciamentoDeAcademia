package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.servicos.aluno.CadastradorDeAluno;
import gerenciamentoDeAcademia.servicos.aluno.ConsultaDeAlunos;
import gerenciamentoDeAcademia.servicos.aluno.DesmatricularAluno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("aluno")
@CrossOrigin("*")
public class GerenciarAlunoController {
    @Autowired
    CadastradorDeAluno cadastradorDeAluno;
    @Autowired
    DesmatricularAluno desmatricularAluno;
    @Autowired
    ConsultaDeAlunos consultaDeAlunos;

    @PostMapping("/matricularAluno")
    public ResponseEntity<String> aluno(@RequestBody AlunoDto alunoDto) {
        cadastradorDeAluno.cadastrar(alunoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Aluno cadastrado com sucesso!");
    }

    @DeleteMapping("/desmatricularAluno/{cpf}")
    public ResponseEntity<String> desmatricularAlunoPorCpf(@PathVariable("cpf") String cpf) {
        desmatricularAluno.excluirCadastro(cpf);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Aluno desmatriculado com sucesso!");
    }

    @GetMapping("/consultarAluno")
    public List<AlunoDto> listarAlunos() {
        return consultaDeAlunos.listarAlunos();
    }

    @GetMapping("/consultarAluno/{cpf}")
    public ResponseEntity<AlunoDto> consultarAlunoPorCpf(@PathVariable("cpf") String cpf) {
        var aluno = consultaDeAlunos.consultaAlunoPorCpf(cpf);

        return aluno != null ? ResponseEntity.ok(aluno) : ResponseEntity.notFound().build();
    }
}
