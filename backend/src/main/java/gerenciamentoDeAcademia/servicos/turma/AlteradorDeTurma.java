package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IAlteradorDeTurma;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlteradorDeTurma implements IAlteradorDeTurma {
    TurmaRepository turmaRepository;
    FuncionarioRepository funcionarioRepository;
    InstituicaoRepository instituicaoRepository;
    AlunoRepository alunoRepository;
    VinculoTurmaAluno vinculoTurmaAluno;

    @Override
    public void alterarTurma(Turma turmaParaAlterar) {
        Optional<Turma> turma = turmaRepository.findById(turmaParaAlterar.getId());
        ExcecaoDeDominio.quandoNulo(turma, "Turma não encontrada na base");

        if (StringUtils.hasText(turmaParaAlterar.getModalidade())) {
            turma.get().setModalidade(turmaParaAlterar.getModalidade());
        }
        if (turmaParaAlterar.getDias() != null) {
            turma.get().setDias(turmaParaAlterar.getDias());
        }
        if (StringUtils.hasText(turmaParaAlterar.getHorario())) {
            turma.get().setHorario(turmaParaAlterar.getHorario());
        }
        if (turmaParaAlterar.getSala() != null) {
            turma.get().setSala(turmaParaAlterar.getSala().isBlank() ? null : turmaParaAlterar.getSala());
        }

        turmaRepository.save(turma.get());
    }

    public void vincularProfessor(Long turmaId, String cpfProfessor) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ExcecaoDeDominio("Turma não encontrada na base"));
        if (!StringUtils.hasText(cpfProfessor)) {
            turma.setProfessor(null);
        } else {
            String cpf = cpfProfessor.replaceAll("\\D", "");
            Funcionario professor = funcionarioRepository.findByCpf(cpf);
            ExcecaoDeDominio.quandoNulo(professor, "Professor não encontrado na base");
            ExcecaoDeDominio.quando(!professor.getTipoFuncionario().podeAtuarComoProfessor(),
                    "O colaborador informado não pode atuar como professor (perfil Professor, Diretor ou Administrador).");
            Long instituicaoId = turma.getInstituicao() != null ? turma.getInstituicao().getId() : null;
            ExcecaoDeDominio.quando(instituicaoId == null
                            || !instituicaoRepository.existsByCnpjAndFuncionarioCpf(instituicaoId, cpf),
                    "Professor não vinculado à instituição desta turma.");
            turma.setProfessor(professor);
        }
        turmaRepository.save(turma);
    }

    @Override
    public void adicionarAlunoNaTurma(Turma turmaParaAlterar) {
        Optional<Turma> turma = turmaRepository.findById(turmaParaAlterar.getId());
        ExcecaoDeDominio.quandoNulo(turma, "Turma não encontrada na base");

        for (Aluno aluno : turmaParaAlterar.getAlunos()) {
            Aluno gerenciado = alunoRepository.findByCpf(aluno.getCpf());
            ExcecaoDeDominio.quandoNulo(gerenciado, "Aluno não encontrado na base");
            ExcecaoDeDominio.quando(turma.get().getAlunos().contains(gerenciado),
                    String.format("Aluno %s já matriculado na turma", gerenciado.getNome()));

            vinculoTurmaAluno.vincular(turma.get(), gerenciado);
        }

        turmaRepository.save(turma.get());
    }

    @Override
    public void removerAlunoNaTurma(Turma turmaParaAlterar) {
        Optional<Turma> turma = turmaRepository.findById(turmaParaAlterar.getId());
        ExcecaoDeDominio.quandoNulo(turma, "Turma não encontrada na base");

        for (Aluno aluno : turmaParaAlterar.getAlunos()) {
            Aluno gerenciado = alunoRepository.findByCpf(aluno.getCpf());
            ExcecaoDeDominio.quandoNulo(gerenciado, "Aluno não encontrado na base");
            ExcecaoDeDominio.quando(!turma.get().getAlunos().contains(gerenciado), "Aluno não matriculado na turma");

            vinculoTurmaAluno.desvincular(turma.get(), gerenciado);
        }

        turmaRepository.save(turma.get());
    }
}
