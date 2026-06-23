package gerenciamentoDeAcademia.servicos.programacao;

import gerenciamentoDeAcademia.dto.ConflitoHorarioDto;
import gerenciamentoDeAcademia.dto.GradeHorariaEventoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.repositorios.ItemProgramacaoAlunoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.util.IntervaloHorario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ServicoGradeHoraria {

    private static final Locale PT = Locale.forLanguageTag("pt-BR");

    private final TurmaRepository turmaRepository;
    private final ItemProgramacaoAlunoRepository programacaoRepository;

    @Transactional(readOnly = true)
    public List<GradeHorariaEventoDto> montarGrade(Long instituicaoId, LocalDate semanaReferencia) {
        LocalDate ref = semanaReferencia != null ? semanaReferencia : LocalDate.now();
        List<GradeHorariaEventoDto> eventos = new ArrayList<>();
        eventos.addAll(eventosTurmas(instituicaoId, ref));
        eventos.addAll(eventosProgramacao(instituicaoId, ref));
        return marcarConflitos(eventos);
    }

    @Transactional(readOnly = true)
    public List<ConflitoHorarioDto> detectarConflitosItem(Long instituicaoId, ItemProgramacaoAluno item, Long ignorarId) {
        if (item.getDataPrevista() == null) {
            return List.of();
        }
        IntervaloHorario novo = intervaloItem(item);
        if (novo == null) {
            return List.of();
        }
        String sala = item.getSala();
        if (sala == null || sala.isBlank()) {
            return List.of();
        }

        List<ConflitoHorarioDto> conflitos = new ArrayList<>();
        Set<String> chaves = new HashSet<>();

        for (LocalDate data : datasDoItem(item)) {
            List<GradeHorariaEventoDto> grade = montarGrade(instituicaoId, data);
            String dia = diaSemanaPt(data.getDayOfWeek());

            for (GradeHorariaEventoDto ev : grade) {
                if ("PROGRAMACAO".equals(ev.origem()) && ev.referenciaId() != null
                        && ev.referenciaId().equals(ignorarId)) {
                    continue;
                }
                if (!mesmoDia(ev, dia, data)) {
                    continue;
                }
                if (ev.horaInicio() == null || ev.horaFim() == null) {
                    continue;
                }
                IntervaloHorario existente = new IntervaloHorario(ev.horaInicio(), ev.horaFim());
                if (!novo.sobrepoe(existente)) {
                    continue;
                }

                if (mesmaSala(sala, ev.sala())) {
                    adicionarConflito(conflitos, chaves,
                            "Sobreposição de sala em " + sala + " em " + formatarData(data)
                                    + " entre \"" + item.getTitulo() + "\" e \"" + ev.titulo() + "\"",
                            item.getTitulo(), ev.titulo(), sala);
                }

                if (conflitoAluno(item, ev, instituicaoId, data, novo, ignorarId)) {
                    String alvo = rotuloAlvo(item);
                    adicionarConflito(conflitos, chaves,
                            "Conflito de agenda para " + alvo + " em " + formatarData(data)
                                    + " entre \"" + item.getTitulo() + "\" e \"" + ev.titulo() + "\"",
                            item.getTitulo(), ev.titulo(), sala);
                }
            }
        }
        return conflitos;
    }

    private boolean conflitoAluno(
            ItemProgramacaoAluno item,
            GradeHorariaEventoDto ev,
            Long instituicaoId,
            LocalDate data,
            IntervaloHorario novo,
            Long ignorarId) {
        Set<String> cpfsAfetados = cpfsImpactados(item);
        if (cpfsAfetados.isEmpty()) {
            return false;
        }

        if ("PROGRAMACAO".equals(ev.origem())) {
            if (ev.referenciaId() == null) {
                return false;
            }
            ItemProgramacaoAluno outro = programacaoRepository.findById(ev.referenciaId()).orElse(null);
            if (outro == null || (ignorarId != null && ignorarId.equals(outro.getId()))) {
                return false;
            }
            Set<String> cpfsOutro = cpfsImpactados(outro);
            for (String cpf : cpfsAfetados) {
                if (cpfsOutro.contains(cpf)) {
                    return true;
                }
            }
            return false;
        }

        if ("TURMA".equals(ev.origem()) && ev.referenciaId() != null) {
            Turma turma = turmaRepository.findByIdComAlunos(ev.referenciaId()).orElse(null);
            if (turma == null || turma.getAlunos() == null) {
                return false;
            }
            if (item.getTurma() != null && item.getTurma().getId().equals(turma.getId())) {
                return false;
            }
            for (Aluno aluno : turma.getAlunos()) {
                if (aluno != null && aluno.getCpf() != null && cpfsAfetados.contains(aluno.getCpf())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<String> cpfsImpactados(ItemProgramacaoAluno item) {
        Set<String> cpfs = new HashSet<>();
        if (item.getAluno() != null && item.getAluno().getCpf() != null) {
            cpfs.add(item.getAluno().getCpf());
        }
        if (item.getTurma() != null && item.getTurma().getAlunos() != null) {
            item.getTurma().getAlunos().stream()
                    .filter(a -> a != null && a.getCpf() != null)
                    .forEach(a -> cpfs.add(a.getCpf()));
        }
        return cpfs;
    }

    private String rotuloAlvo(ItemProgramacaoAluno item) {
        if (item.getAluno() != null) {
            return item.getAluno().getNome();
        }
        if (item.getTurma() != null) {
            return "turma " + (item.getTurma().getModalidade() != null ? item.getTurma().getModalidade() : item.getTurma().getId());
        }
        return "participante";
    }

    private void adicionarConflito(
            List<ConflitoHorarioDto> conflitos,
            Set<String> chaves,
            String mensagem,
            String tituloA,
            String tituloB,
            String sala) {
        if (chaves.add(mensagem)) {
            conflitos.add(new ConflitoHorarioDto(mensagem, tituloA, tituloB, sala));
        }
    }

    private List<LocalDate> datasDoItem(ItemProgramacaoAluno item) {
        LocalDate inicio = item.getDataPrevista();
        LocalDate fim = item.getDataFim() != null ? item.getDataFim() : inicio;
        List<LocalDate> datas = new ArrayList<>();
        for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) {
            datas.add(d);
        }
        return datas;
    }

    private String formatarData(LocalDate data) {
        return data.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private List<GradeHorariaEventoDto> marcarConflitos(List<GradeHorariaEventoDto> eventos) {
        List<GradeHorariaEventoDto> resultado = new ArrayList<>();
        for (int i = 0; i < eventos.size(); i++) {
            GradeHorariaEventoDto a = eventos.get(i);
            boolean conflito = false;
            if (a.sala() != null && !a.sala().isBlank() && a.horaInicio() != null && a.horaFim() != null) {
                IntervaloHorario ia = new IntervaloHorario(a.horaInicio(), a.horaFim());
                for (int j = i + 1; j < eventos.size(); j++) {
                    GradeHorariaEventoDto b = eventos.get(j);
                    if (!mesmoSlot(a, b)) {
                        continue;
                    }
                    if (!a.sala().equalsIgnoreCase(b.sala())) {
                        continue;
                    }
                    IntervaloHorario ib = new IntervaloHorario(b.horaInicio(), b.horaFim());
                    if (ia.sobrepoe(ib)) {
                        conflito = true;
                        break;
                    }
                }
            }
            resultado.add(new GradeHorariaEventoDto(
                    a.origem(), a.referenciaId(), a.titulo(), a.subtitulo(), a.tipoProgramacao(),
                    a.modalidade(), a.sala(), a.diaSemana(), a.data(), a.horaInicio(), a.horaFim(), conflito));
        }
        return resultado;
    }

    private boolean mesmoSlot(GradeHorariaEventoDto a, GradeHorariaEventoDto b) {
        if (a.data() != null && b.data() != null) {
            return a.data().equals(b.data());
        }
        return a.diaSemana() != null && a.diaSemana().equals(b.diaSemana());
    }

    private List<GradeHorariaEventoDto> eventosTurmas(Long instituicaoId, LocalDate semanaRef) {
        List<GradeHorariaEventoDto> lista = new ArrayList<>();
        for (Turma turma : turmaRepository.findByInstituicao_IdComDias(instituicaoId)) {
            IntervaloHorario intervalo = intervaloTurma(turma);
            if (intervalo == null || turma.getDias() == null) {
                continue;
            }
            for (String dia : turma.getDias()) {
                LocalDate data = dataDoDiaNaSemana(semanaRef, dia);
                String professor = turma.getProfessor() != null ? turma.getProfessor().getNome() : "Sem professor";
                lista.add(new GradeHorariaEventoDto(
                        "TURMA",
                        turma.getId(),
                        turma.getModalidade() != null ? turma.getModalidade() : "Turma",
                        professor,
                        null,
                        turma.getModalidade(),
                        turma.getSala(),
                        dia,
                        data,
                        intervalo.inicio(),
                        intervalo.fim(),
                        false));
            }
        }
        return lista;
    }

    private List<GradeHorariaEventoDto> eventosProgramacao(Long instituicaoId, LocalDate semanaRef) {
        List<GradeHorariaEventoDto> lista = new ArrayList<>();
        LocalDate inicioSemana = semanaRef.with(DayOfWeek.MONDAY);
        LocalDate fimSemana = inicioSemana.plusDays(6);
        for (ItemProgramacaoAluno item : programacaoRepository.findByInstituicao_IdOrderByDataPrevistaAscIdAsc(instituicaoId)) {
            if (item.getDataPrevista() == null) {
                continue;
            }
            IntervaloHorario intervalo = intervaloItem(item);
            if (intervalo == null) {
                continue;
            }
            LocalDate fimItem = item.getDataFim() != null ? item.getDataFim() : item.getDataPrevista();
            for (LocalDate data : datasEntre(item.getDataPrevista(), fimItem)) {
                if (data.isBefore(inicioSemana) || data.isAfter(fimSemana)) {
                    continue;
                }
                String subtitulo = item.getAluno() != null
                        ? item.getAluno().getNome()
                        : (item.getTurma() != null ? "Turma: " + item.getTurma().getModalidade() : null);
                String dia = diaSemanaPt(data.getDayOfWeek());
                lista.add(new GradeHorariaEventoDto(
                        "PROGRAMACAO",
                        item.getId(),
                        item.getTitulo(),
                        subtitulo,
                        item.getTipo(),
                        null,
                        item.getSala(),
                        dia,
                        data,
                        intervalo.inicio(),
                        intervalo.fim(),
                        false));
            }
        }
        return lista;
    }

    private List<LocalDate> datasEntre(LocalDate inicio, LocalDate fim) {
        List<LocalDate> datas = new ArrayList<>();
        for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) {
            datas.add(d);
        }
        return datas;
    }

    private IntervaloHorario intervaloTurma(Turma turma) {
        if (turma.getHoraInicio() != null && turma.getHoraFim() != null) {
            return new IntervaloHorario(turma.getHoraInicio(), turma.getHoraFim());
        }
        if (turma.getHorario() != null && !turma.getHorario().isBlank()) {
            try {
                return IntervaloHorario.parse(turma.getHorario());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private IntervaloHorario intervaloItem(ItemProgramacaoAluno item) {
        if (item.getHoraInicio() != null && item.getHoraFim() != null) {
            return new IntervaloHorario(item.getHoraInicio(), item.getHoraFim());
        }
        if (item.getHorario() != null && !item.getHorario().isBlank()) {
            try {
                return IntervaloHorario.parse(item.getHorario());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean mesmaSala(String a, String b) {
        if (a == null || a.isBlank() || b == null || b.isBlank()) {
            return false;
        }
        return a.equalsIgnoreCase(b);
    }

    private boolean mesmoDia(GradeHorariaEventoDto ev, String dia, LocalDate data) {
        if (ev.data() != null && data != null) {
            return ev.data().equals(data);
        }
        return dia != null && dia.equals(ev.diaSemana());
    }

    private LocalDate dataDoDiaNaSemana(LocalDate semanaRef, String diaNome) {
        DayOfWeek alvo = diaSemanaDeNome(diaNome);
        if (alvo == null) {
            return semanaRef;
        }
        LocalDate segunda = semanaRef.with(DayOfWeek.MONDAY);
        return segunda.with(alvo);
    }

    private DayOfWeek diaSemanaDeNome(String dia) {
        if (dia == null) {
            return null;
        }
        String d = dia.toLowerCase();
        if (d.startsWith("seg")) return DayOfWeek.MONDAY;
        if (d.startsWith("ter")) return DayOfWeek.TUESDAY;
        if (d.startsWith("qua")) return DayOfWeek.WEDNESDAY;
        if (d.startsWith("qui")) return DayOfWeek.THURSDAY;
        if (d.startsWith("sex")) return DayOfWeek.FRIDAY;
        if (d.startsWith("sáb") || d.startsWith("sab")) return DayOfWeek.SATURDAY;
        if (d.startsWith("dom")) return DayOfWeek.SUNDAY;
        return null;
    }

    private String diaSemanaPt(DayOfWeek day) {
        return day.getDisplayName(TextStyle.FULL, PT);
    }
}
