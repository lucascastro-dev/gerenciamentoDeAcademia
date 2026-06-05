package gerenciamentoDeAcademia.servicos.master;

import gerenciamentoDeAcademia.entidades.Funcionario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicoMasterPlataformaTest {

    private ServicoMasterPlataforma servico;

    @BeforeEach
    void setUp() {
        servico = new ServicoMasterPlataforma();
        ReflectionTestUtils.setField(servico, "masterCpf", "00000000191");
    }

    @Test
    void deveReconhecerMasterRaizComOuSemMascara() {
        assertTrue(servico.ehMasterRaiz("000.000.001-91"));
        assertTrue(servico.ehMasterRaiz("00000000191"));
        assertFalse(servico.ehMasterRaiz("12345678901"));
    }

    @Test
    void subMasterEhOperadorMasNaoRaiz() {
        Funcionario sub = Funcionario.builder()
                .cpf("99999999999")
                .permitirGerenciarFuncoes(true)
                .build();
        assertTrue(servico.ehOperadorPlataforma(sub));
        assertFalse(servico.ehMasterRaiz(sub.getCpf()));
        assertFalse(servico.podeConcederSubMaster(sub));
    }

    @Test
    void masterRaizPodeConcederSubMaster() {
        Funcionario raiz = Funcionario.builder().cpf("00000000191").build();
        assertTrue(servico.podeConcederSubMaster(raiz));
    }
}
