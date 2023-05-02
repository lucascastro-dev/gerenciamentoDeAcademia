package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.servicos.MontadorDeTurma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
public class GerenciarTurmaController {

    @Autowired
    MontadorDeTurma montadorDeTurma;

    @PostMapping("/montarTurma")
    public Turma turmaMontada(@RequestBody TurmaDto turmaDto) {
        return montadorDeTurma.montar(turmaDto);
    }
}