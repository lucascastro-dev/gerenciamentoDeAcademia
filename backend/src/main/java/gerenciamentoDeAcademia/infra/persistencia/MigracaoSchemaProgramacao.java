package gerenciamentoDeAcademia.infra.persistencia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ajustes idempotentes de schema que o Hibernate ddl-auto não aplica em bases já existentes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MigracaoSchemaProgramacao implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        tornarAlunoOpcionalEmItemProgramacao();
    }

    private void tornarAlunoOpcionalEmItemProgramacao() {
        try {
            String nullable = jdbcTemplate.queryForObject(
                    """
                    SELECT is_nullable
                    FROM information_schema.columns
                    WHERE table_schema = 'public'
                      AND table_name = 'tb_item_programacao_aluno'
                      AND column_name = 'aluno_id'
                    """,
                    String.class);
            if ("NO".equalsIgnoreCase(nullable)) {
                jdbcTemplate.execute("ALTER TABLE tb_item_programacao_aluno ALTER COLUMN aluno_id DROP NOT NULL");
                log.info("Schema: tb_item_programacao_aluno.aluno_id agora aceita NULL (programação por turma).");
            }
        } catch (Exception ex) {
            log.warn("Não foi possível ajustar schema de programação: {}", ex.getMessage());
        }
    }
}
