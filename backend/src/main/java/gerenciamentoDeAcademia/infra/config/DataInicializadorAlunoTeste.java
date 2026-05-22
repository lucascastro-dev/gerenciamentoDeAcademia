package gerenciamentoDeAcademia.infra.config;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.servicos.aluno.ServicoAcessoAluno;
import gerenciamentoDeAcademia.servicos.aluno.ServicoVinculoAlunoInstituicao;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Aluno de teste para o portal — CPF 12345678909, senha 123.
 */
@Component
@Profile({"docker", "local"})
@RequiredArgsConstructor
public class DataInicializadorAlunoTeste {

    private static final Logger log = LoggerFactory.getLogger(DataInicializadorAlunoTeste.class);
    public static final String CPF_ALUNO_TESTE = "12345678909";
    private static final String CNPJ_MASTER = "00000000000191";

    private final AlunoRepository alunoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final ServicoAcessoAluno servicoAcessoAluno;
    private final ServicoVinculoAlunoInstituicao servicoVinculoAlunoInstituicao;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void criarAlunoTeste() {
        Instituicao instituicao = instituicaoRepository.findByCnpj(CNPJ_MASTER);
        if (instituicao == null) {
            log.warn("Instituição master não encontrada; aluno de teste não criado.");
            return;
        }

        LocalDate hoje = LocalDate.now();
        int diaVencimento = Math.max(1, hoje.minusDays(3).getDayOfMonth());

        Aluno aluno = alunoRepository.findByCpf(CPF_ALUNO_TESTE);
        if (aluno == null) {
            aluno = new Aluno();
            aluno.setNome("Teste Portal Aluno");
            aluno.setRg("RG123456");
            aluno.setCpf(CPF_ALUNO_TESTE);
            aluno.setDataDeNascimento(LocalDate.of(2000, 3, 10));
            aluno.setEndereco("{\"logradouro\":\"Rua Aluno\",\"numero\":\"10\",\"cidade\":\"Rio de Janeiro\",\"uf\":\"RJ\"}");
            aluno.setTelefone("21988887777");
            aluno.setValorMensalidade(150.0);
            log.info("Aluno de teste criado — CPF {}", CPF_ALUNO_TESTE);
        }
        aluno.setDiaVencimentoMensalidade(diaVencimento);
        aluno.setDataUltimoPagamentoMensalidade(hoje.minusMonths(2));
        aluno = alunoRepository.save(aluno);

        servicoAcessoAluno.garantirUsuarioPortal(aluno);
        servicoVinculoAlunoInstituicao.vincularAlunoNaInstituicao(instituicao.getId(), aluno);
        log.info("Aluno de teste — vencimento dia {} (em tolerância de cobrança para popup no login)", diaVencimento);
    }
}
