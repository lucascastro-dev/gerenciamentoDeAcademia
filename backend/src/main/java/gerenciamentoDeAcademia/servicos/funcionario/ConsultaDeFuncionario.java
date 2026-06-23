package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.AuditoriaRevisionDto;
import gerenciamentoDeAcademia.dto.FuncionarioConsultaCompletaDto;
import gerenciamentoDeAcademia.dto.FuncionarioVinculoInstituicaoDto;
import gerenciamentoDeAcademia.dto.PessoaListagemDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import gerenciamentoDeAcademia.enums.PermissaoSistema;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.VinculoFuncionarioInstituicaoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeFuncionario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.history.Revision;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConsultaDeFuncionario implements IConsultaDeFuncionario {
    private final FuncionarioRepository funcionarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final VinculoFuncionarioInstituicaoRepository vinculoRepository;

    @Override
    public List<Funcionario> listarFuncionarios() {
        return funcionarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PessoaListagemDto> listarParaListagem(UsuarioAutenticado usuario) {
        boolean master = usuario != null && usuario.isOperadorPlataforma();
        List<VinculoFuncionarioInstituicao> vinculos;
        if (master) {
            vinculos = vinculoRepository.findAllComDetalhes();
        } else {
            Long instituicaoId = usuario != null ? usuario.getInstituicaoId() : null;
            ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição não identificada na sessão.");
            vinculos = vinculoRepository.findByInstituicaoIdComDetalhes(instituicaoId);
        }
        return vinculos.stream()
                .map(PessoaListagemDto::deVinculoFuncionario)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FuncionarioConsultaCompletaDto consultarCompletoPorCpf(String cpf, UsuarioAutenticado usuario) {
        boolean operadorPlataforma = usuario != null && usuario.isOperadorPlataforma();
        Long instituicaoId = usuario != null ? usuario.getInstituicaoId() : null;
        Funcionario funcionario = consultarFuncionarioPorCpfEscopo(
                cpf, instituicaoId, operadorPlataforma, podeConsultarParaVinculo(usuario));
        FuncionarioConsultaCompletaDto dto = new FuncionarioConsultaCompletaDto(funcionario);
        List<VinculoFuncionarioInstituicao> vinculos = vinculoRepository
                .findByFuncionarioCpfOrderByInstituicaoRazaoSocialAsc(cpf);
        if (!operadorPlataforma && instituicaoId != null && instituicaoId > 0) {
            vinculos = vinculos.stream()
                    .filter(v -> instituicaoId.equals(v.getInstituicao().getId()))
                    .toList();
        }
        dto.setVinculos(vinculos.stream().map(FuncionarioVinculoInstituicaoDto::de).toList());
        return dto;
    }

    @Override
    public Funcionario consultarFuncionarioPorCpf(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF obrigatório para consultar funcionário!");
        return funcionarioRepository.findByCpf(cpf);
    }

    public Funcionario consultarFuncionarioPorCpfEscopo(
            String cpf, Long instituicaoId, boolean operadorPlataforma, boolean podeConsultarParaVinculo) {
        Funcionario funcionario = consultarFuncionarioPorCpf(cpf);
        ExcecaoDeDominio.quandoNulo(funcionario, "Funcionário não encontrado.");
        if (operadorPlataforma || instituicaoId == null || instituicaoId <= 0 || podeConsultarParaVinculo) {
            return funcionario;
        }
        ExcecaoDeDominio.quando(
                !funcionarioVinculadoInstituicao(instituicaoId, cpf),
                "Funcionário não vinculado à instituição do seu acesso.");
        return funcionario;
    }

    private boolean funcionarioVinculadoInstituicao(Long instituicaoId, String cpf) {
        if (instituicaoRepository.existsByCnpjAndFuncionarioCpf(instituicaoId, cpf)) {
            return true;
        }
        return vinculoRepository.findByFuncionarioCpfAndInstituicaoId(cpf, instituicaoId).isPresent();
    }

    private boolean podeConsultarParaVinculo(UsuarioAutenticado usuario) {
        if (usuario == null || usuario.isOperadorPlataforma()) {
            return true;
        }
        return usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(this::permiteConsultaPreVinculo);
    }

    private boolean permiteConsultaPreVinculo(String codigo) {
        return PermissaoSistema.FUNCIONARIO_ATIVAR.getCodigo().equals(codigo)
                || PermissaoSistema.FUNCIONARIO_CADASTRAR.getCodigo().equals(codigo)
                || PermissaoSistema.FUNCIONARIO_EDITAR.getCodigo().equals(codigo);
    }

    public List<String> listarLogs(Long id) {
        return listarRevisoesDetalhadas(id).stream()
                .map(r -> "Rev. " + r.revisionNumber() + " — " + r.nome() + " (" + r.tipoFuncionario() + ")")
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditoriaRevisionDto> listarRevisoesDetalhadas(Long id) {
        ExcecaoDeDominio.quandoNulo(id, "ID do funcionário é obrigatório");
        var page = funcionarioRepository.findRevisions(id, PageRequest.of(0, 100));
        return StreamSupport.stream(page.spliterator(), false)
                .sorted((a, b) -> Long.compare(
                        b.getRevisionNumber().orElse(0L),
                        a.getRevisionNumber().orElse(0L)))
                .map(this::mapearRevisao)
                .collect(Collectors.toList());
    }

    private AuditoriaRevisionDto mapearRevisao(Revision<Long, Funcionario> revision) {
        Funcionario f = revision.getEntity();
        String endereco = f != null && f.getEndereco() != null
                ? (f.getEndereco().length() > 80 ? f.getEndereco().substring(0, 80) + "…" : f.getEndereco())
                : "";
        return new AuditoriaRevisionDto(
                revision.getRevisionNumber().orElse(0L),
                revision.getRevisionInstant().orElse(null),
                f != null ? f.getCpf() : null,
                f != null ? f.getNome() : null,
                f != null && f.getTipoFuncionario() != null ? f.getTipoFuncionario().name() : null,
                f != null ? f.getCadastroAtivo() : null,
                endereco
        );
    }
}
