package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeAlunos;
import gerenciamentoDeAcademia.util.CpfUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConsultaDeAlunos implements IConsultaDeAlunos {

    private final AlunoRepository alunoRepository;
    private final InstituicaoRepository instituicaoRepository;

    @Override
    public List<Aluno> listarAlunos(Long instituicaoId) {
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição é obrigatória para listar alunos.");
        return alunoRepository.findDistinctByTurma_Instituicao_IdOrderByNomeAsc(instituicaoId);
    }

    @Override
    public Aluno consultaAlunoPorCpf(String cpf, Long instituicaoId) {
        String cpfLimpo = CpfUtil.somenteDigitos(cpf);
        ExcecaoDeDominio.quando(cpfLimpo.length() != 11, "CPF obrigatório com 11 dígitos para consulta do aluno.");

        Aluno alunoEncontrado = alunoRepository.findByCpf(cpfLimpo);
        ExcecaoDeDominio.quandoNulo(alunoEncontrado, "Aluno não encontrado na base.");

        if (instituicaoId != null) {
            ExcecaoDeDominio.quando(
                    !instituicaoRepository.alunoVinculadoInstituicao(cpfLimpo, instituicaoId),
                    "Aluno não vinculado a esta instituição.");
        }

        return alunoEncontrado;
    }
}
