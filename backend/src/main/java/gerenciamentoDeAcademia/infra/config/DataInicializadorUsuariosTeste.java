package gerenciamentoDeAcademia.infra.config;

import gerenciamentoDeAcademia.entidades.Academia;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.AreaTerceirizado;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.enums.UserRole;
import gerenciamentoDeAcademia.repositorios.AcademiaRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Usuários de teste por perfil — senha: 123 (apenas ambiente local/docker).
 */
@Component
@Profile({"docker", "local"})
@RequiredArgsConstructor
public class DataInicializadorUsuariosTeste {

    private static final Logger log = LoggerFactory.getLogger(DataInicializadorUsuariosTeste.class);
    private static final String SENHA_TESTE = "123";
    private static final String CNPJ_MASTER = "00000000000191";

    private final FuncionarioRepository funcionarioRepository;
    private final AcademiaRepository academiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private record PerfilTeste(String cpf, String nome, TipoFuncionario tipo) {}

    private static final PerfilTeste[] PERFIS = {
            new PerfilTeste("52998224725", "Teste Financeiro", TipoFuncionario.FINANCEIRO),
            new PerfilTeste("71428793860", "Teste RH", TipoFuncionario.RH),
            new PerfilTeste("39053344705", "Teste TI", TipoFuncionario.TI),
            new PerfilTeste("94325755004", "Teste Administrador", TipoFuncionario.ADMINISTRADOR),
            new PerfilTeste("86833851085", "Teste Recepção", TipoFuncionario.RECEPCIONISTA),
            new PerfilTeste("61482582007", "Teste Professor", TipoFuncionario.PROFESSOR),
            new PerfilTeste("58236123030", "Teste Estagiário", TipoFuncionario.ESTAGIARIO),
            new PerfilTeste("74572288020", "Teste Serviços Gerais", TipoFuncionario.SERVICOS_GERAIS),
            new PerfilTeste("45449941013", "Teste Terceirizado RH", TipoFuncionario.TERCEIRIZADO),
    };

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void criarUsuariosTeste() {
        Academia academia = academiaRepository.findByCnpj(CNPJ_MASTER);
        if (academia == null) {
            log.warn("Instituição master não encontrada; usuários de teste não criados.");
            return;
        }

        String senhaHash = passwordEncoder.encode(SENHA_TESTE);

        for (PerfilTeste perfil : PERFIS) {
            if (usuarioRepository.existsByLogin(perfil.cpf())) {
                continue;
            }
            Funcionario f = funcionarioRepository.findByCpf(perfil.cpf());
            if (f == null) {
                f = Funcionario.builder()
                        .nome(perfil.nome())
                        .rg("RG" + perfil.cpf().substring(0, 6))
                        .cpf(perfil.cpf())
                        .dataDeNascimento(LocalDate.of(1995, 6, 15))
                        .endereco("{\"logradouro\":\"Rua Teste\",\"numero\":\"1\",\"cidade\":\"Rio de Janeiro\",\"uf\":\"RJ\"}")
                        .telefone("21999990000")
                        .tipoFuncionario(perfil.tipo())
                        .cargo(perfil.tipo().getDescricao())
                        .especializacao(perfil.tipo() == TipoFuncionario.PROFESSOR ? "Judô" : "N/A")
                        .areaTerceirizado(perfil.tipo() == TipoFuncionario.TERCEIRIZADO
                                ? AreaTerceirizado.RH : null)
                        .permitirGerenciarFuncoes(false)
                        .senha(SENHA_TESTE)
                        .cadastroAtivo(true)
                        .build();
                f = funcionarioRepository.save(f);
            } else {
                f.setCadastroAtivo(true);
                f.setTipoFuncionario(perfil.tipo());
                funcionarioRepository.save(f);
            }
            if (!academia.getFuncionarios().contains(f)) {
                academia.getFuncionarios().add(f);
            }
            usuarioRepository.save(Usuario.builder()
                    .login(perfil.cpf())
                    .password(senhaHash)
                    .role(UserRole.USER)
                    .build());
            log.info("Usuário teste {} — CPF {} senha {}", perfil.tipo(), perfil.cpf(), SENHA_TESTE);
        }
        academiaRepository.save(academia);
    }
}
