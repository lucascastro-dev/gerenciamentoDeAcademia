package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.servicos.CadastradorDeFuncionario;
import gerenciamentoDeAcademia.servicos.ConsultaDeFuncionario;
import gerenciamentoDeAcademia.servicos.ExcluirFuncionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> funcionario(@RequestBody FuncionarioDto funcionarioDto) {
        cadastradorDeFuncionario.cadastrar(funcionarioDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Funcionario cadastrado com sucesso!");
    }

    @DeleteMapping("/excluirFuncionario/{cpf}")
    public ResponseEntity<String> exlcuirFuncionarioPorCpf(@PathVariable("cpf") String cpf) {
        excluirFuncionario.excluirCadastro(cpf);
        return new ResponseEntity<>("Funcionário excluído com sucesso!", HttpStatus.OK);
    }

    @GetMapping("/consultarFuncionario")
    public List<FuncionarioDto> listarFuncionarios() {
        return consultaDeFuncionario.listarFuncionarios();
    }

    @GetMapping("/revision/{id}")
    public List<String> revisions(@PathVariable("id") Long id) {
        return consultaDeFuncionario.listarLogs(id);
    }
}
