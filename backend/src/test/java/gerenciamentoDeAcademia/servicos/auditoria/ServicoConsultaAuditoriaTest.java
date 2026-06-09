package gerenciamentoDeAcademia.servicos.auditoria;

import gerenciamentoDeAcademia.dto.AuditoriaRegistroDto;
import gerenciamentoDeAcademia.entidades.RegistroAuditoria;
import gerenciamentoDeAcademia.repositorios.AuditoriaRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.RevisaoAuditoriaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
class ServicoConsultaAuditoriaTest {

    @InjectMocks
    ServicoConsultaAuditoria servicoConsultaAuditoria;
    @Mock
    AuditoriaRepository auditoriaRepository;
    @Mock
    FuncionarioRepository funcionarioRepository;
    @Mock
    RevisaoAuditoriaRepository revisaoAuditoriaRepository;

    @Test
    void deveListarRegistrosManuais() {
        RegistroAuditoria registro = RegistroAuditoria.builder()
                .id(1L)
                .dataHora(LocalDateTime.of(2026, 6, 1, 10, 0))
                .usuarioLogin("admin")
                .acao("ALTERACAO")
                .entidade("FUNCIONARIO")
                .identificador("12345678901")
                .detalhes("Perfil atualizado")
                .build();

        Mockito.when(auditoriaRepository.findAllByOrderByDataHoraDesc()).thenReturn(List.of(registro));
        Mockito.when(funcionarioRepository.findAll()).thenReturn(Collections.emptyList());

        List<AuditoriaRegistroDto> lista = servicoConsultaAuditoria.listarRegistros();

        Assertions.assertEquals(1, lista.size());
        AuditoriaRegistroDto dto = lista.get(0);
        Assertions.assertEquals("Edição", dto.ajuste());
        Assertions.assertEquals("admin", dto.usuarioLogin());
        Assertions.assertEquals("Perfil atualizado", dto.motivo());
    }
}
