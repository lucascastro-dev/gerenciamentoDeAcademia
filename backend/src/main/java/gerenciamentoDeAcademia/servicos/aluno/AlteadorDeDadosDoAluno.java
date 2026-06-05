package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.util.CpfUtil;
import gerenciamentoDeAcademia.servicos.interfaces.IAlteradorDeDadosDoAluno;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlteadorDeDadosDoAluno implements IAlteradorDeDadosDoAluno {

    private final AlunoRepository alunoRepository;
    private final ServicoMatriculaInstituicao servicoMatriculaInstituicao;

    @Override
    public void alterarAluno(AlunoDto alunoDto) {
        Aluno aluno = alunoRepository.findByCpf(CpfUtil.somenteDigitos(alunoDto.getCpf()));

        ExcecaoDeDominio.quandoNulo(aluno, "Aluno não encontrado!");

        aluno.setNome(alunoDto.getNome());
        aluno.setRg(alunoDto.getRg());
        aluno.setDataDeNascimento(alunoDto.getDataDeNascimento());
        aluno.setEndereco(alunoDto.getEndereco());
        aluno.setTelefone(alunoDto.getTelefone());
        if (alunoDto.getEmail() != null) {
            aluno.setEmail(alunoDto.getEmail().isBlank() ? null : alunoDto.getEmail().trim());
        }
        aluno.setNomeResponsavel(alunoDto.getNomeResponsavel());
        aluno.setTelefoneResponsavel(alunoDto.getTelefoneResponsavel());

        alunoRepository.save(aluno);

        if (alunoDto.getInstituicaoId() != null
                && alunoDto.getValorMensalidade() != null
                && alunoDto.getDiaVencimentoMensalidade() != null) {
            servicoMatriculaInstituicao.atualizarFinanceiro(aluno, alunoDto);
        }
    }
}
