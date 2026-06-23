package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.RegistroDiaPonto;
import gerenciamentoDeAcademia.util.FolhaPontoUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegistroDiaPontoDto(
        LocalDate data,
        LocalDateTime horaEntrada,
        LocalDateTime horaSaida,
        Long minutosTrabalhados,
        String horasFormatadas,
        String situacao
) {

    public static RegistroDiaPontoDto of(RegistroDiaPonto registro) {
        long minutos = FolhaPontoUtil.minutosTrabalhados(registro.getHoraEntrada(), registro.getHoraSaida());
        String situacao;
        if (registro.getHoraEntrada() == null) {
            situacao = "Sem registro";
        } else if (registro.getHoraSaida() == null) {
            situacao = "Em aberto";
        } else {
            situacao = "Completo";
        }
        return new RegistroDiaPontoDto(
                registro.getDataRegistro(),
                registro.getHoraEntrada(),
                registro.getHoraSaida(),
                minutos > 0 ? minutos : null,
                minutos > 0 ? FolhaPontoUtil.formatarHoras(minutos) : "—",
                situacao);
    }
}
