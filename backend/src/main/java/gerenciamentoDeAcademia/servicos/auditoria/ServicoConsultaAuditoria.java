package gerenciamentoDeAcademia.servicos.auditoria;

import gerenciamentoDeAcademia.auditoria.RevisaoAuditoria;
import gerenciamentoDeAcademia.dto.AuditoriaRegistroDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.RegistroAuditoria;
import gerenciamentoDeAcademia.repositorios.AuditoriaRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.RevisaoAuditoriaRepository;
import gerenciamentoDeAcademia.util.MapeadorAuditoria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ServicoConsultaAuditoria {

    private static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");
    private static final int JANELA_DEDUPLICACAO_SEGUNDOS = 10;

    private final AuditoriaRepository auditoriaRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final RevisaoAuditoriaRepository revisaoAuditoriaRepository;

    @Transactional(readOnly = true)
    public List<AuditoriaRegistroDto> listarRegistros() {
        List<AuditoriaRegistroDto> manuais = auditoriaRepository.findAllByOrderByDataHoraDesc().stream()
                .map(this::mapearRegistroManual)
                .collect(Collectors.toCollection(ArrayList::new));

        List<AuditoriaRegistroDto> envers = listarRevisoesEnvers(manuais);
        return Stream.concat(manuais.stream(), envers.stream())
                .sorted(Comparator.comparing(AuditoriaRegistroDto::dataHora).reversed())
                .collect(Collectors.toList());
    }

    private List<AuditoriaRegistroDto> listarRevisoesEnvers(List<AuditoriaRegistroDto> manuais) {
        List<AuditoriaRegistroDto> revisoes = new ArrayList<>();
        for (Funcionario funcionario : funcionarioRepository.findAll()) {
            if (funcionario.getId() == null) {
                continue;
            }
            var pagina = funcionarioRepository.findRevisions(funcionario.getId(), PageRequest.of(0, 200));
            StreamSupport.stream(pagina.spliterator(), false)
                    .map(rev -> mapearRevisaoEnvers(rev, funcionario))
                    .filter(dto -> !duplicadoDeRegistroManual(manuais, dto))
                    .forEach(revisoes::add);
        }
        return revisoes;
    }

    private boolean duplicadoDeRegistroManual(List<AuditoriaRegistroDto> manuais, AuditoriaRegistroDto envers) {
        if (envers.referencia() == null || envers.dataHora() == null) {
            return false;
        }
        return manuais.stream().anyMatch(manual ->
                envers.referencia().equals(manual.referencia())
                        && envers.ajuste().equals(manual.ajuste())
                        && Math.abs(Duration.between(manual.dataHora(), envers.dataHora()).getSeconds())
                        <= JANELA_DEDUPLICACAO_SEGUNDOS);
    }

    private AuditoriaRegistroDto mapearRegistroManual(RegistroAuditoria registro) {
        return new AuditoriaRegistroDto(
                registro.getId(),
                MapeadorAuditoria.ajusteExibicao(registro.getAcao()),
                registro.getDataHora(),
                registro.getUsuarioLogin(),
                registro.getEntidade(),
                registro.getIdentificador(),
                registro.getDetalhes()
        );
    }

    private AuditoriaRegistroDto mapearRevisaoEnvers(Revision<Long, Funcionario> revision, Funcionario ref) {
        Long numeroRevisao = revision.getRevisionNumber().orElse(0L);
        Instant instante = revision.getRevisionInstant().orElse(Instant.EPOCH);
        LocalDateTime dataHora = LocalDateTime.ofInstant(instante, FUSO_PADRAO);

        String usuario = revisaoAuditoriaRepository.findById(numeroRevisao)
                .map(RevisaoAuditoria::getUsuarioLogin)
                .filter(u -> u != null && !u.isBlank())
                .orElse("Não registrado");

        Funcionario entidade = revision.getEntity();
        String cpf = entidade != null && entidade.getCpf() != null ? entidade.getCpf() : ref.getCpf();
        String nome = entidade != null && entidade.getNome() != null ? entidade.getNome() : ref.getNome();
        String perfil = entidade != null && entidade.getTipoFuncionario() != null
                ? entidade.getTipoFuncionario().name()
                : (ref.getTipoFuncionario() != null ? ref.getTipoFuncionario().name() : "—");

        String ajuste = MapeadorAuditoria.ajusteDeRevisao(revision.getMetadata().getRevisionType());
        String motivo = String.format(
                "Revisão Envers nº %d — %s (%s)",
                numeroRevisao,
                nome != null ? nome : "registro",
                perfil
        );

        return new AuditoriaRegistroDto(
                null,
                ajuste,
                dataHora,
                usuario,
                "FUNCIONARIO",
                cpf,
                motivo
        );
    }
}
