package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.dto.AlunoTurmaProfessorDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.util.CpfUtil;
import gerenciamentoDeAcademia.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoTurmaProfessor {

    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final ServicoEscopoProfessor servicoEscopoProfessor;

    @Transactional(readOnly = true)
    public List<AlunoTurmaProfessorDto> listarAlunos(Long turmaId, UsuarioAutenticado usuario) {
        Turma turma = servicoEscopoProfessor.exigirTurmaDoProfessor(turmaId, usuario);
        return turma.getAlunos().stream()
                .sorted(Comparator.comparing(Aluno::getNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(AlunoTurmaProfessorDto::of)
                .toList();
    }

    @Transactional
    public void adicionarAluno(Long turmaId, String cpf, UsuarioAutenticado usuario) {
        Turma turma = servicoEscopoProfessor.exigirTurmaDoProfessor(turmaId, usuario);
        String cpfLimpo = CpfUtil.somenteDigitos(cpf);
        ExcecaoDeDominio.quando(cpfLimpo.length() != 11, "CPF obrigatório com 11 dígitos.");

        Aluno aluno = alunoRepository.findByCpf(cpfLimpo);
        ExcecaoDeDominio.quandoNulo(aluno, "Aluno não encontrado.");

        Long instituicaoId = turma.getInstituicao() != null ? IdUtil.toLong(turma.getInstituicao().getId()) : null;
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Turma sem instituição vinculada.");
        ExcecaoDeDominio.quando(
                !instituicaoRepository.alunoVinculadoInstituicao(cpfLimpo, instituicaoId),
                "Aluno não está matriculado nesta instituição. Encaminhe à secretaria.");

        boolean jaNaTurma = turma.getAlunos().stream()
                .anyMatch(a -> cpfLimpo.equals(CpfUtil.somenteDigitos(a.getCpf())));
        ExcecaoDeDominio.quando(jaNaTurma, "Aluno já pertence a esta turma.");

        turma.getAlunos().add(aluno);
        turmaRepository.save(turma);
    }

    @Transactional
    public void removerAluno(Long turmaId, String cpf, UsuarioAutenticado usuario) {
        Turma turma = servicoEscopoProfessor.exigirTurmaDoProfessor(turmaId, usuario);
        String cpfLimpo = CpfUtil.somenteDigitos(cpf);
        ExcecaoDeDominio.quando(cpfLimpo.length() != 11, "CPF obrigatório com 11 dígitos.");

        Aluno aluno = alunoRepository.findByCpf(cpfLimpo);
        ExcecaoDeDominio.quandoNulo(aluno, "Aluno não encontrado nesta turma.");
        ExcecaoDeDominio.quando(!turma.getAlunos().contains(aluno), "Aluno não encontrado nesta turma.");

        turma.getAlunos().remove(aluno);
        turmaRepository.save(turma);
    }
}
