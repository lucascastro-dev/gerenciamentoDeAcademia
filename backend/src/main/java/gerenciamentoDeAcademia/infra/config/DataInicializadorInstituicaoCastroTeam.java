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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Instituição Judô Castro Team e colaboradores RH/Financeiro para testes de perfis administrativos.
 * Senha padrão: 123456
 */
@Component
@Profile({"docker", "local"})
@ConditionalOnProperty(name = "app.seed.demo-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataInicializadorInstituicaoCastroTeam {

    private static final Logger log = LoggerFactory.getLogger(DataInicializadorInstituicaoCastroTeam.class);
    private static final String RAZAO_SOCIAL = "Judô Castro Team";
    private static final String CNPJ_CASTRO = "33444555000178";
    private static final String SENHA = "123456";

    private static final String CPF_RH = "90714464090";
    private static final String CPF_FINANCEIRO = "11144477735";

    private final InstituicaoRepository instituicaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicoAssinaturaPlataforma servicoAssinaturaPlataforma;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void inicializar() {
        Instituicao instituicao = instituicaoRepository.findFirstByRazaoSocialIgnoreCase(RAZAO_SOCIAL)
                .orElseGet(() -> instituicaoRepository.findByCnpj(CNPJ_CASTRO));

        if (instituicao == null) {
            instituicao = new Instituicao();
            instituicao.setRazaoSocial(RAZAO_SOCIAL);
            instituicao.setCnpj(CNPJ_CASTRO);
            instituicao.setCadastroAtivo(true);
            instituicao = instituicaoRepository.save(instituicao);
            log.info("Instituição {} criada (CNPJ {}).", RAZAO_SOCIAL, CNPJ_CASTRO);
        }

        servicoAssinaturaPlataforma.garantirTrial(instituicao);

        criarOuVincular(instituicao, CPF_RH, "Maria RH Castro Team", TipoFuncionario.RH);
        criarOuVincular(instituicao, CPF_FINANCEIRO, "Carlos Financeiro Castro Team", TipoFuncionario.FINANCEIRO);
    }

    private void criarOuVincular(Instituicao instituicao, String cpf, String nome, TipoFuncionario tipo) {
        String senhaHash = passwordEncoder.encode(SENHA);
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf);
        if (funcionario == null) {
            funcionario = Funcionario.builder()
                    .nome(nome)
                    .rg("RG" + cpf.substring(0, 6))
                    .cpf(cpf)
                    .dataDeNascimento(LocalDate.of(1988, 4, 12))
                    .endereco("{\"logradouro\":\"Rua Castro\",\"numero\":\"100\",\"cidade\":\"Rio de Janeiro\",\"uf\":\"RJ\"}")
                    .telefone("21987654321")
                    .email(nome.toLowerCase().replace(' ', '.') + "@castro.team")
                    .tipoFuncionario(tipo)
                    .cargo(tipo.getDescricao())
                    .especializacao("N/A")
                    .cadastroAtivo(true)
                    .permitirGerenciarFuncoes(false)
                    .senha(SENHA)
                    .build();
            funcionario = funcionarioRepository.save(funcionario);
        } else {
            funcionario.setNome(nome);
            funcionario.setTipoFuncionario(tipo);
            funcionario.setCargo(tipo.getDescricao());
            funcionario.setCadastroAtivo(true);
            funcionarioRepository.save(funcionario);
        }

        if (!instituicao.getFuncionarios().contains(funcionario)) {
            instituicao.getFuncionarios().add(funcionario);
        }

        Usuario usuario = usuarioRepository.findByLogin(cpf);
        if (usuario == null) {
            usuarioRepository.save(Usuario.builder()
                    .login(cpf)
                    .password(senhaHash)
                    .role(UserRole.USER)
                    .build());
        } else {
            usuario.setPassword(senhaHash);
            usuarioRepository.save(usuario);
        }

        instituicaoRepository.save(instituicao);
        log.info("Colaborador {} ({}) — CPF {} senha {} vinculado à {}.",
                nome, tipo, cpf, SENHA, RAZAO_SOCIAL);
    }
}
