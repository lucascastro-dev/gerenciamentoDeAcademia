package gerenciamentoDeAcademia.infra.config;

import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Garante que instituições antigas sem flag explícita fiquem como cadastro inativo.
 */
@Component
@Profile({"docker", "local"})
@RequiredArgsConstructor
public class NormalizadorCadastroInstituicao {

    private static final Logger log = LoggerFactory.getLogger(NormalizadorCadastroInstituicao.class);

    private final InstituicaoRepository instituicaoRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void normalizarCadastroAtivoNulo() {
        long corrigidas = instituicaoRepository.normalizarCadastroAtivoNulo();
        if (corrigidas > 0) {
            log.info("Normalizadas {} instituição(ões) com cadastro_ativo nulo para inativo.", corrigidas);
        }
    }
}
