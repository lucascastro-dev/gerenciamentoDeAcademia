package gerenciamentoDeAcademia.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.history.RevisionMetadata;

class MapeadorAuditoriaTest {

    @Test
    void deveMapearAcoesManuais() {
        Assertions.assertEquals("Criação", MapeadorAuditoria.ajusteExibicao("CADASTRO"));
        Assertions.assertEquals("Criação", MapeadorAuditoria.ajusteExibicao("PRE_CADASTRO"));
        Assertions.assertEquals("Edição", MapeadorAuditoria.ajusteExibicao("ALTERACAO"));
        Assertions.assertEquals("Exclusão", MapeadorAuditoria.ajusteExibicao("EXCLUSAO"));
    }

    @Test
    void deveMapearTiposEnvers() {
        Assertions.assertEquals("Criação", MapeadorAuditoria.ajusteDeRevisao(RevisionMetadata.RevisionType.INSERT));
        Assertions.assertEquals("Edição", MapeadorAuditoria.ajusteDeRevisao(RevisionMetadata.RevisionType.UPDATE));
        Assertions.assertEquals("Exclusão", MapeadorAuditoria.ajusteDeRevisao(RevisionMetadata.RevisionType.DELETE));
    }
}
