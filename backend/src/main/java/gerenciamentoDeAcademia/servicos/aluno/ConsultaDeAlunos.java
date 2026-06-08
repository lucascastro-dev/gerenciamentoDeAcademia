package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoConsultaCompletaDto;
import gerenciamentoDeAcademia.dto.AlunoConsultaProfessorDto;
import gerenciamentoDeAcademia.dto.AlunoMatriculaInstituicaoDto;
import gerenciamentoDeAcademia.dto.PessoaListagemDto;
import gerenciamentoDeAcademia.dto.TurmaResumoDto;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.repositorios.MatriculaInstituicaoRepository;
import gerenciamentoDeAcademia.excecao.ExcecaoDeAcesso;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.entidades.MatriculaInstituicao;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeAlunos;
import gerenciamentoDeAcademia.util.CpfUtil;
import gerenciamentoDeAcademia.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConsultaDeAlunos implements IConsultaDeAlunos {

    private final AlunoRepository alunoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final TurmaRepository turmaRepository;
    private final MatriculaInstituicaoRepository matriculaInstituicaoRepository;
    private final ServicoMatriculaInstituicao servicoMatriculaInstituicao;

    @Override
    public List<Aluno> listarAlunos(Long instituicaoId) {
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição é obrigatória para listar alunos.");
        return alunoRepository.findDistinctByTurma_Instituicao_IdOrderByNomeAsc(instituicaoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PessoaListagemDto> listarParaListagem(UsuarioAutenticado usuario) {
        boolean master = usuario != null && usuario.isOperadorPlataforma();

        if (master) {
            return alunoRepository.findAllByOrderByNomeAsc().stream()
                    .map(a -> PessoaListagemDto.deAluno(a, false))
                    .toList();
        }

        Long instituicaoId = usuario != null ? usuario.getInstituicaoId() : null;
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição não identificada na sessão.");

        boolean mascararCpf = false;
        return alunosVinculadosInstituicao(instituicaoId).stream()
                .map(a -> PessoaListagemDto.deAluno(a, mascararCpf))
                .toList();
    }

    /**
     * Alunos com matrícula na instituição ou vinculados a turmas da unidade
     * (mesmo critério operacional de Minhas turmas / adicionar aluno).
     */
    private List<Aluno> alunosVinculadosInstituicao(Long instituicaoId) {
        Map<Long, Aluno> porId = new LinkedHashMap<>();

        matriculaInstituicaoRepository.findByInstituicao_IdOrderByAluno_NomeAsc(instituicaoId).stream()
                .map(MatriculaInstituicao::getAluno)
                .filter(a -> a != null && a.getId() != null)
                .forEach(a -> porId.putIfAbsent(a.getId(), a));

        alunoRepository.findDistinctByTurma_Instituicao_IdOrderByNomeAsc(instituicaoId).stream()
                .filter(a -> a != null && a.getId() != null)
                .forEach(a -> porId.putIfAbsent(a.getId(), a));

        return new ArrayList<>(porId.values()).stream()
                .sorted(Comparator.comparing(Aluno::getNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    @Override
    public Aluno consultaAlunoPorCpf(String cpf, Long instituicaoId) {
        String cpfLimpo = CpfUtil.somenteDigitos(cpf);
        ExcecaoDeDominio.quando(cpfLimpo.length() != 11, "CPF obrigatório com 11 dígitos para consulta do aluno.");

        Aluno alunoEncontrado = alunoRepository.findByCpf(cpfLimpo);
        ExcecaoDeDominio.quandoNulo(alunoEncontrado, "Aluno não encontrado na base.");

        if (instituicaoId != null) {
            ExcecaoDeDominio.quando(
                    !instituicaoRepository.alunoVinculadoInstituicao(cpfLimpo, instituicaoId),
                    "Aluno não vinculado a esta instituição.");
        }

        return alunoEncontrado;
    }

    @Override
    @Transactional(readOnly = true)
    public AlunoConsultaCompletaDto consultaCompletaPorCpf(String cpf, UsuarioAutenticado usuario) {
        String cpfLimpo = CpfUtil.somenteDigitos(cpf);
        ExcecaoDeDominio.quando(cpfLimpo.length() != 11, "CPF obrigatório com 11 dígitos para consulta do aluno.");

        Aluno aluno = alunoRepository.findByCpf(cpfLimpo);
        ExcecaoDeDominio.quandoNulo(aluno, "Aluno não encontrado na base.");

        boolean master = usuario != null && usuario.isOperadorPlataforma();

        Long instituicaoFiltro = null;
        if (!master && usuario != null && usuario.getInstituicaoId() != null && usuario.getInstituicaoId() > 0) {
            instituicaoFiltro = usuario.getInstituicaoId();
            ExcecaoDeDominio.quando(
                    !instituicaoRepository.alunoVinculadoInstituicao(cpfLimpo, instituicaoFiltro),
                    "Aluno não vinculado a esta instituição.");
        }

        AlunoConsultaCompletaDto dto = new AlunoConsultaCompletaDto(aluno);
        dto.setMatriculas(montarMatriculas(cpfLimpo, instituicaoFiltro));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public AlunoConsultaProfessorDto consultaProfessorPorCpf(String cpf, UsuarioAutenticado usuario) {
        String cpfLimpo = CpfUtil.somenteDigitos(cpf);
        ExcecaoDeDominio.quando(cpfLimpo.length() != 11, "CPF obrigatório com 11 dígitos para consulta do aluno.");

        Aluno aluno = alunoRepository.findByCpf(cpfLimpo);
        if (aluno == null) {
            ExcecaoDeAcesso.naoEncontrado("Dados não encontrados.");
        }

        Long instituicaoId = usuario != null ? usuario.getInstituicaoId() : null;
        if (instituicaoId == null || instituicaoId <= 0) {
            ExcecaoDeAcesso.acessoNegado("Instituição não identificada na sessão.");
        }

        if (!instituicaoRepository.alunoVinculadoInstituicao(cpfLimpo, instituicaoId)) {
            ExcecaoDeAcesso.naoEncontrado("Dados não encontrados.");
        }

        List<TurmaResumoDto> turmas = turmaRepository.findTurmasMatriculadasPorCpf(cpfLimpo).stream()
                .filter(t -> t.getInstituicao() != null && instituicaoId.equals(IdUtil.toLong(t.getInstituicao().getId())))
                .map(TurmaResumoDto::of)
                .toList();

        return AlunoConsultaProfessorDto.of(aluno, turmas);
    }

    @Override
    @Transactional(readOnly = true)
    public AlunoConsultaProfessorDto consultaProfessorPorId(Long alunoId, UsuarioAutenticado usuario) {
        ExcecaoDeDominio.quandoNulo(alunoId, "Aluno não informado.");
        Aluno aluno = alunoRepository.findById(alunoId).orElse(null);
        if (aluno == null) {
            ExcecaoDeAcesso.naoEncontrado("Dados não encontrados.");
        }
        return consultaProfessorPorCpf(aluno.getCpf(), usuario);
    }

    private boolean ehProfessor(UsuarioAutenticado usuario) {
        return usuario != null
                && usuario.getFuncionario() != null
                && usuario.getFuncionario().getTipoFuncionario() == TipoFuncionario.PROFESSOR;
    }

    private java.util.List<AlunoMatriculaInstituicaoDto> montarMatriculas(String cpf, Long instituicaoFiltro) {
        java.util.List<Turma> turmas = new java.util.ArrayList<>(turmaRepository.findTurmasMatriculadasPorCpf(cpf));
        turmas.sort(Comparator
                .comparing((Turma t) -> t.getInstituicao() != null ? t.getInstituicao().getRazaoSocial() : "",
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Turma::getModalidade, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

        java.util.Map<Long, AlunoMatriculaInstituicaoDto> porInstituicao = new java.util.LinkedHashMap<>();

        for (Turma turma : turmas) {
            Instituicao inst = turma.getInstituicao();
            if (inst == null) {
                continue;
            }
            Long instId = IdUtil.toLong(inst.getId());
            if (instId == null) {
                continue;
            }
            if (instituicaoFiltro != null && !instituicaoFiltro.equals(instId)) {
                continue;
            }
            AlunoMatriculaInstituicaoDto mat = porInstituicao.computeIfAbsent(instId, id -> {
                AlunoMatriculaInstituicaoDto m = new AlunoMatriculaInstituicaoDto();
                m.setInstituicaoId(instId);
                m.setRazaoSocial(inst.getRazaoSocial());
                m.setTurmas(new java.util.ArrayList<>());
                MatriculaInstituicao financeiro = servicoMatriculaInstituicao.consultarFinanceiro(
                        alunoRepository.findByCpf(cpf), instId);
                if (financeiro != null) {
                    m.setValorMensalidade(financeiro.getValorMensalidade());
                    m.setDiaVencimentoMensalidade(financeiro.getDiaVencimentoMensalidade());
                    m.setDataUltimoPagamentoMensalidade(financeiro.getDataUltimoPagamentoMensalidade());
                }
                return m;
            });
            mat.getTurmas().add(TurmaResumoDto.of(turma));
        }
        return porInstituicao.values().stream()
                .sorted(Comparator.comparing(AlunoMatriculaInstituicaoDto::getRazaoSocial,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }
}
