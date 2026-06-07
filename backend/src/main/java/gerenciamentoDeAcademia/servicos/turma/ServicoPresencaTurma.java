package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.dto.PresencaGradeDto;
import gerenciamentoDeAcademia.dto.PresencaSalvarDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.PresencaAluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.enums.StatusPresenca;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.PresencaAlunoRepository;
import gerenciamentoDeAcademia.util.CpfUtil;
import gerenciamentoDeAcademia.util.DiaSemanaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicoPresencaTurma {

    private final PresencaAlunoRepository presencaAlunoRepository;
    private final ServicoEscopoProfessor servicoEscopoProfessor;
    private final GeradorRelatorioPresencaPdf geradorRelatorioPresencaPdf;

    @Transactional(readOnly = true)
    public PresencaGradeDto montarGrade(Long turmaId, int ano, int mes, UsuarioAutenticado usuario) {
        Turma turma = servicoEscopoProfessor.exigirTurmaDoProfessor(turmaId, usuario);
        YearMonth ym = YearMonth.of(ano, mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fim = ym.atEndOfMonth();

        List<Integer> diasComAula = calcularDiasComAula(turma, ym);
        List<PresencaAluno> registros = presencaAlunoRepository.findByTurmaIdAndDataAulaBetween(turmaId, inicio, fim);

        Map<String, Map<Integer, StatusPresenca>> porAluno = new HashMap<>();
        for (PresencaAluno registro : registros) {
            porAluno
                    .computeIfAbsent(registro.getAlunoCpf(), k -> new HashMap<>())
                    .put(registro.getDataAula().getDayOfMonth(), registro.getStatus());
        }

        PresencaGradeDto grade = new PresencaGradeDto();
        grade.setTurmaId(turmaId);
        grade.setModalidade(turma.getModalidade());
        grade.setSala(turma.getSala());
        grade.setHorario(turma.getHorario());
        grade.setAno(ano);
        grade.setMes(mes);
        grade.setDiasComAula(diasComAula);

        List<Aluno> alunos = turma.getAlunos().stream()
                .sorted(Comparator.comparing(Aluno::getNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();

        for (Aluno aluno : alunos) {
            PresencaGradeDto.AlunoPresencaLinhaDto linha = new PresencaGradeDto.AlunoPresencaLinhaDto();
            linha.setNome(aluno.getNome());
            linha.setCpf(aluno.getCpf());
            Map<Integer, StatusPresenca> mapa = porAluno.getOrDefault(aluno.getCpf(), Map.of());
            for (Integer dia : diasComAula) {
                StatusPresenca status = mapa.get(dia);
                if (status != null) {
                    linha.getRegistros().put(dia, status.name());
                }
            }
            calcularTotais(linha, diasComAula.size());
            grade.getAlunos().add(linha);
        }
        return grade;
    }

    @Transactional
    public void salvar(Long turmaId, PresencaSalvarDto dto, UsuarioAutenticado usuario) {
        Turma turma = servicoEscopoProfessor.exigirTurmaDoProfessor(turmaId, usuario);
        ExcecaoDeDominio.quandoNulo(dto.getAno(), "Ano é obrigatório.");
        ExcecaoDeDominio.quandoNulo(dto.getMes(), "Mês é obrigatório.");

        YearMonth ym = YearMonth.of(dto.getAno(), dto.getMes());
        Set<Integer> diasValidos = calcularDiasComAula(turma, ym).stream().collect(Collectors.toSet());
        Set<String> cpfsTurma = turma.getAlunos().stream().map(Aluno::getCpf).collect(Collectors.toSet());

        LocalDate inicio = ym.atDay(1);
        LocalDate fim = ym.atEndOfMonth();
        List<PresencaAluno> existentes = presencaAlunoRepository.findByTurmaIdAndDataAulaBetween(turmaId, inicio, fim);
        Map<String, PresencaAluno> indice = new HashMap<>();
        for (PresencaAluno p : existentes) {
            indice.put(chave(p.getAlunoCpf(), p.getDataAula().getDayOfMonth()), p);
        }

        String cpfProfessor = usuario != null ? CpfUtil.somenteDigitos(usuario.getUsername()) : null;
        LocalDateTime agora = LocalDateTime.now();

        for (PresencaSalvarDto.RegistroPresencaDto reg : dto.getRegistros()) {
            if (reg.getDia() == null || reg.getStatus() == null || reg.getStatus().isBlank()) {
                continue;
            }
            String cpf = CpfUtil.somenteDigitos(reg.getAlunoCpf());
            ExcecaoDeDominio.quando(!cpfsTurma.contains(cpf), "Aluno não pertence à turma.");
            ExcecaoDeDominio.quando(!diasValidos.contains(reg.getDia()), "Dia inválido para esta turma no mês informado.");

            StatusPresenca status = StatusPresenca.valueOf(reg.getStatus().trim().toUpperCase(Locale.ROOT));
            LocalDate dataAula = ym.atDay(reg.getDia());
            String chave = chave(cpf, reg.getDia());

            PresencaAluno entidade = indice.get(chave);
            if (entidade == null) {
                entidade = new PresencaAluno();
                entidade.setTurmaId(turmaId);
                entidade.setAlunoCpf(cpf);
                entidade.setDataAula(dataAula);
                indice.put(chave, entidade);
            }
            entidade.setStatus(status);
            entidade.setAtualizadoPorCpf(cpfProfessor);
            entidade.setAtualizadoEm(agora);
            presencaAlunoRepository.save(entidade);
        }
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdf(Long turmaId, int ano, int mes, UsuarioAutenticado usuario) {
        PresencaGradeDto grade = montarGrade(turmaId, ano, mes, usuario);
        Turma turma = servicoEscopoProfessor.exigirTurmaDoProfessor(turmaId, usuario);
        String professor = turma.getProfessor() != null ? turma.getProfessor().getNome() : "";
        String instituicao = turma.getInstituicao() != null ? turma.getInstituicao().getRazaoSocial() : "";
        return geradorRelatorioPresencaPdf.gerar(instituicao, professor, grade);
    }

    private static String chave(String cpf, int dia) {
        return cpf + ":" + dia;
    }

    private List<Integer> calcularDiasComAula(Turma turma, YearMonth ym) {
        List<String> diasTurma = turma.getDias() != null ? turma.getDias() : List.of();
        if (diasTurma.isEmpty()) {
            List<Integer> todos = new ArrayList<>();
            for (int d = 1; d <= ym.lengthOfMonth(); d++) {
                todos.add(d);
            }
            return todos;
        }
        Set<DayOfWeek> diasSemana = DiaSemanaUtil.resolverDiasDaTurma(diasTurma);
        if (diasSemana.isEmpty()) {
            return List.of();
        }

        List<Integer> dias = new ArrayList<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            LocalDate data = ym.atDay(d);
            if (diasSemana.contains(data.getDayOfWeek())) {
                dias.add(d);
            }
        }
        return dias;
    }

    private void calcularTotais(PresencaGradeDto.AlunoPresencaLinhaDto linha, int totalAulas) {
        int p = 0, f = 0, j = 0, a = 0;
        for (String codigo : linha.getRegistros().values()) {
            switch (codigo) {
                case "P" -> p++;
                case "F" -> f++;
                case "J" -> j++;
                case "A" -> a++;
                default -> { }
            }
        }
        linha.getTotais().put("P", p);
        linha.getTotais().put("F", f);
        linha.getTotais().put("J", j);
        linha.getTotais().put("A", a);
        int marcados = p + f + j + a;
        int base = totalAulas > 0 ? totalAulas : (marcados > 0 ? marcados : 1);
        linha.getPercentuais().put("P", percentual(p, base));
        linha.getPercentuais().put("F", percentual(f, base));
        linha.getPercentuais().put("J", percentual(j, base));
        linha.getPercentuais().put("A", percentual(a, base));
    }

    private int percentual(int valor, int base) {
        return (int) Math.round((valor * 100.0) / base);
    }
}
