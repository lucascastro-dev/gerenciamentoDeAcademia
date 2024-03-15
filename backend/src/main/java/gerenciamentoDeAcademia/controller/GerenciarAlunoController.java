package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.servicos.aluno.AlteadorDeDadosDoAluno;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    @Autowired
    AlteadorDeDadosDoAluno alteadorDeDadosDoAluno;

    @PostMapping("/matricularAluno")
    @ResponseStatus(HttpStatus.CREATED)
    public void matricularAluno(@RequestBody AlunoDto alunoDto) {
        cadastradorDeAluno.cadastrar(alunoDto);
    }

    @DeleteMapping("/desmatricularAluno/{cpf}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desmatricularAlunoPorCpf(@PathVariable("cpf") String cpf) {
        desmatricularAluno.excluirCadastro(cpf);
    }

    @PutMapping("/alterarAluno")
    @ResponseStatus(HttpStatus.OK)
    public void alterarAluno(@RequestBody AlunoDto alunoDto) {
        alteadorDeDadosDoAluno.alterarAluno(alunoDto);
    }

    @GetMapping("/consultarAluno")
    @ResponseStatus(HttpStatus.OK)
    public List<AlunoDto> listarAlunos() {
        return consultaDeAlunos.listarAlunos();
    }

    @GetMapping("/consultarAluno/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Aluno> consultarAlunoPorCpf(@PathVariable("cpf") String cpf) {
        var aluno = consultaDeAlunos.consultaAlunoPorCpf(cpf);

        return aluno != null ? ResponseEntity.ok(aluno) : ResponseEntity.notFound().build();
    }
}
