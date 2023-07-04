package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
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
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
public class GerenciarFuncionarioController {
    @Autowired
    CadastradorDeFuncionario cadastradorDeFuncionario;
    @Autowired
    ExcluirFuncionario excluirFuncionario;
    @Autowired
    ConsultaDeFuncionario consultaDeFuncionario;
    @Autowired
    FuncionarioRepository funcionarioRepository;

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

    @GetMapping("/revision/{id}")
    public List<String> revisions(@PathVariable("id") Long id) {
        return funcionarioRepository.findRevisions(id)
                .stream().map(Object::toString).collect(Collectors.toList());
    }
}
