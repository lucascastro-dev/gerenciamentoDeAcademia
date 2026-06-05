package gerenciamentoDeAcademia.servicos.instituicao;

import gerenciamentoDeAcademia.dto.AssinaturaPlataformaDto;
import gerenciamentoDeAcademia.dto.AtivarInstituicaoRequest;
import gerenciamentoDeAcademia.dto.InstituicaoDetalheDto;
import gerenciamentoDeAcademia.dto.TrocarAdministradorRequest;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.enums.UserRole;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import org.springframework.http.HttpStatus;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import gerenciamentoDeAcademia.servicos.plano.ServicoAssinaturaPlataforma;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicoAtivacaoInstituicao {

    private final InstituicaoRepository instituicaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicoAssinaturaPlataforma servicoAssinaturaPlataforma;

    @Transactional
    public InstituicaoDetalheDto ativarUnidade(AtivarInstituicaoRequest request) {
        ExcecaoDeDominio.quandoNulo(request, "Dados de ativação são obrigatórios.");
        String cnpj = normalizarCnpj(request.getCnpj());
        String cpfAdmin = normalizarCpf(request.getCpfAdministrador());
        PlanoInstituicaoTipo plano = request.getPlano();
        ExcecaoDeDominio.quandoNuloOuVazio(cnpj, "Informe o CNPJ da instituição.");
        ExcecaoDeDominio.quandoNuloOuVazio(cpfAdmin, "Informe o CPF do administrador.");
        ExcecaoDeDominio.quandoNulo(plano, "Informe o plano.");

        Instituicao instituicao = instituicaoRepository.findByCnpj(cnpj);
        ExcecaoDeDominio.quandoNulo(instituicao, "Instituição não cadastrada na plataforma. Cadastre-a em Nova instituição.");
        ExcecaoDeDominio.quando(Boolean.TRUE.equals(instituicao.getCadastroAtivo()),
                "Instituição já está ativa.");

        Funcionario administrador = funcionarioRepository.findByCpf(cpfAdmin);
        ExcecaoDeDominio.quandoNulo(administrador,
                "CPF não encontrado. O administrador deve ter pré-cadastro na plataforma.");

        instituicao.setCadastroAtivo(true);
        vincularAdministrador(instituicao, administrador);
        instituicaoRepository.save(instituicao);

        AssinaturaPlataformaDto assinatura = servicoAssinaturaPlataforma.ativarPlano(instituicao, plano);
        return InstituicaoDetalheDto.of(instituicao, assinatura);
    }

    @Transactional
    public InstituicaoDetalheDto ativarCadastro(String cnpj, PlanoInstituicaoTipo plano) {
        ExcecaoDeDominio.quandoNulo(plano, "Informe o plano.");
        String cnpjLimpo = normalizarCnpj(cnpj);
        Instituicao instituicao = instituicaoRepository.findByCnpj(cnpjLimpo);
        ExcecaoDeDominio.quandoNulo(instituicao, "Instituição não encontrada.");
        if (Boolean.TRUE.equals(instituicao.getCadastroAtivo())) {
            throw new ApplicationException("Instituição já está com cadastro ativo.", HttpStatus.BAD_REQUEST);
        }
        boolean temAdmin = instituicao.getFuncionarios().stream()
                .anyMatch(f -> f.getTipoFuncionario() == TipoFuncionario.ADMINISTRADOR);
        ExcecaoDeDominio.quando(!temAdmin,
                "Vincule um administrador antes de ativar o cadastro da instituição.");

        instituicao.setCadastroAtivo(true);
        instituicaoRepository.save(instituicao);
        AssinaturaPlataformaDto assinatura = servicoAssinaturaPlataforma.ativarPlano(instituicao, plano);
        return InstituicaoDetalheDto.of(instituicao, assinatura);
    }

    @Transactional
    public InstituicaoDetalheDto trocarAdministrador(TrocarAdministradorRequest request) {
        ExcecaoDeDominio.quandoNulo(request, "Dados obrigatórios.");
        String cnpj = normalizarCnpj(request.getCnpj());
        String cpfAdmin = normalizarCpf(request.getCpfAdministrador());
        ExcecaoDeDominio.quandoNuloOuVazio(cnpj, "Informe o CNPJ da instituição.");
        ExcecaoDeDominio.quandoNuloOuVazio(cpfAdmin, "Informe o CPF do administrador.");

        Instituicao instituicao = instituicaoRepository.findByCnpj(cnpj);
        ExcecaoDeDominio.quandoNulo(instituicao, "Instituição não encontrada.");

        Funcionario novoAdmin = funcionarioRepository.findByCpf(cpfAdmin);
        ExcecaoDeDominio.quandoNulo(novoAdmin,
                "CPF não encontrado. O administrador deve ter pré-cadastro na plataforma.");

        for (Funcionario vinculado : instituicao.getFuncionarios()) {
            if (vinculado.getTipoFuncionario() == TipoFuncionario.ADMINISTRADOR
                    && !cpfAdmin.equals(vinculado.getCpf())) {
                vinculado.setTipoFuncionario(TipoFuncionario.RECEPCIONISTA);
                vinculado.setCargo(TipoFuncionario.RECEPCIONISTA.getDescricao());
                funcionarioRepository.save(vinculado);
            }
        }

        vincularAdministrador(instituicao, novoAdmin);
        instituicaoRepository.save(instituicao);

        AssinaturaPlataformaDto assinatura = null;
        try {
            assinatura = servicoAssinaturaPlataforma.consultar(instituicao.getId());
        } catch (Exception ignored) {
            // opcional
        }
        return InstituicaoDetalheDto.of(instituicao, assinatura);
    }

    private void vincularAdministrador(Instituicao instituicao, Funcionario administrador) {
        administrador.setTipoFuncionario(TipoFuncionario.ADMINISTRADOR);
        administrador.setCargo(TipoFuncionario.ADMINISTRADOR.getDescricao());
        administrador.setAreaTerceirizado(null);
        if (!instituicao.getFuncionarios().contains(administrador)) {
            instituicao.getFuncionarios().add(administrador);
        }
        administrador.ativar();
        funcionarioRepository.save(administrador);

        String cpf = administrador.getCpf();
        if (!usuarioRepository.existsByLogin(cpf)) {
            usuarioRepository.save(Usuario.builder()
                    .login(cpf)
                    .password(passwordEncoder.encode(administrador.getSenha()))
                    .role(UserRole.USER)
                    .build());
        }
        instituicao.atualizarStatusPendencias();
    }

    private String normalizarCnpj(String cnpj) {
        return cnpj != null ? cnpj.replaceAll("\\D", "") : "";
    }

    private String normalizarCpf(String cpf) {
        return cpf != null ? cpf.replaceAll("\\D", "") : "";
    }
}
