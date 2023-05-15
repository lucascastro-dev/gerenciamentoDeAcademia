package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.servicos.CadastradorDeAluno;
import gerenciamentoDeAcademia.servicos.CadastradorDeFuncionario;
import gerenciamentoDeAcademia.servicos.DesmatricularAluno;
import gerenciamentoDeAcademia.servicos.ExcluirFuncionario;
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
    @Autowired
    ExcluirFuncionario excluirFuncionario;

    @PostMapping("/matricularAluno")
    public Aluno aluno(@RequestBody AlunoDto alunoDto) {
        return cadastradorDeAluno.cadastrar(alunoDto);
    }

    @DeleteMapping("/desmatricularAluno/{cpf}")
    public ResponseEntity<String> desmatricularAlunoPorCpf(@PathVariable("cpf") String cpf) {
        desmatricularAluno.excluirCadastro(cpf);
        return new ResponseEntity<>("Aluno desmatriculado com sucesso!", HttpStatus.OK);
    }

    @PostMapping("/cadastrarFuncionario")
    public Funcionario funcionario(@RequestBody FuncionarioDto funcionarioDto) {
        return cadastradorDeFuncionario.cadastrar(funcionarioDto);
    }

    @DeleteMapping("/excluirFuncionario/{cpf}")
    public ResponseEntity<String> exlcuirFuncionarioPorCpf(@PathVariable("cpf") String cpf) {
        excluirFuncionario.excluirCadastro(cpf);
        return new ResponseEntity<>("Funcionário excluído com sucesso!", HttpStatus.OK);
    }
}
