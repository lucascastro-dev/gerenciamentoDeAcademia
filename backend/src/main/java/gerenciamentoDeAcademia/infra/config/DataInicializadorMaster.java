package gerenciamentoDeAcademia.infra.config;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.enums.UserRole;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import gerenciamentoDeAcademia.servicos.plano.ServicoAssinaturaPlataforma;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Cria ou sincroniza usuário master (operador da plataforma) com PostgreSQL/Docker.
 */
@Component
@Profile({"docker", "local"})
@RequiredArgsConstructor
public class DataInicializadorMaster {

    private static final Logger log = LoggerFactory.getLogger(DataInicializadorMaster.class);

    private final FuncionarioRepository funcionarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicoAssinaturaPlataforma servicoAssinaturaPlataforma;

    @Value("${app.master.cpf}")
    private String masterCpf;

    @Value("${app.master.password}")
    private String masterPassword;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void inicializarMaster() {
        Instituicao instituicaoExistente = instituicaoRepository.findByCnpj("00000000000191");
        if (instituicaoExistente != null) {
            servicoAssinaturaPlataforma.garantirTrial(instituicaoExistente);
        }
        Funcionario master = funcionarioRepository.findByCpf(masterCpf);
        if (master == null) {
            master = Funcionario.builder()
                    .nome("Administrador Master")
                    .rg("0000000")
                    .cpf(masterCpf)
                    .dataDeNascimento(LocalDate.of(1990, 1, 1))
                    .endereco("Sede")
                    .telefone("00000000000")
                    .tipoFuncionario(TipoFuncionario.OPERADOR_PLATAFORMA)
                    .cargo(TipoFuncionario.OPERADOR_PLATAFORMA.getDescricao())
                    .especializacao("Gestão")
                    .permitirGerenciarFuncoes(false)
                    .senha(masterPassword)
                    .cadastroAtivo(true)
                    .build();
            master = funcionarioRepository.save(master);
        } else {
            master.setTipoFuncionario(TipoFuncionario.OPERADOR_PLATAFORMA);
            master.setCargo(TipoFuncionario.OPERADOR_PLATAFORMA.getDescricao());
            master.setCadastroAtivo(true);
            master.setPermitirGerenciarFuncoes(false);
            funcionarioRepository.save(master);
        }

        if (usuarioRepository.existsByLogin(masterCpf)) {
            log.info("Usuário master já existe (CPF: {}); perfil sincronizado como operador da plataforma.", masterCpf);
            return;
        }

        Instituicao instituicao = instituicaoRepository.findByCnpj("00000000000191");
        if (instituicao == null) {
            instituicao = new Instituicao();
            instituicao.setRazaoSocial("Instituição Master");
            instituicao.setCnpj("00000000000191");
            instituicao.setCadastroAtivo(true);
            instituicao = instituicaoRepository.save(instituicao);
        }
        if (!instituicao.getFuncionarios().contains(master)) {
            instituicao.getFuncionarios().add(master);
            instituicaoRepository.save(instituicao);
        }

        Usuario usuario = Usuario.builder()
                .login(masterCpf)
                .password(passwordEncoder.encode(masterPassword))
                .role(UserRole.ADMIN)
                .build();
        usuarioRepository.save(usuario);

        servicoAssinaturaPlataforma.garantirTrial(instituicao);

        log.warn("Usuário MASTER criado. CPF: {} | Altere a senha em produção!", masterCpf);
    }
}
