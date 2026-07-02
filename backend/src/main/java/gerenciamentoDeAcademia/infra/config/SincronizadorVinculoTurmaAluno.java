package gerenciamentoDeAcademia.infra.config;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.turma.VinculoTurmaAluno;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

/**
 * Garante consistência bidirecional turma ↔ aluno após correção do mapeamento JPA.
 */
@Component
@RequiredArgsConstructor
public class SincronizadorVinculoTurmaAluno {

    private static final Logger log = LoggerFactory.getLogger(SincronizadorVinculoTurmaAluno.class);

    private final TurmaRepository turmaRepository;
    private final VinculoTurmaAluno vinculoTurmaAluno;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void sincronizar() {
        int reparos = 0;
        for (Turma turma : turmaRepository.findAll()) {
            if (turma.getAlunos() == null || turma.getAlunos().isEmpty()) {
                continue;
            }
            for (Aluno aluno : new HashSet<>(turma.getAlunos())) {
                if (aluno.getTurma() == null || !aluno.getTurma().contains(turma)) {
                    vinculoTurmaAluno.vincular(turma, aluno);
                    reparos++;
                }
            }
        }
        if (reparos > 0) {
            log.info("Sincronizados {} vínculos turma-aluno (lado inverso).", reparos);
        }
    }
}
