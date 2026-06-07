package gerenciamentoDeAcademia.infra.config;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.enums.TipoItemProgramacao;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.ItemProgramacaoAlunoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Massa de dados para testar grade horária, conflitos de sala e Minha programação (perfis local/docker).
 */
@Component
@Profile({"docker", "local"})
@ConditionalOnProperty(name = "app.seed.demo-enabled", havingValue = "true", matchIfMissing = true)
@Order(50)
@RequiredArgsConstructor
public class DataInicializadorCenariosTeste {

    private static final Logger log = LoggerFactory.getLogger(DataInicializadorCenariosTeste.class);
    private static final String CNPJ_MASTER = "00000000000191";
    private static final String CPF_PROFESSOR = "61482582007";

    public static final String TURMA_JUDO = "[Demo] Judô — turma portal";
    public static final String TURMA_KARATE = "[Demo] Karatê — conflito Dojo 1";
    public static final String TURMA_PILATES = "[Demo] Pilates — Sala 2";

    private final TurmaRepository turmaRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final AlunoRepository alunoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ItemProgramacaoAlunoRepository programacaoRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedCenarios() {
        Instituicao master = instituicaoRepository.findByCnpj(CNPJ_MASTER);
        Aluno aluno = alunoRepository.findByCpf(DataInicializadorAlunoTeste.CPF_ALUNO_TESTE);
        if (master == null || aluno == null) {
            return;
        }

        Funcionario professor = funcionarioRepository.findByCpf(CPF_PROFESSOR);

        Turma judo = garantirTurma(master, TURMA_JUDO, professor,
                List.of("Segunda", "Quarta"),
                "18:00-19:30", LocalTime.of(18, 0), LocalTime.of(19, 30), "Dojo 1");
        garantirTurma(master, TURMA_KARATE, professor,
                List.of("Terça", "Quinta"),
                "18:00-19:30", LocalTime.of(18, 0), LocalTime.of(19, 30), "Dojo 1");
        garantirTurma(master, TURMA_PILATES, null,
                List.of("Sexta"),
                "19:00-20:00", LocalTime.of(19, 0), LocalTime.of(20, 0), "Sala 2");

        vincularAlunoTurma(aluno, judo);

        LocalDate segundaDestaSemana = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate quartaDestaSemana = LocalDate.now().with(DayOfWeek.WEDNESDAY);
        LocalDate sabadoDestaSemana = LocalDate.now().with(DayOfWeek.SATURDAY);

        garantirItemProgramacao(master, aluno, "Prova faixa amarela — Dojo 1 (conflito)",
                TipoItemProgramacao.PROVA,
                "Simulado de prova no mesmo horário/sala das turmas de Judô/Karatê na grade.",
                segundaDestaSemana, "18:00-19:00", LocalTime.of(18, 0), LocalTime.of(19, 0), "Dojo 1");

        garantirItemProgramacao(master, aluno, "Série de treino — força",
                TipoItemProgramacao.SERIE_TREINO,
                "Treino complementar sem conflito de sala.",
                quartaDestaSemana, "20:00-21:00", LocalTime.of(20, 0), LocalTime.of(21, 0), "Laboratório");

        garantirItemProgramacao(master, aluno, "Open day — recepção",
                TipoItemProgramacao.EVENTO,
                "Evento institucional para alunos e responsáveis.",
                sabadoDestaSemana, "10:00-12:00", LocalTime.of(10, 0), LocalTime.of(12, 0), "Sala 2");

        atualizarTurmasLegadasSemHorario(master);

        log.info("Cenários de teste (turmas, programação, conflitos) verificados para instituição master.");
    }

    private Turma garantirTurma(
            Instituicao instituicao,
            String modalidade,
            Funcionario professor,
            List<String> dias,
            String horario,
            LocalTime inicio,
            LocalTime fim,
            String sala) {
        List<Turma> existentes = turmaRepository.findByModalidade(modalidade);
        if (!existentes.isEmpty()) {
            Turma t = existentes.get(0);
            if (t.getHoraInicio() == null) {
                t.setHorario(horario);
                t.setHoraInicio(inicio);
                t.setHoraFim(fim);
                t.setSala(sala);
                turmaRepository.save(t);
            }
            return t;
        }
        Turma turma = new Turma();
        turma.setInstituicao(instituicao);
        turma.setModalidade(modalidade);
        turma.setDias(dias);
        turma.setHorario(horario);
        turma.setHoraInicio(inicio);
        turma.setHoraFim(fim);
        turma.setSala(sala);
        turma.setProfessor(professor);
        return turmaRepository.save(turma);
    }

    private void vincularAlunoTurma(Aluno aluno, Turma turma) {
        if (turma.getAlunos() == null) {
            turma.setAlunos(new java.util.HashSet<>());
        }
        if (!turma.getAlunos().contains(aluno)) {
            turma.getAlunos().add(aluno);
            turmaRepository.save(turma);
        }
    }

    private void garantirItemProgramacao(
            Instituicao instituicao,
            Aluno aluno,
            String titulo,
            TipoItemProgramacao tipo,
            String descricao,
            LocalDate data,
            String horario,
            LocalTime inicio,
            LocalTime fim,
            String sala) {
        boolean existe = programacaoRepository.findByAluno_CpfAndInstituicao_IdOrderByDataPrevistaAsc(
                aluno.getCpf(), instituicao.getId()).stream()
                .anyMatch(i -> titulo.equals(i.getTitulo()));
        if (existe) {
            return;
        }
        programacaoRepository.save(ItemProgramacaoAluno.builder()
                .instituicao(instituicao)
                .aluno(aluno)
                .tipo(tipo)
                .titulo(titulo)
                .descricao(descricao)
                .dataPrevista(data)
                .horario(horario)
                .horaInicio(inicio)
                .horaFim(fim)
                .sala(sala)
                .build());
    }

    private void atualizarTurmasLegadasSemHorario(Instituicao master) {
        for (Turma turma : turmaRepository.findByInstituicao_Id(master.getId())) {
            if (turma.getHoraInicio() != null) {
                continue;
            }
            if (turma.getHorario() == null || turma.getHorario().isBlank()) {
                continue;
            }
            try {
                var intervalo = gerenciamentoDeAcademia.util.IntervaloHorario.parse(turma.getHorario());
                turma.setHoraInicio(intervalo.inicio());
                turma.setHoraFim(intervalo.fim());
                turmaRepository.save(turma);
            } catch (Exception ignored) {
                // mantém string legada
            }
        }
    }
}
