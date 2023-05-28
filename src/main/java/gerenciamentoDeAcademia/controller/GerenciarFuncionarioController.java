package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.servicos.CadastradorDeFuncionario;
import gerenciamentoDeAcademia.servicos.ExcluirFuncionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
public class GerenciarFuncionarioController {
    @Autowired
    CadastradorDeFuncionario cadastradorDeFuncionario;
    @Autowired
    ExcluirFuncionario excluirFuncionario;

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
