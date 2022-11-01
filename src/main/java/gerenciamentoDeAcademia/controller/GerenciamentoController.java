package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.entidades.*;
import gerenciamentoDeAcademia.servicos.CadastradorDeAluno;
import gerenciamentoDeAcademia.servicos.CadastradorDeFuncionario;
import gerenciamentoDeAcademia.servicos.MontadorDeTurma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
public class GerenciamentoController {

    @Autowired
    CadastradorDeAluno cadastradorDeAluno;
    @Autowired
    CadastradorDeFuncionario cadastradorDeFuncionario;
    @Autowired
    MontadorDeTurma montadorDeTurma;

    @PostMapping("/cadastrarAluno")
    public AlunoCadastrado alunoCadastrado(@RequestBody Aluno aluno) {
        return cadastradorDeAluno.cadastrar(aluno);
    }

    @PostMapping("/cadastrarFuncionario")
    public FuncionarioCadastrado funcionarioCadastrado(@RequestBody Funcionario funcionario) {
        return cadastradorDeFuncionario.cadastrar(funcionario);
    }
}