package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.servicos.CadastradorDeAluno;
import gerenciamentoDeAcademia.servicos.CadastradorDeFuncionario;
import gerenciamentoDeAcademia.servicos.DesmatricularAluno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
public class GerenciarPessoasController {

    @Autowired
    CadastradorDeAluno cadastradorDeAluno;
    @Autowired
    DesmatricularAluno desmatricularAluno;
    @Autowired
    CadastradorDeFuncionario cadastradorDeFuncionario;

    @PostMapping("/matricularAluno")
    public Aluno aluno(@RequestBody AlunoDto alunoDto) {
        return cadastradorDeAluno.cadastrar(alunoDto);
    }

    @DeleteMapping("/desmatricularAluno/{cpf}")
    public ResponseEntity<String> desmatricularAlunoPorCpf(@PathVariable("cpf") String cpf) {
        desmatricularAluno.desmatricular(cpf);
        return new ResponseEntity<>("Aluno desmatriculado com sucesso!", HttpStatus.OK);
    }

    @PostMapping("/cadastrarFuncionario")
    public Funcionario funcionario(@RequestBody FuncionarioDto funcionarioDto) {
        return cadastradorDeFuncionario.cadastrar(funcionarioDto);
    }
}
