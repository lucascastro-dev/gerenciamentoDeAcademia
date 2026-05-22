package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;
import gerenciamentoDeAcademia.enums.TipoItemProgramacao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record ItemProgramacaoDto(
        Long id,
        String cpfAluno,
        String nomeAluno,
        TipoItemProgramacao tipo,
        String tipoDescricao,
        String titulo,
        String descricao,
        LocalDate dataPrevista,
        String horario,
        LocalTime horaInicio,
        LocalTime horaFim,
        String sala
) {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static ItemProgramacaoDto of(ItemProgramacaoAluno item) {
        if (item == null) {
            return null;
        }
        String horarioExibicao = item.getHorario();
        if ((horarioExibicao == null || horarioExibicao.isBlank())
                && item.getHoraInicio() != null && item.getHoraFim() != null) {
            horarioExibicao = item.getHoraInicio().format(HORA) + " - " + item.getHoraFim().format(HORA);
        }
        return new ItemProgramacaoDto(
                item.getId(),
                item.getAluno() != null ? item.getAluno().getCpf() : null,
                item.getAluno() != null ? item.getAluno().getNome() : null,
                item.getTipo(),
                item.getTipo() != null ? item.getTipo().getDescricao() : null,
                item.getTitulo(),
                item.getDescricao(),
                item.getDataPrevista(),
                horarioExibicao,
                item.getHoraInicio(),
                item.getHoraFim(),
                item.getSala()
        );
    }
}
