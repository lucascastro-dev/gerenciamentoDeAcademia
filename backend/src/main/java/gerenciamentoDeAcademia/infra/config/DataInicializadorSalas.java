package gerenciamentoDeAcademia.infra.config;

import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Sala;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"docker", "local"})
@RequiredArgsConstructor
public class DataInicializadorSalas {

    private final SalaRepository salaRepository;
    private final InstituicaoRepository instituicaoRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedSalas() {
        Instituicao master = instituicaoRepository.findByCnpj("00000000000191");
        if (master == null || !salaRepository.findByInstituicao_IdOrderByNomeAsc(master.getId()).isEmpty()) {
            return;
        }
        salaRepository.save(Sala.builder().instituicao(master).nome("Dojo 1").capacidade(30).ativa(true).build());
        salaRepository.save(Sala.builder().instituicao(master).nome("Sala 2").capacidade(25).ativa(true).build());
        salaRepository.save(Sala.builder().instituicao(master).nome("Laboratório").capacidade(20).ativa(true).build());
    }
}
