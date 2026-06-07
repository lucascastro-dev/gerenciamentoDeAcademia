package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AlunoConsultaCompletaDto;
import gerenciamentoDeAcademia.dto.AlunoConsultaProfessorDto;
import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.aluno.AlteadorDeDadosDoAluno;
import gerenciamentoDeAcademia.servicos.aluno.CadastradorDeAluno;
import gerenciamentoDeAcademia.servicos.aluno.ConsultaDeAlunos;
import gerenciamentoDeAcademia.servicos.aluno.DesmatricularAluno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("aluno")
@CrossOrigin("*")
public class GerenciarAlunoController {
    @Autowired
    CadastradorDeAluno cadastradorDeAluno;
    @Autowired
    DesmatricularAluno desmatricularAluno;
    @Autowired
    ConsultaDeAlunos consultaDeAlunos;
    @Autowired
    AlteadorDeDadosDoAluno alteadorDeDadosDoAluno;

    @PostMapping("/matricularAluno")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno:matricular')")
    public void matricularAluno(@RequestBody AlunoDto alunoDto) {
        cadastradorDeAluno.cadastrar(alunoDto);
    }

    @DeleteMapping("/desmatricularAluno/{cpf}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno:desmatricular')")
    public void desmatricularAlunoPorCpf(@PathVariable("cpf") String cpf) {
        desmatricularAluno.excluirCadastro(cpf);
    }

    @PutMapping("/alterarAluno")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno:editar')")
    public void alterarAluno(@RequestBody AlunoDto alunoDto) {
        alteadorDeDadosDoAluno.alterarAluno(alunoDto);
    }

    @GetMapping("/consultarAluno")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno:consultar')")
    public List<Aluno> listarAlunos(@RequestParam("instituicaoId") Long instituicaoId) {
        return consultaDeAlunos.listarAlunos(instituicaoId);
    }

    @GetMapping("/consultarAluno/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno:consultar')")
    public ResponseEntity<Aluno> consultarAlunoPorCpf(
            @PathVariable("cpf") String cpf,
            @RequestParam("instituicaoId") Long instituicaoId) {
        return ResponseEntity.ok(consultaDeAlunos.consultaAlunoPorCpf(cpf, instituicaoId));
    }

    @GetMapping("/consultarPorCpf/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno:consultar')")
    public AlunoConsultaCompletaDto consultarPorCpf(
            @PathVariable("cpf") String cpf,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return consultaDeAlunos.consultaCompletaPorCpf(cpf.replaceAll("\\D", ""), usuario);
    }

    @GetMapping("/professor/consultar/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'aluno:consultar')")
    public AlunoConsultaProfessorDto consultarProfessorPorCpf(
            @PathVariable("cpf") String cpf,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return consultaDeAlunos.consultaProfessorPorCpf(cpf.replaceAll("\\D", ""), usuario);
    }
}
