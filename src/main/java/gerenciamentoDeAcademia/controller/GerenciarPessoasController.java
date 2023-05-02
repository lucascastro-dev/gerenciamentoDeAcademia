package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.servicos.CadastradorDeAluno;
import gerenciamentoDeAcademia.servicos.CadastradorDeFuncionario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
public class GerenciarPessoasController {
    @Autowired
    CadastradorDeAluno cadastradorDeAluno;
    @Autowired
    CadastradorDeFuncionario cadastradorDeFuncionario;

    @PostMapping("/cadastrarAluno")
    public Aluno alunoCadastrado(@RequestBody AlunoDto alunoDto) {
        return cadastradorDeAluno.cadastrar(alunoDto);
    }

    @PostMapping("/cadastrarFuncionario")
    public Funcionario funcionarioCadastrado(@RequestBody FuncionarioDto funcionarioDto) {
        return cadastradorDeFuncionario.cadastrar(funcionarioDto);
    }
}
