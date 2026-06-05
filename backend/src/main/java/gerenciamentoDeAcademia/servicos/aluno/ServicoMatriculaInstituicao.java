package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.MatriculaInstituicao;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.MatriculaInstituicaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ServicoMatriculaInstituicao {

    private final MatriculaInstituicaoRepository matriculaRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final AlunoRepository alunoRepository;

    @Transactional
    public MatriculaInstituicao salvarFinanceiro(Long instituicaoId, Aluno aluno, AlunoDto dados) {
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição é obrigatória.");
        ExcecaoDeDominio.quandoNulo(aluno, "Aluno inválido.");
        validarFinanceiro(dados.getValorMensalidade(), dados.getDiaVencimentoMensalidade());

        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));

        MatriculaInstituicao matricula = matriculaRepository
                .findByAluno_IdAndInstituicao_Id(aluno.getId(), instituicaoId)
                .orElseGet(() -> {
                    MatriculaInstituicao nova = new MatriculaInstituicao();
                    nova.setAluno(aluno);
                    nova.setInstituicao(instituicao);
                    return nova;
                });

        matricula.setValorMensalidade(dados.getValorMensalidade());
        matricula.setDiaVencimentoMensalidade(dados.getDiaVencimentoMensalidade());
        if (matricula.getDataUltimoPagamentoMensalidade() == null
                && aluno.getDataUltimoPagamentoMensalidade() != null) {
            matricula.setDataUltimoPagamentoMensalidade(aluno.getDataUltimoPagamentoMensalidade());
        }
        return matriculaRepository.save(matricula);
    }

    @Transactional
    public MatriculaInstituicao atualizarFinanceiro(Aluno aluno, AlunoDto dados) {
        ExcecaoDeDominio.quandoNulo(dados.getInstituicaoId(), "Informe a instituição para alterar a mensalidade.");
        return salvarFinanceiro(dados.getInstituicaoId(), aluno, dados);
    }

    @Transactional
    public MatriculaInstituicao obterOuMigrarLegado(String cpf, Long instituicaoId) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF obrigatório.");
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição obrigatória.");

        return matriculaRepository.findByAluno_CpfAndInstituicao_Id(cpf, instituicaoId)
                .orElseGet(() -> {
                    Aluno aluno = alunoRepository.findByCpf(cpf);
                    return garantirMigracaoLegado(aluno, instituicaoId);
                });
    }

    @Transactional
    public void registrarBaixa(String cpf, Long instituicaoId) {
        MatriculaInstituicao matricula = obterOuMigrarLegado(cpf, instituicaoId);
        ExcecaoDeDominio.quandoNulo(matricula, "Matrícula financeira não encontrada para esta instituição.");
        matricula.setDataUltimoPagamentoMensalidade(LocalDate.now());
        matriculaRepository.save(matricula);
    }

    @Transactional(readOnly = true)
    public MatriculaInstituicao consultarFinanceiro(Aluno aluno, Long instituicaoId) {
        if (aluno == null || instituicaoId == null) {
            return null;
        }
        return matriculaRepository.findByAluno_IdAndInstituicao_Id(aluno.getId(), instituicaoId)
                .orElseGet(() -> financeiroLegadoSemPersistir(aluno));
    }

    private MatriculaInstituicao financeiroLegadoSemPersistir(Aluno aluno) {
        if (aluno.getValorMensalidade() == null || aluno.getDiaVencimentoMensalidade() == null) {
            return null;
        }
        MatriculaInstituicao legado = new MatriculaInstituicao();
        legado.setAluno(aluno);
        legado.setValorMensalidade(aluno.getValorMensalidade());
        legado.setDiaVencimentoMensalidade(aluno.getDiaVencimentoMensalidade());
        legado.setDataUltimoPagamentoMensalidade(aluno.getDataUltimoPagamentoMensalidade());
        return legado;
    }

    @Transactional
    public MatriculaInstituicao garantirMigracaoLegado(Aluno aluno, Long instituicaoId) {
        if (aluno == null || instituicaoId == null) {
            return null;
        }
        return matriculaRepository.findByAluno_IdAndInstituicao_Id(aluno.getId(), instituicaoId)
                .orElseGet(() -> {
                    if (aluno.getValorMensalidade() == null || aluno.getDiaVencimentoMensalidade() == null) {
                        return null;
                    }
                    Instituicao instituicao = instituicaoRepository.findById(instituicaoId).orElse(null);
                    if (instituicao == null) {
                        return null;
                    }
                    MatriculaInstituicao matricula = new MatriculaInstituicao();
                    matricula.setAluno(aluno);
                    matricula.setInstituicao(instituicao);
                    matricula.setValorMensalidade(aluno.getValorMensalidade());
                    matricula.setDiaVencimentoMensalidade(aluno.getDiaVencimentoMensalidade());
                    matricula.setDataUltimoPagamentoMensalidade(aluno.getDataUltimoPagamentoMensalidade());
                    return matriculaRepository.save(matricula);
                });
    }

    private void validarFinanceiro(Double valor, Integer dia) {
        ExcecaoDeDominio.quandoNuloOuVazio(valor, "Valor da mensalidade é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(dia, "Dia de vencimento da mensalidade é obrigatório!");
        ExcecaoDeDominio.quando(dia < 1 || dia > 28, "Dia de vencimento deve estar entre 1 e 28.");
    }
}
