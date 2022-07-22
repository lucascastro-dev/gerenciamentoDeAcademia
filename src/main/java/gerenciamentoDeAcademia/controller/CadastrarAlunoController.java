package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.AlunoCadastrado;
import gerenciamentoDeAcademia.servicos.CadastradorDeAluno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/aluno")
@CrossOrigin("*")
public class CadastrarAlunoController {

    @Autowired
    CadastradorDeAluno cadastradorDeAluno;

    @PostMapping("/cadastrar")
    public AlunoCadastrado alunoCadastrado(@RequestBody Aluno aluno) {
        return cadastradorDeAluno.cadastrar(aluno);
    }
}