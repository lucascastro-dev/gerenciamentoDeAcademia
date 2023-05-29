package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.servicos.CadastradorDeFuncionario;
import gerenciamentoDeAcademia.servicos.ConsultaDeFuncionario;
import gerenciamentoDeAcademia.servicos.ExcluirFuncionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
public class GerenciarFuncionarioController {
    @Autowired
    CadastradorDeFuncionario cadastradorDeFuncionario;
    @Autowired
    ExcluirFuncionario excluirFuncionario;
    @Autowired
    ConsultaDeFuncionario consultaDeFuncionario;

    @PostMapping("/cadastrarFuncionario")
    public Funcionario funcionario(@RequestBody FuncionarioDto funcionarioDto) {
        return cadastradorDeFuncionario.cadastrar(funcionarioDto);
    }

    @DeleteMapping("/excluirFuncionario/{cpf}")
    public ResponseEntity<String> exlcuirFuncionarioPorCpf(@PathVariable("cpf") String cpf) {
        excluirFuncionario.excluirCadastro(cpf);
        return new ResponseEntity<>("Funcionário excluído com sucesso!", HttpStatus.OK);
    }

    @GetMapping("/consultarFuncionario")
    public List<Funcionario> listarFuncionarios() {
        return consultaDeFuncionario.listarFuncionarios();
    }
}
