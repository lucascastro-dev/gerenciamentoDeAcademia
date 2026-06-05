package gerenciamentoDeAcademia.servicos.instituicao;

import gerenciamentoDeAcademia.dto.AssinaturaPlataformaDto;
import gerenciamentoDeAcademia.dto.AtivarInstituicaoRequest;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import gerenciamentoDeAcademia.servicos.plano.ServicoAssinaturaPlataforma;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoAtivacaoInstituicaoTest {

    @Mock
    private InstituicaoRepository instituicaoRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ServicoAssinaturaPlataforma servicoAssinaturaPlataforma;

    @InjectMocks
    private ServicoAtivacaoInstituicao servico;

    @Test
    void deveBloquearSegundoTrial() {
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        inst.setCadastroAtivo(false);
        inst.setTrialUtilizado(true);
        Funcionario admin = Funcionario.builder().cpf("11111111111").senha("Senha@123").build();
        when(instituicaoRepository.findByCnpj("123")).thenReturn(inst);
        when(funcionarioRepository.findByCpf("11111111111")).thenReturn(admin);
        when(usuarioRepository.existsByLogin("11111111111")).thenReturn(false);
        when(instituicaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doAnswer(inv -> {
            throw new ExcecaoDeDominio(
                    "O teste grátis de 7 dias já foi utilizado por esta instituição.");
        }).when(servicoAssinaturaPlataforma).ativarPlano(eq(inst), eq(PlanoInstituicaoTipo.TRIAL_7_DIAS));

        AtivarInstituicaoRequest req = new AtivarInstituicaoRequest();
        req.setCnpj("123");
        req.setCpfAdministrador("11111111111");
        req.setPlano(PlanoInstituicaoTipo.TRIAL_7_DIAS);

        assertThrows(ExcecaoDeDominio.class, () -> servico.ativarUnidade(req));
    }

    @Test
    void deveAtivarComPlanoPagoEPendenciaFinanceira() {
        Instituicao inst = new Instituicao();
        inst.setId(2L);
        inst.setCadastroAtivo(false);
        inst.setFuncionarios(new HashSet<>());
        Funcionario admin = Funcionario.builder().cpf("22222222222").senha("Senha@123").build();

        when(instituicaoRepository.findByCnpj("456")).thenReturn(inst);
        when(funcionarioRepository.findByCpf("22222222222")).thenReturn(admin);
        when(usuarioRepository.existsByLogin("22222222222")).thenReturn(false);
        when(instituicaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doAnswer(inv -> {
            inst.setStatusFinanceiro(StatusFinanceiroInstituicao.PENDENTE_PAGAMENTO);
            return new AssinaturaPlataformaDto();
        }).when(servicoAssinaturaPlataforma).ativarPlano(inst, PlanoInstituicaoTipo.MENSAL);

        AtivarInstituicaoRequest req = new AtivarInstituicaoRequest();
        req.setCnpj("456");
        req.setCpfAdministrador("22222222222");
        req.setPlano(PlanoInstituicaoTipo.MENSAL);

        servico.ativarUnidade(req);

        assertTrue(inst.getCadastroAtivo());
        assertEquals(StatusFinanceiroInstituicao.PENDENTE_PAGAMENTO, inst.getStatusFinanceiro());
        verify(servicoAssinaturaPlataforma).ativarPlano(inst, PlanoInstituicaoTipo.MENSAL);
    }

    @Test
    void deveAtivarCadastroComPlanoObrigatorio() {
        Instituicao inst = new Instituicao();
        inst.setId(6L);
        inst.setCadastroAtivo(false);
        inst.setFuncionarios(new HashSet<>());
        inst.getFuncionarios().add(Funcionario.builder()
                .cpf("15179950783")
                .tipoFuncionario(gerenciamentoDeAcademia.enums.TipoFuncionario.ADMINISTRADOR)
                .build());

        when(instituicaoRepository.findByCnpj("23498897000120")).thenReturn(inst);
        when(instituicaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        servico.ativarCadastro("23498897000120", PlanoInstituicaoTipo.MENSAL);

        assertTrue(inst.getCadastroAtivo());
        verify(servicoAssinaturaPlataforma).ativarPlano(inst, PlanoInstituicaoTipo.MENSAL);
    }
}
