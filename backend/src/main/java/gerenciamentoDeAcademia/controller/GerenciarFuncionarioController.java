package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.dto.AlterarSenhaDto;
import gerenciamentoDeAcademia.dto.AuditoriaRevisionDto;
import gerenciamentoDeAcademia.servicos.funcionario.AlteradorSenha;
import gerenciamentoDeAcademia.servicos.funcionario.CadastradorDeFuncionario;
import gerenciamentoDeAcademia.servicos.funcionario.ConsultaDeFuncionario;
import gerenciamentoDeAcademia.servicos.funcionario.ExcluirFuncionario;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.servicos.master.ServicoDelegacaoSubMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("funcionario")
@CrossOrigin("*")
public class GerenciarFuncionarioController {
    @Autowired
    CadastradorDeFuncionario cadastradorDeFuncionario;
    @Autowired
    ExcluirFuncionario excluirFuncionario;
    @Autowired
    ConsultaDeFuncionario consultaDeFuncionario;
    @Autowired
    AlteradorSenha alteradorSenha;

    @Autowired
    ServicoDelegacaoSubMaster servicoDelegacaoSubMaster;

    @GetMapping("/meuPerfil")
    public Funcionario meuPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario.getFuncionario();
    }

    @PutMapping("/meuPerfil")
    public ResponseEntity<String> atualizarMeuPerfil(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody FuncionarioDto funcionarioDto) {
        Funcionario funcionario = usuario.getFuncionario();
        funcionarioDto.setCpf(funcionario.getCpf());
        funcionarioDto.setTipoFuncionario(funcionario.getTipoFuncionario());
        funcionarioDto.setCadastroAtivo(funcionario.getCadastroAtivo());
        cadastradorDeFuncionario.atualizarMeuPerfil(funcionario, funcionarioDto);
        return ResponseEntity.ok("Dados atualizados com sucesso!");
    }

    @PutMapping("/alterarSenha")
    public ResponseEntity<String> alterarSenha(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody AlterarSenhaDto dto) {
        alteradorSenha.alterarSenhaDoUsuarioLogado(usuario.getFuncionario().getCpf(), dto);
        return ResponseEntity.ok("Senha alterada com sucesso!");
    }

    @PostMapping("/cadastrarFuncionario")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'funcionario:cadastrar')")
    public ResponseEntity<String> cadastrarFuncionario(@RequestBody FuncionarioDto funcionarioDto) {
        cadastradorDeFuncionario.cadastrar(funcionarioDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Funcionario cadastrado com sucesso!");
    }

    @PostMapping("/preCadastroColaborador")
    public ResponseEntity<String> preCadastroColaborador(@RequestBody FuncionarioDto funcionarioDto) {
        cadastradorDeFuncionario.cadastrarPreCadastro(funcionarioDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                "Pré-cadastro realizado. O RH da instituição irá ativar seu acesso e definir sua função.");
    }

    @GetMapping("/tipos")
    public TipoFuncionario[] listarTiposFuncionario() {
        return TipoFuncionario.values();
    }

    @PutMapping("/editarFuncionario")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'funcionario:editar')")
    public ResponseEntity<String> editarFuncionario(@RequestBody FuncionarioDto funcionarioDto) {
        funcionarioDto.setPermitirGerenciarFuncoes(null);
        cadastradorDeFuncionario.editar(funcionarioDto);
        return ResponseEntity.status(HttpStatus.OK).body("Funcionario editado com sucesso!");
    }

    @PutMapping("/{cpf}/sub-master")
    @PreAuthorize("@permissaoEvaluator.possuiMasterRaiz(authentication)")
    public ResponseEntity<String> definirSubMaster(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("cpf") String cpf,
            @RequestBody java.util.Map<String, Boolean> body) {
        boolean habilitar = body != null && Boolean.TRUE.equals(body.get("habilitar"));
        servicoDelegacaoSubMaster.definirSubMaster(usuario.getFuncionario(), cpf, habilitar);
        return ResponseEntity.ok(habilitar
                ? "Operador sub-master habilitado."
                : "Operador sub-master desabilitado.");
    }

    @DeleteMapping("/excluirFuncionario/{cpf}")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'funcionario:excluir')")
    public ResponseEntity<String> exlcuirFuncionarioPorCpf(@PathVariable("cpf") String cpf) {
        excluirFuncionario.excluirCadastro(cpf);
        return new ResponseEntity<>("Funcionário excluído com sucesso!", HttpStatus.OK);
    }

    @GetMapping("/consultarPorCpf/{cpf}")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'funcionario:consultar')")
    public Funcionario consultarPorCpf(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("cpf") String cpf) {
        String cpfLimpo = cpf.replaceAll("\\D", "");
        Long instituicaoId = usuario.getInstituicaoId();
        return consultaDeFuncionario.consultarFuncionarioPorCpfEscopo(
                cpfLimpo, instituicaoId, usuario.isOperadorPlataforma());
    }

    @GetMapping("/consultarFuncionario")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'funcionario:consultar')")
    public List<Funcionario> listarFuncionarios() {
        return consultaDeFuncionario.listarFuncionarios();
    }

    @GetMapping("/revision/{id}")
    @PreAuthorize("@permissaoEvaluator.possui(authentication, 'auditoria:consultar') or @permissaoEvaluator.possui(authentication, 'funcionario:auditoria')")
    public List<AuditoriaRevisionDto> revisions(@PathVariable("id") Long id) {
        return consultaDeFuncionario.listarRevisoesDetalhadas(id);
    }
}



