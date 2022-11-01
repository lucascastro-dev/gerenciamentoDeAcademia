package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.dto.TurmaDto;
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
    public AlunoCadastrado alunoCadastrado(@RequestBody AlunoDto alunoDto) {
        return cadastradorDeAluno.cadastrar(alunoDto);
    }

    @PostMapping("/cadastrarFuncionario")
    public FuncionarioCadastrado funcionarioCadastrado(@RequestBody FuncionarioDto funcionarioDto) {
        return cadastradorDeFuncionario.cadastrar(funcionarioDto);
    }

    @PostMapping("/montarTurma")
    public TurmaMontada turmaMontada(@RequestBody TurmaDto turmaDto) {
        return montadorDeTurma.montar(turmaDto);
    }
}